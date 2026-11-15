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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    private val permissionGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionGranted.value = UsageStatsPermission.hasPermission(this)
        if (permissionGranted.value) refreshIfEmpty(force = false)
        setContent {
            MostUsedWidgetTheme {
                if (permissionGranted.value) {
                    MainScreen()
                } else {
                    PermissionGuideScreen(onCheck = {
                        permissionGranted.value = UsageStatsPermission.hasPermission(this)
                    })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val wasGranted = permissionGranted.value
        val nowGranted = UsageStatsPermission.hasPermission(this)
        permissionGranted.value = nowGranted
        // 首次授权成功后立即抓取一次统计数据
        if (!wasGranted && nowGranted) refreshIfEmpty(force = true)
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
    var selectedTab by remember { mutableIntStateOf(0) }
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
