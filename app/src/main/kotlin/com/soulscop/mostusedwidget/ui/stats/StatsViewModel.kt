package com.soulscop.mostusedwidget.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HeatItem(
    val entity: UsageCacheEntity,
    val isPinned: Boolean
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as MostUsedApp

    val settings: StateFlow<SettingsEntity> = app.configRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    val premium: StateFlow<Boolean> = app.premiumState.isPremium

    val isRefreshing = MutableStateFlow(false)

    val heatList: StateFlow<List<HeatItem>> = combine(
        app.database.usageCacheDao().observeAll(),
        app.configRepository.hiddenApps,
        app.configRepository.pinnedApps
    ) { cache, hidden, pinned ->
        val hiddenSet = hidden.map { it.packageName }.toSet()
        val pinnedMap = pinned.associate { it.packageName to it.sortOrder }
        cache.filter { it.packageName !in hiddenSet }
            .map { HeatItem(it, it.packageName in pinnedMap) }
            .sortedWith(
                compareByDescending<HeatItem> { it.isPinned }
                    .thenBy { pinnedMap[it.entity.packageName] ?: Int.MAX_VALUE }
                    .thenByDescending { it.entity.score }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            app.usageStatsRepository.refresh()
            isRefreshing.value = false
        }
    }

    fun hide(packageName: String) = viewModelScope.launch { app.configRepository.hideApp(packageName) }

    fun pin(packageName: String) = viewModelScope.launch {
        val order = app.configRepository.getPinnedList().size
        app.configRepository.pinApp(packageName, order)
    }

    fun unpin(packageName: String) = viewModelScope.launch { app.configRepository.unpinApp(packageName) }

    fun purchase() = viewModelScope.launch { app.billingManager.purchase() }
}
