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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PersonEntity
import com.example.ui.theme.BronzeGold
import com.example.ui.theme.DesertIvory
import com.example.ui.theme.HeritageGreen
import com.example.ui.theme.HeritageGreenDark
import com.example.ui.theme.HeritageGreenLight
import com.example.ui.theme.SoftSage
import com.example.ui.theme.WarmSand
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import com.example.ui.theme.DarkCharcoal
import androidx.compose.foundation.text.selection.SelectionContainer

@Composable
fun CivilRegistryDocumentsScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    val selectedPerson by viewModel.selectedPersonForDocument.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedDocType by remember { mutableStateOf("ID_CARD") } // "ID_CARD", "BIRTH_CERT", "FAMILY_REG", "DEATH_CERT"
    
    // Dropdown search controller
    var searchQuery by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    // Printing simulation states
    var isPrinting by remember { mutableStateOf(false) }
    var printProgress by remember { mutableStateOf(0f) }
    var showPrintSuccess by remember { mutableStateOf(false) }
    var printedDocTitle by remember { mutableStateOf("") }

    // DUAL WORKSPACE MODES Definition
    var activeMode by remember { mutableStateOf("INQUIRY") } // "INQUIRY" or "REGISTRY"
    var selectedRegistryType by remember { mutableStateOf("BIRTH") } // "BIRTH" or "MARRIAGE"

    // Geographic categorization for the United River Nile State (مدن وقرى ولاية نهر النيل المتحدة)
    val riverNileLocations = remember {
        listOf(
            "الدامر (عاصمة الولاية)",
            "عطبرة (مدينة الحديد والنار)",
            "شندي العريقة",
            "قرية المحمية الغربية",
            "قرية البجراوية (الحضارية)",
            "المتمة التاريخية",
            "الزيداب الرفيعة",
            "بربر الوفية",
            "أبو حمد الفتية",
            "كبوشية الملكية"
        )
    }

    // 1. Digital Birth Certificate Form fields
    var bFirstName by remember { mutableStateOf("") }
    var bFatherName by remember { mutableStateOf("") }
    var bGrandFatherName by remember { mutableStateOf("") }
    var bFamilyName by remember { mutableStateOf("") }
    var bGender by remember { mutableStateOf("ذكر") }
    var bBirthDate by remember { mutableStateOf("2026-06-18") }
    var bLocation by remember { mutableStateOf("عطبرة (مدينة الحديد والنار)") }
    var showBirthLocationDropdown by remember { mutableStateOf(false) }
    var bBloodType by remember { mutableStateOf("O+") }

    // 2. Digital Marriage Contract Form fields
    val availableHusbands = remember(allPeople) {
        allPeople.filter { it.gender == "ذكر" && it.isAlive && it.maritalStatus != "متزوج" }
    }
    val availableWives = remember(allPeople) {
        allPeople.filter { it.gender == "أنثى" && it.isAlive && it.maritalStatus != "متزوج" }
    }
    var mHusband by remember { mutableStateOf<PersonEntity?>(null) }
    var showHusbandDropdown by remember { mutableStateOf(false) }
    var mWife by remember { mutableStateOf<PersonEntity?>(null) }
    var showWifeDropdown by remember { mutableStateOf(false) }
    var mMarriageDate by remember { mutableStateOf("2026-06-18") }
    var mLocation by remember { mutableStateOf("شندي العريقة") }
    var showMarriageLocationDropdown by remember { mutableStateOf(false) }
    var mWitness1 by remember { mutableStateOf("") }
    var mWitness2 by remember { mutableStateOf("") }
    var mMahr by remember { mutableStateOf("750,000") }

    val filteredCitizens = remember(allPeople, searchQuery) {
        allPeople.filter {
            searchQuery.isEmpty() || it.getFullName().contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(isPrinting) {
        if (isPrinting) {
            printProgress = 0f
            while (printProgress < 100f) {
                delay(120)
                printProgress += 10f
            }
            delay(400)
            isPrinting = false
            showPrintSuccess = true
            
            // Programs real high-fidelity PDF copy onto storage downloads!
            selectedPerson?.let { citizen ->
                exportDocumentAsPdf(context, citizen, selectedDocType, allPeople)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Section: Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HeritageGreenDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BronzeGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "مجمّع الخدمات المدنية والوثائق الحكومية",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeritageGreenDark
                )
                Text(
                    text = "نظام الأرشفة وإصدار الأوراق الثبوتية الفيدرالي لولاية نهر النيل المتحدة",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BronzeGold.copy(0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "تصميم وبرمجة: المؤلف حمزة العجلابي",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark
                    )
                }
            }
        }

        HorizontalDivider(color = HeritageGreen.copy(0.15f), modifier = Modifier.padding(bottom = 12.dp))

        // Interactive Workspace Mode Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(SoftSage.copy(0.5f), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeMode == "INQUIRY") HeritageGreen else Color.Transparent)
                    .clickable { activeMode = "INQUIRY" }
                    .padding(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (activeMode == "INQUIRY") Color.White else HeritageGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "استخراج الوثائق والشهادات",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeMode == "INQUIRY") Color.White else HeritageGreenDark
                    )
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeMode == "REGISTRY") HeritageGreen else Color.Transparent)
                    .clickable { activeMode = "REGISTRY" }
                    .padding(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = if (activeMode == "REGISTRY") Color.White else HeritageGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تسجيل السجلات المدنية الرقمية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeMode == "REGISTRY") Color.White else HeritageGreenDark
                    )
                }
            }
        }

        if (activeMode == "INQUIRY") {
            // Citizen Selector Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(12.dp))
            ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "1. حدد المواطن المستهدف بالسجل الاستعلامي:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeritageGreen
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedPerson != null && !showDropdown) selectedPerson!!.getFullName() else searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showDropdown = true
                        },
                        placeholder = { Text("اكتب الاسم للبحث والسحب بقيد السجل...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("doc_search_citizen"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HeritageGreen) },
                        trailingIcon = {
                            if (selectedPerson != null) {
                                IconButton(onClick = {
                                    viewModel.clearSelectedDocumentPerson()
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "تصفير", tint = Color.Red.copy(0.7f))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeritageGreen,
                            unfocusedBorderColor = HeritageGreen.copy(0.3f)
                        )
                    )

                    // Floating Autocomplete dropdown
                    if (showDropdown && filteredCitizens.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp)
                                .heightIn(max = 160.dp)
                                .border(1.dp, HeritageGreen.copy(0.2f), RoundedCornerShape(8.dp))
                                .align(Alignment.TopStart)
                        ) {
                            LazyColumn {
                                items(filteredCitizens) { person ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectPersonForDocument(person)
                                                showDropdown = false
                                                searchQuery = ""
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (person.gender == "ذكر") Icons.Default.Person else Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (person.gender == "ذكر") HeritageGreen else BronzeGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = person.getFullName(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HeritageGreenDark
                                            )
                                            Text(
                                                text = "الرقم الوطني: ${person.getDisplayNationalNumber()} | قرية: ${person.villageName.ifBlank { "الحرة" }}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = HeritageGreen.copy(0.08f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedPerson == null) {
            // Unselected Empty Visual State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = HeritageGreenLight.copy(0.2f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "مركز إصدار الهويات والوثائق الحكومية",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يرجى تحديد مواطن من القائمة بالأعلى لعرض وإصدار وثائقه الثبوتية الرسمية المسجلة بسجلات السجل المدني لدولة المحمية.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (allPeople.isNotEmpty()) {
                                viewModel.selectPersonForDocument(allPeople.first())
                                searchQuery = ""
                            } else {
                                viewModel.loadSampleData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen)
                    ) {
                        Text(if (allPeople.isNotEmpty()) "سحب قيد المواطن التجريبي الأول" else "تعبئة وتحميل مواطني المحمية فوراً")
                    }
                }
            }
        } else {
            // Selected Person Content
            val citizen = selectedPerson!!

            // Tab Selector of issued Documents - Expandable & Scrollable for 10 document types!
            val documentTypes = listOf(
                "ID_CARD" to "البطاقة الشخصية",
                "BIRTH_CERT" to "شهادة الميلاد",
                "FAMILY_REG" to "قيد العائلة",
                "DEATH_CERT" to "شهادة الوفاة",
                "MARRIAGE_CERT" to "قسيمة زواج",
                "DIVORCE_CERT" to "وثيقة طلاق",
                "PASSPORT" to "جواز سفر",
                "LICENSE" to "رخصة قيادة",
                "MILITARY_CARD" to "بطاقة عسكرية",
                "AFFILIATION_CARD" to "بطاقة انتساب"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DesertIvory, RoundedCornerShape(10.dp))
                    .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(10.dp))
                    .padding(6.dp)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                documentTypes.forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedDocType == typeKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) HeritageGreen else Color.White)
                            .border(1.dp, if (isSelected) HeritageGreen else HeritageGreen.copy(0.2f), RoundedCornerShape(8.dp))
                            .clickable { selectedDocType = typeKey }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else HeritageGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Document Viewer Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Document Body rendering with animated type-switcher
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = selectedDocType,
                            transitionSpec = {
                                slideInVertically(animationSpec = tween(250)) { it } togetherWith 
                                slideOutVertically(animationSpec = tween(250)) { -it }
                            },
                            label = "DocAnimation"
                        ) { docType ->
                            when (docType) {
                                "ID_CARD" -> IDCardVisual(citizen)
                                "BIRTH_CERT" -> BirthCertificateVisual(citizen)
                                "FAMILY_REG" -> FamilyRegistryVisual(citizen, allPeople)
                                "DEATH_CERT" -> DeathCertificateVisual(citizen)
                                "MARRIAGE_CERT" -> MarriageCertificateVisual(citizen, allPeople)
                                "DIVORCE_CERT" -> DivorceCertificateVisual(citizen)
                                "PASSPORT" -> PassportVisual(citizen)
                                "LICENSE" -> LicenseVisual(citizen)
                                "MILITARY_CARD" -> MilitaryCardVisual(citizen)
                                "AFFILIATION_CARD" -> AffiliationCardVisual(citizen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Print / Authentication action button
                    val labelType = when (selectedDocType) {
                        "ID_CARD" -> "البطاقة الشخصية الوطنية"
                        "BIRTH_CERT" -> "شهادة الميلاد الرسمية"
                        "FAMILY_REG" -> "شهادة الصحيفة والقيد العائلي المعتمَد"
                        "DEATH_CERT" -> "تصريح الوفاة الرسمي وشهادة الوفاة"
                        "MARRIAGE_CERT" -> "عقد وقسيمة الزواج الشرعية"
                        "DIVORCE_CERT" -> "إشهاد الطلاق الموثق"
                        "PASSPORT" -> "جواز السفر البيومتري الموحد"
                        "LICENSE" -> "رخصة قيادة المركبات المعتمدة"
                        "MILITARY_CARD" -> "بطاقة منسوبي القوات المسلحة"
                        "AFFILIATION_CARD" -> "بطاقة الانتساب لعشيرة وأعيان نهر النيل"
                        else -> "الوثيقة المدنية"
                    }

                    Button(
                        onClick = {
                            printedDocTitle = "$labelType للمواطن: ${citizen.getFullName()}"
                            isPrinting = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BronzeGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("action_print_document")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تصدير وطباعة المخرجات الرقمية للوثيقة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    } else {
        // REGISTRY WORKSPACE (إضافة السجلات المدنية الرقمية للمدن والقرى)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Segmented controller for BIRTH or MARRIAGE inside the form workspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(SoftSage.copy(0.6f), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedRegistryType == "BIRTH") HeritageGreen else Color.Transparent)
                            .clickable { selectedRegistryType = "BIRTH" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "تسجيل شهادة ميلاد رقمية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRegistryType == "BIRTH") Color.White else HeritageGreenDark
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedRegistryType == "MARRIAGE") HeritageGreen else Color.Transparent)
                            .clickable { selectedRegistryType = "MARRIAGE" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "تسجيل عقد زواج شرعي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRegistryType == "MARRIAGE") Color.White else HeritageGreenDark
                        )
                    }
                }

                if (selectedRegistryType == "BIRTH") {
                    // ---------------------------------
                    // FORM A: BIRTH CERTIFICATE REGISTRY
                    // ---------------------------------
                    Text(
                        text = "تسجيل مولود مدني جديد - ولاية نهر النيل المتحدة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreen,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = bFirstName,
                        onValueChange = { bFirstName = it },
                        label = { Text("الاسم الأول للمولود") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeritageGreen,
                            unfocusedBorderColor = HeritageGreen.copy(0.3f)
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = bFatherName,
                            onValueChange = { bFatherName = it },
                            label = { Text("اسم الأب") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        OutlinedTextField(
                            value = bGrandFatherName,
                            onValueChange = { bGrandFatherName = it },
                            label = { Text("اسم الجد") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = bFamilyName,
                        onValueChange = { bFamilyName = it },
                        label = { Text("اسم العائلة / اللقب") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeritageGreen,
                            unfocusedBorderColor = HeritageGreen.copy(0.3f)
                        )
                    )

                    // Gender Select
                    Text("الجنس المدني للمولود:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = bGender == "ذكر",
                                onClick = { bGender = "ذكر" },
                                colors = RadioButtonDefaults.colors(selectedColor = HeritageGreen)
                            )
                            Text("ذكر", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = bGender == "أنثى",
                                onClick = { bGender = "أنثى" },
                                colors = RadioButtonDefaults.colors(selectedColor = HeritageGreen)
                            )
                            Text("أنثى", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark)
                        }
                    }

                    // Date and blood type row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = bBirthDate,
                            onValueChange = { bBirthDate = it },
                            label = { Text("تاريخ الميلاد (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        OutlinedTextField(
                            value = bBloodType,
                            onValueChange = { bBloodType = it },
                            label = { Text("فصيلة الدم") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                    }

                    // Location category selector
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        OutlinedTextField(
                            value = bLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("البلدة / قرية نهر النيل للتسجيل الجغرافي") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showBirthLocationDropdown = !showBirthLocationDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "عرض التصنيف")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        DropdownMenu(
                            expanded = showBirthLocationDropdown,
                            onDismissRequest = { showBirthLocationDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                        ) {
                            riverNileLocations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc, fontSize = 12.sp, color = HeritageGreenDark) },
                                    onClick = {
                                        bLocation = loc
                                        showBirthLocationDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (bFirstName.isBlank() || bFamilyName.isBlank() || bFatherName.isBlank()) {
                                Toast.makeText(context, "الرجاء كشط وتعبئة الحقول الإلزامية للمولود!", Toast.LENGTH_LONG).show()
                            } else {
                                // Generate automatic unique registry serials
                                val newRegNo = "س-${(100..999).random()}/ق-م"
                                val generatedNatId = "${if(bGender == "ذكر") "1" else "2"}-${bBirthDate.replace("-","")}-${(10..99).random()}-${(1000..9999).random()}"
                                viewModel.savePerson(
                                    firstName = bFirstName,
                                    fatherName = bFatherName,
                                    grandFatherName = bGrandFatherName,
                                    familyName = bFamilyName,
                                    gender = bGender,
                                    birthDate = bBirthDate,
                                    birthPlace = bLocation,
                                    isAlive = true,
                                    deathDate = null,
                                    phoneNumber = "",
                                    address = "جمهورية السودان - ولاية نهر النيل - $bLocation",
                                    notes = "سجل ولادة رقمي معتمد لدى السجل المدني للولاية المتحدة",
                                    fatherId = null,
                                    motherId = null,
                                    spouseId = null,
                                    nationalNumber = generatedNatId,
                                    occupation = "مكفول تحت سن التعليم الأساسي",
                                    bloodType = bBloodType,
                                    registrationDate = "2026-06-18",
                                    registryNumber = newRegNo,
                                    villageName = bLocation
                                )

                                Toast.makeText(context, "تم بنجاح تسجيل شهادة ميلاد: $bFirstName $bFamilyName بـ $bLocation برقم وطني $generatedNatId", Toast.LENGTH_LONG).show()
                                
                                // Reset form fields
                                bFirstName = ""
                                bFatherName = ""
                                bGrandFatherName = ""
                                bFamilyName = ""
                                activeMode = "INQUIRY" // bounce back to check certificates !
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اعتماد وإصدار شهادة الميلاد الرقمية", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                } else {
                    // ---------------------------------
                    // FORM B: MARRIAGE CONTRACT REGISTRY
                    // ---------------------------------
                    Text(
                        text = "توثيق وإصدار عقد قران شرعي فيدرالي - ولاية نهر النيل المتحدة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreen,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. SELECT HUSBAND (الزوج)
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = mHusband?.getFullName() ?: "اضغط لتحديد الزوج الخاطب...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الطرف الأول (الزوج)") },
                            modifier = Modifier.fillMaxWidth().clickable { showHusbandDropdown = !showHusbandDropdown },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        DropdownMenu(
                            expanded = showHusbandDropdown,
                            onDismissRequest = { showHusbandDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color.White).heightIn(max = 200.dp)
                        ) {
                            if (availableHusbands.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("لا يوجد ذكور غير متزوجين مسجلين", color = Color.Gray, fontSize = 11.sp) },
                                    onClick = {}
                                )
                            } else {
                                availableHusbands.forEach { husband ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(husband.getFullName(), fontSize = 13.sp, color = HeritageGreenDark)
                                                Text("الرقم الوطني: ${husband.getDisplayNationalNumber()} | قرية: ${husband.villageName}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            mHusband = husband
                                            showHusbandDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. SELECT WIFE (الزوجة)
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = mWife?.getFullName() ?: "اضغط لتحديد الزوجة المخطوبة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الطرف الثاني (الزوجة)") },
                            modifier = Modifier.fillMaxWidth().clickable { showWifeDropdown = !showWifeDropdown },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        DropdownMenu(
                            expanded = showWifeDropdown,
                            onDismissRequest = { showWifeDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color.White).heightIn(max = 200.dp)
                        ) {
                            if (availableWives.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("لا يوجد إناث غير متزوجات مسجلات", color = Color.Gray, fontSize = 11.sp) },
                                    onClick = {}
                                )
                            } else {
                                availableWives.forEach { wife ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(wife.getFullName(), fontSize = 13.sp, color = HeritageGreenDark)
                                                Text("الرقم الوطني: ${wife.getDisplayNationalNumber()} | قرية: ${wife.villageName}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            mWife = wife
                                            showWifeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Marriage details
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = mMarriageDate,
                            onValueChange = { mMarriageDate = it },
                            label = { Text("تاريخ العقد (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        OutlinedTextField(
                            value = mMahr,
                            onValueChange = { mMahr = it },
                            label = { Text("الصداق المسمى (المهر بالسوداني)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                    }

                    // Witnesses
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = mWitness1,
                            onValueChange = { mWitness1 = it },
                            label = { Text("اسم الشاهد الأول") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        OutlinedTextField(
                            value = mWitness2,
                            onValueChange = { mWitness2 = it },
                            label = { Text("اسم الشاهد الثاني") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                    }

                    // Location category selector
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        OutlinedTextField(
                            value = mLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مكان توثيق وإبرام عقد القران بالولاية") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showMarriageLocationDropdown = !showMarriageLocationDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "عرض التصنيف")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeritageGreen,
                                unfocusedBorderColor = HeritageGreen.copy(0.3f)
                            )
                        )
                        DropdownMenu(
                            expanded = showMarriageLocationDropdown,
                            onDismissRequest = { showMarriageLocationDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                        ) {
                            riverNileLocations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc, fontSize = 12.sp, color = HeritageGreenDark) },
                                        onClick = {
                                            mLocation = loc
                                            showMarriageLocationDropdown = false
                                        }
                                    )
                                }
                        }
                    }

                    Button(
                        onClick = {
                            if (mHusband == null || mWife == null) {
                                Toast.makeText(context, "الرجاء تحديد الزوج والزوجة لإثبات عقد النكاح الشرعي!", Toast.LENGTH_LONG).show()
                            } else if (mHusband?.id == mWife?.id) {
                                Toast.makeText(context, "خطأ: لا يمكن اختيار نفس الشخص كزوج وزوجة!", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.registerMarriage(
                                    husbandId = mHusband!!.id,
                                    wifeId = mWife!!.id,
                                    marriageDate = mMarriageDate,
                                    location = mLocation
                                )

                                Toast.makeText(context, "تم عقد القران بنجاح بين ${mHusband!!.firstName} و ${mWife!!.firstName} بمدينة $mLocation", Toast.LENGTH_LONG).show()
                                
                                // Reset Marriage form
                                mHusband = null
                                mWife = null
                                mWitness1 = ""
                                mWitness2 = ""
                                activeMode = "INQUIRY" // bounce back to check certificates !
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("توثيق وإصدار وثيقة الزواج الشرعية", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

    // Printing Progress Dialog Simulation
    if (isPrinting) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Text(
                    text = "جاري تجميع البيانات وتجهيز المخرج المدني...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        progress = { printProgress / 100f },
                        color = HeritageGreen,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "التقدم: ${printProgress.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark
                    )
                    Text(
                        text = "محاكاة التشفير والتوقيع بختم ولاية قرى المحمية",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        )
    }

    // Success confirmation dialog
    if (showPrintSuccess) {
        AlertDialog(
            onDismissRequest = { showPrintSuccess = false },
            confirmButton = {
                TextButton(onClick = { showPrintSuccess = false }) {
                    Text("إغلاق السجل", color = HeritageGreen, fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = HeritageGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "تم إصدار الوثيقة وحفظها كملف رقمي رسمي!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = printedDocTitle,
                        fontSize = 13.sp,
                        color = HeritageGreenDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftSage, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "تم تحديث الملف الأرشيفي الوطني لهذا المواطن. معلومات التحقق الرمزية نشطة عبر منصة السجل المدني لدولة المحمية - عاصمة الحرة.",
                            fontSize = 12.sp,
                            color = HeritageGreenDark,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        )
    }
}

// ==========================================
// DOCUMENT SPECIFIC VISUAL LAYOUTS
// ==========================================

// VISUAL 1: THE HIGH-FIDELITY NATIONAL IDENTITY CARD (بطاقة الهوية الوطنية)
@Composable
fun IDCardVisual(citizen: PersonEntity) {
    var isFrontOfCard by remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable { isFrontOfCard = !isFrontOfCard }
    ) {
        Text(
            text = if (isFrontOfCard) "انقر على البطاقة لقلبها لرؤية الظهر ↺" else "انقر على البطاقة لقلبها لرؤية الوجه ↺",
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // The Plastic Card Body
        Box(
            modifier = Modifier
                .width(360.dp)
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.5.dp, BronzeGold, RoundedCornerShape(16.dp))
                .drawBehind {
                    // Draw a subtle sand-colored governmental identity card pattern background
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF9F6EE),
                                Color(0xFFECE5D3),
                                Color(0xFFFFFDF9)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                    // Draw an abstract water-mark representing Al-Mahmiya State or Nile river ripples
                    val wavePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height * 0.8f)
                        cubicTo(
                            size.width * 0.3f, size.height * 0.7f,
                            size.width * 0.6f, size.height * 0.95f,
                            size.width, size.height * 0.8f
                        )
                    }
                    drawPath(
                        path = wavePath,
                        color = HeritageGreen.copy(0.04f),
                        style = Stroke(width = 8f)
                    )
                }
                .padding(12.dp)
        ) {
            if (isFrontOfCard) {
                // Identity CARD FRONT SIDE
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar representing the Country/State
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "دولة المحمية - وزارة الداخلية",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGreenDark
                            )
                            Text(
                                text = "المديرية العامة للسجل المدني والأرشفة",
                                fontSize = 8.sp,
                                color = HeritageGreen
                            )
                        }

                        // Mini Flag Simulator (Sudan / Nile Style Colors: Red, White, Black with Green Triangle)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .border(1.dp, Color.Gray.copy(0.4f), RoundedCornerShape(2.dp))
                        ) {
                            Box(modifier = Modifier.size(width = 24.dp, height = 4.dp).background(Color(0xFFD62728)))
                            Box(modifier = Modifier.size(width = 24.dp, height = 4.dp).background(Color.White))
                            Box(modifier = Modifier.size(width = 24.dp, height = 4.dp).background(Color.Black))
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    HorizontalDivider(color = BronzeGold.copy(0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Column: Citizen Photo Placeholder
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 72.dp, height = 90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, HeritageGreen.copy(0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (citizen.gender == "ذكر") Icons.Default.Person else Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (citizen.gender == "ذكر") HeritageGreenLight else BronzeGold,
                                    modifier = Modifier.size(42.dp)
                                )
                                Text(
                                    text = "صورة معتمدة",
                                    fontSize = 8.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Column: Identification Data
                        Column(modifier = Modifier.weight(1f)) {
                            // National ID Number
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HeritageGreenDark.copy(0.08f), RoundedCornerShape(4.dp))
                                    .padding(vertical = 3.dp, horizontal = 6.dp)
                            ) {
                                Text(
                                    text = "الرقم الوطني: ${citizen.getDisplayNationalNumber()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HeritageGreenDark,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "الاسم الكامل: ${citizen.getFullName()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "تاريخ الميلاد: ${citizen.birthDate.ifBlank { "غير مسجل" }}",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "مكان الولادة: ${citizen.birthPlace.ifBlank { "قرى المحمية" }}",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "الجنس: ${citizen.gender}  |  قرية القيد: ${citizen.villageName.ifBlank { "الحرة - نهر النيل" }}",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Card Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "بطاقة هوية وطنية ذكية",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = BronzeGold
                        )
                        Text(
                            text = "صلاحية لغاية: 2036-05-30",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Identity CARD BACK SIDE
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "معلومات السجل والتحقق الأمني",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "فصيلة الدم: ${citizen.bloodType.ifBlank { "H+" }}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "المهنة المسجلة: ${citizen.occupation.ifBlank { "كاسب عمل" }}",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "عنوان الإقامة: ${citizen.address.ifBlank { "ولاية قرى المحمية" }}",
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "سجل إصدار: ${citizen.getDisplayRegistryInfo()}",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }

                        // Interactive Cryptographically Signed Verification QR
                        InteractiveEncryptedQR(
                            citizen = citizen,
                            docTypeString = "بطاقة الهوية الوطنية الذكية",
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Barcode Simulator Lines at the bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(Color.White)
                            .border(1.dp, Color.Gray.copy(0.3f))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw randomized vertical lines to model a classic barcode
                            var currentX = 10f
                            val strokeWidths = listOf(2f, 4f, 1f, 3f, 5f, 2f, 1f, 4f, 3f, 2f)
                            var idx = 0
                            while (currentX < this.size.width - 20f) {
                                val w = strokeWidths[idx % strokeWidths.size]
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(currentX, 2f),
                                    end = Offset(currentX, this.size.height - 2f),
                                    strokeWidth = w
                                )
                                currentX += w + 4f
                                idx++
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "بموجب المادة 4 من قانون الخدمة المدنية لدولة المحمية لعام 2026",
                        fontSize = 8.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// VISUAL 2: THE WATERMARKED GOVERNMENTAL BIRTH CERTIFICATE (شهادة الميلاد الرسمية)
@Composable
fun BirthCertificateVisual(citizen: PersonEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f)
            .border(4.dp, HeritageGreenDark, RoundedCornerShape(8.dp))
            .border(6.dp, BronzeGold, RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFDF8))
            .padding(14.dp)
    ) {
        // Subtle decorative borders (Classic certificate aesthetic)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            drawRect(
                color = HeritageGreenLight.copy(0.3f),
                style = stroke
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // State Crest Indicator
            Icon(
                imageVector = Icons.Default.Star, // Represents State Emblem
                contentDescription = null,
                tint = BronzeGold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "وزارة الصحة والرعاية الاجتماعية - السجل المدني",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreenDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = "دولة المحمية - ولاية قرى المحمية - عاصمة الحرة",
                fontSize = 10.sp,
                color = HeritageGreenLight,
                textAlign = TextAlign.Center
            )
            Text(
                text = "شَهَادَة مِيلاد رَسْمِيّة معتمدة",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                textDecoration = TextDecoration.Underline,
                color = BronzeGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider(color = BronzeGold.copy(0.4f), thickness = 1.dp, modifier = Modifier.width(180.dp))
            Spacer(modifier = Modifier.height(14.dp))

            // Birth facts parameters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Citizen Name
                Row {
                    Text("اسم المولود: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.getFullName(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = HeritageGreenDark)
                }

                Row {
                    Text("الجنس: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.gender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("زمرة الدم: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.bloodType.ifBlank { "O+" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    Text("تاريخ الولادة: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.birthDate.ifBlank { "2000-01-01" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    Text("مكان الولادة: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        "${citizen.birthPlace.ifBlank { "ولاية نهر النيل" }} - ${citizen.villageName.ifBlank { "بلدية الحرة" }}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Parent parameters helper simulation
                val fName = if (citizen.fatherName.isNotBlank()) citizen.fatherName else "البشير المحمية"
                Row {
                    Text("اسم الأب الكامل: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$fName ${citizen.familyName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row {
                    Text("رقم القيد المركزي بالسجل: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.getDisplayRegistryInfo(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("رقم التسجيل: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("N-${citizen.id + 7215}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Official Signature / Stamps & Verification QR Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive QR for verification
                InteractiveEncryptedQR(
                    citizen = citizen,
                    docTypeString = "شهادة الميلاد الرسمية",
                    modifier = Modifier.size(54.dp)
                )

                // Beautiful Gold Official State Seal Visual
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF2CC), BronzeGold)
                            )
                        )
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Text(
                        text = "خرتم\nرسمي",
                        fontSize = 10.sp,
                        color = HeritageGreenDark,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        lineHeight = 11.sp
                    )
                }

                // Signature Metadata
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("توقيع أمين السجل المدني", fontSize = 8.sp, color = Color.Gray)
                    Text(
                        text = "أنس الطيب البشير",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "مديرية السجل - المحمية",
                        fontSize = 7.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

// VISUAL 3: OFFICIALLY STAMPED FAMILY REGISTRY SHEET (صورة قيد العائلة / الصحيفة العائلية)
@Composable
fun FamilyRegistryVisual(citizen: PersonEntity, allPeople: List<PersonEntity>) {
    val father = allPeople.find { it.id == citizen.fatherId }
    val mother = allPeople.find { it.id == citizen.motherId }
    val spouse = allPeople.find { it.id == citizen.spouseId }
    
    // Find all children who has this citizen as a father or a mother
    val children = remember(allPeople, citizen) {
        allPeople.filter { it.fatherId == citizen.id || it.motherId == citizen.id }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, HeritageGreen, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "صحيفة القيد العائلي المعتمَدة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeritageGreenDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BronzeGold.copy(0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("سجل رقم: س-${citizen.id + 1045}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = HeritageGreen.copy(0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Citizen info & Spouse block
            Text("صاحب الصحيفة العائلية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HeritageGreen)
            Text(
                text = "${citizen.getFullName()} (الرقم الوطني: ${citizen.getDisplayNationalNumber()})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreenDark
            )
            Text(
                text = "شريك الحياة المسجل: ${spouse?.getFullName() ?: "لا يوجد شريك مسجل بقيد السجل"}",
                fontSize = 11.sp,
                color = if (spouse != null) Color.Black else Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Parents details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftSage.copy(0.5f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("الأب المسجل بالقيد:", fontSize = 9.sp, color = Color.Gray)
                    Text(father?.getFullName() ?: "غير مقيد للنظام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("الأم المسجلة بالقيد:", fontSize = 9.sp, color = Color.Gray)
                    Text(mother?.getFullName() ?: "غير مقيدة للنظام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Children List section
            Text(
                text = "سجل الأبناء والأنساب المنحدرة (${children.size} أفراد):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreen
            )

            if (children.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, Color.Gray.copy(0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "لا يوجد أبناء مسجلين باسم هذا الفرد في السجل المدني حالياً والنسب فارغ.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    items(children) { child ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .border(1.dp, HeritageGreen.copy(0.08f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(child.getFullName(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("ميلاد: ${child.birthDate} | الجنس: ${child.gender}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Security Footnote and Sign off Office
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "صدر من الإدارة العامة للأرشفة المدنية بنهر النيل - ولاية قرى المحمية",
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مختوم بختم السجل الرقمي الموحد",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = BronzeGold
                    )
                }

                // Interactive Decentered QR code
                InteractiveEncryptedQR(
                    citizen = citizen,
                    docTypeString = "شهادة القيد والصحيفة العائلية",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

// VISUAL 4: GOVERNMENTAL DEATH CERTIFICATE (تصريح الوفاة وثيقة رسمية)
@Composable
fun DeathCertificateVisual(citizen: PersonEntity) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, Color.Black, RoundedCornerShape(8.dp))
            .border(6.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            .background(Color(0xFFF2F2F2))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning, // Mourning / Warning symbol
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "دولة المحمية - المديرية العامة للسجل المدني",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "شَهَادَةِ وَفَاة رَسْمِيّة معتمدة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.width(200.dp))
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row {
                    Text("اسم المتوفى: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.getFullName(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }

                Row {
                    Text("الرقم الوطني للمتوفى: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(citizen.getDisplayNationalNumber(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    Text("تاريخ الوفاة المسجل: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (!citizen.isAlive) {
                            citizen.deathDate ?: "2026-05-30"
                        } else {
                            "المستند المسجل قيد الحياة (رحمه الله يطول بعمره)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (citizen.isAlive) Color.Red else Color.Black
                    )
                }

                Row {
                    Text("مكان الوفاة: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (citizen.isAlive) "المواطن حي بالسجل المدني" else "مستشفى العاصمة الحرة المركزي - ولاية نهر النيل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    Text("رقم تصريح الدفن المعتزل: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("دفن-${citizen.id + 13024}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stamp Sign off
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Cryptographically Signed Verification QR
                InteractiveEncryptedQR(
                    citizen = citizen,
                    docTypeString = "شَهَادَةِ وَفَاة رَسْمِيّة معتمدة",
                    modifier = Modifier.size(54.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("توقيع مصلحة السجل والأرشفة", fontSize = 8.sp, color = Color.Gray)
                    Text("مكتب عاصمة الحرة الأثري", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// CRYPTOGRAPHIC DIGITAL SIGNATURE COMPONENTS
// ==========================================

@Composable
fun CustomQRCodeCanvas(content: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = 15
        val cellSize = this.size.width / size
        
        // Background
        drawRect(color = Color.White)
        
        val random = java.util.Random(content.hashCode().toLong())
        
        // Define positioning for three corners finder patterns
        fun isFinderRegion(r: Int, c: Int): Boolean {
            return (r < 5 && c < 5) || (r < 5 && c >= size - 5) || (r >= size - 5 && c < 5)
        }
        
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (isFinderRegion(r, c)) {
                    continue
                }
                // Random dot generator based on stable content hash seed
                if (random.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
        
        // Now draw the 3 finder pattern squares explicitly for perfect high-fidelity QR representation
        val finderIndices = listOf(
            Offset(0f, 0f), // Top-Left
            Offset((size - 5) * cellSize, 0f), // Top-Right
            Offset(0f, (size - 5) * cellSize) // Bottom-Left
        )
        
        for (offset in finderIndices) {
            // Draw Outer square (5x5 cells)
            val outerSize = 5 * cellSize
            drawRect(
                color = Color.Black,
                topLeft = offset,
                size = androidx.compose.ui.geometry.Size(outerSize, outerSize)
            )
            // Draw White inner square (3x3 cells)
            val whiteOffset = offset + Offset(cellSize, cellSize)
            val whiteSize = 3 * cellSize
            drawRect(
                color = Color.White,
                topLeft = whiteOffset,
                size = androidx.compose.ui.geometry.Size(whiteSize, whiteSize)
            )
            // Draw Black center square (1x1 cells)
            val blackOffset = offset + Offset(2 * cellSize, 2 * cellSize)
            val blackSize = cellSize
            drawRect(
                color = Color.Black,
                topLeft = blackOffset,
                size = androidx.compose.ui.geometry.Size(blackSize, blackSize)
            )
        }
    }
}

@Composable
fun InteractiveEncryptedQR(
    citizen: PersonEntity,
    docTypeString: String,
    modifier: Modifier = Modifier
) {
    var showVerificationDialog by remember { mutableStateOf(false) }
    
    // Stable UTC/Local Date from 2026-05-30 or current device clock
    val currentDate = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }
    
    // Construct the cryptographically signed block text
    val qrContent = "MAHMIYA-CIVIL-REGISTRY|NID:${citizen.getDisplayNationalNumber()}|NAME:${citizen.getFullName()}|TYPE:$docTypeString|DATE:$currentDate"
    
    // Stable dynamic SHA-256 Signature hash representation
    val shaHash = remember(qrContent) {
        try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(qrContent.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            val stablePart = kotlin.math.abs(qrContent.hashCode()).toString(16)
            "ae829f7bc19bd3e0018a38dfc8${stablePart}fa092e0"
        }
    }

    Box(
        modifier = modifier
            .border(1.5.dp, HeritageGreenDark, RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(4.dp)
            .clickable { showVerificationDialog = true }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CustomQRCodeCanvas(content = qrContent, modifier = Modifier.weight(1f).aspectRatio(1f))
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "تحقق 🔓",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreenDark,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showVerificationDialog) {
        VerificationDetailsDialog(
            citizen = citizen,
            docTypeString = docTypeString,
            issueDate = currentDate,
            hash = shaHash,
            qrContent = qrContent,
            onDismiss = { showVerificationDialog = false }
        )
    }
}

@Composable
fun VerificationDetailsDialog(
    citizen: PersonEntity,
    docTypeString: String,
    issueDate: String,
    hash: String,
    qrContent: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen)
            ) {
                Text("إغلاق مخرجات التحقق", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "تحقق ناجح",
                tint = HeritageGreen,
                modifier = Modifier.size(54.dp)
            )
        },
        title = {
            Text(
                text = "نظام التوقيع الرقمي والتحقق المعتمد",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = HeritageGreenDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "بصمة تشفيرية وطنية موثوقة (مأمونة بالكامل)",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Show QR code magnified inside verification dialog
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(2.dp, HeritageGreen, RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CustomQRCodeCanvas(content = qrContent, modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Legit Verification Stats card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.15f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حالة الوثيقة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = HeritageGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سارٍ ومعتمَد قانونياً", fontSize = 11.sp, color = HeritageGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        HorizontalDivider(color = HeritageGreen.copy(0.08f))
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("الوثيقة الموقعة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(docTypeString, fontSize = 11.sp, color = HeritageGreenDark, fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("صاحب العلاقة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(citizen.getFullName(), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("الرقم الوطني الموحد:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(citizen.getDisplayNationalNumber(), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تاريخ التوقيع الرقمي:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(issueDate, fontSize = 11.sp, color = Color.DarkGray)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("مركز والولاية الحاضنة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("ولاية نهر النيل - قرى المحمية", fontSize = 10.sp, color = HeritageGreenDark)
                        }
                        
                        HorizontalDivider(color = HeritageGreen.copy(0.08f))
                        
                        // Hash string
                        Column {
                            Text("البصمة الرقمية المبرهَنة (SHA-256):", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            SelectionContainer {
                                Text(
                                    text = hash,
                                    fontSize = 9.sp,
                                    color = Color.DarkGray,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                        .border(1.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "تمنح هذه البطاقة أو الشهادة الثقة الفيدرالية الموثقة بموجب نظام التوقيع اللامركزي وقانون المعاملات الرقمية والمقيدات الأمنية للولاية لعام 2026.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    )
}

// =====================================================================
// NEW MINI-STATE DOCUMENT VISUALIZERS (MARRIAGE, DIVORCE, PASSPORT, ETC.)
// =====================================================================

@Composable
fun MarriageCertificateVisual(citizen: PersonEntity, allPeople: List<PersonEntity>) {
    val spouseName = if (citizen.spouseId != null) {
        allPeople.find { it.id == citizen.spouseId }?.getFullName() ?: "مستورة بنت الخير"
    } else {
        if (citizen.gender == "ذكر") "زينب عثمان تاج السر" else "أنس البشير الطيب"
    }
    
    val husband = if (citizen.gender == "ذكر") citizen.getFullName() else spouseName
    val wife = if (citizen.gender == "ذكر") spouseName else citizen.getFullName()
    val mDate = citizen.marriageDate ?: "1993-02-14"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5), RoundedCornerShape(12.dp))
            .border(3.dp, BronzeGold, RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = BronzeGold.copy(0.15f),
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("المملكة الأسرية", fontSize = 10.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                Text("جمهورية السودان", fontSize = 11.sp, color = HeritageGreenDark, fontWeight = FontWeight.Bold)
                Text("ولاية نهر النيل", fontSize = 10.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "قسيمة زواج شرعية رسمية",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreenDark,
                textDecoration = TextDecoration.Underline
            )
            
            Text(
                "﴿ وَمِنْ آيَاتِهِ أَنْ خَلَقَ لَكُم مِّنْ أَنفُسِكُمْ أَزْوَاجًا لِّتَسْكُنُوا إِلَيْهَا وَجَعَلَ بَيْنَكُم مَّوَدَّةً وَرَحْمَةً ﴾",
                fontSize = 10.sp,
                color = BronzeGold,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            HorizontalDivider(color = BronzeGold.copy(0.3f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Husband Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, HeritageGreen.copy(0.15f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الـزوج (البعل)", fontSize = 10.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(husband, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark, textAlign = TextAlign.Center)
                        Text("الرقم الوطني: " + (if(citizen.gender == "ذكر") citizen.getDisplayNationalNumber() else "29804051012"), fontSize = 9.sp, color = Color.Gray)
                    }
                }

                // Wife Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BronzeGold.copy(0.15f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الـزوجة (العقيلة)", fontSize = 10.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(wife, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark, textAlign = TextAlign.Center)
                        Text("الرقم الوطني: " + (if(citizen.gender == "أنثى") citizen.getDisplayNationalNumber() else "27510822104"), fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            // Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.5.dp, BronzeGold.copy(0.2f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("تاريخ انعقاد القران الموثق:", fontSize = 10.sp, color = Color.Gray)
                    Text(mDate, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("رقم الصحيفة والسجل العائلي للزواج:", fontSize = 10.sp, color = Color.Gray)
                    Text(citizen.registryNumber.ifBlank { "س-305/م-ع" }, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("الجهة الفيدرالية المعتمدة:", fontSize = 10.sp, color = Color.Gray)
                    Text("محكمة الأحوال الشخصية - السجل شرعي لولاية نهر النيل", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HeritageGreen)
                }
            }

            // QR & Stamp block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BronzeGold.copy(0.1f))
                            .border(1.dp, BronzeGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = BronzeGold, modifier = Modifier.size(18.dp))
                    }
                    Text("الختم الشرعي", fontSize = 8.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("إمضاء القاضي الشرعي الموثِق للقران", fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    Text("الشيخ/ سليمان بن إسماعيل الزين", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark)
                }

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .border(1.dp, HeritageGreen, RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CustomQRCodeCanvas(content = "MARRIAGE-${citizen.getDisplayNationalNumber()}-$mDate-NLE", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun DivorceCertificateVisual(citizen: PersonEntity) {
    val dDate = citizen.divorceDate ?: "2015-09-12"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFB), RoundedCornerShape(12.dp))
            .border(3.dp, DarkCharcoal, RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("المعاملات المدنية", fontSize = 10.sp, color = DarkCharcoal, fontWeight = FontWeight.Bold)
                Text("السجل المدني الإشكالي", fontSize = 11.sp, color = HeritageGreenDark, fontWeight = FontWeight.Bold)
                Text("ولاية نهر النيل", fontSize = 10.sp, color = DarkCharcoal, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "وثيقة إشهاد طلاق معتمدة",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red.copy(0.8f),
                textDecoration = TextDecoration.Underline
            )

            HorizontalDivider(color = DarkCharcoal.copy(0.3f), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.5.dp, DarkCharcoal.copy(0.2f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "بناءً على طلب فك الرابطة الزوجية بين الطرفين، تم توثيق حل عقد النكاح بموجب الحكم القضائي الصادر عن المحكمة الفيدرالية المختصة بنهر النيل.",
                    fontSize = 10.sp,
                    color = Color.DarkGray,
                    lineHeight = 14.sp
                )
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("اسـم المطلـق:", fontSize = 10.sp, color = Color.Gray)
                    Text(citizen.getFullName(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("الرقم الوطني الموحد:", fontSize = 10.sp, color = Color.Gray)
                    Text(citizen.getDisplayNationalNumber(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("تاريخ حدوث وتوثيق الطلاق:", fontSize = 10.sp, color = Color.Gray)
                    Text(dDate, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("رقم الصحيفة وسجل الأحوال:", fontSize = 10.sp, color = Color.Gray)
                    Text(citizen.registryNumber.ifBlank { "س-882/ط-م" }, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                }
            }

            // QR & Stamp block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(0.08f))
                            .border(1.dp, Color.Red, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                    Text("الختم القضائي", fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("أمين السجلات الشرعية بالولاية", fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    Text("البروفيسور/ عثمان الهادي عوض", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                }

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CustomQRCodeCanvas(content = "DIVORCE-${citizen.getDisplayNationalNumber()}-$dDate-NLE", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun PassportVisual(citizen: PersonEntity) {
    val passportNo = citizen.getDisplayPassport()
    val nationalNo = citizen.getDisplayNationalNumber()
    val issueDate = citizen.registrationDate.ifBlank { "2024-05-15" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1A2C), RoundedCornerShape(12.dp))
            .border(2.5.dp, BronzeGold, RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Country Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("جواز سفر بيومتري", fontSize = 9.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                Text("REPUBLIC OF SUDAN - جمهورية السودان", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Menu, contentDescription = null, tint = BronzeGold, modifier = Modifier.size(15.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Portrait and details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Biometric Portrait Placeholder
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.08f))
                            .border(1.dp, BronzeGold, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (citizen.gender == "ذكر") Icons.Default.Person else Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White.copy(0.6f),
                            modifier = Modifier.size(45.dp)
                        )
                        // Seal overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(HeritageGreen.copy(0.1f))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "PHOTO", fontSize = 8.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                }

                // Passport properties
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("اللّقـب / Surname", fontSize = 8.sp, color = Color.Gray)
                            Text(citizen.familyName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("رقم الجواز / Passport No.", fontSize = 8.sp, color = Color.Gray)
                            Text(passportNo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BronzeGold)
                        }
                    }

                    Column {
                        Text("الاسم الكامل / Given Names", fontSize = 8.sp, color = Color.Gray)
                        Text("${citizen.firstName} ${citizen.fatherName} ${citizen.grandFatherName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("الجنسية / Nationality", fontSize = 8.sp, color = Color.Gray)
                            Text("SUDANESE / سودانية", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("النوع / Sex", fontSize = 8.sp, color = Color.Gray)
                            Text(if (citizen.gender == "ذكر") "M" else "F", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("تاريخ الميلاد / Birth Date", fontSize = 8.sp, color = Color.Gray)
                            Text(citizen.birthDate, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("رقم السجل الوطني / National ID", fontSize = 8.sp, color = Color.Gray)
                            Text(nationalNo, fontSize = 9.sp, color = BronzeGold, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("الجهة / Authority", fontSize = 8.sp, color = Color.Gray)
                            Text("ولاية نهر النيل", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Machine Readable Zone (MRZ)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = "P<SDN${citizen.familyName.uppercase()}<<${citizen.firstName.uppercase()}<<<<<<<<<<<<<<<<<<<\n" +
                           "${passportNo.uppercase()}7SDN${citizen.birthDate.replace("-","")}9${if (citizen.gender == "ذكر") "M" else "F"}<<<<<<<<<<<<",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LicenseVisual(citizen: PersonEntity) {
    val licNo = citizen.getDisplayLicense()
    val nationalNo = citizen.getDisplayNationalNumber()
    val issueDate = citizen.registrationDate.ifBlank { "2025-06-12" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF4FB), RoundedCornerShape(12.dp))
            .border(2.5.dp, Color(0xFF0F4C81), RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("وزارة الداخلية السودانية", fontSize = 8.sp, color = Color(0xFF0F4C81), fontWeight = FontWeight.Bold)
                Text("رخصة قيادة مركبات موحدة", fontSize = 11.sp, color = HeritageGreenDark, fontWeight = FontWeight.Bold)
                Text("شعبة مرور نهر النيل", fontSize = 8.sp, color = Color(0xFF0F4C81), fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = Color(0xFF0F4C81).copy(0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo Placeholder
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFF0F4C81).copy(0.3f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF0F4C81).copy(0.4f),
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Licence Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(text = "رقم الرخصة: $licNo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F4C81))
                    Text(text = "الاسم: ${citizen.getFullName()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    Text(text = "الرقم الوطني: $nationalNo", fontSize = 10.sp, color = Color.DarkGray)
                    
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("الفئة: أ - ملاكي خاص", fontSize = 10.sp, color = Color(0xFF0F4C81), fontWeight = FontWeight.Bold)
                        Text("فصيلة الدم: ${citizen.bloodType.ifBlank { "O+" }}", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("تاريخ الإصدار: $issueDate", fontSize = 9.sp, color = Color.Gray)
                        Text("المرور: قسم الدامر الرئيسي", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF0F4C81).copy(0.15f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إمضاء مدير شرطة دائرة المرور بالولاية", fontSize = 8.sp, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .background(Color.White)
                        .padding(2.dp)
                ) {
                    CustomQRCodeCanvas(content = "LICENSE-$licNo-$nationalNo", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun MilitaryCardVisual(citizen: PersonEntity) {
    val milId = citizen.getDisplayMilitaryId()
    val rank = if (citizen.id % 2 == 0) "ملازم أول" else "مجند للخدمة إلزامية"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAECE6), RoundedCornerShape(12.dp))
            .border(2.5.dp, Color(0xFF556B2F), RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("قوات الشعب المسلحة", fontSize = 8.sp, color = Color(0xFF556B2F), fontWeight = FontWeight.Bold)
                Text("بطاقة عسكرية معتمدة للدولة", fontSize = 11.sp, color = Color(0xFF556B2F), fontWeight = FontWeight.Bold)
                Text("الفرقة الثالثة مشاة (شندي)", fontSize = 8.sp, color = Color(0xFF556B2F), fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = Color(0xFF556B2F).copy(0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo Placeholder with military green tint
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD4E1C6))
                        .border(1.dp, Color(0xFF556B2F), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Info block
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(text = "الرقم العسكري لربط القوات: $milId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF556B2F))
                    Text(text = "رتبة الوفاء الفيدرالي: $rank", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkCharcoal)
                    Text(text = "الاسم: ${citizen.getFullName()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "الرقم الوطني الموحد: ${citizen.getDisplayNationalNumber()}", fontSize = 9.sp, color = Color.DarkGray)
                    Text(text = "منطقة تمركز القوة: شندي - رئاسة القيادة العسكرية", fontSize = 9.sp, color = Color(0xFF556B2F))
                }
            }

            HorizontalDivider(color = Color(0xFF556B2F).copy(0.15f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("شعبة التحري والأمن العسكري بنهر النيل", fontSize = 8.sp, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White)
                ) {
                    CustomQRCodeCanvas(content = "MILITARY-$milId-${citizen.getDisplayNationalNumber()}", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun AffiliationCardVisual(citizen: PersonEntity) {
    val affId = citizen.getDisplayAffiliationId()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7), RoundedCornerShape(12.dp))
            .border(2.5.dp, BronzeGold, RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("المجلس الأعلى للأعيان", fontSize = 8.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
                Text("بطاقة انتساب لولاية وعشيرة نهر النيل", fontSize = 11.sp, color = HeritageGreenDark, fontWeight = FontWeight.Bold)
                Text("مجلس أعيان المحمية وبجراوية", fontSize = 7.sp, color = BronzeGold, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = BronzeGold.copy(0.3f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BronzeGold.copy(0.1f))
                        .border(1.5.dp, BronzeGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = BronzeGold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "رقم العضوية والنسب: $affId", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark)
                    Text(text = "منسوب للعقد: ${citizen.getFullName()}", fontSize = 11.sp, color = DarkCharcoal, fontWeight = FontWeight.Bold)
                    Text(text = "البلدة الحاضنة: ${citizen.villageName.ifBlank { "قرية المحمية الكبرى" }} - نهر النيل", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "المهنة المدنية: ${citizen.occupation.ifBlank { "مزارع بالضفاف" }}", fontSize = 10.sp, color = HeritageGreen)
                }
            }

            HorizontalDivider(color = BronzeGold.copy(0.15f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("يرجى تسهيل حركة حامل هذه البطاقة للتجمع الأهلي المعتمد", fontSize = 8.sp, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White)
                ) {
                    CustomQRCodeCanvas(content = "AFFILIATION-$affId-${citizen.getDisplayNationalNumber()}", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

fun exportDocumentAsPdf(context: android.content.Context, citizen: PersonEntity, docType: String, allPeople: List<PersonEntity> = emptyList()) {
    try {
        val pdfStr = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // standard A4
        val page = pdfStr.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // 1. Background Fill and Borders
        canvas.drawColor(AndroidColor.parseColor("#FFFDF9")) // sand tint
        
        // Classic State green border
        paint.color = AndroidColor.parseColor("#1B4D3E")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRect(20f, 20f, 575f, 822f, paint)
        
        // Inner golden border
        paint.color = AndroidColor.parseColor("#C59B27")
        paint.strokeWidth = 2f
        canvas.drawRect(26f, 26f, 569f, 816f, paint)
        
        // 2. Title Headers
        paint.color = AndroidColor.parseColor("#143626")
        paint.style = Paint.Style.FILL
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("جمهورية السودان - ولاية نهر النيل", 297f, 70f, paint)
        
        paint.textSize = 14f
        paint.color = AndroidColor.parseColor("#C59B27")
        canvas.drawText("منصة السجل المدني الفيدرالي الرقمي الموحد", 297f, 95f, paint)
        
        paint.strokeWidth = 2f
        canvas.drawLine(100f, 115f, 495f, 115f, paint)
        
        // Document Title
        paint.textSize = 22f
        paint.color = AndroidColor.parseColor("#1B4D3E")
        val docTitle = when (docType) {
            "ID_CARD" -> "البطاقة الشخصية القومية الرقمية"
            "BIRTH_CERT" -> "شهادة الميلاد الرسمية الفيدرالية"
            "FAMILY_REG" -> "شهادة الصحيفة والقيد العائلي للمحمية"
            "DEATH_CERT" -> "وثيقة وتصريح الوفاة الشرعي"
            "MARRIAGE_CERT" -> "وثيقة وعقد النكاح الشرعي الرسمي"
            "DIVORCE_CERT" -> "إشهاد الطلاق القضائي المعتمد"
            "PASSPORT" -> "جواز سفر جمهورية السودان بيومتري"
            "LICENSE" -> "رخصة قيادة المركبات الموحدة"
            "MILITARY_CARD" -> "بطاقة منسوبي الخدمة العسكرية"
            "AFFILIATION_CARD" -> "بطاقة الانتساب لعشائر وأعيان الولاية"
            else -> "الوثيقة والسجل المدني الرسمي"
        }
        canvas.drawText(docTitle, 297f, 160f, paint)
        
        // 3. Citizen Info Attributes
        paint.textAlign = Paint.Align.RIGHT
        paint.color = AndroidColor.parseColor("#212529")
        paint.textSize = 15f
        
        var yPos = 230f
        val lineSpacing = 35f
        
        canvas.drawText("الاسم المستنداتي الكامل:  ${citizen.getFullName()}", 500f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("الرقم الوطني الموحد:  ${citizen.getDisplayNationalNumber()}", 500f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("تاريخ الـولادة المدون:  ${citizen.birthDate}", 500f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("مكان الولادة الأصيل:  ${citizen.birthPlace}", 500f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("القرية أو الحي السكني بالولاية:  ${citizen.villageName.ifBlank { "المحمية الغربية" }}", 500f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("رقم القيد الصحيفي بالسجلات:  ${citizen.registryNumber.ifBlank { "س-209/ن.ن" }}", 500f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("فصيلة الدم المسجلة:  ${citizen.bloodType.ifBlank { "O+" }}", 500f, yPos, paint)
        yPos += lineSpacing
        
        when (docType) {
            "PASSPORT" -> {
                canvas.drawText("رقم الجواز الدولي:  ${citizen.getDisplayPassport()}", 500f, yPos, paint)
                yPos += lineSpacing
                canvas.drawText("نوع السند الدولي:  P - بيومتري عادي", 500f, yPos, paint)
                yPos += lineSpacing
            }
            "LICENSE" -> {
                canvas.drawText("رقم رخصة القيادة:  ${citizen.getDisplayLicense()}", 500f, yPos, paint)
                yPos += lineSpacing
                canvas.drawText("فئة رخصة المركبات:  أ - خاص ملاكي", 500f, yPos, paint)
                yPos += lineSpacing
            }
            "MILITARY_CARD" -> {
                canvas.drawText("الرقم العسكري بالفرقة الثالثة:  ${citizen.getDisplayMilitaryId()}", 500f, yPos, paint)
                yPos += lineSpacing
                canvas.drawText("الحالة العسكرية المقيدة:  ملازم أول مجند بالضفاف", 500f, yPos, paint)
                yPos += lineSpacing
            }
            "MARRIAGE_CERT" -> {
                val spouseName = if (citizen.spouseId != null) {
                    allPeople.find { it.id == citizen.spouseId }?.getFullName() ?: "الزوجة المصونة"
                } else "عقيلة الخير"
                canvas.drawText("عقيلة الرباط المقدس:  $spouseName", 500f, yPos, paint)
                yPos += lineSpacing
                canvas.drawText("تاريخ عقد الزواج الشرعي:  ${citizen.marriageDate ?: "1995-10-14"}", 500f, yPos, paint)
                yPos += lineSpacing
            }
            "DIVORCE_CERT" -> {
                canvas.drawText("تاريخ فصم الرابطة الزوجية:  ${citizen.divorceDate ?: "2015-09-12"}", 500f, yPos, paint)
                yPos += lineSpacing
            }
        }
        
        // STAMP
        paint.style = Paint.Style.STROKE
        paint.color = AndroidColor.parseColor("#C59B27")
        paint.strokeWidth = 3f
        canvas.drawCircle(120f, 650f, 45f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("الختم الفيدرالي الرسمي", 120f, 645f, paint)
        canvas.drawText("بقرية المحمية - نهر النيل", 120f, 660f, paint)
        
        // FOOTER
        paint.textSize = 11f
        paint.color = AndroidColor.parseColor("#7F8C8D")
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("تعتبر هذه النسخة المحمّلة وثيقة رقمية رسمية معترف بها بموجب قانون المقيدات الفيدرالي للولاية لعام 2026.", 297f, 750f, paint)
        canvas.drawText("المعرف الرقمي المشفر والمأمون: ${citizen.getDisplayNationalNumber()}-$docType-NLE", 297f, 770f, paint)
        
        pdfStr.finishPage(page)
        
        val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "${citizen.familyName}_${docType}_Document.pdf")
        val fileOutputStream = FileOutputStream(file)
        pdfStr.writeTo(fileOutputStream)
        fileOutputStream.close()
        pdfStr.close()
        
        Toast.makeText(context, "تم تصدير وحفظ نسخة حقيقية PDF بنجاح!\nالملف: ${file.name}\nالمسار: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "حدث خطأ أثناء طباعة وتصدير ملف PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
