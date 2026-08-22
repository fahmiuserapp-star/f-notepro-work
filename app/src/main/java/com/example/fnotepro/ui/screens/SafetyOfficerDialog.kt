package com.example.fnotepro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.ui.theme.GoldAccent
import com.example.fnotepro.ui.theme.OkGreen
import com.example.fnotepro.ui.theme.OliveDark
import com.example.fnotepro.ui.theme.OlivePrimary
import com.example.fnotepro.ui.theme.SecurityBlue
import com.example.fnotepro.ui.theme.WhatsAppColor
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization

@Composable
fun SafetyOfficerDialog(
    guide: Guide,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val officerName = if (guide.safetyOfficerName.isNotBlank()) guide.safetyOfficerName else "عون السلامة محمد التونسي"
    val officerPhone = if (guide.safetyOfficerPhone.isNotBlank()) guide.safetyOfficerPhone else "+21698123456"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = SecurityBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Localization.get("safety_officer", language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = SecurityBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SecurityBlue)
                        .border(3.dp, GoldAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!guide.safetyOfficerPhoto.isNullOrBlank()) {
                        AsyncImage(
                            model = guide.safetyOfficerPhoto,
                            contentDescription = officerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = officerName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "نزل Les Oliviers Palace - صفاقس",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = officerPhone,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$officerPhone"))
                            context.startActivity(dial)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OkGreen)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.get("call", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cleanNumber = officerPhone.replace("[^0-9+]".toRegex(), "")
                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${cleanNumber.replace("+", "")}"))
                            context.startActivity(waIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.get("whatsapp", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
