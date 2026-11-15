package com.soulscop.mostusedwidget.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.widget.MostUsedWidgetProvider

class RefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MostUsedApp
        val settings = app.configRepository.getSettings()
        // FIX #8: free users' widgets never refreshed before. Free users get
        // a daily baseline refresh; premium users control it via the toggle.
        val shouldRefresh = !settings.isPremium || settings.autoRefreshEnabled
        if (shouldRefresh) {
            app.usageStatsRepository.refresh()
            MostUsedWidgetProvider.requestUpdateAll(applicationContext)
        }
        return Result.success()
    }
}
