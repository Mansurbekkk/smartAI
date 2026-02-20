package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ota-ona Paneli 👨‍👩‍👧", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Child summary card
            item { ChildSummaryCard() }

            // Quick stats row
            item {
                Text("Haftalik Ko'rsatkichlar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(weeklyStats) { stat ->
                        StatChip(stat)
                    }
                }
            }

            // Subject breakdown
            item {
                Text("Fanlar bo'yicha Mahorat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            items(subjectProgress) { subject ->
                SubjectProgressRow(subject)
            }

            // Safety section
            item { SafetyCard() }

            // Achievements
            item {
                Text("Yutuqlar 🏆", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recentBadges) { badge ->
                        BadgeCard(badge)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// Child Summary
// ------------------------------------------------------------------
@Composable
fun ChildSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null,
                    tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
            }
            Column {
                Text("Alibek Toshmatov", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("8-sinf • Kids rejimi", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null,
                        tint = Color(0xFFF5A623), modifier = Modifier.size(18.dp))
                    Text(" 7 kunlik seriya!", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// Stats
// ------------------------------------------------------------------
data class StatItem(val label: String, val value: String, val icon: ImageVector, val color: Color)

val weeklyStats = listOf(
    StatItem("O'rganish", "4.5 soat", Icons.Default.Schedule, Color(0xFF4A90E2)),
    StatItem("Quiz", "23 ta", Icons.Default.HelpOutline, Color(0xFF7ED321)),
    StatItem("Diqqat", "78%", Icons.Default.Visibility, Color(0xFFF5A623)),
    StatItem("XP", "+450", Icons.Default.Star, Color(0xFF9C27B0)),
)

@Composable
fun StatChip(stat: StatItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = stat.color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).width(90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(stat.icon, null, tint = stat.color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(stat.value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = stat.color)
            Text(stat.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }
    }
}

// ------------------------------------------------------------------
// Subject Progress
// ------------------------------------------------------------------
data class SubjectItem(val name: String, val mastery: Float, val trend: String)

val subjectProgress = listOf(
    SubjectItem("Matematika", 0.72f, "↑+5%"),
    SubjectItem("Ingliz tili", 0.55f, "↑+2%"),
    SubjectItem("Fizika",     0.38f, "↓-1%"),
    SubjectItem("Biologiya",  0.80f, "→ 0%"),
)

@Composable
fun SubjectProgressRow(subject: SubjectItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(subject.name, modifier = Modifier.width(100.dp), fontWeight = FontWeight.Medium)
        LinearProgressIndicator(
            progress = { subject.mastery },
            modifier = Modifier.weight(1f).height(10.dp),
            color = when {
                subject.mastery > 0.7f -> Color(0xFF4CAF50)
                subject.mastery > 0.4f -> Color(0xFFF5A623)
                else                   -> Color(0xFFE53935)
            },
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "${(subject.mastery * 100).toInt()}%",
            fontWeight = FontWeight.Bold, fontSize = 13.sp,
            modifier = Modifier.width(36.dp)
        )
        Text(
            subject.trend,
            fontSize = 11.sp,
            color = if (subject.trend.startsWith("↑")) Color(0xFF4CAF50) else
                    if (subject.trend.startsWith("↓")) Color(0xFFE53935) else
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

// ------------------------------------------------------------------
// Safety / Content Card
// ------------------------------------------------------------------
@Composable
fun SafetyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Shield, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp))
            Column {
                Text("Xavfsizlik Holati", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text("Barcha suhbatlar filterlangan ✅\nOg'ir kontent topilmadi", fontSize = 13.sp,
                    color = Color(0xFF388E3C))
            }
        }
    }
}

// ------------------------------------------------------------------
// Badges
// ------------------------------------------------------------------
data class BadgeItem(val emoji: String, val title: String)

val recentBadges = listOf(
    BadgeItem("🔍", "Birinchi Skan"),
    BadgeItem("🔥", "3-kun Seriya"),
    BadgeItem("⭐", "Yarim Ustoz"),
    BadgeItem("📝", "Quiz Masteri"),
)

@Composable
fun BadgeCard(badge: BadgeItem) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.size(80.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(badge.emoji, fontSize = 28.sp)
            Text(badge.title, fontSize = 9.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
