package com.example.mobileappprojectvocab

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.util.Calendar

class VocabularyWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_SHUFFLE = "com.example.mobileappprojectvocab.ACTION_SHUFFLE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SHUFFLE) {
            val dataManager = DataManager(context)
            val currentOffset = dataManager.getWidgetOffset()
            dataManager.saveWidgetOffset(currentOffset + 1)
            
            // Update all widgets
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VocabularyWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int) {
        
        val dataManager = DataManager(context)
        val words = dataManager.loadWords() ?: emptyList()
        val offset = dataManager.getWidgetOffset()
        
        val views = RemoteViews(context.packageName, R.layout.vocabulary_widget)
        
        if (words.isNotEmpty()) {
            // Daily index + manual shuffle offset
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val index = (dayOfYear + offset) % words.size
            val word = words[index]
            
            views.setTextViewText(R.id.widget_word, word.word)
            views.setTextViewText(R.id.widget_translation, word.translation)
            
            if (!word.pos.isNullOrBlank()) {
                views.setViewVisibility(R.id.widget_pos, View.VISIBLE)
                views.setTextViewText(R.id.widget_pos, word.pos)
            } else {
                views.setViewVisibility(R.id.widget_pos, View.GONE)
            }
        } else {
            views.setTextViewText(R.id.widget_word, "No words")
            views.setTextViewText(R.id.widget_translation, "Open app to add words")
            views.setViewVisibility(R.id.widget_pos, View.GONE)
        }

        // Setup Shuffle Intent
        val shuffleIntent = Intent(context, VocabularyWidget::class.java).apply {
            action = ACTION_SHUFFLE
        }
        val shufflePendingIntent = PendingIntent.getBroadcast(
            context, 0, shuffleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_shuffle, shufflePendingIntent)

        // Setup App Launch Intent (Click anywhere on the widget container)
        val launchIntent = Intent(context, MainActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, launchPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
