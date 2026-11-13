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
        if (!settings.isPremium || !settings.autoRefreshEnabled) return Result.success()
        app.usageStatsRepository.refresh()
        MostUsedWidgetProvider.requestUpdateAll(applicationContext)
        return Result.success()
    }
}
