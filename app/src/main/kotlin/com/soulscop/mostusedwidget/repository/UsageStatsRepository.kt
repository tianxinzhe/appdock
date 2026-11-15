package com.soulscop.mostusedwidget.repository

import android.content.Context
import androidx.room.withTransaction
import com.soulscop.mostusedwidget.data.local.AppDatabase
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import com.soulscop.mostusedwidget.domain.AppFilter
import com.soulscop.mostusedwidget.domain.AppInfoResolver
import com.soulscop.mostusedwidget.domain.ScoreCalculator
import com.soulscop.mostusedwidget.domain.UsageStatsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val provider: UsageStatsProvider,
    private val resolver: AppInfoResolver,
    private val config: ConfigRepository
) {
    suspend fun refresh() = withContext(Dispatchers.IO) {
        if (!provider.hasPermission()) return@withContext
        val raw = provider.collect()
        val settings = config.getSettings()
        val hidden = config.getHiddenPackages()
        val self = context.packageName
        val now = System.currentTimeMillis()

        val entities = raw
            .filter { (pkg, _) ->
                AppFilter.shouldInclude(pkg, self, hidden, settings, resolver::isSystemApp, resolver::isGame)
            }
            .map { (pkg, u) ->
                UsageCacheEntity(
                    packageName = pkg,
                    appLabel = resolver.resolveLabel(pkg),
                    launchCount7d = u.launchCount7d,
                    launchCount30d = u.launchCount30d,
                    foregroundTime7d = u.foregroundTime7d,
                    foregroundTime30d = u.foregroundTime30d,
                    lastUsedTime = u.lastUsedTime,
                    score = ScoreCalculator.calculate(
                        u.launchCount7d, u.launchCount30d, u.foregroundTime7d, u.lastUsedTime, now, settings
                    ),
                    lastUpdated = now
                )
            }

        // FIX #10: replace-all + delete-diff inside one transaction so UI flows
        // never observe an empty intermediate state (no list flash).
        db.withTransaction {
            db.usageCacheDao().upsertAll(entities)
            db.usageCacheDao().deleteNotIn(entities.map { it.packageName })
        }
    }
}
