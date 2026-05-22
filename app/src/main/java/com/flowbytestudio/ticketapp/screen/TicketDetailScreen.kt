package com.flowbytestudio.ticketapp.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketStatus
import com.flowbytestudio.core.domain.TicketType
import com.flowbytestudio.ticketapp.viewmodel.TicketDetailUiState
import com.flowbytestudio.ticketapp.viewmodel.TicketDetailViewModel
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBackClick: () -> Unit,
    viewModel: TicketDetailViewModel = koinViewModel { parametersOf(ticketId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bilet Detayı", fontWeight = FontWeight.Bold) },
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
                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage ?: "Bilinmeyen bir hata oluştu.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadTicketDetails() }) {
                            Text("Yeniden Dene")
                        }
                    }
                }
                state.ticket != null -> {
                    TicketDetailContent(
                        ticket = state.ticket!!,
                        event = state.event,
                        ticketType = state.ticketType
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketDetailContent(
    ticket: Ticket,
    event: Event?,
    ticketType: TicketType?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Main Ticket Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Event Information Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TicketStatusBadge(status = ticket.status)
                        Text(
                            text = "#${ticket.id.shortId().uppercase(Locale.getDefault())}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = event?.name ?: "Etkinlik Bilgisi Yok",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = event?.venue ?: "Konum bilgisi mevcut değil.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date & Time info
                    event?.let {
                        Text(
                            text = it.startsAt.toDisplayDateRange(it.endsAt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 2. Beautiful Dashed Cut-out Ticket Divider
                TicketDashedDivider()

                // 3. Ticket Pricing and QR Code Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BİLET TÜRÜ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ticketType?.name ?: "Bilinmeyen Tür",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "FİYAT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ticketType?.priceCents?.toPriceLabel() ?: "0,00 TL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Large Premium QR Code Box
                    Surface(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Detailed visual representation of a mock QR Code
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Draw QR-like pattern
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    val sizePx = size.width
                                    val cellSize = sizePx / 9f
                                    
                                    // Draw outer corner detection squares
                                    drawDetectionSquare(0f, 0f, cellSize)
                                    drawDetectionSquare(sizePx - 3 * cellSize, 0f, cellSize)
                                    drawDetectionSquare(0f, sizePx - 3 * cellSize, cellSize)

                                    // Draw random mock QR blocks to make it look highly authentic
                                    val points = listOf(
                                        Offset(4f, 0f), Offset(4f, 1f), Offset(4f, 3f), Offset(4f, 4f), Offset(4f, 7f),
                                        Offset(0f, 4f), Offset(1f, 4f), Offset(3f, 4f), Offset(7f, 4f), Offset(8f, 4f),
                                        Offset(5f, 5f), Offset(6f, 6f), Offset(5f, 7f), Offset(7f, 5f), Offset(8f, 8f),
                                        Offset(3f, 3f), Offset(3f, 5f), Offset(5f, 3f), Offset(6f, 2f), Offset(2f, 6f)
                                    )

                                    points.forEach { pt ->
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(pt.x * cellSize, pt.y * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = ticket.qrCode.uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = "Giriş sırasında bu QR kodu görevliye okutun.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDetectionSquare(x: Float, y: Float, cellSize: Float) {
    // Outer black frame
    drawRect(
        color = Color.Black,
        topLeft = Offset(x, y),
        size = androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3)
    )
    // Inner white frame
    drawRect(
        color = Color.White,
        topLeft = Offset(x + cellSize, y + cellSize),
        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
    )
}

@Composable
private fun TicketDashedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                pathEffect = pathEffect,
                strokeWidth = 3f
            )
        }
    }
}

@Composable
private fun TicketStatusBadge(status: TicketStatus) {
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
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// Private local helpers for conversions
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

@Composable
private fun BackArrowIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidthPx = 2.5.dp.toPx()
        
        // Horizontal line
        drawLine(
            color = color,
            start = Offset(width * 0.15f, height * 0.5f),
            end = Offset(width * 0.85f, height * 0.5f),
            strokeWidth = strokeWidthPx
        )
        // Top diagonal arrow line
        drawLine(
            color = color,
            start = Offset(width * 0.15f, height * 0.5f),
            end = Offset(width * 0.45f, height * 0.22f),
            strokeWidth = strokeWidthPx
        )
        // Bottom diagonal arrow line
        drawLine(
            color = color,
            start = Offset(width * 0.15f, height * 0.5f),
            end = Offset(width * 0.45f, height * 0.78f),
            strokeWidth = strokeWidthPx
        )
    }
}
