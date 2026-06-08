package com.design.readerapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object SignalRManager {
    const val CHANNEL_ID = "notifications_channel"

    private var hubConnection: HubConnection? = null
    private val _notifications = MutableSharedFlow<Notification>()
    val notifications = _notifications.asSharedFlow()

    private var contextRef: WeakReference<Context>? = null

    fun start(context: Context, scope: CoroutineScope) {
        contextRef = WeakReference(context.applicationContext)
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        scope.launch(Dispatchers.IO) {
            connect(context, scope)
        }
    }

    suspend fun reconnectIfNeeded(context: Context, scope: CoroutineScope) {
        contextRef = WeakReference(context.applicationContext)
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        // Detener conexión colgada antes de crear una nueva
        try { hubConnection?.stop() } catch (_: Exception) {}
        hubConnection = null

        connect(context, scope)
    }

    private suspend fun connect(context: Context, scope: CoroutineScope) {
        try {
            // Negotiate siempre fresco — token válido 1 hora desde este momento
            val info = BooksService.negotiateSignalR()

            hubConnection = HubConnectionBuilder.create(info.url)
                .withAccessTokenProvider(Single.just(info.accessToken))
                .build()

            @Suppress("UNCHECKED_CAST")
            hubConnection?.on("notificationReceived", { raw: Map<*, *> ->
                try {
                    val notification = Notification(
                        id              = raw["id"]?.toString(),
                        recipientUserId = raw["recipientUserId"]?.toString() ?: "",
                        actorUserId     = raw["actorUserId"]?.toString() ?: "",
                        actorName       = raw["actorName"]?.toString() ?: "",
                        bookId          = raw["bookId"]?.toString(),
                        type            = raw["type"]?.toString() ?: "",
                        message         = raw["message"]?.toString() ?: "",
                        isRead          = raw["isRead"] as? Boolean ?: false,
                        createdAt       = raw["createdAt"]?.toString() ?: ""
                    )

                    contextRef?.get()?.let { ctx ->
                        NotificationStore.prepend(ctx, notification)
                        sendPushNotification(ctx, notification)
                    }

                    scope.launch { _notifications.emit(notification) }
                } catch (e: Exception) {
                    Log.e("SignalR", "Error al construir Notification", e)
                }
            }, Map::class.java)

            hubConnection?.start()?.blockingAwait()
            Log.d("SignalR", "Conectado al hub de notificaciones")
        } catch (e: Exception) {
            Log.e("SignalR", "Error al conectar SignalR", e)
        }
    }

    fun stop() {
        try { hubConnection?.stop() } catch (_: Exception) {}
        hubConnection = null
    }

    private fun sendPushNotification(context: Context, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "notifications")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(notification.actorName.ifBlank { "Nueva notificación" })
            .setContentText(notification.message.ifBlank { "Tienes una nueva notificación" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
