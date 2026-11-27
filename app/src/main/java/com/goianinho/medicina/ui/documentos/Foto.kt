package com.goianinho.medicina.ui.documentos

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

private const val LADO_MAX = 1280
private const val LIMITE_BYTES = 700_000

// Reduz a foto para no máximo 1280 px no lado maior, JPEG 70%.
// Devolve o Base64, ou null se passar de 700 KB (RF-16).
fun fotoParaBase64(original: Bitmap): String? {
    val escala = minOf(1f, LADO_MAX.toFloat() / maxOf(original.width, original.height))
    val reduzida = if (escala < 1f) {
        Bitmap.createScaledBitmap(
            original,
            (original.width * escala).toInt(),
            (original.height * escala).toInt(),
            true
        )
    } else {
        original
    }
    val bytes = ByteArrayOutputStream()
        .also { reduzida.compress(Bitmap.CompressFormat.JPEG, 70, it) }
        .toByteArray()
    return if (bytes.size > LIMITE_BYTES) null else Base64.encodeToString(bytes, Base64.NO_WRAP)
}