package com.flowbytestudio.ticketapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketStatus
import com.flowbytestudio.core.domain.TicketType
import com.flowbytestudio.ticketapp.viewmodel.HomeTab
import com.flowbytestudio.ticketapp.viewmodel.HomeUiState
import com.flowbytestudio.ticketapp.viewmodel.HomeViewModel
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onRefresh = viewModel::refresh,
        onTabSelected = viewModel::selectTab,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onTabSelected: (HomeTab) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                HomeHeader(
                    eventsCount = state.events.size,
                    ticketsCount = state.tickets.size,
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                )
            }

            state.errorMessage?.let { message ->
                item { ErrorBanner(message = message, onRetry = onRefresh) }
            }

            item {
                HomeTabs(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                )
            }

            if (state.isLoading) {
                item { LoadingState() }
            } else {
                when (state.selectedTab) {
                    HomeTab.Events -> eventsContent(events = state.events)
                    HomeTab.Tickets -> ticketsContent(
                        tickets = state.tickets,
                        events = state.events,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    eventsCount: Int,
    ticketsCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Etkinlikler ve Biletlerim",
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Yaklaşan etkinlikler ve aktif biletlerin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            TextButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
            ) {
                Text(if (isRefreshing) "Bekle" else "Yenile")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HeaderMetric(
                modifier = Modifier.weight(1f),
                label = "Etkinlik",
                value = eventsCount.toString(),
            )
            HeaderMetric(
                modifier = Modifier.weight(1f),
                label = "Bilet",
                value = ticketsCount.toString(),
            )
        }

        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun HeaderMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label.uppercase(Locale.forLanguageTag("tr-TR")),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeTabs(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            HomeTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.title, maxLines = 1) },
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.eventsContent(events: List<Event>) {
    if (events.isEmpty()) {
        item {
            EmptyState(
                title = "Yaklaşan etkinlik yok",
                body = "Yeni etkinlikler eklendiğinde burada görünecek.",
            )
        }
    } else {
        items(events, key = { it.id }) { event ->
            EventCard(event = event)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.ticketsContent(
    tickets: List<Ticket>,
    events: List<Event>,
) {
    if (tickets.isEmpty()) {
        item {
            EmptyState(
                title = "Henüz biletin yok",
                body = "Satın aldığın biletler burada listelenecek.",
            )
        }
    } else {
        items(tickets, key = { it.id }) { ticket ->
            val ticketInfo = remember(events, ticket.ticketTypeId) {
                events.ticketInfoByType()[ticket.ticketTypeId]
            }
            TicketCard(ticket = ticket, info = ticketInfo)
        }
    }
}

@Composable
private fun EventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = event.venue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                StatusPill(text = event.cheapestTicketLabel())
            }

            Text(
                text = event.startsAt.toDisplayDateRange(event.endsAt),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            event.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryPill(label = "Kalan", value = event.remainingTicketCount().toString())
                SummaryPill(label = "Tür", value = event.ticketTypes.size.toString())
            }

            if (event.ticketTypes.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(event.ticketTypes, key = { it.id }) { ticketType ->
                        TicketTypePill(ticketType = ticketType)
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: Ticket,
    info: TicketDisplayInfo?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = info?.eventName ?: "Bilet",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = info?.ticketTypeName ?: "Bilet türü: ${ticket.ticketTypeId.shortId()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TicketStatusPill(status = ticket.status)
            }

            info?.let {
                Text(
                    text = "${it.venue} • ${it.startsAt.toDisplayDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "QR Kod",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = ticket.qrCode.shortId().uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "QR",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketTypePill(ticketType: TicketType) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = ticketType.name, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "${ticketType.priceCents.toPriceLabel()} • ${ticketType.remaining} kaldı",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = "$label: $value",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Composable
private fun TicketStatusPill(status: TicketStatus) {
    val text = when (status) {
        TicketStatus.VALID -> "Geçerli"
        TicketStatus.USED -> "Kullanıldı"
    }
    val containerColor = when (status) {
        TicketStatus.VALID -> MaterialTheme.colorScheme.primaryContainer
        TicketStatus.USED -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (status) {
        TicketStatus.VALID -> MaterialTheme.colorScheme.onPrimaryContainer
        TicketStatus.USED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text("Tekrar dene")
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class TicketDisplayInfo(
    val eventName: String,
    val ticketTypeName: String,
    val venue: String,
    val startsAt: String,
)

private fun List<Event>.ticketInfoByType(): Map<String, TicketDisplayInfo> =
    flatMap { event ->
        event.ticketTypes.map { ticketType ->
            ticketType.id to TicketDisplayInfo(
                eventName = event.name,
                ticketTypeName = ticketType.name,
                venue = event.venue,
                startsAt = event.startsAt,
            )
        }
    }.toMap()

private fun Event.cheapestTicketLabel(): String {
    val minPrice = ticketTypes.minOfOrNull { it.priceCents } ?: return "Bilet yok"
    return minPrice.toPriceLabel()
}

private fun Event.remainingTicketCount(): Int = ticketTypes.sumOf { it.remaining }

private fun Int.toPriceLabel(): String {
    if (this == 0) return "Ücretsiz"
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

private fun String.shortId(): String = take(8).ifBlank { "--------" }
