package com.soulscop.mostusedwidget.ui.settings

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.util.CsvExporter
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

    fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        viewModelScope.launch {
            app.configRepository.saveSettings(transform(app.configRepository.getSettings()))
        }
    }

    fun unhide(packageName: String) = viewModelScope.launch { app.configRepository.unhideApp(packageName) }

    fun clearData() = viewModelScope.launch { app.configRepository.clearUsageData() }

    fun purchase() = viewModelScope.launch { app.billingManager.purchase() }

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
            val chooser = Intent.createChooser(send, ctx.getString(R.string.settings_export_csv))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { ctx.startActivity(chooser) }
            Toast.makeText(ctx, R.string.settings_export_done, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, R.string.settings_export_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
