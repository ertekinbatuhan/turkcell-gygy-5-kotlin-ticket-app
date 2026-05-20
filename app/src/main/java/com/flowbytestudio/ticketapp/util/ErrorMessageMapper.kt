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
        context == AuthErrorContext.Register && code == 409 -> "Bu email ile kayıtlı bir hesap zaten var"
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
