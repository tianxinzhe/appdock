package com.soulscop.mostusedwidget.domain

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowPackageManager

/**
 * AppInfoResolver 的跨版本兼容性测试。
 *
 * 覆盖的核心版本敏感分支是 [AppInfoResolver.isGame]：
 * - API 26+（Android 8.0）使用 [ApplicationInfo.category] == CATEGORY_GAME 判断游戏；
 * - API 25 及以下退回「应用名是否含 game/游戏」的启发式判断。
 *
 * 通过 @Config(sdk = [...]) 让同一组断言在多个 API level 上运行，
 * 验证两个分支在各版本上都按预期生效。
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AppInfoResolverTest {

    private lateinit var context: Context
    private lateinit var shadowPm: ShadowPackageManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        shadowPm = shadowOf(context.packageManager)
    }

    /**
     * 安装一个模拟应用。category 仅在 API 26+ 才有意义，
     * 因此低版本上不设置该字段（与源码中的版本守卫保持一致，避免 NoSuchFieldError）。
     */
    private fun installPackage(packageName: String, label: String, category: Int) {
        val info = ApplicationInfo()
        info.packageName = packageName
        info.nonLocalizedLabel = label
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info.category = category
        }
        installPackageInfo(packageName, info)
    }

    private fun installPackageInfo(packageName: String, info: ApplicationInfo) {
        val packageInfo = PackageInfo().apply {
            this.packageName = packageName
            this.applicationInfo = info
        }
        shadowPm.installPackage(packageInfo)
    }

    @Test
    @Config(sdk = [26, 28, 33, 34, 35])
    fun isGame_api26Plus_usesCategory() {
        // 标签不含任何游戏关键词，但 category 标记为 GAME -> 应判为游戏
        installPackage("com.example.chess", "Chess Master", ApplicationInfo.CATEGORY_GAME)
        assertTrue(AppInfoResolver(context).isGame("com.example.chess"))
    }

    @Test
    @Config(sdk = [21, 24, 25])
    fun isGame_belowApi26_ignoresCategory_usesLabelHeuristic() {
        // category 虽标记为 GAME，但低版本不看 category，标签无关键词 -> 不应判为游戏
        installPackage("com.example.chess", "Chess Master", ApplicationInfo.CATEGORY_GAME)
        assertFalse(AppInfoResolver(context).isGame("com.example.chess"))
    }

    @Test
    @Config(sdk = [21, 24, 25])
    fun isGame_belowApi26_detectsEnglishKeyword() {
        installPackage("com.example.foo", "My Game Studio", ApplicationInfo.CATEGORY_UNDEFINED)
        assertTrue(AppInfoResolver(context).isGame("com.example.foo"))
    }

    @Test
    @Config(sdk = [21, 24, 25])
    fun isGame_belowApi26_detectsChineseKeyword() {
        installPackage("com.example.foo", "超级游戏", ApplicationInfo.CATEGORY_UNDEFINED)
        assertTrue(AppInfoResolver(context).isGame("com.example.foo"))
    }

    @Test
    @Config(sdk = [28])
    fun isSystemApp_detectsSystemFlag() {
        val info = ApplicationInfo()
        info.packageName = "com.android.systemui"
        info.nonLocalizedLabel = "SystemUI"
        info.flags = ApplicationInfo.FLAG_SYSTEM
        installPackageInfo("com.android.systemui", info)

        assertTrue(AppInfoResolver(context).isSystemApp("com.android.systemui"))
    }

    @Test
    @Config(sdk = [28])
    fun isSystemApp_returnsFalseForNonSystem() {
        installPackage("com.example.app", "Example App", ApplicationInfo.CATEGORY_UNDEFINED)
        assertFalse(AppInfoResolver(context).isSystemApp("com.example.app"))
    }
}
