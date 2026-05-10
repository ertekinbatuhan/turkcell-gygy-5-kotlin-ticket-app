package com.flowbytestudio.ticketapp.di
import com.flowbytestudio.ticketapp.viewmodel.LoginViewModel
import com.flowbytestudio.ticketapp.viewmodel.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // viewModel
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
