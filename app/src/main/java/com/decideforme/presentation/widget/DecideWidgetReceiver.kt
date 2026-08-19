package com.decideforme.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.decideforme.R
import com.decideforme.presentation.MainActivity

/**
 * Widget that allows one-tap decisions from the home screen.
 * Tapping opens the app and triggers an instant decision.
 */
class DecideWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_decide)
            views.setTextViewText(R.id.widget_title, "Decide For Me")
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
