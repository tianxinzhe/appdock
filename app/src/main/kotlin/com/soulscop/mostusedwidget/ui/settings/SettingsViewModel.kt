package com.soulscop.mostusedwidget.ui.settings

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.util.CsvExporter
import com.soulscop.mostusedwidget.widget.MostUsedWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as MostUsedApp

    val settings: StateFlow<SettingsEntity> = app.configRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    val premium: StateFlow<Boolean> = app.premiumState.isPremium

    val hiddenApps: StateFlow<List<String>> = app.configRepository.hiddenApps
        .map { list -> list.map { it.packageName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var refreshJob: Job? = null

    /**
     * FIX #9: don't start Activities from a ViewModel with an application context
     * (background-start restrictions). The UI observes this and launches the chooser.
     */
    val csvShareEvent = MutableStateFlow<Intent?>(null)

    fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        viewModelScope.launch {
            app.configRepository.saveSettings(transform(app.configRepository.getSettings()))
            scheduleRefresh()
        }
    }

    fun unhide(packageName: String) = viewModelScope.launch {
        app.configRepository.unhideApp(packageName)
        scheduleRefresh()
    }

    fun clearData() = viewModelScope.launch {
        app.configRepository.clearUsageData()
        MostUsedWidgetProvider.requestUpdateAll(getApplication())
    }

    fun purchase() = viewModelScope.launch {
        if (app.billingManager.purchase()) {
            app.usageStatsRepository.refresh()
            MostUsedWidgetProvider.requestUpdateAll(getApplication())
        }
    }

    /**
     * 设置变更会改变过滤/算分结果，做短延迟合并后再重算，避免滑块拖动时频繁刷新。
     */
    private fun scheduleRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            delay(600)
            app.usageStatsRepository.refresh()
            MostUsedWidgetProvider.requestUpdateAll(getApplication())
        }
    }

    fun restore() = viewModelScope.launch { app.billingManager.restore() }

    fun exportCsv() = viewModelScope.launch {
        val ctx = getApplication<Application>()
        val items = app.database.usageCacheDao().getAll()
        val uri = CsvExporter.export(ctx, items)
        if (uri != null) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            csvShareEvent.value = Intent.createChooser(send, null)
        }
    }

    fun onCsvShareConsumed() { csvShareEvent.value = null }
}
