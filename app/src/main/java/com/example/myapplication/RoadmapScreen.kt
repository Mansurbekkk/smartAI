package com.example.myapplication

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RoadmapStep(
    val stepNumber: Int,
    val topic: String,
    val type: String,   // "Quiz" | "Tutorial" | "AI_Chat"
    val isComplete: Boolean,
    val isCurrent: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(isTeens: Boolean = false, onBack: () -> Unit = {}) {
    val steps = listOf(
        RoadmapStep(1, "Manfiy sonlar takrorlash",  "Quiz",     isComplete = true,  isCurrent = false),
        RoadmapStep(2, "Qavslarni ochish",           "Tutorial", isComplete = true,  isCurrent = false),
        RoadmapStep(3, "Ko'paytuvchilarga ajratish", "AI_Chat",  isComplete = false, isCurrent = true),
        RoadmapStep(4, "Diskriminant formulasi",     "Tutorial", isComplete = false, isCurrent = false),
        RoadmapStep(5, "Kvadrat tenglamalar",        "Quiz",     isComplete = false, isCurrent = false),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O'quv Xaritasi", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                RoadmapHeaderCard(completedCount = 2, totalCount = steps.size, isTeens = isTeens)
                Spacer(modifier = Modifier.height(24.dp))
            }
            itemsIndexed(steps) { index, step ->
                RoadmapStepItem(
                    step   = step,
                    isLast = index == steps.lastIndex,
                    isTeens = isTeens
                )
            }
        }
    }
}

@Composable
fun RoadmapHeaderCard(completedCount: Int, totalCount: Int, isTeens: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text       = if (isTeens) "Learning Roadmap" else "🗺️ Mening Xaritam",
                fontWeight = FontWeight.Bold, fontSize = 20.sp,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Kvadrat tenglamalar — $completedCount/$totalCount",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress  = { completedCount.toFloat() / totalCount },
                modifier  = Modifier.fillMaxWidth().height(10.dp),
                color     = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun RoadmapStepItem(step: RoadmapStep, isLast: Boolean, isTeens: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    step.isComplete -> {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            tint     = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    step.isCurrent -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                            label = "alpha"
                        )
                        Surface(
                            shape    = CircleShape,
                            color    = primaryColor.copy(alpha = alpha),
                            modifier = Modifier.size(36.dp)
                        ) {}
                        Text(
                            "${step.stepNumber}",
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    else -> {
                        Surface(
                            shape    = CircleShape,
                            color    = outlineColor,
                            modifier = Modifier.size(36.dp)
                        ) {}
                        Text(
                            "${step.stepNumber}",
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            if (!isLast) {
                Canvas(modifier = Modifier.width(2.dp).height(48.dp)) {
                    drawLine(
                        color       = outlineColor,
                        start       = Offset(size.width / 2, 0f),
                        end         = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap         = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Step card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    step.isCurrent  -> MaterialTheme.colorScheme.primaryContainer
                    step.isComplete -> MaterialTheme.colorScheme.surfaceVariant
                    else            -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            border = if (step.isCurrent) CardDefaults.outlinedCardBorder() else null
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        step.topic,
                        fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val typeLabel = when (step.type) {
                        "Quiz"     -> "📝 Viktorina"
                        "Tutorial" -> "📖 Dars"
                        "AI_Chat"  -> "🤖 AI Chat"
                        else       -> step.type
                    }
                    Text(
                        typeLabel, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                if (step.isCurrent) {
                    Icon(
                        Icons.Default.PlayArrow, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
