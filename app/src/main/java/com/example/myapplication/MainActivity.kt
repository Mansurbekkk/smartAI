package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

enum class Screen { Home, Scanner, Chat, Roadmap, Parent }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isTeens by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf(Screen.Home) }

            MyApplicationTheme(isTeens = isTeens) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartScholarApp(
                        isTeens = isTeens,
                        currentScreen = currentScreen,
                        onScreenChange = { currentScreen = it },
                        onToggleMode = { isTeens = !isTeens }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Navigation scaffold
// ---------------------------------------------------------------------------
@Composable
fun SmartScholarApp(
    isTeens: Boolean,
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    onToggleMode: () -> Unit
) {
    data class NavItem(val screen: Screen, val label: String, val icon: ImageVector)

    val navItems = listOf(
        NavItem(Screen.Home,    "Bosh sahifa", Icons.Default.Home),
        NavItem(Screen.Scanner, "Skaner",      Icons.Default.DocumentScanner),
        NavItem(Screen.Chat,    "AI Chat",     Icons.Default.AutoAwesome),
        NavItem(Screen.Roadmap, "Xarita",      Icons.Default.Map),
        NavItem(Screen.Parent,  "Ota-ona",     Icons.Default.SupervisorAccount),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentScreen == item.screen,
                        onClick  = { onScreenChange(item.screen) },
                        icon     = { Icon(item.icon, contentDescription = item.label) },
                        label    = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedContent(targetState = currentScreen, label = "nav") { screen ->
                when (screen) {
                    Screen.Home    -> HomeScreen(
                        isTeens      = isTeens,
                        onToggleMode = onToggleMode,
                        onNavigate   = { onScreenChange(it) }
                    )
                    Screen.Scanner -> ScannerScreen(onBack = { onScreenChange(Screen.Home) })
                    Screen.Chat    -> ChatScreen(isTeens = isTeens, onBack = { onScreenChange(Screen.Home) })
                    Screen.Roadmap -> RoadmapScreen(isTeens = isTeens, onBack = { onScreenChange(Screen.Home) })
                    Screen.Parent  -> ParentPortalScreen(onBack = { onScreenChange(Screen.Home) })
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Home Screen
// ---------------------------------------------------------------------------
@Composable
fun HomeScreen(
    isTeens: Boolean,
    onToggleMode: () -> Unit,
    onNavigate: (Screen) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isTeens) "Akademik Markaz" else "Salom, O'quvchi!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isTeens) "Bugungi rejangizni ko'ring" else "Bugun nima o'rganamiz?",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onToggleMode) {
                Icon(
                    imageVector = if (isTeens) Icons.Default.Face else Icons.Default.School,
                    contentDescription = "Rejimni o'zgartirish",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MainActionCard(
                title    = if (isTeens) "Scan & Solve" else "Skaner",
                icon     = Icons.Default.DocumentScanner,
                modifier = Modifier.weight(1f),
                onClick  = { onNavigate(Screen.Scanner) }
            )
            MainActionCard(
                title    = if (isTeens) "Socratic AI" else "AI Do'st",
                icon     = Icons.Default.AutoAwesome,
                modifier = Modifier.weight(1f),
                onClick  = { onNavigate(Screen.Chat) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Roadmap shortcut card
        ElevatedCard(
            onClick  = { onNavigate(Screen.Roadmap) },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Map, null,
                        tint     = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = if (isTeens) "Learning Roadmap" else "Mening Xaritam",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "3 ta qadam qoldi",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text     = if (isTeens) "Mahorat darajasi" else "Mening Progressim",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        RoadmapItem("Matematika: Ko'paytirish",    0.7f, isTeens)
        RoadmapItem("Ingliz tili: Present Simple", 0.4f, isTeens)
    }
}

// ---------------------------------------------------------------------------
// MainActionCard
// ---------------------------------------------------------------------------
@Composable
fun MainActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        onClick  = onClick,
        modifier = modifier.height(160.dp),
        shape    = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = title,
                modifier           = Modifier.size(48.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// RoadmapItem (progress bar row used on Home screen)
// ---------------------------------------------------------------------------
@Composable
fun RoadmapItem(title: String, progress: Float, isTeens: Boolean) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress    = { progress },
            modifier    = Modifier.fillMaxWidth().height(8.dp),
            color       = if (isTeens) MaterialTheme.colorScheme.secondary
                          else MaterialTheme.colorScheme.primary,
            strokeCap   = StrokeCap.Round
        )
    }
}