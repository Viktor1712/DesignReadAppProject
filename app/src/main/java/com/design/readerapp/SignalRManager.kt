package com.design.readerapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object SignalRManager {
    private var hubConnection: HubConnection? = null
    private val _notifications = MutableSharedFlow<Notification>()
    val notifications = _notifications.asSharedFlow()

    fun start(context: Context, scope: CoroutineScope) {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        scope.launch(Dispatchers.IO) {
            try {
                val connectionInfo = BooksService.negotiateSignalR()
                
                hubConnection = HubConnectionBuilder.create(connectionInfo.url)
                    .withAccessTokenProvider(Single.just(connectionInfo.accessToken))
                    .build()

                hubConnection?.on("ReceiveNotification", { notification: Notification ->
                    scope.launch {
                        _notifications.emit(notification)
                    }
                }, Notification::class.java)

                hubConnection?.start()?.blockingAwait()
                Log.d("SignalR", "Connected to MS-3 Hub")
            } catch (e: Exception) {
                Log.e("SignalR", "Error connecting", e)
            }
        }
    }

    fun stop() {
        hubConnection?.stop()
    }
}
