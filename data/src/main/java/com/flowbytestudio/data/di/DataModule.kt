package com.flowbytestudio.data.di

import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.core.domain.EventRepository
import com.flowbytestudio.data.local.TokenStore
import com.flowbytestudio.data.network.AuthInterceptor
import com.flowbytestudio.data.network.TokenAuthenticator
import com.flowbytestudio.remote.AuthApi
import com.flowbytestudio.remote.EventApi
import com.flowbytestudio.repository.AuthRepositoryImpl
import com.flowbytestudio.repository.EventRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL = "https://tickets-api.halitkalayci.com/"

// Single ( Singleton) uygulama yaşam döngü boyunca tek örnek
// retrofit tek örnek
val dataModule = module {
    // Scope (Kapsam)
    // 3 temel seçenek

    // Yaşam döngüsündeki bağımlılığın davranış biçimi

    // Single (Singleton) -> Uygulama yaşam döngüsü boyunca tek örnek.
    single {
        Json {
            ignoreUnknownKeys = true // Cevapta var olan ama classta olmayan alanları ignore et.
            explicitNulls = false
            isLenient = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        TokenStore(context=get())
    }
    single { AuthInterceptor(tokenStore = get()) }

    single {
        TokenAuthenticator(
            tokenStore = get(),
            refreshApiProvider = { get<AuthApi>() }
        )
    }

    // HTTP isteklerini yönetmek..
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>().create(AuthApi::class.java) }
    single { get<Retrofit>().create(EventApi::class.java) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStore = get()
        )
    }

    single<EventRepository> {
        EventRepositoryImpl(eventApi = get())
    }

    // factory -> Her çağırıldığı noktada yeni instance üretir. Her fonksiyon için birer örnek

    // scoped -> Class -> tüm fonksiyonlarına 1 örnek
}
