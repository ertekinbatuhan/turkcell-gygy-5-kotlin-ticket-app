package com.flowbytestudio.core.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.location.LocationRequestCompat

@Composable
fun QRCodeImage(
    qrCodeData: String,
    modifier: Modifier = Modifier,
    qrSize: Int = 15
) {
    val qrImageBitmap = remember(qrCodeData) {
        runCatching {
            val qrCode = qrcode.QRCode.ofSquares()
                .withSize(qrSize)
                .build(qrCodeData)
            val qrBytes = qrCode.renderToBytes()
            val bitmap = BitmapFactory.decodeByteArray(qrBytes, 0, qrBytes.size)
            bitmap.asImageBitmap()
        }.getOrNull()
    }

    if (qrImageBitmap != null) {
        Image(
            bitmap = qrImageBitmap,
            contentDescription = "QR Code",
            modifier = modifier,
            filterQuality = FilterQuality.None
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
