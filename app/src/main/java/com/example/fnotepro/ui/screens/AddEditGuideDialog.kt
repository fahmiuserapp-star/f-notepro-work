package com.example.fnotepro.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.ui.components.DelegationTypes
import com.example.fnotepro.ui.components.TunisianPlateBadge
import com.example.fnotepro.ui.theme.GoldAccent
import com.example.fnotepro.ui.theme.OkGreen
import com.example.fnotepro.ui.theme.OliveDark
import com.example.fnotepro.ui.theme.OlivePrimary
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditGuideDialog(
    initialGuide: Guide?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Guide) -> Unit
) {
    var delegationType by remember { mutableStateOf(initialGuide?.delegationType ?: "tourist") }
    var delegationTypeExpanded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(initialGuide?.name ?: "") }
    var phone by remember { mutableStateOf(initialGuide?.phone ?: "") }
    var nationality by remember { mutableStateOf(initialGuide?.nationality ?: "") }
    var peopleCount by remember { mutableStateOf(initialGuide?.peopleCount ?: "1") }
    var carCount by remember { mutableStateOf((initialGuide?.carCount ?: 1).coerceAtLeast(1)) }
    var transportType by remember { mutableStateOf(initialGuide?.transportType ?: "حافلة كبيرة") }
    var transportExpanded by remember { mutableStateOf(false) }
    var fromLocation by remember { mutableStateOf(initialGuide?.fromLocation ?: "") }
    var toLocation by remember { mutableStateOf(initialGuide?.toLocation ?: "") }

    // Plate logic: Tunisian standard (4 digits + تونس + 3 digits) vs Foreign / Custom
    val existingPlate = initialGuide?.plate ?: ""
    var isForeignPlate by remember { mutableStateOf(initialGuide?.isForeignPlate ?: (!existingPlate.contains("تونس") && existingPlate.isNotBlank())) }
    
    // Parse parts for 4 digits (left) and 3 digits (right)
    val parsedParts = if (existingPlate.contains("تونس")) {
        existingPlate.split("تونس").map { it.trim() }
    } else listOf("", "")
    
    var plate4Digits by remember { mutableStateOf(if (parsedParts.isNotEmpty()) parsedParts[0] else "") }
    var plate3Digits by remember { mutableStateOf(if (parsedParts.size > 1) parsedParts[1] else "") }
    var foreignPlateNumber by remember { mutableStateOf(if (isForeignPlate) existingPlate else "") }

    // Photos
    var guidePhotoUrl by remember { mutableStateOf(initialGuide?.guidePhoto ?: "") }
    var vehiclePhotos by remember {
        mutableStateOf(
            if (initialGuide?.vehiclePhotos?.isNotEmpty() == true) initialGuide.vehiclePhotos
            else if (!initialGuide?.busPhoto.isNullOrBlank()) listOf(initialGuide.busPhoto!!)
            else emptyList()
        )
    }
    var motorcyclePhotos by remember { mutableStateOf(initialGuide?.motorcyclePhotos ?: emptyList()) }

    var photoDialogTarget by remember { mutableStateOf<String?>(null) } // "guide", "vehicle", "moto"
    var tempPhotoInput by remember { mutableStateOf("") }

    var agency by remember { mutableStateOf(initialGuide?.agency ?: "") }
    var notes by remember { mutableStateOf(initialGuide?.notes ?: "") }
    var visibility by remember { mutableStateOf(initialGuide?.visibility ?: "public") }

    // Photo input dialog
    if (photoDialogTarget != null) {
        AlertDialog(
            onDismissRequest = { photoDialogTarget = null },
            title = {
                Text(
                    text = when (photoDialogTarget) {
                        "guide" -> "📸 إضافة رابط صورة المرشد"
                        "vehicle" -> "🚌 إضافة رابط صورة المركبة / الحافلة"
                        else -> "🏍️ إضافة رابط صورة الدراجة النارية"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = OlivePrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempPhotoInput,
                        onValueChange = { tempPhotoInput = it },
                        placeholder = { Text("https://example.com/photo.jpg") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPhotoInput.isNotBlank()) {
                            when (photoDialogTarget) {
                                "guide" -> guidePhotoUrl = tempPhotoInput.trim()
                                "vehicle" -> {
                                    if (vehiclePhotos.size < 5) {
                                        vehiclePhotos = vehiclePhotos + tempPhotoInput.trim()
                                    }
                                }
                                "moto" -> {
                                    if (motorcyclePhotos.size < 10) {
                                        motorcyclePhotos = motorcyclePhotos + tempPhotoInput.trim()
                                    }
                                }
                            }
                        }
                        tempPhotoInput = ""
                        photoDialogTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkGreen)
                ) {
                    Text("إضافة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    tempPhotoInput = ""
                    photoDialogTarget = null
                }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialGuide == null) Localization.get("add_guide_title", language) else Localization.get("edit_guide_title", language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = OlivePrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: نوع الوفد ومعلومات المرشد
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📋 1. نوع الوفد والمعلومات العامة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OlivePrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Delegation Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = delegationTypeExpanded,
                            onExpandedChange = { delegationTypeExpanded = !delegationTypeExpanded }
                        ) {
                            val currentInfo = DelegationTypes.getInfo(delegationType)
                            OutlinedTextField(
                                value = "${currentInfo.icon} ${Localization.get(currentInfo.labelKey, language)}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(Localization.get("delegation_type", language), fontWeight = FontWeight.Bold) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = delegationTypeExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentInfo.primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = delegationTypeExpanded,
                                onDismissRequest = { delegationTypeExpanded = false }
                            ) {
                                DelegationTypes.list.forEach { typeItem ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(typeItem.icon, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = Localization.get(typeItem.labelKey, language),
                                                    fontWeight = FontWeight.Bold,
                                                    color = typeItem.primaryColor
                                                )
                                            }
                                        },
                                        onClick = {
                                            delegationType = typeItem.key
                                            delegationTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guide Name & Phone
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(Localization.get("guide_name", language)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(Localization.get("phone", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = peopleCount,
                                onValueChange = { peopleCount = it },
                                label = { Text(Localization.get("people_count", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nationality & Suggestions
                        OutlinedTextField(
                            value = nationality,
                            onValueChange = { nationality = it },
                            label = { Text(Localization.get("nationality", language)) },
                            placeholder = { Text("مثال: فرنسية 🇫🇷 / إيطالية 🇮🇹") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Quick Nationality flag chips
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("تونسية 🇹🇳", "فرنسية 🇫🇷", "إيطالية 🇮🇹", "ألمانية 🇩🇪", "بريطانية 🇬🇧", "جزائرية 🇩🇿", "ليبية 🇱🇾").forEach { nat ->
                                Surface(
                                    onClick = { nationality = nat },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (nationality == nat) OlivePrimary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = nat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (nationality == nat) Color.White else OlivePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = agency,
                            onValueChange = { agency = it },
                            label = { Text(Localization.get("agency", language)) },
                            placeholder = { Text("وكالة الأسفار / الشركة المنظمة") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: وسائل النقل واللوحة
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🚌 2. وسائل النقل واللوحة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OlivePrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transport Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = transportExpanded,
                            onExpandedChange = { transportExpanded = !transportExpanded }
                        ) {
                            OutlinedTextField(
                                value = transportType,
                                onValueChange = { transportType = it },
                                label = { Text(Localization.get("transport_type", language)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = transportExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = transportExpanded,
                                onDismissRequest = { transportExpanded = false }
                            ) {
                                listOf(
                                    Localization.get("large_bus", language),
                                    Localization.get("small_bus", language),
                                    Localization.get("car", language),
                                    Localization.get("van", language),
                                    Localization.get("motorcycle", language),
                                    Localization.get("other", language)
                                ).forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            transportType = item
                                            transportExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vehicle Count with Stepper (Defaults to 1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Localization.get("cars_count", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("الافتراضي: 1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (carCount > 1) carCount-- },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .border(1.dp, OlivePrimary, CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = OlivePrimary, modifier = Modifier.size(18.dp))
                                }

                                Text(
                                    text = "$carCount",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(
                                    onClick = { carCount++ },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(OlivePrimary, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Plate Selection: Standard Tunisian vs Foreign/Custom Plate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isForeignPlate) Localization.get("foreign_plate", language) else Localization.get("tunisian_plate", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = OlivePrimary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("لوحة أجنبية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isForeignPlate,
                                    onCheckedChange = { isForeignPlate = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = OliveDark)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isForeignPlate) {
                            // Foreign / Special Plate Single Field
                            OutlinedTextField(
                                value = foreignPlateNumber,
                                onValueChange = { foreignPlateNumber = it },
                                label = { Text(Localization.get("custom_plate", language)) },
                                placeholder = { Text("مثال: 75 CD 1234 / 8888 POL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            // Standard Tunisian Plate: 4 Digits + "تونس" + 3 Digits
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 4 digits (left/registration)
                                OutlinedTextField(
                                    value = plate4Digits,
                                    onValueChange = { if (it.length <= 5) plate4Digits = it },
                                    label = { Text("4 أرقام (يسار)", fontSize = 11.sp) },
                                    placeholder = { Text("4210") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Central "تونس" Badge
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDC2626).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "تونس",
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFDC2626),
                                        fontSize = 14.sp
                                    )
                                }

                                // 3 digits (right/series)
                                OutlinedTextField(
                                    value = plate3Digits,
                                    onValueChange = { if (it.length <= 4) plate3Digits = it },
                                    label = { Text("3 أرقام (يمين)", fontSize = 11.sp) },
                                    placeholder = { Text("185") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Route: From & To
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = fromLocation,
                                onValueChange = { fromLocation = it },
                                label = { Text(Localization.get("from", language)) },
                                placeholder = { Text("مطار صفاقس...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = toLocation,
                                onValueChange = { toLocation = it },
                                label = { Text(Localization.get("to", language)) },
                                placeholder = { Text("Les Oliviers Palace...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: الصور المرفقة (مرشد، مركبات حتى 5، دراجات حتى 10)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📸 3. الصور المرفقة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OlivePrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guide Photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(OlivePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (guidePhotoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = guidePhotoUrl,
                                            contentDescription = "Guide",
                                            modifier = Modifier.matchParentSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = OlivePrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Localization.get("guide_photo", language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { photoDialogTarget = "guide" },
                                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (guidePhotoUrl.isBlank()) "+ إضافة" else "تغيير", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Vehicle Photos (Up to 5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚌 صور المركبات (${vehiclePhotos.size}/5)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (vehiclePhotos.size < 5) {
                                TextButton(onClick = { photoDialogTarget = "vehicle" }) {
                                    Text("+ إضافة صورة", color = OlivePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        if (vehiclePhotos.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                vehiclePhotos.forEachIndexed { idx, url ->
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Vehicle",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                        IconButton(
                                            onClick = { vehiclePhotos = vehiclePhotos.filterIndexed { i, _ -> i != idx } },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(20.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Motorcycle Photos (Up to 10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏍️ صور الدراجات النارية (${motorcyclePhotos.size}/10)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (motorcyclePhotos.size < 10) {
                                TextButton(onClick = { photoDialogTarget = "moto" }) {
                                    Text("+ إضافة صورة", color = OlivePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        if (motorcyclePhotos.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                motorcyclePhotos.forEachIndexed { idx, url ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Motorcycle",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                        IconButton(
                                            onClick = { motorcyclePhotos = motorcyclePhotos.filterIndexed { i, _ -> i != idx } },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(20.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 4: الملاحظات ومستوى الظهور
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📝 4. الملاحظات ومستوى الظهور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OlivePrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(Localization.get("notes", language)) },
                            placeholder = { Text("تفاصيل التنسيق، ساعة الوصول، الترتيبات...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visibility Options (Public vs Private)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { visibility = "public" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (visibility == "public") OlivePrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = Localization.get("public", language),
                                    color = if (visibility == "public") Color.White else OlivePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            Surface(
                                onClick = { visibility = "private" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (visibility == "private") OlivePrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = Localization.get("private", language),
                                    color = if (visibility == "private") Color.White else OlivePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Save & Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.get("cancel", language))
                    }

                    Button(
                        onClick = {
                            val computedPlate = if (isForeignPlate) {
                                foreignPlateNumber.trim()
                            } else {
                                if (plate4Digits.isNotBlank() || plate3Digits.isNotBlank()) {
                                    "${plate4Digits.trim()} تونس ${plate3Digits.trim()}".trim()
                                } else ""
                            }

                            val guideToSave = initialGuide?.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                nationality = nationality.trim(),
                                peopleCount = peopleCount.trim(),
                                carCount = carCount,
                                transportType = transportType,
                                delegationType = delegationType,
                                fromLocation = fromLocation.trim(),
                                toLocation = toLocation.trim(),
                                plate = computedPlate,
                                isForeignPlate = isForeignPlate,
                                guidePhoto = guidePhotoUrl.ifBlank { null },
                                busPhoto = vehiclePhotos.firstOrNull(),
                                vehiclePhotos = vehiclePhotos,
                                motorcyclePhotos = motorcyclePhotos,
                                agency = agency.trim(),
                                notes = notes.trim(),
                                visibility = visibility
                            ) ?: Guide(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                phone = phone.trim(),
                                nationality = nationality.trim(),
                                peopleCount = peopleCount.trim(),
                                carCount = carCount,
                                transportType = transportType,
                                delegationType = delegationType,
                                fromLocation = fromLocation.trim(),
                                toLocation = toLocation.trim(),
                                plate = computedPlate,
                                isForeignPlate = isForeignPlate,
                                guidePhoto = guidePhotoUrl.ifBlank { null },
                                busPhoto = vehiclePhotos.firstOrNull(),
                                vehiclePhotos = vehiclePhotos,
                                motorcyclePhotos = motorcyclePhotos,
                                agency = agency.trim(),
                                notes = notes.trim(),
                                visibility = visibility
                            )
                            onSave(guideToSave)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.get("save_guide", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
