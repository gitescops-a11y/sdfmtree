package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PersonEntity
import com.example.ui.theme.BronzeGold
import com.example.ui.theme.DesertIvory
import com.example.ui.theme.HeritageGreen
import com.example.ui.theme.HeritageGreenDark
import com.example.ui.theme.DarkCharcoal

@Composable
fun DashboardScreen(
    viewModel: FamilyViewModel,
    allPeople: List<PersonEntity>
) {
    // 1. Calculate Statistics
    val totalCount = allPeople.size
    val maleCount = allPeople.count { it.gender == "ذكر" }
    val femaleCount = allPeople.count { it.gender == "أنثى" }
    
    val totalBankCapital = allPeople.sumOf { it.bankBalance }
    val averageBalance = if (totalCount > 0) totalBankCapital / totalCount else 0.0
    
    val marriedCount = allPeople.count { it.marriageDate != null && it.divorceDate == null }
    val divorcedCount = allPeople.count { it.divorceDate != null }
    val militaryCount = allPeople.count { it.militaryId != null && it.militaryId!!.isNotBlank() }
    
    // Population by City
    val cities = listOf("الدامر", "شندي", "عطبرة", "بربر", "أبو حمد", "المتمة")
    val cityDistribution = cities.associateWith { city ->
        allPeople.count { it.birthPlace.contains(city) }
    }
    val maxCityValue = cityDistribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DesertIvory)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // State Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HeritageGreenDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ولاية نهر النيل • لوحة المعلومات والتحليلات الجغرافية",
                        color = BronzeGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الرؤية الرقمية الاستباقية لهيكلة المقيدات الفيدرالية والإحصاء السكاني",
                        color = Color.White.copy(0.75f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Summary Cards Grid (2 columns)
        item {
            Text(
                text = "المؤشرات المدنية العامة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HeritageGreenDark,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي النسمة المسجلة",
                    value = "$totalCount فرد",
                    icon = Icons.Default.Person,
                    accentColor = HeritageGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "متوسط الأرصدة المصرفية",
                    value = "${String.format("%,.0f", averageBalance)} ج.س",
                    icon = Icons.Default.AccountBox,
                    accentColor = BronzeGold
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "مجمل الأصول البنكية",
                    value = "${String.format("%,.0f", totalBankCapital)} ج.س",
                    icon = Icons.Default.Lock,
                    accentColor = BronzeGold
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "القوة العسكرية النشطة",
                    value = "$militaryCount مقاتل",
                    icon = Icons.Default.Star,
                    accentColor = HeritageGreenDark
                )
            }
        }

        // Advanced Demographics (Gender & Marital State)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HeritageGreen.copy(0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التوزيع الديموغرافي والجغرافي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Male vs Female proportion bar
                    Text(
                        text = "النوع الاجتماعي: ذكور ($maleCount) مقابل إناث ($femaleCount)",
                        fontSize = 11.sp,
                        color = DarkCharcoal,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val maleRatio = if (totalCount > 0) maleCount.toFloat() / totalCount else 0.5f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9ECEF))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(maleRatio.coerceAtLeast(0.01f))
                                    .background(HeritageGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((1f - maleRatio).coerceAtLeast(0.01f))
                                    .background(BronzeGold)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Marital ratio bar
                    val totalMarital = (marriedCount + divorcedCount).coerceAtLeast(1)
                    val marriedRatio = marriedCount.toFloat() / totalMarital
                    Text(
                        text = "الحالة القانونية: متزوجون ($marriedCount) مقابل مطلقون ($divorcedCount)",
                        fontSize = 11.sp,
                        color = DarkCharcoal,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9ECEF))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(marriedRatio.coerceAtLeast(0.01f))
                                    .background(Color(0xFF2E7D32))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((1f - marriedRatio).coerceAtLeast(0.01f))
                                    .background(Color(0xFFC62828))
                            )
                        }
                    }
                }
            }
        }

        // Custom Visual Column Bar Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HeritageGreen.copy(0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التعداد السكاني للمدن الكبرى بنهر النيل",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeritageGreenDark
                    )
                    Text(
                        text = "مؤشر يعكس عدد المقيدين لكل بلدية بالولاية",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Interactive Canvas Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val widthSpace = size.width / cities.size
                        val chartHeight = size.height - 90f
                        
                        cities.forEachIndexed { index, city ->
                            val count = cityDistribution[city] ?: 0
                            val barRatio = count.toFloat() / maxCityValue
                            val barHeight = chartHeight * barRatio
                            
                            val xPos = widthSpace * index + (widthSpace - 64f) / 2
                            val yPos = chartHeight - barHeight
                            
                            // Draw the bar shadow container background
                            drawRoundRect(
                                color = Color(0xF5EAEAEC),
                                topLeft = Offset(xPos, 0f),
                                size = Size(64f, chartHeight),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            
                            // Draw active data column bar
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(HeritageGreen, HeritageGreenDark)
                                ),
                                topLeft = Offset(xPos, yPos),
                                size = Size(64f, barHeight),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            
                            // Draw value tag on top of bar if count > 0
                            if (count > 0) {
                                drawCircle(
                                    color = BronzeGold,
                                    radius = 8f,
                                    center = Offset(xPos + 32f, yPos + 18f)
                                )
                            }
                        }
                    }
                    
                    // City Names Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        cities.forEach { city ->
                            val count = cityDistribution[city] ?: 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(city, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HeritageGreenDark)
                                Text("$count نسمة", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, HeritageGreen.copy(0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accentColor.copy(0.12f))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoal,
                maxLines = 1
            )
        }
    }
}
