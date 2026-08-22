package com.example.fnotepro.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.data.model.SavedNumber
import com.example.fnotepro.data.model.SendHistoryItem
import com.example.fnotepro.ui.components.TunisianPlateBadge
import com.example.fnotepro.ui.theme.GoldAccent
import com.example.fnotepro.ui.theme.OkGreen
import com.example.fnotepro.ui.theme.OliveDark
import com.example.fnotepro.ui.theme.OlivePrimary
import com.example.fnotepro.ui.theme.SecurityBlue
import com.example.fnotepro.ui.theme.WhatsAppColor
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecurityCardDialog(
    guide: Guide,
    language: AppLanguage,
    savedNumbers: List<SavedNumber>,
    sendHistory: List<SendHistoryItem>,
    onAddSavedNumbers: (String) -> Unit,
    onRecordSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customParagraph by remember {
        mutableStateOf("دخول المجموعة إلى hôtel les oliviers palace بأمان")
    }
    var newNumbersInput by remember { mutableStateOf("") }
    var isCardReady by remember { mutableStateOf(true) }

    fun sendToNumber(phone: String) {
        val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
        val formattedPhone = if (cleanPhone.startsWith("+")) cleanPhone else if (cleanPhone.startsWith("216")) "+$cleanPhone" else "+216$cleanPhone"
        val message = """
📢 *F-Note Pro Max | كرت التنسيق والمتابعة*
🏨 *Les Oliviers Palace - Sfax*
---------------------------------
📌 *الحالة:* $customParagraph
👤 *المرشد:* ${guide.name}
📞 *الهاتف:* ${guide.phone}
🌍 *الجنسية:* ${guide.nationality}
👥 *العدد:* ${guide.peopleCount} | 🚐 *الوسيلة:* ${guide.transportType}
🇹🇳 *اللوحة:* ${guide.plate}
🏢 *الوكالة:* ${guide.agency}
🛡️ *عون السلامة:* ${guide.safetyOfficerName} (${guide.safetyOfficerPhone})
---------------------------------
        """.trimIndent()

        onRecordSend(formattedPhone)
        val url = "https://wa.me/${formattedPhone.replace("+", "")}?text=${URLEncoder.encode(message, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

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
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecurityBlue)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.get("security_card_title", language),
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Custom Paragraph Section
                    Text(
                        text = Localization.get("custom_group_label", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecurityBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customParagraph,
                        onValueChange = {
                            customParagraph = it
                            isCardReady = true
                        },
                        placeholder = { Text(Localization.get("custom_group_placeholder", language)) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Card Preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏨 Hôtel Les Oliviers Palace",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OlivePrimary
                                )
                                Text(
                                    text = "SFAX",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SecurityBlue.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = customParagraph.ifBlank { "—" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecurityBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("👤 ${guide.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("📞 ${guide.phone}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🌍 ${guide.nationality} | 👥 ${guide.peopleCount}", fontSize = 11.sp)
                                Text("🚐 ${guide.transportType}", fontSize = 11.sp)
                            }

                            if (guide.plate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TunisianPlateBadge(plateText = guide.plate)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Saved Numbers Chips
                    Text(
                        text = Localization.get("saved_numbers_label", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val defaultNumbers = if (savedNumbers.isEmpty()) {
                        listOf(
                            SavedNumber(guideId = guide.id, phone = "98123456"),
                            SavedNumber(guideId = guide.id, phone = "97654321"),
                            SavedNumber(guideId = guide.id, phone = "55443322")
                        )
                    } else savedNumbers

                    val sentPhonesSet = sendHistory.map { it.phone.replace("[^0-9]".toRegex(), "").takeLast(8) }.toSet()

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        defaultNumbers.forEach { item ->
                            val cleanDigits = item.phone.replace("[^0-9]".toRegex(), "").takeLast(8)
                            val isSent = sentPhonesSet.contains(cleanDigits)

                            Surface(
                                onClick = { sendToNumber(item.phone) },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSent) OkGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSent) OkGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSent) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Sent", tint = OkGreen, modifier = Modifier.size(16.dp))
                                    } else {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = WhatsAppColor, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+216 $cleanDigits",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSent) OkGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Add new numbers batch
                    Text(
                        text = Localization.get("add_numbers_label", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newNumbersInput,
                        onValueChange = { newNumbersInput = it },
                        placeholder = { Text("98123456\n55443322") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newNumbersInput.isNotBlank()) {
                                onAddSavedNumbers(newNumbersInput)
                                newNumbersInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Localization.get("save_numbers", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Send History Section
                    if (sendHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = OlivePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.get("send_history", language), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            sendHistory.take(5).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = OkGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(item.phone, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        TextButton(
                                            onClick = { sendToNumber(item.phone) },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                                        ) {
                                            Text(Localization.get("resend", language), fontSize = 10.sp, color = SecurityBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
