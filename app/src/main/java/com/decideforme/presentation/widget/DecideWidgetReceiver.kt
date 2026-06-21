package com.decideforme.presentation.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.decideforme.R

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
            val views = RemoteViews(context.packageName, R.layout.widget_decide)
            views.setTextViewText(R.id.widget_title, "Decide For Me")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
