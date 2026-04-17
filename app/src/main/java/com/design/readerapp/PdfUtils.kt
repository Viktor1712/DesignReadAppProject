package com.design.readerapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

object PdfUtils {

    fun generateThumbnail(context: Context, uri: Uri): Bitmap? {
        return try {

            val fileDescriptor =
                context.contentResolver.openFileDescriptor(uri, "r") ?: return null

            val renderer = PdfRenderer(fileDescriptor)
            val page = renderer.openPage(0)

            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            fileDescriptor.close()

            bitmap

        } catch (e: Exception) {
            null
        }
    }
}