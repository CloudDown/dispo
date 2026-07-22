package com.dispo.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoRepository
import com.dispo.app.ui.ChatPanel
import com.dispo.app.ui.DispoButton
import com.dispo.app.ui.DISPO_BUTTON_FRACTION
import com.dispo.app.ui.HomePanel
import com.dispo.app.ui.LooneyRings
import com.dispo.app.ui.MapScreen
import com.dispo.app.ui.ProfilePanel
import com.dispo.app.ui.STAR_BURST_MS
import kotlinx.coroutines.delay
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
        // Demande le taux de rafraîchissement max de l'écran (90/120 Hz)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes = window.attributes.apply {
                preferredRefreshRate = 120f
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        setContent {
            DispoTheme {
                DispoApp()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DispoApp() {
    val context = LocalContext.current
    val repository = DispoRepository.get(context)
    val state by repository.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabs = listOf("😀 DISPO", "💬 CHAT", "👤 PROFIL")
    val pageTransition = tween<Float>(durationMillis = 550, easing = FastOutSlowInEasing)

    // Carte plein écran par-dessus tout (hors pager : pas de conflit de gestes)
    var mapOpen by remember { mutableStateOf(false) }
    BackHandler(enabled = mapOpen) { mapOpen = false }

    // À chaque ouverture de l'app : état « pas dispo » par défaut
    LaunchedEffect(Unit) {
        repository.resetDispoOnLaunch()
    }

    // Ouvre le chat après le burst d’étoiles (sinon le swipe coupe l’anim)
    val previousUnlocked = remember { mutableStateOf(state.chatUnlocked) }
    LaunchedEffect(state.chatUnlocked) {
        if (state.chatUnlocked && !previousUnlocked.value) {
            if (state.meDispo) delay(STAR_BURST_MS.toLong())
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
            .background(backgroundColor),
    ) {
        // Tornade Looney Tunes plein écran (derrière la barre de statut
        // et les onglets), qui s'estompe quand on glisse vers le chat
        val homeVisibility =
            1f - (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
        if (homeVisibility > 0f) {
            LooneyRings(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = homeVisibility },
            )
        }

        @OptIn(ExperimentalLayoutApi::class)
        val keyboardVisible = WindowInsets.isImeVisible

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // beyondViewport = 0 : la page d'accueil (tornade + ticker LED) est
            // détruite hors écran, sinon ses animations volent des frames au chat.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 0,
            ) { page ->
                when (page) {
                    0 -> {
                        // Effet parallaxe uniquement sur l'accueil pendant le swipe
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
                                },
                        ) {
                            HomePanel(state = state)
                        }
                    }
                    1 -> ChatPanel(
                        state = state,
                        onSend = { text -> repository.sendMessage(text) },
                        onOpenMap = { mapOpen = true },
                    )
                    2 -> ProfilePanel(
                        state = state,
                        onUpdateName = { name ->
                            scope.launch { repository.updateDisplayName(name) }
                        },
                        onPickAvatar = { uri ->
                            scope.launch { repository.setAvatarFromUri(uri) }
                        },
                        onAddFriend = { id ->
                            scope.launch { repository.addFriendById(id) }
                        },
                        onRemoveFriend = { id ->
                            scope.launch { repository.removeFriend(id) }
                        },
                        onCreateGroup = { name ->
                            scope.launch { repository.createGroup(name) }
                        },
                        onJoinGroup = { code ->
                            scope.launch { repository.joinGroupByCode(code) }
                        },
                        onAddFriendToGroup = { groupId, friendId ->
                            scope.launch { repository.addFriendToGroup(groupId, friendId) }
                        },
                        onLeaveGroup = { groupId ->
                            scope.launch { repository.leaveGroup(groupId) }
                        },
                        onClearFeedback = { repository.clearFeedback() },
                    )
                }
            }

            // Masquée quand le clavier est ouvert pour ne pas pousser la barre d'écriture
            if (!keyboardVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
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
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                                color = if (selected) Cream else InkBrown,
                            )
                        }
                    }
                }
            }
        }

        // Bouton calé au centre écran, même repère que le cœur jaune de la
        // tornade. Dessiné AU-DESSUS du pager : sinon le pager (scrollable)
        // intercepte les taps et le bouton ne reçoit jamais le clic.
        if (homeVisibility > 0f) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = homeVisibility },
                contentAlignment = Alignment.Center,
            ) {
                val buttonSize: Dp = minOf(maxWidth, maxHeight) * DISPO_BUTTON_FRACTION
                DispoButton(
                    dispo = state.meDispo,
                    onToggle = {
                        scope.launch {
                            repository.toggleMeDispo()
                            DispoWidget.refreshAll(context)
                        }
                    },
                    size = buttonSize,
                )
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
                onPickLocation = { lat, lon ->
                    repository.sendMessage("On se retrouve ici !", lat = lat, lon = lon)
                    mapOpen = false
                },
                onClose = { mapOpen = false },
            )
        }
    }
}
