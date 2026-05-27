package com.flowbytestudio.ticketapp.util

import com.flowbytestudio.data.network.ApiException
import com.flowbytestudio.data.network.NetworkException

internal enum class AuthErrorContext {
    Login,
    Register,
}

internal enum class HomeErrorContext(val label: String) {
    Events("Etkinlikler"),
    Tickets("Biletler"),
}

internal fun Throwable.toAuthUserMessage(context: AuthErrorContext): String = when (this) {
    is ApiException -> toAuthApiUserMessage(context)
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu."
}

internal fun Throwable.toHomeUserMessage(context: HomeErrorContext): String = when (this) {
    is ApiException -> toHomeApiUserMessage(context)
    is NetworkException -> "${context.label} için internet bağlantısı yok"
    else -> message ?: "${context.label} alınamadı"
}

private fun ApiException.toAuthApiUserMessage(context: AuthErrorContext): String =
    when {
        context == AuthErrorContext.Login && code == 401 -> "Email veya şifre hatalı"
        context == AuthErrorContext.Register && code == 400 -> "Email veya şifre formatı geçersiz"
        context == AuthErrorContext.Register && code == 409 -> "Bu email zaten kayıtlı"
        code in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> "Beklenmeyen bir hata oluştu"
    }

private fun ApiException.toHomeApiUserMessage(context: HomeErrorContext): String =
    when {
        code == 401 -> "Oturum süren dolmuş olabilir. Tekrar giriş yap."
        code == 403 -> "${context.label} için yetkin yok"
        code in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> "${context.label} alınamadı"
    }

internal fun Throwable.toPurchaseUserMessage(): String = when (this) {
    is ApiException -> toPurchaseApiUserMessage()
    is NetworkException -> "İnternet bağlantısı yok. Lütfen bağlantınızı kontrol edip tekrar deneyin."
    else -> message ?: "Satın alma işlemi sırasında bilinmeyen bir hata oluştu."
}

private fun ApiException.toPurchaseApiUserMessage(): String {
    val errorStr = errorMessage ?: ""
    return when {
        code == 409 && errorStr.contains("capacity_exceeded", ignoreCase = true) -> "Seçtiğiniz bilet türü için stok yetersizdir. Lütfen sayfayı yenileyip tekrar deneyin."
        code == 409 && errorStr.contains("already_paid", ignoreCase = true) -> "Bu sipariş zaten ödenmiştir."
        code == 403 && errorStr.contains("not_purchase_owner", ignoreCase = true) -> "Bu satın alma işlemi size ait değil."
        code == 404 -> "Sipariş veya etkinlik bulunamadı."
        code in 500..599 -> "Sunucu şu anda yanıt veremiyor. Lütfen daha sonra tekrar deneyin."
        else -> errorMessage ?: "İşlem gerçekleştirilemedi (Hata kodu: $code)"
    }
}
