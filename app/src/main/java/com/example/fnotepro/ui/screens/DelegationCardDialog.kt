package com.example.fnotepro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.ui.components.DelegationBadge
import com.example.fnotepro.ui.components.TunisianPlateBadge
import com.example.fnotepro.ui.theme.GoldAccent
import com.example.fnotepro.ui.theme.OkGreen
import com.example.fnotepro.ui.theme.OliveDark
import com.example.fnotepro.ui.theme.OlivePrimary
import com.example.fnotepro.ui.theme.SecurityBlue
import com.example.fnotepro.ui.theme.WhatsAppColor
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DelegationCardDialog(
    guide: Guide,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentDateStr = SimpleDateFormat("EEEE d MMMM yyyy - HH:mm", Locale("ar")).format(Date())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with official Logo & weather & date
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(OliveDark, OlivePrimary)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, GoldAccent, CircleShape)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = Localization.APP_LOGO_URL,
                                        contentDescription = "Logo",
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = Localization.get("app_title", language),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Les Oliviers Palace - Sfax",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Localization.get("weather_sfax", language),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = currentDateStr,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Vehicle Hero Image & Overlaid Guide Photo
                val primaryVehiclePhoto = guide.vehiclePhotos.firstOrNull() ?: guide.busPhoto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFF2C3E50)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!primaryVehiclePhoto.isNullOrBlank()) {
                        AsyncImage(
                            model = primaryVehiclePhoto,
                            contentDescription = "Vehicle",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                            Text("Les Oliviers Palace - Sfax", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Overlaid guide avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 16.dp, y = 30.dp)
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(OlivePrimary)
                            .border(3.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!guide.guidePhoto.isNullOrBlank()) {
                            AsyncImage(
                                model = guide.guidePhoto,
                                contentDescription = guide.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Guide Name, Delegation Badge & Quick Contacts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (guide.name.isNotBlank()) guide.name else Localization.get("guide_name", language),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OlivePrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                DelegationBadge(delegationType = guide.delegationType, language = language)
                            }
                            if (guide.nationality.isNotBlank()) {
                                Text(
                                    text = guide.nationality,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldAccent
                                )
                            }
                        }

                        // Call, WhatsApp & Map quick actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (guide.phone.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+216${guide.phone.replace("[^0-9]".toRegex(), "")}"))
                                        context.startActivity(dialIntent)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(OkGreen, CircleShape)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val waNumber = "+216" + guide.phone.replace("[^0-9]".toRegex(), "").takeLast(8)
                                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waNumber"))
                                        context.startActivity(waIntent)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(WhatsAppColor, CircleShape)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }

                            // Quick Map button
                            IconButton(
                                onClick = {
                                    openGoogleMapsRoute(context, guide.fromLocation, guide.toLocation)
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(OlivePrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = "Map", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Specs Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SpecRow(label = Localization.get("transport_type", language), value = guide.transportType.ifBlank { "—" })
                            SpecRow(label = Localization.get("people_count", language), value = guide.peopleCount.ifBlank { "—" })
                            SpecRow(label = Localization.get("cars_count", language), value = "${guide.carCount}")
                            if (guide.fromLocation.isNotBlank() || guide.toLocation.isNotBlank()) {
                                SpecRow(label = "خط السير / Trajectoire", value = "${guide.fromLocation} ➡️ ${guide.toLocation}")
                            }
                            if (guide.plate.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Localization.get("plate", language),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TunisianPlateBadge(
                                        plateText = guide.plate,
                                        isForeign = guide.isForeignPlate
                                    )
                                }
                            }
                        }
                    }

                    // Delegation Route & Google Maps Card
                    Spacer(modifier = Modifier.height(12.dp))
                    DelegationRouteMapCard(
                        fromLocation = guide.fromLocation,
                        toLocation = guide.toLocation,
                        transportType = guide.transportType,
                        language = language,
                        onOpenMap = {
                            openGoogleMapsRoute(context, guide.fromLocation, guide.toLocation)
                        }
                    )

                    // Gallery for Vehicle Photos (up to 5)
                    if (guide.vehiclePhotos.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("🚌 صور المركبات (${guide.vehiclePhotos.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            guide.vehiclePhotos.forEach { photoUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Vehicle",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                            }
                        }
                    }

                    // Gallery for Motorcycle Photos (up to 10)
                    if (guide.motorcyclePhotos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("🏍️ صور الدراجات النارية (${guide.motorcyclePhotos.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            guide.motorcyclePhotos.forEach { photoUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Motorcycle",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                            }
                        }
                    }

                    // Agency Card
                    if (guide.agency.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = OlivePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(Localization.get("agency", language), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(guide.agency, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OlivePrimary)
                                }
                            }
                        }
                    }

                    // Notes Box
                    if (guide.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(guide.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Safety Officer Box
                    if (guide.safetyOfficerName.isNotBlank() || guide.safetyOfficerPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(Localization.get("safety_officer", language), fontSize = 10.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                                        Text(guide.safetyOfficerName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (guide.safetyOfficerPhone.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+216${guide.safetyOfficerPhone.replace("[^0-9]".toRegex(), "")}"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(OkGreen, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Share Card Intent Button
                    Button(
                        onClick = {
                            val shareBody = buildString {
                                appendLine("🏷️ F-Note Pro Max - بطاقة وفد")
                                appendLine("🏨 نزل Les Oliviers Palace - صفاقس")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                appendLine("🌴 نوع الوفد: ${guide.delegationType}")
                                appendLine("👤 المرشد: ${guide.name}")
                                appendLine("📞 الهاتف: ${guide.phone}")
                                appendLine("🌍 الجنسية: ${guide.nationality}")
                                appendLine("👥 عدد الأفراد: ${guide.peopleCount}")
                                appendLine("🚐 وسيلة النقل: ${guide.transportType} (عدد: ${guide.carCount})")
                                if (guide.plate.isNotBlank()) appendLine("🔢 اللوحة: ${guide.plate}")
                                if (guide.fromLocation.isNotBlank() || guide.toLocation.isNotBlank()) appendLine("📍 المسار: ${guide.fromLocation} ➡️ ${guide.toLocation}")
                                if (guide.agency.isNotBlank()) appendLine("🏢 الوكالة: ${guide.agency}")
                                if (guide.safetyOfficerName.isNotBlank()) appendLine("🛡️ عون السلامة: ${guide.safetyOfficerName} (${guide.safetyOfficerPhone})")
                                if (guide.notes.isNotBlank()) appendLine("📝 ملاحظات: ${guide.notes}")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                appendLine("تاريخ الإصدار: $currentDateStr")
                            }

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "بطاقة وفد - ${guide.name}")
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, Localization.get("share_card", language)))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.get("share_card", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.get("cancel", language))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun openGoogleMapsRoute(context: android.content.Context, from: String, to: String) {
    val origin = from.trim().ifBlank { "صفاقس تونس" }
    val destination = to.trim().ifBlank { "Les Oliviers Palace Sfax Tunisia" }

    val uri = if (from.isNotBlank() && to.isNotBlank()) {
        Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${Uri.encode(destination)}&travelmode=driving")
    } else if (to.isNotBlank()) {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")
    } else if (from.isNotBlank()) {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(origin)}")
    } else {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("Les Oliviers Palace Sfax Tunisia")}")
    }

    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    try {
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(browserIntent)
    }
}

@Composable
fun DelegationRouteMapCard(
    fromLocation: String,
    toLocation: String,
    transportType: String,
    language: AppLanguage,
    onOpenMap: () -> Unit
) {
    val originDisplay = fromLocation.ifBlank { Localization.get("from", language) + ": صفاقس" }
    val destDisplay = toLocation.ifBlank { Localization.get("to", language) + ": نزل الزيتونة (Les Oliviers Palace)" }

    // Approximate distance & time estimate based on known Tunisian destinations
    val estimateInfo = rememberRouteEstimate(fromLocation, toLocation)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = OlivePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.get("route_map", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = estimateInfo.distance,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Origin -> Destination Flow Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Origin Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = Localization.get("origin_location", language),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = originDisplay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Connector line with vehicle icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 11.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(OlivePrimary.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${getTransportEmoji(transportType)} ${estimateInfo.duration}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Destination Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("B", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = Localization.get("destination_location", language),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = destDisplay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Approximate Route Canvas / Tactical Map Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { onOpenMap() }
            ) {
                // Map background Canvas with grid and route path
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Background grid
                    val gridColor = Color(0x22FFFFFF)
                    val step = 20.dp.toPx()
                    var x = 0f
                    while (x < w) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                        x += step
                    }
                    var y = 0f
                    while (y < h) {
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                        y += step
                    }

                    // Route path curve
                    val startPoint = Offset(w * 0.18f, h * 0.72f)
                    val endPoint = Offset(w * 0.82f, h * 0.28f)
                    val control1 = Offset(w * 0.45f, h * 0.85f)
                    val control2 = Offset(w * 0.55f, h * 0.15f)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(startPoint.x, startPoint.y)
                        cubicTo(control1.x, control1.y, control2.x, control2.y, endPoint.x, endPoint.y)
                    }

                    // Glow line
                    drawPath(
                        path = path,
                        color = Color(0x66D4AF37),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                    )

                    // Solid route line
                    drawPath(
                        path = path,
                        color = Color(0xFFFFD700),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )

                    // Origin Beacon
                    drawCircle(
                        color = Color(0x4410B981),
                        radius = 14f,
                        center = startPoint
                    )
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 6f,
                        center = startPoint
                    )

                    // Destination Beacon
                    drawCircle(
                        color = Color(0x44EF4444),
                        radius = 14f,
                        center = endPoint
                    )
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = 6f,
                        center = endPoint
                    )
                }

                // Overlay information
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color(0xCC0F172A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "34.7406° N, 10.7603° E (Sfax)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Click to view hint
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0xCC0F172A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📍 ${Localization.get("approximate_route", language)}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button to Google Maps
            Button(
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = Localization.get("open_google_maps", language),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class RouteEstimate(val distance: String, val duration: String)

private fun rememberRouteEstimate(from: String, to: String): RouteEstimate {
    val f = from.lowercase()
    val t = to.lowercase()

    return when {
        (f.contains("تونس") || f.contains("tunis")) && (t.contains("صفاقس") || t.contains("sfax")) ->
            RouteEstimate("~ 270 km", "~ 3h 15min")
        (f.contains("صفاقس") || f.contains("sfax")) && (t.contains("تونس") || t.contains("tunis")) ->
            RouteEstimate("~ 270 km", "~ 3h 15min")
        (f.contains("جربة") || f.contains("djerba")) || (t.contains("جربة") || t.contains("djerba")) ->
            RouteEstimate("~ 230 km", "~ 3h 30min")
        (f.contains("توزر") || f.contains("tozeur")) || (t.contains("توزر") || t.contains("tozeur")) ->
            RouteEstimate("~ 290 km", "~ 3h 45min")
        (f.contains("سوسة") || f.contains("sousse")) || (t.contains("سوسة") || t.contains("sousse")) ->
            RouteEstimate("~ 135 km", "~ 1h 30min")
        (f.contains("حمامات") || f.contains("hammamet")) || (t.contains("حمامات") || t.contains("hammamet")) ->
            RouteEstimate("~ 200 km", "~ 2h 15min")
        (f.contains("مطار") || f.contains("aeroport") || f.contains("airport")) ->
            RouteEstimate("~ 12 km", "~ 18 min")
        from.isNotBlank() && to.isNotBlank() ->
            RouteEstimate("~ مسار محدد", "~ حسب حركة المرور")
        else ->
            RouteEstimate("نزل الزيتونة - صفاقس", "Les Oliviers Palace")
    }
}

private fun getTransportEmoji(transport: String): String {
    return when {
        transport.contains("حافلة") || transport.contains("bus") -> "🚌"
        transport.contains("فان") || transport.contains("van") -> "🚐"
        transport.contains("دراجة") || transport.contains("moto") -> "🏍️"
        else -> "🚗"
    }
}
