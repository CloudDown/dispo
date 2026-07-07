package com.dispo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoRepository
import com.dispo.app.ui.ChatPanel
import com.dispo.app.ui.HomePanel
import com.dispo.app.ui.MapScreen
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.CircusRedDark
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoTheme
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.widget.DispoWidget
import kotlin.math.absoluteValue
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

    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs = listOf("😀 DISPO", "💬 CHAT")
    val pageTransition = tween<Float>(durationMillis = 550, easing = FastOutSlowInEasing)

    // Carte plein écran par-dessus tout (hors pager : pas de conflit de gestes)
    var mapOpen by remember { mutableStateOf(false) }
    BackHandler(enabled = mapOpen) { mapOpen = false }

    // Ouvre le chat automatiquement quand il se déverrouille
    val previousUnlocked = remember { mutableStateOf(state.chatUnlocked) }
    LaunchedEffect(state.chatUnlocked) {
        if (state.chatUnlocked && !previousUnlocked.value) {
            pagerState.animateScrollToPage(1, animationSpec = pageTransition)
        }
        previousUnlocked.value = state.chatUnlocked
    }

    // Le fond suit la page : rouge Looney Tunes sur l'accueil, crème ailleurs
    val backgroundColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 0) CircusRedDark else Cream,
        animationSpec = tween(400),
        label = "pageBackground",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .safeDrawingPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1,
            ) { page ->
                // Parallaxe + fondu + léger zoom pendant le swipe
                val pageOffset =
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        .absoluteValue
                        .coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val visibility = 1f - pageOffset
                            alpha = 0.35f + 0.65f * visibility
                            val scale = 0.92f + 0.08f * visibility
                            scaleX = scale
                            scaleY = scale
                            translationX = pageOffset * size.width * 0.08f *
                                if (page < pagerState.currentPage) -1f else 1f
                        },
                ) {
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
                                mapOpen = true
                            },
                            onOpenMap = { mapOpen = true },
                        )
                    }
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
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        index,
                                        animationSpec = pageTransition,
                                    )
                                }
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

        // Carte plein écran par-dessus le pager et les onglets
        AnimatedVisibility(
            visible = mapOpen,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.92f, animationSpec = tween(250)),
        ) {
            MapScreen(
                pins = state.messages.filter { it.hasLocation },
                onClose = { mapOpen = false },
            )
        }
    }
}
