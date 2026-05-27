package com.flowbytestudio.ticketapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.TicketType
import com.flowbytestudio.ticketapp.viewmodel.EventDetailUiState
import com.flowbytestudio.ticketapp.viewmodel.EventDetailViewModel
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.res.stringResource
import com.flowbytestudio.ticketapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    viewModel: EventDetailViewModel = koinViewModel { parametersOf(eventId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            onPurchaseSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.event_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        BackArrowIcon(color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (state.event != null && !state.isLoading) {
                EventDetailBottomBar(
                    totalPriceCents = state.totalPriceCents,
                    canPurchase = state.canPurchase,
                    isCreating = state.isCreatingPurchase,
                    onPurchaseClick = viewModel::createPurchase
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                state.errorMessage != null && state.event == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage ?: stringResource(id = R.string.event_detail_unknown_error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::loadEventDetails) {
                            Text(stringResource(id = R.string.event_detail_retry))
                        }
                    }
                }
                state.event != null -> {
                    EventDetailContent(
                        event = state.event!!,
                        quantities = state.quantities,
                        onQtyChange = viewModel::onQuantityChange,
                        errorMessage = state.errorMessage,
                        onErrorDismiss = viewModel::clearError
                    )
                }
            }

            if (state.showPaymentConfirmation && state.createdPurchase != null) {
                PaymentConfirmationDialog(
                    purchase = state.createdPurchase!!,
                    isPaying = state.isPayingPurchase,
                    onConfirm = viewModel::confirmPayment,
                    onDismiss = viewModel::cancelPayment
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    quantities: Map<String, Int>,
    onQtyChange: (String, Int) -> Unit,
    errorMessage: String?,
    onErrorDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rich Gradient Card for Event Name and Venue
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = event.venue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                    Text(
                        text = event.startsAt.toDisplayDateRange(event.endsAt),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Event Description Section
        event.description?.takeIf { it.isNotBlank() }?.let { description ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.event_detail_description),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.25f
                    )
                }
            }
        }

        // Error message if any (non-blocking banner)
        errorMessage?.let { msg ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = onErrorDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(id = R.string.event_detail_close))
                    }
                }
            }
        }

        // Ticket Types List Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(id = R.string.event_detail_ticket_types),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (event.ticketTypes.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.event_detail_no_tickets),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            } else {
                event.ticketTypes.forEach { ticketType ->
                    val selectedQty = quantities[ticketType.id] ?: 0
                    TicketTypeSelectionCard(
                        ticketType = ticketType,
                        selectedQty = selectedQty,
                        onQtyChange = { qty -> onQtyChange(ticketType.id, qty) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Avoid getting covered by bottom bar
    }
}

@Composable
private fun TicketTypeSelectionCard(
    ticketType: TicketType,
    selectedQty: Int,
    onQtyChange: (Int) -> Unit
) {
    val remaining = ticketType.remaining
    val isSoldOut = remaining <= 0
    val maxSelectable = minOf(20, remaining)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = ticketType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSoldOut) stringResource(id = R.string.event_detail_sold_out) else stringResource(id = R.string.event_detail_remaining_stock, remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSoldOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ticketType.priceCents.toPriceLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!isSoldOut) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Minus Button
                    IconButton(
                        onClick = { onQtyChange(selectedQty - 1) },
                        enabled = selectedQty > 0
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedQty > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "-",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    Text(
                        text = selectedQty.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 20.dp),
                        textAlign = TextAlign.Center
                    )

                    // Plus Button
                    IconButton(
                        onClick = { onQtyChange(selectedQty + 1) },
                        enabled = selectedQty < maxSelectable
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedQty < maxSelectable) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "+",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailBottomBar(
    totalPriceCents: Int,
    canPurchase: Boolean,
    isCreating: Boolean,
    onPurchaseClick: () -> Unit
) {
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(id = R.string.event_detail_total_amount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = totalPriceCents.toPriceLabel(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onPurchaseClick,
                enabled = canPurchase && !isCreating,
                modifier = Modifier
                    .height(52.dp)
                    .widthIn(min = 140.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(id = R.string.event_detail_purchase), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun PaymentConfirmationDialog(
    purchase: com.flowbytestudio.core.domain.purchase.Purchase,
    isPaying: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isPaying) onDismiss() },
        title = {
            Text(
                stringResource(id = R.string.event_detail_payment_confirmation),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    stringResource(id = R.string.event_detail_payment_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(id = R.string.event_detail_order_no), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(purchase.id.take(8).uppercase(Locale.getDefault()), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(id = R.string.event_detail_total_payment), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(purchase.totalCents.toPriceLabel(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isPaying,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isPaying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(id = R.string.event_detail_complete_payment))
                }
            }
        },
        dismissButton = {
            if (!isPaying) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.event_detail_cancel))
                }
            }
        }
    )
}

// Helpers
@Composable
private fun Int.toPriceLabel(): String {
    if (this == 0) return stringResource(id = R.string.event_detail_free)
    val lira = this / 100
    val kurus = this % 100
    return String.format(Locale.forLanguageTag("tr-TR"), "%d,%02d TL", lira, kurus)
}

private fun String.toDisplayDateRange(endsAt: String): String =
    "${toDisplayDate()} - ${endsAt.toDisplayTime()}"

private fun String.toDisplayDate(): String {
    val datePart = take(10)
    val parts = datePart.split("-")
    val time = toDisplayTime()
    return if (parts.size == 3) {
        "${parts[2]}.${parts[1]}.${parts[0]} $time"
    } else {
        this
    }
}

private fun String.toDisplayTime(): String {
    val timePart = substringAfter("T", missingDelimiterValue = "")
    return timePart.take(5).ifBlank { "--:--" }
}

@Composable
private fun BackArrowIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidthPx = 2.5.dp.toPx()
        
        // Horizontal line
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.5f),
            strokeWidth = strokeWidthPx
        )
        // Top diagonal arrow line
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(width * 0.45f, height * 0.22f),
            strokeWidth = strokeWidthPx
        )
        // Bottom diagonal arrow line
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(width * 0.45f, height * 0.78f),
            strokeWidth = strokeWidthPx
        )
    }
}
