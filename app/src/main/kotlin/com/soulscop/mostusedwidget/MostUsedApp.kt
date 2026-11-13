package com.soulscop.mostusedwidget

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.soulscop.mostusedwidget.billing.BillingManager
import com.soulscop.mostusedwidget.billing.PremiumState
import com.soulscop.mostusedwidget.billing.StubBillingManager
import com.soulscop.mostusedwidget.data.local.AppDatabase
import com.soulscop.mostusedwidget.domain.AppInfoResolver
import com.soulscop.mostusedwidget.domain.UsageStatsProvider
import com.soulscop.mostusedwidget.repository.ConfigRepository
import com.soulscop.mostusedwidget.repository.UsageStatsRepository
import com.soulscop.mostusedwidget.util.RefreshWorker
import java.util.concurrent.TimeUnit

class MostUsedApp : Application() {
    lateinit var database: AppDatabase
    lateinit var configRepository: ConfigRepository
    lateinit var usageStatsRepository: UsageStatsRepository
    lateinit var premiumState: PremiumState
    lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        configRepository = ConfigRepository(database)
        val provider = UsageStatsProvider(this)
        val resolver = AppInfoResolver(this)
        usageStatsRepository = UsageStatsRepository(this, database, provider, resolver, configRepository)
        premiumState = PremiumState(configRepository)
        billingManager = StubBillingManager(configRepository, premiumState)

        val request = PeriodicWorkRequestBuilder<RefreshWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_refresh", ExistingPeriodicWorkPolicy.KEEP, request
        )
    }
}
