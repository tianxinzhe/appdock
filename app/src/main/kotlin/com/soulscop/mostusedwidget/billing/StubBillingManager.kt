package com.soulscop.mostusedwidget.billing

import com.soulscop.mostusedwidget.repository.ConfigRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

class StubBillingManager(
    private val configRepository: ConfigRepository,
    private val premiumState: PremiumState
) : BillingManager {
    override val isPremium: StateFlow<Boolean> = premiumState.isPremium

    override suspend fun purchase(): Boolean {
        delay(400)
        configRepository.setPremium(true)
        return true
    }

    override suspend fun restore(): Boolean {
        return configRepository.getSettings().isPremium
    }
}
