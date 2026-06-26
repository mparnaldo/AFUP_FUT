package com.afup.afupfut.util

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object ImageUtils {
    @Composable
    fun rememberBase64Image(base64Str: String?): ImageBitmap? {
        if (base64Str.isNullOrBlank() || !base64Str.startsWith("data:image")) return null
        return remember(base64Str) {
            try {
                val pureBase64 = base64Str.substringAfter("base64,")
                val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
