package com.soulscop.mostusedwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.lifecycleScope
import com.soulscop.mostusedwidget.ui.permission.PermissionGuideScreen
import com.soulscop.mostusedwidget.ui.settings.SettingsScreen
import com.soulscop.mostusedwidget.ui.stats.HeatListScreen
import com.soulscop.mostusedwidget.ui.theme.MostUsedWidgetTheme
import com.soulscop.mostusedwidget.ui.widget.WidgetManageScreen
import com.soulscop.mostusedwidget.util.UsageStatsPermission
import com.soulscop.mostusedwidget.widget.MostUsedWidgetProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // FIX #5: re-check on every ON_RESUME, refresh data after permission granted
        setContent {
            MostUsedWidgetTheme {
                var permissionGranted by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf(UsageStatsPermission.hasPermission(this))
                }
                var lastGranted by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf(UsageStatsPermission.hasPermission(this))
                }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    permissionGranted = UsageStatsPermission.hasPermission(this@MainActivity)
                    // First grant → fetch data immediately
                    if (permissionGranted && !lastGranted) refreshIfEmpty(force = true)
                    lastGranted = permissionGranted
                }
                if (permissionGranted) {
                    MainScreen()
                } else {
                    PermissionGuideScreen(
                        onCheck = { permissionGranted = UsageStatsPermission.hasPermission(this) }
                    )
                }
            }
        }
    }

    private fun refreshIfEmpty(force: Boolean) {
        val app = application as MostUsedApp
        lifecycleScope.launch {
            if (force || app.database.usageCacheDao().getAll().isEmpty()) {
                app.usageStatsRepository.refresh()
                MostUsedWidgetProvider.requestUpdateAll(this@MainActivity)
            }
        }
    }
}

@Composable
fun MainScreen() {
    // FIX #17: rememberSaveable survives configuration changes (rotation)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_stats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_widget)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_settings)) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> HeatListScreen()
                1 -> WidgetManageScreen()
                else -> SettingsScreen()
            }
        }
    }
}
