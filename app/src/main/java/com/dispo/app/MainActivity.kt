package com.dispo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dispo.app.core.DispoRepository
import com.dispo.app.ui.ChatPanel
import com.dispo.app.ui.HomePanel
import com.dispo.app.ui.MapPanel
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoTheme
import com.dispo.app.widget.DispoWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DispoTheme {
                DispoApp()
            }
        }
    }
}

@Composable
fun DispoApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = DispoRepository.get(context)
    val state by repository.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabs = listOf("😀 Dispo", "💬 Chat", "🗺️ Carte")

    // Ouvre le chat automatiquement quand il se déverrouille
    val previousUnlocked = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(state.chatUnlocked)
    }
    LaunchedEffect(state.chatUnlocked) {
        if (state.chatUnlocked && !previousUnlocked.value) {
            pagerState.animateScrollToPage(1)
        }
        previousUnlocked.value = state.chatUnlocked
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
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

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Cream,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .background(Cream, RoundedCornerShape(20.dp)),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) },
                )
            }
        }
    }
}
