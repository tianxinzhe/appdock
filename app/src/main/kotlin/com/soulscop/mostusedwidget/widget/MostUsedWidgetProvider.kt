package com.soulscop.mostusedwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.soulscop.mostusedwidget.MostUsedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MostUsedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as MostUsedApp
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            for (id in appWidgetIds) {
                val config = app.configRepository.getWidgetConfig(id)
                val top = app.database.usageCacheDao().getAll().take(config.iconCount)
                val views = WidgetRenderer.build(context, top, config, id)
                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val app = context.applicationContext as MostUsedApp
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
                app.usageStatsRepository.refresh()
                requestUpdateAll(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.soulscop.mostusedwidget.action.REFRESH"

        fun requestUpdateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MostUsedWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, MostUsedWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
