package com.soulscop.mostusedwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import com.soulscop.mostusedwidget.util.UsageStatsPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MostUsedWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.soulscop.mostusedwidget.action.REFRESH"

        /**
         * Single shared scope on Dispatchers.Default.
         * A new unmanaged CoroutineScope(Main.immediate) per broadcast leaked
         * jobs and ran DB/PM work on the main thread (ANR risk).
         */
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Re-render when the widget is resized (size options changed).
     * NOTE: widget-visibility events are NOT part of the public API; the
     * AppWidgetHostView#onVisibilityAggregatorChanged path is hidden and
     * unreliable across OEMs, so visibility-driven refresh is deliberately
     * not implemented. Refresh happens on: add/update broadcasts, the
     * in-widget refresh button, app open, and the daily Worker.
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Only trust refresh broadcasts that explicitly target this app.
            // A third-party app could otherwise forge ACTION_REFRESH.
            ACTION_REFRESH -> {
                val senderIsSelf = intent.component?.packageName == context.packageName
                if (!senderIsSelf) return
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val app = context.applicationContext as MostUsedApp
                        app.usageStatsRepository.refresh()
                        requestUpdateAll(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    private suspend fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val app = context.applicationContext as MostUsedApp
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
