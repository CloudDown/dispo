package com.dispo.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dispo.app.core.DispoRepository

/**
 * Widget écran d'accueil : le gros bouton Dispo en version miniature.
 * Un tap = toggle, l'emoji et le texte reflètent l'état.
 */
class DispoWidget : GlanceAppWidget() {

    companion object {
        suspend fun refreshAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val widget = DispoWidget()
            manager.getGlanceIds(DispoWidget::class.java).forEach { id ->
                widget.update(context, id)
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = DispoRepository.get(context)
        val dispo = repository.meDispo

        provideContent {
            GlanceTheme {
                WidgetContent(dispo = dispo)
            }
        }
    }

    @Composable
    private fun WidgetContent(dispo: Boolean) {
        val cream = Color(0xFFFFF8E7)
        val red = Color(0xFFE63946)
        val green = if (dispo) Color(0xFF2ECC71) else Color(0xFF1E8E4E)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(cream))
                .cornerRadius(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(72.dp)
                    .background(ColorProvider(green))
                    .cornerRadius(36.dp)
                    .clickable(actionRunCallback<ToggleDispoAction>()),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (dispo) "😀" else "😒",
                    style = TextStyle(fontSize = 32.sp),
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = if (dispo) "Dispo !" else "Pas dispo",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorProvider(red),
                ),
            )
        }
    }
}

class ToggleDispoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        DispoRepository.get(context).toggleMeDispo()
        DispoWidget().update(context, glanceId)
    }
}

class DispoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DispoWidget()
}
