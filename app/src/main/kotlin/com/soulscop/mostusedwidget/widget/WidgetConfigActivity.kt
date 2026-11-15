package com.soulscop.mostusedwidget.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.lifecycleScope
import com.soulscop.mostusedwidget.MostUsedApp
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity
import com.soulscop.mostusedwidget.ui.theme.MostUsedWidgetTheme
import com.soulscop.mostusedwidget.ui.widget.WidgetConfigScreen
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val app = application as MostUsedApp

        setContent {
            MostUsedWidgetTheme {
                val isPremium by app.premiumState.isPremium.collectAsState()
                val config by produceState<WidgetConfigEntity?>(initialValue = null) {
                    value = app.configRepository.getWidgetConfig(widgetId)
                }

                config?.let { current ->
                    WidgetConfigScreen(
                        initial = current,
                        isPremium = isPremium,
                        onCancel = { finish() },
                        onSave = { updated ->
                            lifecycleScope.launch {
                                app.configRepository.saveWidgetConfig(updated)
                                MostUsedWidgetProvider.requestUpdateAll(this@WidgetConfigActivity)
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                )
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}