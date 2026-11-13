package com.soulscop.mostusedwidget.domain

import com.soulscop.mostusedwidget.data.local.SettingsEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFilterTest {

    private val self = "com.self.app"
    private val hidden = setOf("com.hidden.app")

    @Test
    fun excludesSelfPackage() {
        val settings = SettingsEntity()
        assertFalse(AppFilter.shouldInclude(self, self, emptySet(), settings, { false }, { false }))
    }

    @Test
    fun excludesHiddenPackage() {
        val settings = SettingsEntity()
        assertFalse(AppFilter.shouldInclude("com.hidden.app", self, hidden, settings, { false }, { false }))
    }

    @Test
    fun excludesSystemWhenEnabled() {
        val settings = SettingsEntity(excludeSystem = true)
        assertFalse(AppFilter.shouldInclude("com.sys.app", self, emptySet(), settings, { true }, { false }))
    }

    @Test
    fun excludesGameWhenEnabled() {
        val settings = SettingsEntity(excludeGames = true)
        assertFalse(AppFilter.shouldInclude("com.game.app", self, emptySet(), settings, { false }, { true }))
    }

    @Test
    fun includesNormalPackage() {
        val settings = SettingsEntity()
        assertTrue(AppFilter.shouldInclude("com.normal.app", self, emptySet(), settings, { false }, { false }))
    }

    @Test
    fun includesSystemWhenNotExcluded() {
        val settings = SettingsEntity(excludeSystem = false)
        assertTrue(AppFilter.shouldInclude("com.sys.app", self, emptySet(), settings, { true }, { false }))
    }
}
