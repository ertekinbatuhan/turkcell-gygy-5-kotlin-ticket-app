package com.flowbytestudio.ticketapp.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowbytestudio.ticketapp.viewmodel.StaffViewModel
import com.flowbytestudio.ticketapp.viewmodel.TicketValidationResult
import com.flowbytestudio.ticketapp.viewmodel.StaffUiEffect
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun StaffScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StaffViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setProcessing(true)
            try {
                val image = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        viewModel.setProcessing(false)
                        val qrBarcode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                        if (qrBarcode != null) {
                            viewModel.setScannedResult(qrBarcode.rawValue)
                        } else if (barcodes.isNotEmpty()) {
                            // Found a barcode but not QR – still show it
                            viewModel.setScannedResult(barcodes.first().rawValue)
                        } else {
                            viewModel.showToast("Seçilen görselde QR kod bulunamadı")
                        }
                    }
                    .addOnFailureListener {
                        viewModel.setProcessing(false)
                        viewModel.showToast("QR kod okuma hatası: ${it.localizedMessage}")
                    }
            } catch (e: Exception) {
                viewModel.setProcessing(false)
                viewModel.showToast("Görsel okunamadı: ${e.localizedMessage}")
            }
        }
    }

    // --- Camera Permission ---
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.showToast("Kamera izni verildi. Emülatörde kamera tarama desteklenmiyor, galeriden seçin.")
        } else {
            viewModel.showToast("Kamera izni reddedildi")
        }
    }

    // --- Listen to ViewModel effects ---
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StaffUiEffect.RequestCameraPermission -> {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        Toast.makeText(context, "Kamera izni zaten mevcut. Emülatörde kamera tarama desteklenmiyor, galeriden seçin.", Toast.LENGTH_LONG).show()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                is StaffUiEffect.OpenGallery -> {
                    galleryLauncher.launch("image/*")
                }
                is StaffUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onScanClick() {
        viewModel.showMethodDialog(true)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status badge
            Surface(
                color = Color(0xFFE8F5E9), // Light green
                contentColor = Color(0xFF2E7D32), // Dark green
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "GÖREVLİ PORTALI AKTİF",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // A premium custom scanner frame
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "QR",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "Bilet Tara",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Biletleri kontrol etmek için QR kodu tarayın veya galeriden seçin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = state.isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "QR kod okunuyor…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // QR Scan Button
            Button(
                onClick = { onScanClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isProcessing
            ) {
                Text(
                    "QR Tara",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout Button
            OutlinedButton(
                onClick = {
                    viewModel.logout(onLogoutSuccess = onLogout)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Çıkış Yap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (state.showMethodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showMethodDialog(false) },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "QR Tarama Yöntemi",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bilet QR kodunu nasıl taramak istiyorsunuz?",
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.showMethodDialog(false)
                            viewModel.triggerGalleryScan()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Galeriden Seç",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.showMethodDialog(false)
                            viewModel.triggerCameraScan()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Kamera ile Tara",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    if (state.showResultDialog && state.scannedResult != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setScannedResult(null)
                viewModel.showResultDialog(false)
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = if (state.validationResult == null) "QR Kod Okundu" else "Doğrulama Sonucu",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val result = state.validationResult
                    if (result == null) {
                        Text(
                            text = "Okunan içerik:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = state.scannedResult ?: "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    } else {
                        when (result) {
                            is TicketValidationResult.Success -> {
                                Surface(
                                    color = Color(0xFFE8F5E9), // Light green
                                    contentColor = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = "Bilet Geçerli",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = result.event?.name ?: "Bilinmeyen Etkinlik",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                result.ticketType?.let {
                                    Text(
                                        text = it.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Bilet No: ${result.ticket.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Durum: ${if (result.ticket.status.name == "VALID") "Geçerli" else "Kullanılmış"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (result.ticket.status.name == "VALID") Color(0xFF2E7D32) else Color.Red
                                )
                            }
                            is TicketValidationResult.Invalid -> {
                                Surface(
                                    color = Color(0xFFFFEBEE), // Light red
                                    contentColor = Color(0xFFC62828),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = "Geçersiz Bilet",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = result.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (state.validationResult == null) {
                    Button(
                        onClick = {
                            viewModel.validateTicket(state.scannedResult ?: "")
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !state.isValidating
                    ) {
                        if (state.isValidating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current
                            )
                        } else {
                            Text("Bileti Doğrula")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.setScannedResult(null)
                            viewModel.showResultDialog(false)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tamam")
                    }
                }
            },
            dismissButton = {
                if (state.validationResult == null) {
                    TextButton(
                        onClick = {
                            viewModel.setScannedResult(null)
                            viewModel.showResultDialog(false)
                        }
                    ) {
                        Text("Kapat", color = Color.Gray)
                    }
                }
            }
        )
    }
}
