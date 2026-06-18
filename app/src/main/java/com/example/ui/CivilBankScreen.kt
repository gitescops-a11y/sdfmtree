package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivilBankScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    var selectedCitizenForBank by remember { mutableStateOf<PersonEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var transactionAmountString by remember { mutableStateOf("") }
    
    // Transfer mode states
    var isTransferMode by remember { mutableStateOf(false) }
    var transferReceiverCitizen by remember { mutableStateOf<PersonEntity?>(null) }
    var showTransferDropdown by remember { mutableStateOf(false) }

    // Grant Feedback State
    var showGrantSuccess by remember { mutableStateOf(false) }

    val filteredCitizens = remember(allPeople, searchQuery) {
        allPeople.filter {
            searchQuery.isEmpty() || it.getFullName().contains(searchQuery, ignoreCase = true) ||
            it.bankAccountNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banking Institution Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = HeritageGreenDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "البنك العقاري المركزي الفيدرالي",
                            color = BronzeGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "نظام النقد الموحد لولاية نهر النيل الرقمية",
                            color = Color.White.copy(0.7f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Author credit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BronzeGold.copy(0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "تصميم وبرمجة: المؤلف حمزة العجلابي",
                                color = BronzeGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "قفل مالي مأمون",
                        tint = BronzeGold,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Selected Client Details Card
        item {
            Column {
                Text(
                    text = "الحساب المفتوح النشط",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                if (selectedCitizenForBank == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HeritageGreen.copy(0.15f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(30.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = "اختر عميلاً",
                                tint = BronzeGold.copy(0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "الرجاء اختيار أحد مواطني الولاية بالأسفل لبدء إدارة وتحديث حسابه المصرفي",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val client = allPeople.find { it.id == selectedCitizenForBank!!.id } ?: selectedCitizenForBank!!
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HeritageGreen.copy(0.2f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Client Card top row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = client.getFullName(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "الرقم الوطني الموحد: ${client.getDisplayNationalNumber()}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "رقم المعاملة المصرفية: ${client.bankAccountNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BronzeGold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(HeritageGreen.copy(0.12f))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "رمز العميل",
                                        tint = HeritageGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BronzeGold.copy(0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Client balance block
                            Text(
                                text = "الرصيد الدائري المتوفر",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${String.format("%,.2f", client.bankBalance)} ج.س",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGreen
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Transaction Control Panel
                            OutlinedTextField(
                                value = transactionAmountString,
                                onValueChange = { transactionAmountString = it },
                                label = { Text("مبلغ الحركة المصرفية (جنيه سوداني)") },
                                placeholder = { Text("مثال: 50000") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HeritageGreen,
                                    unfocusedBorderColor = HeritageGreen.copy(0.3f),
                                    focusedLabelColor = HeritageGreen,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Buttons row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        val amt = transactionAmountString.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            viewModel.depositToAccount(client.id, amt)
                                            transactionAmountString = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGreen, contentColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("إيــداع", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        val amt = transactionAmountString.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            viewModel.withdrawFromAccount(client.id, amt)
                                            transactionAmountString = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828), contentColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("سحــب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        isTransferMode = !isTransferMode
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BronzeGold, contentColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تحويل رصيد", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // STATE WELFARE GRANT BUTTON - Brand New Feature!
                            Button(
                                onClick = {
                                    viewModel.disburseStateGrant(client.id, 50000.0)
                                    showGrantSuccess = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HeritageGreenDark,
                                    contentColor = BronzeGold
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BronzeGold, RoundedCornerShape(10.dp))
                            ) {
                                ImageVectorIcon(imageVector = Icons.Default.Star, tint = BronzeGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صرف منحة دعم أسر ولاية نهر النيل (50,000 ج.س)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            AnimatedVisibility(visible = showGrantSuccess) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HeritageGreen.copy(0.12f)),
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HeritageGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تم صرف منحة الدعم الفيدرالي الاستثنائي بنجاح وإيداعها في الحساب المالي للمواطن!",
                                            fontSize = 11.sp,
                                            color = HeritageGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // Transfer Option Reveal
                            AnimatedVisibility(visible = isTransferMode) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    HorizontalDivider(color = BronzeGold.copy(0.15f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "اختيار المستلم من قائمة السجل",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Box {
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
                                                text = transferReceiverCitizen?.getFullName() ?: "انقر هنا لتحديد المستلم المعتمد",
                                                fontSize = 12.sp
                                            )
                                        }
                                        
                                        DropdownMenu(
                                            expanded = showTransferDropdown,
                                            onDismissRequest = { showTransferDropdown = false }
                                        ) {
                                            allPeople.filter { it.id != client.id }.forEach { recv ->
                                                DropdownMenuItem(
                                                    text = { Text(recv.getFullName(), color = MaterialTheme.colorScheme.onSurface) },
                                                    onClick = {
                                                        transferReceiverCitizen = recv
                                                        showTransferDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Button(
                                        onClick = {
                                            val amt = transactionAmountString.toDoubleOrNull() ?: 0.0
                                            val recv = transferReceiverCitizen
                                            if (amt > 0 && recv != null) {
                                                viewModel.transferBetweenAccounts(client.id, recv.id, amt)
                                                transactionAmountString = ""
                                                transferReceiverCitizen = null
                                                isTransferMode = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = HeritageGreenDark,
                                            contentColor = Color.White
                                        ),
                                        enabled = transferReceiverCitizen != null,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تأكيد تحويل الأموال", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clients Directory Listing
        item {
            Column {
                Text(
                    text = "دليل حسابات مواطني الولاية",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث عن مواطن / رقم الحساب...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HeritageGreen,
                        unfocusedBorderColor = HeritageGreen.copy(0.3f),
                        focusedLabelColor = HeritageGreen
                    )
                )
            }
        }

        items(filteredCitizens) { citizen ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedCitizenForBank = citizen
                        showGrantSuccess = false
                    }
                    .border(
                        width = if (selectedCitizenForBank?.id == citizen.id) 2.dp else 1.dp,
                        color = if (selectedCitizenForBank?.id == citizen.id) HeritageGreen else HeritageGreen.copy(0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = citizen.getFullName(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "الحساب: ${citizen.bankAccountNumber}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "النطاق السكني: ${citizen.villageName.ifBlank { "المحمية" }}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "${String.format("%,.0f", citizen.bankBalance)} ج.س",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreen
                    )
                }
            }
        }
    }
}

@Composable
fun ImageVectorIcon(imageVector: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Icon(imageVector = imageVector, contentDescription = null, tint = tint, modifier = modifier)
}
