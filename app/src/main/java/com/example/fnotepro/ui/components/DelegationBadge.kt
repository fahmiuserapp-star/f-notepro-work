package com.example.fnotepro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization

data class DelegationTypeInfo(
    val key: String,
    val labelKey: String,
    val icon: String,
    val primaryColor: Color,
    val bgColor: Color
)

object DelegationTypes {
    val list = listOf(
        DelegationTypeInfo(
            key = "tourist",
            labelKey = "type_tourist",
            icon = "🌴",
            primaryColor = Color(0xFF16A34A),
            bgColor = Color(0xFFDCFCE7)
        ),
        DelegationTypeInfo(
            key = "diplomatic",
            labelKey = "type_diplomatic",
            icon = "🏛️",
            primaryColor = Color(0xFF9333EA),
            bgColor = Color(0xFFF3E8FF)
        ),
        DelegationTypeInfo(
            key = "official",
            labelKey = "type_official",
            icon = "👔",
            primaryColor = Color(0xFF2563EB),
            bgColor = Color(0xFFDBEAFE)
        ),
        DelegationTypeInfo(
            key = "business",
            labelKey = "type_business",
            icon = "💼",
            primaryColor = Color(0xFF475569),
            bgColor = Color(0xFFF1F5F9)
        ),
        DelegationTypeInfo(
            key = "sports",
            labelKey = "type_sports",
            icon = "⚽",
            primaryColor = Color(0xFFEA580C),
            bgColor = Color(0xFFFFEDD5)
        )
    )

    fun getInfo(key: String): DelegationTypeInfo {
        return list.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: list.first()
    }
}

@Composable
fun DelegationBadge(
    delegationType: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val info = DelegationTypes.getInfo(delegationType)

    Box(
        modifier = modifier
            .background(info.bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, info.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = info.icon,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Localization.get(info.labelKey, language),
                color = info.primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
