package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.ui.theme.DarkCharcoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeApp(
    viewModel: FamilyViewModel,
    modifier: Modifier = Modifier
) {
    // Explicitly enforce Right-To-Left layout for native Arabic natural scanning!
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
        val allPeople by viewModel.allPeople.collectAsStateWithLifecycle()
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

        // Snackbars/Alerts
        var showSnackbar by remember { mutableStateOf(false) }
        var snackbarText by remember { mutableStateOf("") }

        LaunchedEffect(errorMessage) {
            errorMessage?.let {
                snackbarText = it
                showSnackbar = true
                viewModel.clearErrorMessage()
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home, // Government office icon standard
                                contentDescription = null,
                                tint = BronzeGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "السجل المدني - دولة المحمية",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 21.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = HeritageGreenDark,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        // Demo data quick injector button!
                        IconButton(
                            onClick = {
                                viewModel.loadSampleData()
                                snackbarText = "تم تسكين وتأسيس قيود السجل المدني لدولة المحمية بنجاح!"
                                showSnackbar = true
                            },
                            modifier = Modifier.testTag("load_demo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "تعبئة السجل المدني بالبيانات النموذجية",
                                tint = BronzeGold
                            )
                        }
                    }
                )
            },
            bottomBar = {
                FamilyBottomNavigation(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = WarmSand,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Tab Routing
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                    },
                    label = "TabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        is ActiveTab.ListTab -> {
                            PeopleListScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                        is ActiveTab.AddEditTab -> {
                            AddEditPersonScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                        is ActiveTab.TreeTab -> {
                            InteractiveTreeScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                        is ActiveTab.DocumentsTab -> {
                            CivilRegistryDocumentsScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                        is ActiveTab.DashboardTab -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                        is ActiveTab.BankTab -> {
                            CivilBankScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                        is ActiveTab.StateTab -> {
                            StateSovereigntyScreen(
                                viewModel = viewModel,
                                allPeople = allPeople
                            )
                        }
                    }
                }

                // Alert Toast / Dialog bottom bar replacement
                if (showSnackbar) {
                    AlertDialog(
                        onDismissRequest = { showSnackbar = false },
                        confirmButton = {
                            TextButton(onClick = { showSnackbar = false }) {
                                Text("حسناً", color = HeritageGreen)
                            }
                        },
                        title = { Text("تنبيه", fontWeight = FontWeight.Bold) },
                        text = { Text(snackbarText) }
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyBottomNavigation(
    activeTab: ActiveTab,
    onTabSelected: (ActiveTab) -> Unit
) {
    NavigationBar(
        containerColor = HeritageGreenDark,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = activeTab is ActiveTab.ListTab,
            onClick = { onTabSelected(ActiveTab.ListTab) },
            icon = { Icon(Icons.Default.Menu, contentDescription = "السجل") },
            label = { Text("السجل", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_list_tab")
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.AddEditTab,
            onClick = { onTabSelected(ActiveTab.AddEditTab) },
            icon = { Icon(Icons.Default.Add, contentDescription = "قيد جديد") },
            label = { Text("قيد", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_add_tab")
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.TreeTab,
            onClick = { onTabSelected(ActiveTab.TreeTab) },
            icon = { Icon(Icons.Default.Share, contentDescription = "الأنساب") },
            label = { Text("الأنساب", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_tree_tab")
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.DocumentsTab,
            onClick = { onTabSelected(ActiveTab.DocumentsTab) },
            icon = { Icon(Icons.Default.Info, contentDescription = "الوثائق") },
            label = { Text("الوثائق", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_docs_tab")
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.DashboardTab,
            onClick = { onTabSelected(ActiveTab.DashboardTab) },
            icon = { Icon(Icons.Default.Home, contentDescription = "الإحصاء") },
            label = { Text("الإحصاء", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_dashboard_tab")
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.BankTab,
            onClick = { onTabSelected(ActiveTab.BankTab) },
            icon = { Icon(Icons.Default.Lock, contentDescription = "البنك") },
            label = { Text("البنك", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_bank_tab")
        )
        NavigationBarItem(
            selected = activeTab is ActiveTab.StateTab,
            onClick = { onTabSelected(ActiveTab.StateTab) },
            icon = { Icon(Icons.Default.Star, contentDescription = "السيادة") },
            label = { Text("السيادة", fontWeight = FontWeight.Bold, fontSize = 8.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HeritageGreen,
                selectedTextColor = BronzeGold,
                indicatorColor = BronzeGold,
                unselectedIconColor = Color.White.copy(0.7f),
                unselectedTextColor = Color.White.copy(0.7f)
            ),
            modifier = Modifier.testTag("nav_state_tab")
        )
    }
}

// ==========================================
// SCREEN 1: PEOPLE LIST SCREEN (قائمة الأشخاص)
// ==========================================
@Composable
fun PeopleListScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val genderFilter by viewModel.genderFilter.collectAsStateWithLifecycle()
    val aliveFilter by viewModel.aliveFilter.collectAsStateWithLifecycle()
    var archiveFilter by remember { mutableStateOf<Boolean?>(false) } // false = نشط, true = مؤرشف, null = الكل

    // Client-side quick filter logic
    val filteredPeople = remember(allPeople, searchQuery, genderFilter, aliveFilter, archiveFilter) {
        allPeople.filter { person ->
            val matchesSearch = person.getFullName().contains(searchQuery, ignoreCase = true) ||
                    person.phoneNumber.contains(searchQuery) ||
                    person.birthPlace.contains(searchQuery, ignoreCase = true) ||
                    person.nationalNumber.contains(searchQuery) ||
                    person.villageName.contains(searchQuery, ignoreCase = true) ||
                    person.occupation.contains(searchQuery, ignoreCase = true) ||
                    person.notes.contains(searchQuery, ignoreCase = true)
            
            val matchesGender = genderFilter == null || person.gender == genderFilter
            val matchesAlive = aliveFilter == null || person.isAlive == aliveFilter
            val matchesArchive = archiveFilter == null || person.isArchived == archiveFilter
            
            matchesSearch && matchesGender && matchesAlive && matchesArchive
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("ابحث بالاسم، المدينة، أو رقم الجوال...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HeritageGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "حذف النص")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = HeritageGreen,
                unfocusedBorderColor = HeritageGreen.copy(0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Rows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = HeritageGreen,
                modifier = Modifier.size(18.dp)
            )
            
            // Gender Filters
            FilterChip(
                selected = genderFilter == null,
                onClick = { viewModel.setGenderFilter(null) },
                label = { Text("الجميع (الجنس)") }
            )
            FilterChip(
                selected = genderFilter == "ذكر",
                onClick = { viewModel.setGenderFilter("ذكر") },
                label = { Text("ذكور") }
            )
            FilterChip(
                selected = genderFilter == "أنثى",
                onClick = { viewModel.setGenderFilter("أنثى") },
                label = { Text("إناث") }
            )

            VerticalDivider(modifier = Modifier.height(20.dp), color = HeritageGreen.copy(0.3f))

            // Vital Status Filters
            FilterChip(
                selected = aliveFilter == null,
                onClick = { viewModel.setAliveFilter(null) },
                label = { Text("الجميع (الحالة)") }
            )
            FilterChip(
                selected = aliveFilter == true,
                onClick = { viewModel.setAliveFilter(true) },
                label = { Text("أحياء") }
            )
            FilterChip(
                selected = aliveFilter == false,
                onClick = { viewModel.setAliveFilter(false) },
                label = { Text("متوفون") }
            )

            VerticalDivider(modifier = Modifier.height(20.dp), color = HeritageGreen.copy(0.3f))

            // Archive Status Filters
            FilterChip(
                selected = archiveFilter == false,
                onClick = { archiveFilter = false },
                label = { Text("السجلات النشطة") }
            )
            FilterChip(
                selected = archiveFilter == true,
                onClick = { archiveFilter = true },
                label = { Text("الأرشيف المدني 🗄️") }
            )
            FilterChip(
                selected = archiveFilter == null,
                onClick = { archiveFilter = null },
                label = { Text("كافة السجلات") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics Summary Label
        Text(
            text = "عدد الأفراد المطابقين: ${filteredPeople.size} من أصل ${allPeople.size}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = HeritageGreenDark.copy(0.8f),
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredPeople.isEmpty()) {
            // Empty State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, HeritageGreen.copy(0.15f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = HeritageGreenLight.copy(0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد نتائج مطابقة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HeritageGreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جرب تغيير فلتر البحث أو قم بإضافة شجرة عائلتك بالضغط على زر \"شحن البيانات العائلية\" بالأعلى للبدء الفوري!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.startAddPerson() },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة أول شخص يدوياً")
                    }
                }
            }
        } else {
            // Main List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("people_lazy_column")
            ) {
                items(filteredPeople, key = { it.id }) { person ->
                    PersonCard(
                        person = person,
                        onViewDocs = { viewModel.selectPersonForDocument(person) },
                        onViewTree = { viewModel.selectPersonForTree(person) },
                        onEdit = { viewModel.startEditPerson(person) },
                        onDelete = { viewModel.deletePerson(person) }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonCard(
    person: PersonEntity,
    onViewDocs: () -> Unit,
    onViewTree: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HeritageGreen.copy(0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customized avatar based on Gender & Status or selected image key
                val avatarEmoji = when (person.profileImage) {
                    "man_traditional" -> "👳‍♂️"
                    "woman_traditional" -> "🧕"
                    "man" -> "👨"
                    "woman" -> "👩"
                    "boy" -> "👦"
                    "girl" -> "👧"
                    "avatar_doc" -> "🪪"
                    "military_uniform" -> "🎖️"
                    else -> null
                }
                val avatarBg = if (person.isArchived) Color(0xFFF1F0EA) else (if (person.gender == "ذكر") SoftSage else Color(0xFFFFF1F2))
                val avatarColor = if (person.gender == "ذكر") HeritageGreen else BronzeGold
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(avatarBg)
                ) {
                    if (avatarEmoji != null) {
                        Text(text = avatarEmoji, fontSize = 24.sp)
                    } else if (person.isAlive) {
                        Icon(
                            imageVector = if (person.gender == "ذكر") Icons.Default.Person else Icons.Default.Star,
                            contentDescription = person.gender,
                            tint = avatarColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // Deceased visual distinction
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "رحمه الله",
                            tint = Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Name and details column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = person.getFullName(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = HeritageGreenDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Status Badge
                        if (!person.isAlive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray.copy(0.5f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "رحمه الله",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Archive status badge
                        if (person.isArchived) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFF3CD))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "مؤرشف 🗄️",
                                    fontSize = 9.sp,
                                    color = Color(0xFF856404),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Location / Phone sublabel
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (person.birthPlace.isNotBlank()) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(
                                text = person.birthPlace,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        
                        if (person.phoneNumber.isNotBlank()) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(
                                text = person.phoneNumber,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Quick display notes snippet if available
            if (person.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmSand.copy(0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = person.notes,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HeritageGreen.copy(0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Document Center issuance button!
                    TextButton(
                        onClick = onViewDocs,
                        colors = ButtonDefaults.textButtonColors(contentColor = BronzeGold),
                        modifier = Modifier.testTag("person_card_docs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "استخراج الوثائق",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Tree View
                    TextButton(
                        onClick = onViewTree,
                        colors = ButtonDefaults.textButtonColors(contentColor = HeritageGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "شجرة القرابة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display citizen NID badge!
                    Text(
                        text = "الرقم الوطني: ${person.getDisplayNationalNumber()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreen.copy(0.7f),
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    // Edit/Delete secondary buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الفرد",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الفرد",
                                tint = Color.Red.copy(0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 2: ADD / EDIT PERSON SCREEN (إضافة / تعديل فرد)
// ==========================================
@Composable
fun AddEditPersonScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    val personToEdit by viewModel.personToEdit.collectAsStateWithLifecycle()

    // Form inputs state
    var firstName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var grandFatherName by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") } // "ذكر" أو "أنثى"
    var birthDate by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var isAlive by remember { mutableStateOf(true) }
    var deathDate by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Civil attributes state
    var nationalNumber by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var registrationDate by remember { mutableStateOf("") }
    var registryNumber by remember { mutableStateOf("") }
    var villageName by remember { mutableStateOf("") }

    // New customizable document/state attributes
    var bankAccountNumber by remember { mutableStateOf("") }
    var bankBalanceStr by remember { mutableStateOf("0.0") }
    var passportNumber by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var militaryId by remember { mutableStateOf("") }
    var affiliationId by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("أعزب") }
    var isArchived by remember { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf<String?>(null) }

    // Relationships reference state
    var selectedFatherId by remember { mutableStateOf<Int?>(null) }
    var selectedMotherId by remember { mutableStateOf<Int?>(null) }
    var selectedSpouseId by remember { mutableStateOf<Int?>(null) }

    // Dropdown controllers
    var fatherSearchQuery by remember { mutableStateOf("") }
    var motherSearchQuery by remember { mutableStateOf("") }
    var spouseSearchQuery by remember { mutableStateOf("") }

    // Pre-populate if editing
    LaunchedEffect(personToEdit) {
        if (personToEdit != null) {
            val p = personToEdit!!
            firstName = p.firstName
            fatherName = p.fatherName
            grandFatherName = p.grandFatherName
            familyName = p.familyName
            gender = p.gender
            birthDate = p.birthDate
            birthPlace = p.birthPlace
            isAlive = p.isAlive
            deathDate = p.deathDate ?: ""
            phoneNumber = p.phoneNumber
            address = p.address
            notes = p.notes
            selectedFatherId = p.fatherId
            selectedMotherId = p.motherId
            selectedSpouseId = p.spouseId
            
            // Civil mappings
            nationalNumber = p.nationalNumber
            occupation = p.occupation
            bloodType = p.bloodType
            registrationDate = p.registrationDate
            registryNumber = p.registryNumber
            villageName = p.villageName

            // Document mappings
            bankAccountNumber = p.bankAccountNumber
            bankBalanceStr = p.bankBalance.toString()
            passportNumber = p.passportNumber
            licenseNumber = p.licenseNumber
            militaryId = p.militaryId
            affiliationId = p.affiliationId
            maritalStatus = p.maritalStatus
            isArchived = p.isArchived
            profileImage = p.profileImage
        } else {
            // Clear all states for brand new entry
            firstName = ""
            fatherName = ""
            grandFatherName = ""
            familyName = ""
            gender = "ذكر"
            birthDate = ""
            birthPlace = ""
            isAlive = true
            deathDate = ""
            phoneNumber = ""
            address = ""
            notes = ""
            selectedFatherId = null
            selectedMotherId = null
            selectedSpouseId = null
            
            // Civil clearings
            nationalNumber = ""
            occupation = ""
            bloodType = ""
            registrationDate = ""
            registryNumber = ""
            villageName = ""

            // Document clearings
            bankAccountNumber = ""
            bankBalanceStr = "0.0"
            passportNumber = ""
            licenseNumber = ""
            militaryId = ""
            affiliationId = ""
            maritalStatus = "أعزب"
            isArchived = false
            profileImage = null
        }
    }

    // Filter available connections to avoid self-reference or illegal loops
    val eligibleFathers = remember(allPeople, personToEdit) {
        allPeople.filter { it.gender == "ذكر" && (personToEdit == null || it.id != personToEdit?.id) }
    }
    val eligibleMothers = remember(allPeople, personToEdit) {
        allPeople.filter { it.gender == "أنثى" && (personToEdit == null || it.id != personToEdit?.id) }
    }
    val eligibleSpouses = remember(allPeople, personToEdit) {
        allPeople.filter { personToEdit == null || it.id != personToEdit?.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Form Title
        Text(
            text = if (personToEdit == null) "إضافة فرد جديد للعائلة" else "تعديل بيانات: ${personToEdit?.getFullName()}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = HeritageGreenDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Wrapper scroll to prevent clipping on small devices
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("add_edit_form_container")
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الاسم الأساسي واللقب", fontWeight = FontWeight.Bold, color = HeritageGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // First name + Family name Row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("الاسم الأول *") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_first_name")
                            )
                            OutlinedTextField(
                                value = familyName,
                                onValueChange = { familyName = it },
                                label = { Text("العائلة / العشيرة *") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_family_name")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Middle Names
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = fatherName,
                                onValueChange = { fatherName = it },
                                label = { Text("اسم الأب") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = grandFatherName,
                                onValueChange = { grandFatherName = it },
                                label = { Text("اسم الجد") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الخصائص الحيوية", fontWeight = FontWeight.Bold, color = HeritageGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Gender Picker
                        Text("الجنس:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = gender == "ذكر",
                                    onClick = { gender = "ذكر" }
                                )
                                Text("ذكر", modifier = Modifier.clickable { gender = "ذكر" })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = gender == "أنثى",
                                    onClick = { gender = "أنثى" }
                                )
                                Text("أنثى", modifier = Modifier.clickable { gender = "أنثى" })
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Birth Details
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { birthDate = it },
                                label = { Text("تاريخ الميلاد (مثال: 2000-01-01)") },
                                placeholder = { Text("YYYY-MM-DD") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = birthPlace,
                                    onValueChange = { birthPlace = it },
                                    label = { Text("بلد / مدينة الولادة") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("الدامر", "شندي", "عطبرة", "بربر", "أبو حمد", "المتمة").forEach { city ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HeritageGreen.copy(0.08f))
                                                .clickable { birthPlace = city }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(city, fontSize = 9.sp, color = HeritageGreenDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Alive/Deceased Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "الفرد على قيد الحياة؟",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Switch(
                                checked = isAlive,
                                onCheckedChange = { isAlive = it }
                            )
                        }

                        // If Deceased, show death date selector
                        AnimatedVisibility(visible = !isAlive) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = deathDate,
                                    onValueChange = { deathDate = it },
                                    label = { Text("تاريخ الوفاة (إن وجد)") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("البيانات والبطاقة المدنية للدولة (دولة المحمية)", fontWeight = FontWeight.Bold, color = HeritageGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // National ID (If blank, system will auto-compute on save)
                        OutlinedTextField(
                            value = nationalNumber,
                            onValueChange = { nationalNumber = it },
                            label = { Text("الرقم الوطني الموحد (اتركه فارغاً للتوليد التلقائي)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_national_number")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Occupation
                            OutlinedTextField(
                                value = occupation,
                                onValueChange = { occupation = it },
                                label = { Text("المهنة / الوظيفة") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            // Blood type
                            OutlinedTextField(
                                value = bloodType,
                                onValueChange = { bloodType = it },
                                label = { Text("زمرة الدم (مثال: O+)") },
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Village Name
                            Column(modifier = Modifier.weight(1.3f)) {
                                OutlinedTextField(
                                    value = villageName,
                                    onValueChange = { villageName = it },
                                    label = { Text("اسم القرية بالولاية (مثال: طيبة)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("المحمية", "طيبة الخواض", "الزيداب", "الحرة", "العالياب", "قدو", "البجراوية").forEach { village ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BronzeGold.copy(0.1f))
                                                .clickable { villageName = village }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(village, fontSize = 9.sp, color = DarkCharcoal, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            // Registry serial
                            OutlinedTextField(
                                value = registryNumber,
                                onValueChange = { registryNumber = it },
                                label = { Text("رقم الصحيفة/السجل الوطني") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("روابط الصلة والقرابة لنظام الشجرة", fontWeight = FontWeight.Bold, color = HeritageGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("اربط هذا الفرد بوالديه أو زوجته لبناء الشجرة آلياً:", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Father Selector Dropdown
                        Text("الأب في النظام:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        SimpleRelationSelector(
                            label = "اختر الأب من القائمة",
                            selectedId = selectedFatherId,
                            searchQuery = fatherSearchQuery,
                            onQueryChange = { fatherSearchQuery = it },
                            people = eligibleFathers,
                            onSelected = { selectedFatherId = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mother Selector
                        Text("الأم في النظام:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        SimpleRelationSelector(
                            label = "اختر الأم من القائمة",
                            selectedId = selectedMotherId,
                            searchQuery = motherSearchQuery,
                            onQueryChange = { motherSearchQuery = it },
                            people = eligibleMothers,
                            onSelected = { selectedMotherId = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Spouse Selector
                        Text("الزوج / الزوجة في النظام:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        SimpleRelationSelector(
                            label = "اختر شريك الحياة من القائمة",
                            selectedId = selectedSpouseId,
                            searchQuery = spouseSearchQuery,
                            onQueryChange = { spouseSearchQuery = it },
                            people = eligibleSpouses,
                            onSelected = { selectedSpouseId = it }
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("تخصيص مستندات المواطن ورمزية الهوية (أحدث نسخة)", fontWeight = FontWeight.Bold, color = BronzeGold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Avatar Selection Row
                        Text("الصورة أو الرمزية الرسمية للهيكل المدني:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HeritageGreenDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            val avatars = listOf(
                                "man_traditional" to "جلابية وعمة 👳‍♂️",
                                "woman_traditional" to "توب سوداني 🧕",
                                "man" to "شاب أنيق 👨",
                                "woman" to "ثوب معاصر 👩",
                                "boy" to "طفل صغير 👦",
                                "girl" to "طفلة صغيرة 👧",
                                "avatar_doc" to "رمزية طبية 🪪",
                                "military_uniform" to "رمز عسكري 🎖️"
                            )
                            avatars.forEach { avatarItem ->
                                val isSelected = profileImage == avatarItem.first
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) HeritageGreen.copy(0.12f) else Color(0xFFF7F7F7))
                                        .border(2.dp, if (isSelected) BronzeGold else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable { profileImage = avatarItem.first }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = avatarItem.second,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) HeritageGreenDark else Color.DarkGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Archiving Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF9E6).copy(0.8f), RoundedCornerShape(8.dp))
                                .border(1.dp, BronzeGold.copy(0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "أرشفة السجل المدني التاريخي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF8A6D3B)
                                )
                                Text(
                                    "سيتم نقل هذا المواطن إلى أرشيف السجلات وحفظ كافة وثائقه.",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = isArchived,
                                onCheckedChange = { isArchived = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BronzeGold,
                                    checkedTrackColor = BronzeGold.copy(0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("تخصيص أرقام المستندات الرسمية ومحفظة البنك", fontWeight = FontWeight.Bold, color = HeritageGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Bank Account & Balance
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = bankAccountNumber,
                                onValueChange = { bankAccountNumber = it },
                                label = { Text("رقم الحساب البنكي") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = bankBalanceStr,
                                onValueChange = { bankBalanceStr = it },
                                label = { Text("الرصيد المالي (ج.س)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Passport & Driver's License
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = passportNumber,
                                onValueChange = { passportNumber = it },
                                label = { Text("رقم جواز السفر") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = licenseNumber,
                                onValueChange = { licenseNumber = it },
                                label = { Text("رقم رخصة القيادة") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Military Card & Affiliation ID
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = militaryId,
                                onValueChange = { militaryId = it },
                                label = { Text("الرقم العسكري (للجيش)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = affiliationId,
                                onValueChange = { affiliationId = it },
                                label = { Text("رقم بطاقة الانتساب") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Marital Status Segmented Control
                        Text("الحالة الاجتماعية للتسجيل المدني:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HeritageGreenDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("أعزب", "متزوج", "مطلق", "أرمل").forEach { status ->
                                val isSelected = maritalStatus == status
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) HeritageGreen else Color(0xFFF1F5F2))
                                        .clickable { maritalStatus = status }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else HeritageGreenDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HeritageGreen.copy(0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("معلومات التواصل وملاحظات إضافية", fontWeight = FontWeight.Bold, color = HeritageGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("رقم جوال للتواصل") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("مكان السكن / الإقامة الحالي") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Notes textbox
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات وسيرة موجزة عن الفرد") },
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    }
                }
            }

            // Save Buttons Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Save
                    Button(
                        onClick = {
                            viewModel.savePerson(
                                firstName = firstName,
                                fatherName = fatherName,
                                grandFatherName = grandFatherName,
                                familyName = familyName,
                                gender = gender,
                                birthDate = birthDate,
                                birthPlace = birthPlace,
                                isAlive = isAlive,
                                deathDate = deathDate,
                                phoneNumber = phoneNumber,
                                address = address,
                                notes = notes,
                                fatherId = selectedFatherId,
                                motherId = selectedMotherId,
                                spouseId = selectedSpouseId,
                                nationalNumber = nationalNumber,
                                occupation = occupation,
                                bloodType = bloodType,
                                registrationDate = registrationDate,
                                registryNumber = registryNumber,
                                villageName = villageName,
                                isArchived = isArchived,
                                profileImage = profileImage,
                                bankAccountNumber = bankAccountNumber,
                                bankBalance = bankBalanceStr.toDoubleOrNull() ?: 125000.0,
                                passportNumber = passportNumber,
                                licenseNumber = licenseNumber,
                                militaryId = militaryId,
                                affiliationId = affiliationId,
                                maritalStatus = maritalStatus
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("save_person_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تقييد السجل وإصدار الوثيقة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Cancel
                    OutlinedButton(
                        onClick = {
                            viewModel.setTab(ActiveTab.ListTab)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGreenDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("إلغاء وتراجع", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// A simple, visual Search-and-Select dropdown for managing ID references in a beautiful M3 way
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleRelationSelector(
    label: String,
    selectedId: Int?,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    people: List<PersonEntity>,
    onSelected: (Int?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedPerson = people.find { it.id == selectedId }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(WarmSand.copy(0.3f), RoundedCornerShape(8.dp))
                .border(1.dp, HeritageGreen.copy(0.2f), RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedPerson != null) {
                    Text(
                        text = "✓ ${selectedPerson.getFullName()} (${selectedPerson.gender})",
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark
                    )
                } else {
                    Text(
                        text = label,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedId != null) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "مسح التحديد",
                            tint = Color.Red.copy(0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    onSelected(null)
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HeritageGreen
                    )
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DesertIvory),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .border(1.dp, HeritageGreen.copy(0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { Text("بحث باسم قريبك تصفية...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    val filtered = people.filter {
                        searchQuery.isEmpty() || it.getFullName().contains(searchQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        Text(
                            "لا يـوجد أقرباء مطابقين لاسم البحث.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            filtered.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelected(p.id)
                                            isExpanded = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "${p.getFullName()} (${p.gender})",
                                        fontSize = 14.sp,
                                        fontWeight = if (p.id == selectedId) FontWeight.Bold else FontWeight.Normal,
                                        color = if (p.id == selectedId) BronzeGold else HeritageGreenDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 3: INTERACTIVE TREE SCREEN (شجرة العائلة التفاعلية)
// ==========================================
@Composable
fun InteractiveTreeScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    val selectedPersonForTree by viewModel.selectedPersonForTree.collectAsStateWithLifecycle()
    val children by viewModel.treeChildren.collectAsStateWithLifecycle()

    if (selectedPersonForTree == null) {
        // Tree visual empty helper state
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = HeritageGreenLight.copy(0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "استعراض شجرة العائلة المتكاملة",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreenDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الرجاء تحديد أحد الأفراد لعرض شجرته وعلاقاته التفاعلية. انقر أدناه لاختيار فرد للبدء:",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Scrollable selector of all available members in database
            if (allPeople.isEmpty()) {
                Button(
                    onClick = { viewModel.loadSampleData() },
                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen)
                ) {
                    Text("شحن شجرة تجريبية كاملة")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(allPeople) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPersonForTree(p) }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.getFullName(),
                                    fontWeight = FontWeight.Bold,
                                    color = HeritageGreenDark
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        p.gender,
                                        fontSize = 12.sp,
                                        color = if (p.gender == "ذكر") HeritageGreenLight else BronzeGold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                     Icon(
                                         imageVector = Icons.Default.KeyboardArrowLeft,
                                         contentDescription = null,
                                         tint = HeritageGreenLight,
                                         modifier = Modifier.size(16.dp)
                                     )
                                }
                            }
                            HorizontalDivider(color = HeritageGreen.copy(0.05f))
                        }
                    }
                }
            }
        }
    } else {
        // Detailed walkthrough visual tree for the selected person!
        val focusPerson = selectedPersonForTree!!
        
        // Find links
        val father = allPeople.find { it.id == focusPerson.fatherId }
        val mother = allPeople.find { it.id == focusPerson.motherId }
        val spouse = allPeople.find { it.id == focusPerson.spouseId }
        
        // Siblings query: people with same father or mother
        val siblings = remember(allPeople, focusPerson) {
            allPeople.filter {
                it.id != focusPerson.id && (
                        (focusPerson.fatherId != null && it.fatherId == focusPerson.fatherId) ||
                        (focusPerson.motherId != null && it.motherId == focusPerson.motherId)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Navigator breadcrumb/back
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconButton(onClick = { viewModel.setTab(ActiveTab.ListTab) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع للقائمة",
                        tint = HeritageGreen
                    )
                }
                Text(
                    text = "عودة للقائمة",
                    fontSize = 14.sp,
                    color = HeritageGreen,
                    modifier = Modifier.clickable { viewModel.setTab(ActiveTab.ListTab) }
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Switch focus helper
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BronzeGold.copy(0.12f))
                        .clickable { viewModel.startEditPerson(focusPerson) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "تعديل هذا الفرد",
                        color = BronzeGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Outer scroll for complete safety & layout scrolling
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tree_screen_container"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TIER 1: PARENTS (الآباء)
                item {
                    Text(
                        text = "الآباء والأمهات (اضغط للتنقل)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Father card
                        if (father != null) {
                            TreeMiniCard(
                                title = "الأب",
                                person = father,
                                indicatorColor = HeritageGreenLight,
                                onClick = { viewModel.selectPersonForTree(father) }
                            )
                        } else {
                            TreeEmptyCard(title = "الأب غير مسجل", icon = Icons.Default.Person)
                        }

                        // Plus or connector symbol
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = HeritageGreen)

                        // Mother card
                        if (mother != null) {
                            TreeMiniCard(
                                title = "الأم",
                                person = mother,
                                indicatorColor = BronzeGold,
                                onClick = { viewModel.selectPersonForTree(mother) }
                            )
                        } else {
                            TreeEmptyCard(title = "الأم غير مسجلة", icon = Icons.Default.Star)
                        }
                    }
                }

                // Custom Graphic drawing lines showing connection from parents to current person
                item {
                    ParentConnectorLines(hasParents = (father != null || mother != null))
                }

                // TIER 2: PRIMARY ROOT & SPOUSE (الزوج والزوجة)
                item {
                    Text(
                        text = "الشخص المنتخب وشريك الحياة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Focus Person prominent Card
                        VerticalFocusedCard(
                            person = focusPerson,
                            borderColor = BronzeGold,
                            badgeText = "بؤرة الشجرة"
                        )

                        if (spouse != null) {
                            // Inline connector visual indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF0F2))
                                        .border(1.dp, Color(0xFFFDA4AF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "الزواج",
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Spouse Card
                            TreeMiniCard(
                                title = "الزوج / الزوجة",
                                person = spouse,
                                indicatorColor = if (spouse.gender == "ذكر") HeritageGreenLight else BronzeGold,
                                onClick = { viewModel.selectPersonForTree(spouse) }
                            )
                        }
                    }
                }

                // SIBLINGS EXPANDABLE TRACK
                if (siblings.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, HeritageGreen.copy(0.12f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "الأخوة والأخوات (${siblings.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = HeritageGreenDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    siblings.forEach { sib ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftSage.copy(0.7f))
                                                .clickable { viewModel.selectPersonForTree(sib) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${sib.firstName} (${sib.gender})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = HeritageGreenDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Connector Downward lines pointing to children
                item {
                    ChildrenConnectorLines(hasChildren = children.isNotEmpty())
                }

                // TIER 3: CHILDREN (الأولاد)
                item {
                    Text(
                        text = "الأبناء الفروع (${children.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (children.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, HeritageGreen.copy(0.08f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لم تسجل فروع أو أبناء في النظام لهذا الفرد.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Display children side-by-side or horizontally
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            children.forEach { child ->
                                Box(modifier = Modifier.padding(6.dp)) {
                                    TreeMiniCard(
                                        title = if (child.gender == "ذكر") "ابن" else "ابنة",
                                        person = child,
                                        indicatorColor = if (child.gender == "ذكر") HeritageGreenLight else BronzeGold,
                                        onClick = { viewModel.selectPersonForTree(child) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Spacer bottom
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// Visual mini card helper inside custom tree graph
@Composable
fun TreeMiniCard(
    title: String,
    person: PersonEntity,
    indicatorColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(135.dp)
            .border(1.dp, indicatorColor.copy(0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(indicatorColor.copy(0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = indicatorColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${person.firstName} ${person.familyName}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = HeritageGreenDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Subinfo alive/location
            Text(
                text = if (person.isAlive) "حي" else "متوفى",
                fontSize = 10.sp,
                color = if (person.isAlive) HeritageGreenLight else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TreeEmptyCard(
    title: String,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(115.dp)
            .border(1.dp, Color.Gray.copy(0.2f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VerticalFocusedCard(
    person: PersonEntity,
    borderColor: Color,
    badgeText: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(155.dp)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(BronzeGold.copy(0.18f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    color = BronzeGold,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            val avatarBg = if (person.gender == "ذكر") SoftSage else Color(0xFFFFF1F2)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBg)
            ) {
                Icon(
                    imageVector = if (person.gender == "ذكر") Icons.Default.Person else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (person.gender == "ذكر") HeritageGreen else BronzeGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = person.getFullName(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = HeritageGreenDark,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (person.birthDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ولد: ${person.birthDate}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Graphic elements for trees
@Composable
fun ParentConnectorLines(hasParents: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        if (hasParents) {
            val width = size.width
            val height = size.height
            
            // Draw connector from parents down
            drawLine(
                color = HeritageGreenLight,
                start = Offset(width / 2.7f, 0f),
                end = Offset(width / 2f, height),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            drawLine(
                color = HeritageGreenLight,
                start = Offset(width / 1.58f, 0f),
                end = Offset(width / 2f, height),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }
    }
}

@Composable
fun ChildrenConnectorLines(hasChildren: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        if (hasChildren) {
            val width = size.width
            val height = size.height
            // Vertical down-pointing centered pointer link
            drawLine(
                color = HeritageGreenLight,
                start = Offset(width / 2f, 0f),
                end = Offset(width / 2f, height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

// Manual compose helper state for Horizontal scroll
@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}
