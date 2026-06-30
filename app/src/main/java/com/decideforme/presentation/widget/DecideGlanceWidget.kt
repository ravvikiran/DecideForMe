package com.decideforme.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.decideforme.R
import com.decideforme.presentation.MainActivity

/**
 * Glance-based home screen widget with full Compose UI.
 * One tap = instant decision from the home screen.
 */
class DecideGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                DecideWidgetContent()
            }
        }
    }

    @Composable
    private fun DecideWidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨",
                    style = TextStyle(fontSize = 28.sp)
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Text(
                    text = "Decide For Me",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "Tap for instant decision",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )
            }
        }
    }
}

/**
 * Receiver that tells Android about the Glance widget
 */
class DecideGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DecideGlanceWidget()
}
