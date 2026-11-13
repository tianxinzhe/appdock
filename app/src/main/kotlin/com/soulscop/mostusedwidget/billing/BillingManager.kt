package com.soulscop.mostusedwidget.billing

import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val isPremium: StateFlow<Boolean>
    suspend fun purchase(): Boolean
    suspend fun restore(): Boolean
}
