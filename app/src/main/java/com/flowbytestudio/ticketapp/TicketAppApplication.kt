package com.flowbytestudio.ticketapp

import android.app.Application
import com.flowbytestudio.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


// Uygulama başlatıldığında  Activitylerden önce oluşturulur
// Singleton (Tek bir instance olarak memory de kalır)
// Uygulama kapanana kadar yok edilmez
class TicketAppApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TicketAppApplication)
            modules(
                dataModule
            )
        }
    }
}
