package com.soulscop.mostusedwidget.domain

import com.soulscop.mostusedwidget.data.local.SettingsEntity

object AppFilter {
    fun shouldInclude(
        packageName: String,
        selfPackage: String,
        hidden: Set<String>,
        settings: SettingsEntity,
        isSystem: (String) -> Boolean,
        isGame: (String) -> Boolean
    ): Boolean {
        if (packageName == selfPackage) return false
        if (packageName in hidden) return false
        if (settings.excludeSystem && isSystem(packageName)) return false
        if (settings.excludeGames && isGame(packageName)) return false
        return true
    }
}
