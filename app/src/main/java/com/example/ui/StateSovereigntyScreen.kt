package com.example.ui

import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PersonEntity
import com.example.ui.theme.BronzeGold
import com.example.ui.theme.HeritageGreen
import com.example.ui.theme.HeritageGreenDark
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.WarmSand
import kotlinx.coroutines.delay

// Data class representing Water Pumps and Wells linked to farms
data class WaterPump(
    val id: String,
    val name: String,
    val capacityHp: Double,
    val location: String,
    val deedId: String,
    val isOperational: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateSovereigntyScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    val context = LocalContext.current
    val landDeeds by viewModel.landDeeds.collectAsState()
    
    var selectedDeed by remember { mutableStateOf<LandDeed?>(null) }
    var showAddDeedForm by remember { mutableStateOf(false) }
    
    // Form fields
    var farmName by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var areaInput by remember { mutableStateOf("") }
    var cropInput by remember { mutableStateOf("") }
    var waterInput by remember { mutableStateOf("") }
    var yieldInput by remember { mutableStateOf("") }
    var selectedOwnerForNewDeed by remember { mutableStateOf<PersonEntity?>(null) }
    var showOwnerDropdown by remember { mutableStateOf(false) }
    
    // Transfer mode
    var isTransferMode by remember { mutableStateOf(false) }
    var newOwnerForTransfer by remember { mutableStateOf<PersonEntity?>(null) }
    var showTransferDropdown by remember { mutableStateOf(false) }
    
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Water Pump Machinery State
    var showPumpForm by remember { mutableStateOf(false) }
    var pumpName by remember { mutableStateOf("") }
    var pumpCapacity by remember { mutableStateOf("") }
    var pumpLocation by remember { mutableStateOf("") }
    var pumpDeedId by remember { mutableStateOf("") }
    var showDeedDropdownForPump by remember { mutableStateOf(false) }

    var waterPumps by remember {
        mutableStateOf(
            listOf(
                WaterPump("PUMP-301", "طلمبة الري المحورية الكبرى بالزيداب", 45.0, "الزيداب", "L-101", true),
                WaterPump("PUMP-302", "بئر الإرواء الجوفي لآل البشير شندي", 25.0, "شندي", "L-103", true),
                WaterPump("PUMP-303", "مضخة الطاقة الشمسية جزر السرة", 15.0, "المحمية", "L-104", true)
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Author and Architect Sovereign Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = HeritageGreenDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BronzeGold, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BronzeGold.copy(0.15f))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "رصيعة سيادية",
                            tint = BronzeGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ولاية نهر النيل المتحدة الفيدرالية",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "جمهورية السودان • السجل الرقمي الفيدرالي العقاري والزراعي",
                        color = BronzeGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Displaying Author Name prominently
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.08f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "المؤلف العبقري والمصمم التقني الأول للولاية الفيدرالية",
                                color = BronzeGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "حمزة العجلابي",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "مستشار التحول الرقمي الفيدرالي لولاية نهر النيل المتحدة",
                                color = Color.White.copy(0.7f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 1.5 Interactive Geographic Map Guide Card
        item {
            StateGeographicMapGuide(allPeople = allPeople, viewModel = viewModel)
        }

        // 2. State Decrees Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HeritageGreen.copy(0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "الدستور",
                            tint = HeritageGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ميثاق الأراضي والسيادة الزراعية لولاية نهر النيل المتحدة لعام 2026",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تنفيذاً لتوجيهات المستشار التقني حمزة العجلابي، يربط هذا النظام السجل الفيدرالي الموحد لولاية نهر النيل المتحدة مع سجل الحيازات والملكيات الزراعية بالولاية لتمكين الملاك من تنقيل الحيازات وإصدار طوابع وصكوك الملكية القانونية الصادرة من مصلحة الأراضي، بالإضافة إلى ترخيص مضخات الري والآبار الجوفية.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 3. active deeds area title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل الحيازات والمسطحات الزراعية بالعواصم والبلديات",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showAddDeedForm = !showAddDeedForm },
                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (showAddDeedForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "تسجيل جديد",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showAddDeedForm) "إغلاق" else "ملكية جديدة",
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Add Deed Form
        if (showAddDeedForm) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BronzeGold.copy(0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "تسجيل حيازة زراعية/عقارية جديدة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeritageGreenDark
                        )
                        
                        OutlinedTextField(
                            value = farmName,
                            onValueChange = { farmName = it },
                            label = { Text("اسم المشروع أو المزرعة") },
                            placeholder = { Text("مثال: مشروع الزيداب الشمالي") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = locationName,
                                onValueChange = { locationName = it },
                                label = { Text("الموقع بالولاية") },
                                placeholder = { Text("مثال: طيبة الخواض") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = areaInput,
                                onValueChange = { areaInput = it },
                                label = { Text("المساحة بالفدان") },
                                placeholder = { Text("مثال: 75") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = cropInput,
                                onValueChange = { cropInput = it },
                                label = { Text("المحصول والزرع المعتمد") },
                                placeholder = { Text("مثال: القمح والبرسيم") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = waterInput,
                                onValueChange = { waterInput = it },
                                label = { Text("مصدر الري والمياه") },
                                placeholder = { Text("طلمبات نيلية") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        OutlinedTextField(
                            value = yieldInput,
                            onValueChange = { yieldInput = it },
                            label = { Text("العائد السنوي المقدر (ج.س)") },
                            placeholder = { Text("3000000") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Select Owner Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showOwnerDropdown = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = selectedOwnerForNewDeed?.getFullName() ?: "انقر هنا لتحديد مالك الحيازة من السجل",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showOwnerDropdown,
                                onDismissRequest = { showOwnerDropdown = false }
                            ) {
                                allPeople.filter { it.isAlive }.forEach { person ->
                                    DropdownMenuItem(
                                        text = { Text(person.getFullName() + " - " + person.villageName, color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            selectedOwnerForNewDeed = person
                                            showOwnerDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                val area = areaInput.toDoubleOrNull() ?: 10.0
                                val yield = yieldInput.toDoubleOrNull() ?: 1000000.0
                                if (farmName.isNotBlank() && locationName.isNotBlank()) {
                                    viewModel.registerNewLandDeed(
                                        name = farmName,
                                        location = locationName,
                                        area = area,
                                        crop = cropInput.ifBlank { "القمح الفيدرالي" },
                                        source = waterInput.ifBlank { "النيل الأزرق" },
                                        yield = yield,
                                        ownerId = selectedOwnerForNewDeed?.id
                                    )
                                    // Reset
                                    farmName = ""
                                    locationName = ""
                                    areaInput = ""
                                    cropInput = ""
                                    waterInput = ""
                                    yieldInput = ""
                                    selectedOwnerForNewDeed = null
                                    showAddDeedForm = false
                                    Toast.makeText(context, "تم تسجيل وتوثيق الحيازة العقارية الجديدة بنجاح في السجل الفيدرالي الموحد!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BronzeGold, contentColor = Color.White),
                            enabled = farmName.isNotBlank() && locationName.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تسجيل وتصديق الملكية", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // List land deeds
        items(landDeeds) { deed ->
            val owner = allPeople.find { it.id == deed.ownerId }
            val isSelected = selectedDeed?.id == deed.id
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedDeed = if (isSelected) null else deed }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BronzeGold else HeritageGreen.copy(0.12f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = deed.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGreenDark
                            )
                            Text(
                                text = "المطابقة الجغرافية: ولاية نهر النيل - ${deed.location}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BronzeGold.copy(0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = deed.id,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BronzeGold.copy(0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المساحة الإجمالية", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("${deed.areaFeddans} فدان", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text("المحصول السائد", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(deed.cropType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text("المالك المقر له بالسيادة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(owner?.getFullName() ?: "وزارة المالية للولاية المتحدة", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = HeritageGreen)
                        }
                    }

                    // Reveal controls if selected
                    AnimatedVisibility(visible = isSelected) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(color = BronzeGold.copy(0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مصدر مياه الري: " + deed.waterSource,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "العائد السنوي: " + String.format("%,.0f", deed.annualYieldSdg) + " ج.س",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeritageGreen
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // PDF Output button
                                Button(
                                    onClick = {
                                        isGeneratingPdf = true
                                        exportLandDeedAsPdf(context, deed, owner)
                                        isGeneratingPdf = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGreenDark, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "طباعة الصك", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("طباعة وتنزيل الصك", fontSize = 11.sp)
                                }
                                
                                // Transfer owner button
                                Button(
                                    onClick = { isTransferMode = !isTransferMode },
                                    colors = ButtonDefaults.buttonColors(containerColor = BronzeGold, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = "نقل الملكية", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTransferMode) "إلغاء النقل" else "نقل الحيازة الشرعية", fontSize = 11.sp)
                                }
                            }
                            
                            // Transfer Option dropdown
                            AnimatedVisibility(visible = isTransferMode) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Text(
                                        text = "المستفيد الجديد (المشتري المعتمد):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { showTransferDropdown = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = newOwnerForTransfer?.getFullName() ?: "انقر لاختيار المشتري للملكية",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        DropdownMenu(
                                            expanded = showTransferDropdown,
                                            onDismissRequest = { showTransferDropdown = false }
                                        ) {
                                            allPeople.filter { it.id != deed.ownerId && it.isAlive }.forEach { p ->
                                                DropdownMenuItem(
                                                    text = { Text(p.getFullName() + " - " + p.villageName, color = MaterialTheme.colorScheme.onSurface) },
                                                    onClick = {
                                                        newOwnerForTransfer = p
                                                        showTransferDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Button(
                                        onClick = {
                                            val targetBuyer = newOwnerForTransfer
                                            if (targetBuyer != null) {
                                                // Fee charging simulations
                                                viewModel.transferLandOwnership(deed.id, targetBuyer.id)
                                                // Deducts some state fee
                                                viewModel.withdrawFromAccount(targetBuyer.id, 5000.0) // 5000 SDG registration fee
                                                
                                                Toast.makeText(context, "تم إفراغ وتحويل حيازة الأرض ونقل الملكية بنجاح لمصلحة المشتري الجديد الرشيد خصماً لرسوم التسجيل (5,000 ج.س)", Toast.LENGTH_LONG).show()
                                                newOwnerForTransfer = null
                                                isTransferMode = false
                                                selectedDeed = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen, contentColor = Color.White),
                                        enabled = newOwnerForTransfer != null,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("توقيع السند وإكمال الإلحاق العقاري", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. WATER PUMP & WELL MACHINERY REGISTRY CENTER - State feature!
        item {
            Column {
                HorizontalDivider(color = BronzeGold.copy(0.2f), modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بوابة تسجيل وترخيص مضخات الري والآبار",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { showPumpForm = !showPumpForm },
                        colors = ButtonDefaults.buttonColors(containerColor = BronzeGold, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(if (showPumpForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showPumpForm) "إغلاق" else "ترخيص مضخة", fontSize = 10.sp)
                    }
                }
                Text(
                    text = "نظام تنظيم وسحب الموارد المائية من نهر النيل والمياه الجوفية للنهوض بالإنتاج الزراعي",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        if (showPumpForm) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("إصدار ترخيص تصريف مائي ومكينة ري", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeritageGreen)

                        OutlinedTextField(
                            value = pumpName,
                            onValueChange = { pumpName = it },
                            label = { Text("نوع / معرّف مضخة الري (مثال: طلمبة ري لودر)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pumpCapacity,
                                onValueChange = { pumpCapacity = it },
                                label = { Text("قوة الماطور (حصان)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pumpLocation,
                                onValueChange = { pumpLocation = it },
                                label = { Text("موقع البئر/المشروع") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Link pump to land deed dropdown
                        Box {
                            Button(
                                onClick = { showDeedDropdownForPump = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (pumpDeedId.isNotBlank()) "مرتبط بالصك العقاري: $pumpDeedId" else "انقر لربط المضخة بصك الحيازة العقاري",
                                    fontSize = 11.sp
                                )
                            }
                            DropdownMenu(
                                expanded = showDeedDropdownForPump,
                                onDismissRequest = { showDeedDropdownForPump = false }
                            ) {
                                landDeeds.forEach { deed ->
                                    DropdownMenuItem(
                                        text = { Text("${deed.id} - ${deed.name}") },
                                        onClick = {
                                            pumpDeedId = deed.id
                                            showDeedDropdownForPump = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val cap = pumpCapacity.toDoubleOrNull() ?: 20.0
                                if (pumpName.isNotBlank() && pumpLocation.isNotBlank() && pumpDeedId.isNotBlank()) {
                                    val newId = "PUMP-${301 + waterPumps.size}"
                                    waterPumps = waterPumps + WaterPump(newId, pumpName, cap, pumpLocation, pumpDeedId, true)
                                    pumpName = ""
                                    pumpCapacity = ""
                                    pumpLocation = ""
                                    pumpDeedId = ""
                                    showPumpForm = false
                                    Toast.makeText(context, "تم إصدار رخصة التشغيل والربط بالصك العقاري سيادياً بنجاح!", Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = pumpName.isNotBlank() && pumpLocation.isNotBlank() && pumpDeedId.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = HeritageGreenDark, contentColor = BronzeGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("اعتماد الترخيص والتشغيل المالي والتقني", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(waterPumps) { pump ->
            val linkedDeed = landDeeds.find { it.id == pump.deedId }
            val pumpDeedOwner = allPeople.find { it.id == linkedDeed?.ownerId }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BronzeGold.copy(0.12f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (pump.isOperational) Color(0xFF2E7D32) else Color.Red)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(pump.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("كود الترخيص الموحد: ${pump.id} | السعة: ${pump.capacityHp} حصان نفوذي", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Text("المركب الجغرافي: ${pump.location} | الصك العقاري التابع: ${pump.deedId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Text("المشغّل القانوني: ${pumpDeedOwner?.getFullName() ?: "وزارة الموارد المائية بالولاية"}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = HeritageGreen)
                    }
                    Icon(Icons.Default.Refresh, contentDescription = "نشط ومسجل", tint = if (pump.isOperational) HeritageGreen else Color.Gray, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

fun exportLandDeedAsPdf(context: android.content.Context, deed: LandDeed, owner: PersonEntity?) {
    try {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // standard A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // Background and border
        canvas.drawColor(AndroidColor.parseColor("#FFFDF9"))
        
        paint.color = AndroidColor.parseColor("#143626") // Deep Green
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRect(20f, 20f, 575f, 822f, paint)
        
        paint.color = AndroidColor.parseColor("#C59B27") // Golden
        paint.strokeWidth = 2f
        canvas.drawRect(26f, 26f, 569f, 816f, paint)
        
        // Header
        paint.color = AndroidColor.parseColor("#143626")
        paint.style = Paint.Style.FILL
        paint.textSize = 18f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("جمهورية السودان - ولاية نهر النيل المتحدة الفيدرالية", 297f, 70f, paint)
        
        paint.textSize = 14f
        paint.color = AndroidColor.parseColor("#C59B27")
        canvas.drawText("مصلحة الأراضي والمقيدات الزراعية المركزية للولاية", 297f, 95f, paint)
        
        paint.strokeWidth = 2f
        canvas.drawLine(100f, 115f, 495f, 115f, paint)
        
        // Document Title
        paint.textSize = 22f
        paint.color = AndroidColor.parseColor("#143626")
        canvas.drawText("وثيقة وصك التملك العقاري والزراعي الشرعي", 297f, 160f, paint)
        
        // Body Text
        paint.textAlign = Paint.Align.RIGHT
        paint.color = AndroidColor.parseColor("#212529")
        paint.textSize = 15f
        
        var y = 230f
        val spacing = 35f
        
        canvas.drawText("رقم الحيازة والأثر العقاري الفيدرالي:  ${deed.id}", 500f, y, paint)
        y += spacing
        canvas.drawText("الاسم الرسمي للمشروع/المزرعة:  ${deed.name}", 500f, y, paint)
        y += spacing
        canvas.drawText("الموقع والمطابقة الجغرافية بالتمكين:  ${deed.location}", 500f, y, paint)
        y += spacing
        canvas.drawText("المساحة الشرعية الكلية:  ${deed.areaFeddans} فدان زراعي معتمد", 500f, y, paint)
        y += spacing
        canvas.drawText("المحاصيل والزروع المصدّقة:  ${deed.cropType}", 500f, y, paint)
        y += spacing
        canvas.drawText("مصدر المياه المرخص:  ${deed.waterSource}", 500f, y, paint)
        y += spacing
        canvas.drawText("العائد السنوي المركزي المقدر:  ${String.format("%,.0f", deed.annualYieldSdg)} جنيه سوداني نفوذي", 500f, y, paint)
        y += spacing
        
        // Owner info
        val ownerName = owner?.getFullName() ?: "وزارة المالية والاستثمار بالولاية"
        val ownerIdNum = owner?.getDisplayNationalNumber() ?: "N/A"
        canvas.drawText("المالك المقيد المقر له بالسيادة:  $ownerName", 500f, y, paint)
        y += spacing
        canvas.drawText("الرقم الوطني للمالك الفيدرالي:  $ownerIdNum", 500f, y, paint)
        y += spacing
        
        // Signature
        paint.textSize = 13f
        paint.color = AndroidColor.parseColor("#143626")
        canvas.drawText("توقيع المحافظ الفيدرالي للأراضي والاستثمار بالولاية", 500f, y + 40f, paint)
        canvas.drawText("رئيس التحول الرقمي بالولاية: حمزة العجلابي", 500f, y + 60f, paint)
        
        // Stamp
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.parseColor("#C59B27")
        paint.strokeWidth = 3f
        canvas.drawCircle(130f, 650f, 45f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ولاية نهر النيل المتحدة", 130f, 640f, paint)
        canvas.drawText("سجل ومصلحة الأراضي", 130f, 652f, paint)
        canvas.drawText("تصميم المبرمجة: حمزة العجلابي", 130f, 664f, paint)
        
        // Footer and laws citation
        paint.textSize = 9f
        paint.color = AndroidColor.parseColor("#7F8C8D")
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("وثيقة رسمية صادرة بموجب ميثاق نهر النيل العقاري لعام 2026 والمطور حمزة العجلابي.", 297f, 755f, paint)
        canvas.drawText("المعرف الرقمي للمقيد الفيدرالي العقاري:  SD-NLE-RE-${deed.id}-2026", 297f, 775f, paint)
        
        pdfDoc.finishPage(page)
        
        val folder = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = File(folder, "LASS_DEED_${deed.id}.pdf")
        val stream = FileOutputStream(file)
        pdfDoc.writeTo(stream)
        stream.close()
        pdfDoc.close()
        
        Toast.makeText(context, "تم إصدار وطباعة صك ملكية الأرض الفيدرالي PDF بنجاح!\nالملف: ${file.name}\nالمسار: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "حدث خطأ أثناء رصد وثيقة صك الملكية: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun StateGeographicMapGuide(
    allPeople: List<PersonEntity>,
    viewModel: FamilyViewModel
) {
    var selectedCityName by remember { mutableStateOf("الدامر") }

    val cities = listOf(
        StateCity("0", "الدامر", "عاصمة الولاية التاريخية وتعرف بحاضرة العلم والقرآن ومقر السجل المدني الفيدرالي الرئيسي في الشمال.", "مبنى السجل المدني الفيدرالي، مسجد خلاوي الدامر العقيدية والمسير التاريخي القديم.", 0.65f, 0.50f, "🏛️"),
        StateCity("1", "عطبرة", "مدينة الحديد والنار وعاصمة السكك الحديدية السودانية وملتقى نهر عطبرة الجاري مع نهر النيل العظيم.", "محطة السكك الحديدية الكبرى، ورش الصيانة الضخمة، كوبري ملتقى النهرين.", 0.65f, 0.40f, "🚂"),
        StateCity("2", "شندي", "أهم المراكز الاقتصادية والثقافية التاريخية في جنوب ولاية نهر النيل، وموطن صناعة النسيج القطني البلدي.", "سوق شندي القديم ومقابر الأسرة الحاكمة، المجمع الفيدرالي الاستكشافي للري.", 0.60f, 0.81f, "🌾"),
        StateCity("3", "المتمة", "الحاضرة التاريخية العريقة على الضفة الغربية لنهر النيل وتعتبر بوابة طريق أم درمان شندي للتجارة.", "عقود العبارات النهرية، الحقول البستانية لآل المحمية العريقة.", 0.32f, 0.81f, "⛴️"),
        StateCity("4", "بربر", "المدينة التاريخية العريقة، منارة خلاوي تعليم القرآن وتفسير الفقه ونقاط قوافل التجارة القديمة.", "خلاوي الغبش الشهيرة، منارات الفقه والتدريس المكتوبة.", 0.42f, 0.30f, "🕌"),
        StateCity("5", "البجراوية", "العاصمة الأثرية لحضارة كوش العريقة وبها أهرامات مروي الملكية الشهيرة التي تمثل الهوية التاريخية العظيمة.", "أهرامات البجراوية الشمالية والجنوبية، المدافن الملكية الكوشية الأثرية.", 0.62f, 0.65f, "📐"),
        StateCity("6", "أبو حمد", "الحاضرة الفيدرالية الهامة في منعرج النهر العظيم شمالاً، وتشتهر بالنخيل والزراعة والتنقيب عن الذهب الصافي.", "منحنى منعرج النهر الكبير، واحات النخيل المروية وحجر العسل شمالاً.", 0.76f, 0.15f, "🌴")
    )

    val selectedCity = cities.find { it.name == selectedCityName } ?: cities[0]

    // Database Relational Join: Find citizens registered or born in this city
    val matchingCitizens = remember(allPeople, selectedCityName) {
        allPeople.filter { citizen ->
            citizen.birthPlace.contains(selectedCityName, ignoreCase = true) ||
                    citizen.villageName.contains(selectedCityName, ignoreCase = true) ||
                    citizen.address.contains(selectedCityName, ignoreCase = true)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BronzeGold, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(HeritageGreen.copy(0.12f))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "دليل الجغرافيا والخرائط",
                        tint = HeritageGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "الدليل الجغرافي وخريطة الولاية التفاعلية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark
                    )
                    Text(
                        "استكشف مدن ولاية نهر النيل وارتباط السجلات المدنية بالمواقع الجغرافية",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Map Graphic Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFFDF8)) // Elegant Soft Sand/Parchment Color
                    .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(16.dp))
            ) {
                // Draw River Nile, River Atbara, and boundaries
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Territory shading
                    drawCircle(
                        color = Color(0xFFFBEFD3).copy(0.35f),
                        radius = h * 0.95f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )

                    // Draw the Blue River Nile path
                    val nilePath = Path().apply {
                        moveTo(w * 0.65f, h) // enters from Khartoum side
                        cubicTo(w * 0.65f, h * 0.85f, w * 0.62f, h * 0.75f, w * 0.62f, h * 0.65f) // through Shendi
                        cubicTo(w * 0.63f, h * 0.55f, w * 0.65f, h * 0.48f, w * 0.65f, h * 0.42f) // through Al Damar and Atbara
                        cubicTo(w * 0.58f, h * 0.38f, w * 0.40f, h * 0.35f, w * 0.42f, h * 0.28f) // Al Shereik loop
                        cubicTo(w * 0.45f, h * 0.20f, w * 0.70f, h * 0.18f, w * 0.76f, h * 0.15f) // Abu Hamad loop
                        lineTo(w * 0.40f, h * 0.05f) // flow to Halfa
                    }

                    // Stroke River
                    drawPath(
                        path = nilePath,
                        color = Color(0xFF4FA8F4).copy(0.55f),
                        style = Stroke(width = 16f)
                    )
                    drawPath(
                        path = nilePath,
                        color = Color(0xFF0F86EC).copy(0.75f),
                        style = Stroke(width = 8f)
                    )

                    // Draw River Atbara merging into the Nile at Atbara coordinates
                    val atbaraRiverPath = Path().apply {
                        moveTo(w, h * 0.42f)
                        cubicTo(w * 0.85f, h * 0.42f, w * 0.72f, h * 0.42f, w * 0.65f, h * 0.42f)
                    }
                    drawPath(
                        path = atbaraRiverPath,
                        color = Color(0xFF76C0F8).copy(0.75f),
                        style = Stroke(width = 8f)
                    )
                }

                // Coordinates overlays
                cities.forEach { city ->
                    val isSelected = selectedCityName == city.name

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val xPos = (maxWidth * city.xPct) - 22.dp
                        val yPos = (maxHeight * city.yPct) - 22.dp

                        // Clickable Node
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(x = xPos, y = yPos)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) HeritageGreen.copy(0.95f) else Color.White.copy(0.85f)
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) BronzeGold else HeritageGreen.copy(0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedCityName = city.name }
                        ) {
                            Text(
                                text = city.avatarRepresentation,
                                fontSize = 18.sp
                            )
                        }

                        // Label
                        val labelYOffset = if (city.yPct > 0.5f) yPos - 18.dp else yPos + 46.dp
                        Box(
                            modifier = Modifier
                                .offset(x = xPos - 10.dp, y = labelYOffset)
                                .background(
                                    if (isSelected) HeritageGreenDark else Color.White.copy(0.85f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = city.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else HeritageGreenDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details card for the selected region
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7FA)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedCity.avatarRepresentation, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حاضرة ${selectedCity.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGreenDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BronzeGold.copy(0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "المسجل الوطني: ${matchingCitizens.size} مواطن",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedCity.desc,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🏛️ المعالم الرئيسية: ${selectedCity.landmarks}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BronzeGold
                    )

                    // Displaying residents of the city
                    if (matchingCitizens.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = HeritageGreen.copy(0.1f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "المواطنون المسجلة وثائقهم في ${selectedCity.name}:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeritageGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            matchingCitizens.forEach { citizen ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .width(180.dp)
                                        .border(
                                            0.5.dp,
                                            HeritageGreen.copy(0.15f),
                                            RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (citizen.profileImage) {
                                                "man_traditional" -> "👳‍♂️"
                                                "woman_traditional" -> "🧕"
                                                "man" -> "👨"
                                                "woman" -> "👩"
                                                "boy" -> "👦"
                                                "girl" -> "👧"
                                                "avatar_doc" -> "🪪"
                                                "military_uniform" -> "🎖️"
                                                else -> "👤"
                                            },
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = citizen.getFullName(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                color = DarkCharcoal
                                            )
                                            Text(
                                                text = "رقم السجل: ${citizen.registryNumber}",
                                                fontSize = 8.sp,
                                                color = Color.Gray,
                                                maxLines = 1
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.selectPersonForDocument(citizen)
                                                viewModel.setTab(ActiveTab.DocumentsTab)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "عرض الملف",
                                                tint = BronzeGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 لا يوجد مواطنون مسجلون في هذه الحاضرة حالياً يدوياً. يمكنك إدخال وتعديل البيانات لربطهم جغرافياً بالخريطة.",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

data class StateCity(
    val id: String,
    val name: String,
    val desc: String,
    val landmarks: String,
    val xPct: Float,
    val yPct: Float,
    val avatarRepresentation: String
)
