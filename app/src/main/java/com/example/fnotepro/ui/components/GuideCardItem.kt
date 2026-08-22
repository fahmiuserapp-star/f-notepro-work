package com.example.fnotepro.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.ui.theme.ErrorRed
import com.example.fnotepro.ui.theme.GoldAccent
import com.example.fnotepro.ui.theme.OkGreen
import com.example.fnotepro.ui.theme.OliveDark
import com.example.fnotepro.ui.theme.OlivePrimary
import com.example.fnotepro.ui.theme.SecurityBlue
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization

@Composable
fun GuideCardItem(
    guide: Guide,
    language: AppLanguage,
    canEdit: Boolean,
    onOpenCard: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenSafetyOfficer: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDetailsExpanded by remember { mutableStateOf(false) }

    fun makeCall(phone: String) {
        val clean = phone.replace("+", "").replace(" ", "").trim()
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    }

    fun openWhatsApp(phone: String) {
        var clean = phone.replace("+", "").replace(" ", "").trim()
        if (clean.length == 8) clean = "216$clean"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$clean"))
        context.startActivity(intent)
    }

    fun openMap(from: String, to: String) {
        val origin = from.trim().ifBlank { "صفاقس تونس" }
        val destination = to.trim().ifBlank { "Les Oliviers Palace Sfax Tunisia" }
        val uri = if (from.isNotBlank() && to.isNotBlank()) {
            Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${Uri.encode(destination)}&travelmode=driving")
        } else if (to.isNotBlank()) {
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")
        } else {
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(origin)}")
        }
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(Localization.get("delete", language), fontWeight = FontWeight.Bold, color = ErrorRed) },
            text = { Text(Localization.get("delete_confirm", language)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(Localization.get("delete", language), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Accent Bar (Green for synced / Gold for local)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(if (guide.isSynced) OkGreen else GoldAccent)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Meta row: Delegation Badge, Nationality with Flag, and Safety Officer badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delegation Type Color-Coded Badge
                    DelegationBadge(
                        delegationType = guide.delegationType,
                        language = language
                    )

                    // Nationality & Flag Chip
                    if (guide.nationality.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = guide.nationality,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Safety Officer Quick Clickable Chip
                    if (guide.safetyOfficerName.isNotBlank()) {
                        Surface(
                            onClick = onOpenSafetyOfficer,
                            shape = RoundedCornerShape(8.dp),
                            color = SecurityBlue.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = SecurityBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = guide.safetyOfficerName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecurityBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Guide Header & Contact Section (Harmonious & Integrated)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Guide Avatar
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(OlivePrimary.copy(alpha = 0.15f))
                            .border(2.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!guide.guidePhoto.isNullOrBlank()) {
                            AsyncImage(
                                model = guide.guidePhoto,
                                contentDescription = "Guide Photo",
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = OlivePrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Guide Name, Agency, and People count
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = guide.name.ifBlank { Localization.get("guide_name", language) },
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (guide.agency.isNotBlank()) {
                            Text(
                                text = guide.agency,
                                fontSize = 12.sp,
                                color = OlivePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${guide.peopleCount} ${Localization.get("people_count", language)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Direct Call, WhatsApp & Map Quick Buttons (Clean & Harmonious)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (guide.phone.isNotBlank()) {
                            // Call Action
                            IconButton(
                                onClick = { makeCall(guide.phone) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(OlivePrimary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // WhatsApp Action
                            IconButton(
                                onClick = { openWhatsApp(guide.phone) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(OkGreen, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // Map / Route Action
                        IconButton(
                            onClick = { openMap(guide.fromLocation, guide.toLocation) },
                            modifier = Modifier
                                .size(38.dp)
                                .background(GoldAccent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map Route",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transport & Plate Bar with "تفاصيل / Details" compact toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Compact Transport & Plate Summary
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = OlivePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${guide.carCount}x ${guide.transportType.ifBlank { Localization.get("car", language) }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Small "تفاصيل / Details" button that toggles inline expansion
                            Surface(
                                onClick = { isDetailsExpanded = !isDetailsExpanded },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDetailsExpanded) OlivePrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isDetailsExpanded) Localization.get("hide", language) else Localization.get("details", language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDetailsExpanded) Color.White else OlivePrimary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = if (isDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = if (isDetailsExpanded) Color.White else OlivePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Collapsible Details Accordion
                        AnimatedVisibility(
                            visible = isDetailsExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Plate
                                if (guide.plate.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = Localization.get("plate", language) + ":",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TunisianPlateBadge(
                                            plateText = guide.plate,
                                            isForeign = guide.isForeignPlate
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // Route: From -> To
                                if (guide.fromLocation.isNotBlank() || guide.toLocation.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${guide.fromLocation} ➔ ${guide.toLocation}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Notes
                                if (guide.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📝 ${guide.notes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }

                                // Vehicle / Motorcycle photos badge
                                if (guide.vehiclePhotos.isNotEmpty() || guide.motorcyclePhotos.isNotEmpty() || !guide.busPhoto.isNullOrBlank()) {
                                    val count = guide.vehiclePhotos.size + guide.motorcyclePhotos.size + (if (guide.busPhoto != null) 1 else 0)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🖼️ $count صور مرفقة",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OkGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom 4 Primary Action Buttons with Vertical Icon + Label (البطاقة, كرت المتابعة, تعديل, حذف)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Delegation Card Button (البطاقة)
                    VerticalActionButton(
                        icon = Icons.Default.Badge,
                        label = Localization.get("card", language),
                        iconColor = Color.White,
                        containerColor = OlivePrimary,
                        onClick = onOpenCard,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // 2. Follow-up / Coordination Card Button (كرت المتابعة)
                    VerticalActionButton(
                        icon = Icons.Default.Security,
                        label = Localization.get("follow_up", language),
                        iconColor = Color.White,
                        containerColor = SecurityBlue,
                        onClick = onOpenSecurity,
                        modifier = Modifier.weight(1f)
                    )

                    if (canEdit) {
                        Spacer(modifier = Modifier.width(6.dp))

                        // 3. Edit Button (تعديل)
                        VerticalActionButton(
                            icon = Icons.Default.Edit,
                            label = Localization.get("edit", language),
                            iconColor = OlivePrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = onEdit,
                            modifier = Modifier.weight(0.9f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // 4. Delete Button (حذف)
                        VerticalActionButton(
                            icon = Icons.Default.Delete,
                            label = Localization.get("delete", language),
                            iconColor = ErrorRed,
                            containerColor = ErrorRed.copy(alpha = 0.12f),
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier.height(58.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                maxLines = 1
            )
        }
    }
}
