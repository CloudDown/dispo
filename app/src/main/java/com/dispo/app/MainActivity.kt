package com.dispo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoRepository
import com.dispo.app.ui.ChatPanel
import com.dispo.app.ui.HomePanel
import com.dispo.app.ui.MapPanel
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.CircusRedDark
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoTheme
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.widget.DispoWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DispoTheme {
                DispoApp()
            }
        }
    }
}

@Composable
fun DispoApp() {
    val context = LocalContext.current
    val repository = DispoRepository.get(context)
    val state by repository.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabs = listOf("😀 DISPO", "💬 CHAT", "🗺️ CARTE")

    // Ouvre le chat automatiquement quand il se déverrouille
    val previousUnlocked = remember { mutableStateOf(state.chatUnlocked) }
    LaunchedEffect(state.chatUnlocked) {
        if (state.chatUnlocked && !previousUnlocked.value) {
            pagerState.animateScrollToPage(1)
        }
        previousUnlocked.value = state.chatUnlocked
    }

    // Le fond suit la page : rouge Looney Tunes sur l'accueil, crème ailleurs
    val backgroundColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 0) CircusRedDark else Cream,
        animationSpec = tween(400),
        label = "pageBackground",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .safeDrawingPadding(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (page) {
                0 -> HomePanel(
                    state = state,
                    onToggle = {
                        scope.launch {
                            repository.toggleMeDispo()
                            DispoWidget.refreshAll(context)
                        }
                    },
                )
                1 -> ChatPanel(
                    state = state,
                    onSend = { text -> repository.sendMessage(text) },
                    onShareLocation = {
                        // MVP : partage un lieu fixe de démo (centre-ville).
                        // À remplacer par la position réelle / un picker.
                        repository.sendMessage(
                            "On se retrouve ici !",
                            lat = 45.5088,
                            lon = -73.5617,
                        )
                        scope.launch { pagerState.animateScrollToPage(2) }
                    },
                )
                2 -> MapPanel(state = state)
            }
        }

        // Barre d'onglets façon fanions de cirque
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(Cream, RoundedCornerShape(22.dp))
                .border(3.dp, InkBrown, RoundedCornerShape(22.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = pagerState.currentPage == index
                val tabColor by animateColorAsState(
                    targetValue = if (selected) CircusRed else Cream,
                    animationSpec = tween(250),
                    label = "tabColor",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(tabColor, RoundedCornerShape(16.dp))
                        .then(
                            if (selected) {
                                Modifier.border(2.5.dp, InkBrown, RoundedCornerShape(16.dp))
                            } else Modifier
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                        color = if (selected) Cream else InkBrown,
                    )
                }
            }
        }
    }
}
