package com.soulscop.mostusedwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import com.soulscop.mostusedwidget.util.UsageStatsPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MostUsedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as MostUsedApp
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            var cache = app.database.usageCacheDao().getAll()
            // 首次添加小组件或缓存为空时主动抓取一次数据
            if (cache.isEmpty() && UsageStatsPermission.hasPermission(context)) {
                app.usageStatsRepository.refresh()
                cache = app.database.usageCacheDao().getAll()
            }

            val settings = app.configRepository.getSettings()
            val hidden = app.configRepository.getHiddenPackages()
            val pinnedOrder = app.configRepository.getPinnedList()
                .associate { it.packageName to it.sortOrder }
            val ordered = cache
                .filter { it.packageName !in hidden }
                .sortedWith(
                    compareByDescending<UsageCacheEntity> { it.packageName in pinnedOrder }
                        .thenBy { pinnedOrder[it.packageName] ?: Int.MAX_VALUE }
                        .thenByDescending { it.score }
                )

            for (id in appWidgetIds) {
                val config = app.configRepository.getWidgetConfig(id)
                val limit = if (settings.isPremium) config.iconCount else minOf(config.iconCount, 4)
                val top = ordered.take(limit)
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