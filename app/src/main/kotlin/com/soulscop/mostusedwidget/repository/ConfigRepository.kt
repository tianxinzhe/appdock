package com.soulscop.mostusedwidget.repository

import com.soulscop.mostusedwidget.data.local.AppDatabase
import com.soulscop.mostusedwidget.data.local.HiddenAppEntity
import com.soulscop.mostusedwidget.data.local.PinnedAppEntity
import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConfigRepository(private val db: AppDatabase) {
    val settingsFlow: Flow<SettingsEntity> = db.settingsDao().observe().map { it ?: SettingsEntity() }
    val hiddenApps: Flow<List<HiddenAppEntity>> = db.hiddenAppDao().observeAll()
    val pinnedApps: Flow<List<PinnedAppEntity>> = db.pinnedAppDao().observeAll()

    suspend fun getSettings(): SettingsEntity = db.settingsDao().get() ?: SettingsEntity()
    suspend fun saveSettings(settings: SettingsEntity) = db.settingsDao().upsert(settings)
    suspend fun setPremium(premium: Boolean) = db.settingsDao().upsert(getSettings().copy(isPremium = premium))

    suspend fun hideApp(packageName: String) = db.hiddenAppDao().insert(HiddenAppEntity(packageName))
    suspend fun unhideApp(packageName: String) = db.hiddenAppDao().delete(packageName)
    suspend fun pinApp(packageName: String, order: Int) = db.pinnedAppDao().upsert(PinnedAppEntity(packageName, order))
    suspend fun unpinApp(packageName: String) = db.pinnedAppDao().delete(packageName)
    suspend fun getHiddenPackages(): Set<String> = db.hiddenAppDao().getAll().map { it.packageName }.toSet()
    suspend fun getPinnedList(): List<PinnedAppEntity> = db.pinnedAppDao().getAll()

    suspend fun getWidgetConfig(widgetId: Int): WidgetConfigEntity =
        db.widgetConfigDao().get(widgetId) ?: WidgetConfigEntity(widgetId = widgetId)
    suspend fun saveWidgetConfig(config: WidgetConfigEntity) = db.widgetConfigDao().upsert(config)

    suspend fun clearUsageData() = db.usageCacheDao().clear()
}
