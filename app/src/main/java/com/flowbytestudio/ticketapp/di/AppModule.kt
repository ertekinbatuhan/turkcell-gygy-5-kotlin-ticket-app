package com.flowbytestudio.ticketapp.di
import com.flowbytestudio.ticketapp.viewmodel.HomeViewModel
import com.flowbytestudio.ticketapp.viewmodel.LoginViewModel
import com.flowbytestudio.ticketapp.viewmodel.RegisterViewModel
import com.flowbytestudio.ticketapp.viewmodel.TicketDetailViewModel
import com.flowbytestudio.ticketapp.viewmodel.EventDetailViewModel
import com.flowbytestudio.ticketapp.viewmodel.StaffViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // viewModel
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::StaffViewModel)
    viewModel { params ->
        TicketDetailViewModel(
            eventRepository = get(),
            ticketId = params.get()
        )
    }
    viewModel {
        EventDetailViewModel(
            eventRepository = get(),
            purchaseRepository = get(),
            savedStateHandle = get()
        )
    }
}
