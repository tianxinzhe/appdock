package com.soulscop.mostusedwidget.domain

import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.util.SortMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    private val now = 1_700_000_000_000L
    private val dayMs = 24L * 60 * 60 * 1000

    @Test
    fun freeFormula_usesLaunch7dTimesTwo() {
        val settings = SettingsEntity(isPremium = false)
        val score = ScoreCalculator.calculate(
            launch7d = 5, launch30d = 100, foregroundTime7d = 999,
            lastUsedTime = now, now = now, settings = settings
        )
        assertEquals(10.0, score, 0.0001)
    }

    @Test
    fun premiumLaunchCount_combinesWeightsAndDecay() {
        val settings = SettingsEntity(
            isPremium = true,
            w1 = 2f, w2 = 0.5f, decayFactorDays = 7f,
            sortMode = SortMode.LAUNCH_COUNT.name
        )
        // lastUsedTime == now => ageDays = 0 => exp(0) = 1 => decay term = 100
        val score = ScoreCalculator.calculate(
            launch7d = 10, launch30d = 20, foregroundTime7d = 0,
            lastUsedTime = now, now = now, settings = settings
        )
        assertEquals(2f * 10 + 0.5f * 20 + 100.0, score, 0.0001)
    }

    @Test
    fun premiumForegroundTime_usesForegroundTime7d() {
        val settings = SettingsEntity(isPremium = true, sortMode = SortMode.FOREGROUND_TIME.name)
        val score = ScoreCalculator.calculate(
            launch7d = 1, launch30d = 2, foregroundTime7d = 5000,
            lastUsedTime = now, now = now, settings = settings
        )
        assertEquals(5000.0, score, 0.0001)
    }

    @Test
    fun premiumLastUsed_usesLastUsedTimestamp() {
        val settings = SettingsEntity(isPremium = true, sortMode = SortMode.LAST_USED.name)
        val lastUsed = 1_234_567_890L
        val score = ScoreCalculator.calculate(
            launch7d = 1, launch30d = 2, foregroundTime7d = 3,
            lastUsedTime = lastUsed, now = now, settings = settings
        )
        assertEquals(lastUsed.toDouble(), score, 0.0001)
    }

    @Test
    fun decayTerm_decreasesWithAge() {
        val fresh = SettingsEntity(
            isPremium = true, w1 = 0f, w2 = 0f, decayFactorDays = 7f,
            sortMode = SortMode.LAUNCH_COUNT.name
        )
        val scoreNow = ScoreCalculator.calculate(0, 0, 0, now, now, fresh)
        val score7Days = ScoreCalculator.calculate(0, 0, 0, now - 7 * dayMs, now, fresh)

        assertEquals(100.0, scoreNow, 0.0001)
        // exp(-7/7) = exp(-1) ≈ 0.3679 => 100 * 0.3679 ≈ 36.79
        assertEquals(100.0 * Math.exp(-1.0), score7Days, 0.01)
    }
}
