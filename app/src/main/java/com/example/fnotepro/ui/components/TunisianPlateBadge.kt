package com.example.fnotepro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fnotepro.ui.theme.GoldAccent

@Composable
fun TunisianPlateBadge(
    plateText: String,
    modifier: Modifier = Modifier,
    isForeign: Boolean = false
) {
    val display = if (plateText.isBlank()) "—" else plateText

    // Check if it matches Tunisian standard plate (contains "تونس" or "TN")
    val isTunisianFormat = display.contains("تونس") || display.contains("TN")

    if (isForeign || !isTunisianFormat) {
        // Foreign / Custom plate badge
        Box(
            modifier = modifier
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF64748B), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🌍",
                    fontSize = 12.sp
                )
                Text(
                    text = display,
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    } else {
        // Standard Tunisian License Plate: 4 Digits + "تونس" + 3 Digits
        // Parse parts if format is "XXXX تونس YYY" or "YYY TN XXXX"
        val parts = if (display.contains("تونس")) {
            display.split("تونس").map { it.trim() }
        } else if (display.contains("TN")) {
            val p = display.split("TN").map { it.trim() }
            if (p.size >= 2) listOf(p[1], p[0]) else listOf(display, "")
        } else {
            listOf(display)
        }

        val leftPart = parts.getOrNull(0) ?: ""
        val rightPart = parts.getOrNull(1) ?: ""

        Box(
            modifier = modifier
                .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leftPart.isNotBlank()) {
                    Text(
                        text = leftPart,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFDC2626).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تونس",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                if (rightPart.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rightPart,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}
