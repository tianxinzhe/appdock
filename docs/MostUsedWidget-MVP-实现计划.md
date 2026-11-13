# MostUsed Widget — MVP 实现计划

## 1. 概述（Summary）

从零搭建 Android 工具 App「MostUsed Widget」，实现 PRD V1.0（MVP）全部能力：
本地读取系统「使用情况访问」数据 → 计算 App 打开热度 → 桌面小组件展示高频 App 快捷启动。

- **应用 ID / 包名**：`com.soulscop.mostusedwidget`
- **UI 技术栈**：主界面 Jetpack Compose（Material 3）；桌面小组件使用 RemoteViews（XML，平台限制）
- **内购**：定义 `BillingManager` 接口 + 本地桩实现（后续再接入真实 Billing 库）
- **文案/国际化**：MVP 中文单语，全部 UI 文案走 `strings.xml`（不硬编码）；多语言国际化延后至功能完成后
- **交付**：一次性实现完整 MVP，产出可编译的 `assembleDebug` APK
- **合规红线**：不申请 `QUERY_ALL_PACKAGES`，用 `<queries>`（LAUNCHER intent）解决包可见性；全部数据本地存储

## 2. 现状分析（Current State）

- 工作目录 `d:\Project_AI\MostUsedWidget` 为空，非 git 仓库，全新项目。
- 本机环境（已确认）：
  - JDK：OpenJDK 17.0.2
  - Android SDK：`C:\Users\yuan_weiping\AppData\Local\Android\Sdk`（`ANDROID_HOME` 未设全局，构建脚本里用 `local.properties` 指向）
  - platforms：android-28 / 33 / 34 / **36**（无 android-35）
  - build-tools：30.0.3 / 34.0.0 / 35.0.0 / 36.1.0；licenses 已接受
  - Gradle 未装全局 → 使用 Gradle Wrapper（需联网下载依赖）
- 结论：采用 `compileSdk = 36`（本机已装）、`targetSdk = 35`（Android 15，PRD 要求）、`minSdk = 21`。targetSdk 只需 ≤ compileSdk，无需额外下载 android-35 platform。

## 3. 技术选型与版本（决策锁定）

| 项 | 选择 | 说明 |
| --- | --- | --- |
| 语言 | Kotlin | — |
| UI | Jetpack Compose + Material 3 | 主 App；小组件仍用 RemoteViews/XML |
| 构建 | Gradle Wrapper 8.11.1 + AGP 8.9.2 | 支持 compileSdk 36 |
| Kotlin / Compose 编译器 | Kotlin 2.0.21 + `org.jetbrains.kotlin.plugin.compose` 2.0.21 | Kotlin 2.0 起 Compose 用独立插件 |
| Room | 2.6.1 + KSP 2.0.21-1.0.27 | 本地持久化（KSP，非 kapt） |
| Compose BOM | 2024.10.01 | Material3、foundation、ui |
| 生命周期 | lifecycle-runtime-ktx / lifecycle-viewmodel-compose 2.8.7 | — |
| Activity Compose | 1.9.3 | — |
| Core KTX | 1.15.0 | — |
| WorkManager | 2.9.1 | Premium 每日自动刷新 |
| 内购 | 无第三方库 | 接口 + 本地桩 |
| 文案/国际化 | 中文单语（`values/strings.xml`） | 文案不硬编码，国际化延后至功能完成后 |

> 依赖补丁号可在实现时微调（解析失败时），但上述大版本决策保持不变。

## 4. 项目结构（File Tree）

```
d:\Project_AI\MostUsedWidget\
├─ settings.gradle.kts
├─ build.gradle.kts
├─ gradle.properties
├─ local.properties                       # sdk.dir=... 指向本机 SDK
├─ gradle\wrapper\gradle-wrapper.properties
├─ gradlew / gradlew.bat / gradle\wrapper\gradle-wrapper.jar
├─ gradle\libs.versions.toml              # version catalog（可选，统一版本）
└─ app\
   ├─ build.gradle.kts
   └─ src\main\
      ├─ AndroidManifest.xml
      ├─ res\
      │  ├─ layout\widget_most_used.xml           # 小组件 RemoteViews 布局
      │  ├─ layout\widget_item.xml                # 单个 App 图标项
      │  ├─ xml\most_used_widget_info.xml         # appwidget-provider
      │  ├─ xml\file_paths.xml                    # FileProvider（CSV 分享）
      │  ├─ drawable\ic_app_placeholder.xml       # 默认占位图标
      │  ├─ mipmap-*\ic_launcher*                 # 启动图标
      │  └─ values\strings.xml / themes.xml / colors.xml     # 中文默认（国际化延后）
      └─ kotlin\com\soulscop\mostusedwidget\
         ├─ MostUsedApp.kt                         # Application：初始化 DB/仓库
         ├─ MainActivity.kt                        # 入口 + 权限判断路由
         ├─ data\
         │  ├─ local\AppDatabase.kt
         │  ├─ local\UsageCacheEntity.kt
         │  ├─ local\HiddenAppEntity.kt
         │  ├─ local\PinnedAppEntity.kt
         │  ├─ local\SettingsEntity.kt
         │  ├─ local\WidgetConfigEntity.kt
         │  └─ local\dao\*.kt                      # 各 DAO
         ├─ repository\
         │  ├─ UsageStatsRepository.kt             # 聚合 UsageStats + 过滤 + 算分 + 缓存
         │  └─ ConfigRepository.kt                 # 设置/隐藏/置顶/小组件配置读写
         ├─ domain\
         │  ├─ UsageStatsProvider.kt               # 封装 UsageStatsManager/UsageEvents
         │  ├─ AppInfoResolver.kt                  # PackageManager 读名称/图标
         │  ├─ ScoreCalculator.kt                  # 热度公式
         │  └─ AppFilter.kt                        # 黑/白/系统/游戏过滤
         ├─ billing\
         │  ├─ BillingManager.kt                   # 接口
         │  ├─ StubBillingManager.kt               # 本地桩
         │  └─ PremiumState.kt                     # StateFlow<Boolean>
         ├─ widget\
         │  ├─ MostUsedWidgetProvider.kt           # AppWidgetProvider
         │  ├─ WidgetRenderer.kt                   # RemoteViews 构建
         │  └─ WidgetConfigActivity.kt             # 添加小组件时的配置页（Premium 可选）
         ├─ ui\
         │  ├─ theme\Color.kt / Theme.kt / Type.kt
         │  ├─ permission\PermissionGuideScreen.kt
         │  ├─ stats\HeatListScreen.kt + StatsViewModel.kt
         │  ├─ widget\WidgetManageScreen.kt + WidgetPreviewScreen.kt
         │  ├─ settings\SettingsScreen.kt + SettingsViewModel.kt
         │  ├─ settings\PrivacyPolicyScreen.kt
         │  └─ components\*.kt                     # AppRow / EmptyState / PremiumGate
         └─ util\
            ├─ UsageStatsPermission.kt             # AppOps 权限检测/跳转
            ├─ WidgetUtils.kt                      # 图标→Bitmap / 启动 PendingIntent
            ├─ CsvExporter.kt                      # CSV 导出
            └─ SortMode.kt                         # 枚举
```

## 5. 模块实现细节

### 5.1 工程脚手架
- `local.properties`：`sdk.dir=C\:\\Users\\yuan_weiping\\AppData\\Local\\Android\\Sdk`（Windows 路径转义）。
- `settings.gradle.kts`：声明插件仓库（google/mavenCentral/gradlePluginPortal），`include(":app")`。
- `app/build.gradle.kts`：应用 `com.android.application`、`kotlin-android`、`kotlin-compose`、`ksp`。
  - `compileSdk=36`、`minSdk=21`、`targetSdk=35`。
  - `buildFeatures.compose=true`，`kotlinOptions.jvmTarget=17`。
  - 依赖见第 3 节表。
- Gradle Wrapper 用 `gradle-wrapper.properties` 指向 `gradle-8.11.1-bin.zip`；`gradle-wrapper.jar` 通过本机 adb 同目录下若有 gradle 或首次 `gradlew` 自动下载（实现时若缺 jar，用 `gradle wrapper` 或手动下载 jar 补齐）。

### 5.2 数据层（Room）
- `AppDatabase`：单例，包含 5 个 entity 及对应 DAO。
- Entity 设计：
  - `UsageCacheEntity(packageName PK, appLabel, launchCount7d, launchCount30d, foregroundTime7d, foregroundTime30d, lastUsedTime, score, lastUpdated)` — 统计缓存。
  - `HiddenAppEntity(packageName PK)` — 黑名单。
  - `PinnedAppEntity(packageName PK, sortOrder Int)` — 白名单置顶。
  - `SettingsEntity(id=0 固定, isPremium, w1, w2, decayFactorDays, sortMode, excludeSystem, excludeGames, autoRefreshEnabled)` — 全局设置单行。
  - `WidgetConfigEntity(widgetId PK, sortMode, w1, w2, decayFactorDays, iconCount, bgColor, textColor, cornerRadiusDp, iconSizeDp, templateId)` — 每小组件独立配置。
- `ConfigRepository`：对上述 DAO 的封装，暴露 `Flow` 供 Compose 收集；`getWidgetConfig(widgetId)` 缺省时返回默认值（免费 = iconCount 4、默认色、LAUNCH_COUNT、w1=2/w2=0）。

### 5.3 统计计算（核心）
- `UsageStatsProvider`：
  - 权限检测用 `AppOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid, pkg)`（无需任何权限）。
  - 时长：`queryUsageStats(INTERVAL_DAILY, start, end)`，聚合 `totalTimeInForeground`、`lastTimeUsed`。
  - 启动次数：`queryEvents(start, end)`，统计 `UsageEvents.Event.MOVE_TO_FOREGROUND`（API 21+ 兼容）的包名出现次数。
  - 区间：7 天 / 30 天两档。
- `AppInfoResolver`：仅对 UsageStats 返回的包名调用 `PackageManager.getApplicationInfo/getApplicationLabel/getApplicationIcon`；失败时用占位图标 + 包名做 label。系统应用判断用 `ApplicationInfo.FLAG_SYSTEM`；游戏判断用 `ApplicationInfo.category == CATEGORY_GAME`（API 26+，低版本回退到 `FLAG_SYSTEM` + 名称启发式兜底）。
- `ScoreCalculator`（决策锁定公式）：
  - 免费固定：`score = launchCount7d * 2`。
  - Premium 按 `sortMode`：
    - `LAUNCH_COUNT`：`score = w1*launch7d + w2*launch30d + 100*exp(-ageDays/decayFactorDays)`（ageDays = 距 now 的 `lastUsedTime` 天数）。
    - `FOREGROUND_TIME`：`score = foregroundTime7d`（毫秒）。
    - `LAST_USED`：`score = lastUsedTime`（时间戳，越大越新）。
  - `w1/w2/decayFactorDays` 由设置页滑块调整（免费版隐藏）。
- `AppFilter`：剔除「自身包名、黑名单、系统（若 excludeSystem）、游戏（若 excludeGames）」；白名单置顶项按 `sortOrder` 排最前，其余按 score 降序。
- `UsageStatsRepository.refresh()`：串联「读 UsageStats → 过滤 → 算分 → 写 Room 缓存」。触发时机：手动刷新、添加小组件、Premium 定时任务、首次授权成功后。

### 5.4 权限引导
- `UsageStatsPermission`：`hasPermission()` / `openSettings(context)`（`Settings.ACTION_USAGE_ACCESS_SETTINGS`）。
- `MainActivity`：onResume 检测；未授权 → 显示 `PermissionGuideScreen`（说明文案 + 「前往开启权限」+「已开启，重新检测」）；已授权 → 进入主 Tab 页。
- 权限关闭时小组件显示占位文案「Enable usage access in settings」。

### 5.5 桌面小组件
- `res/xml/most_used_widget_info.xml`：`minWidth/minHeight` 对应 4×2，`updatePeriodMillis=0`（避免高频轮询，统一走手动/事件刷新），`previewLayout`，`targetCellWidth/Height`（Android 12+ 规范）。
- `MostUsedWidgetProvider`：`onUpdate` 遍历 widgetId → 从 Room 读 Top N → `WidgetRenderer` 构建 RemoteViews → `updateAppWidget`；`onEnabled/onDisabled/onAppWidgetOptionsChanged` 兜底刷新。
- `WidgetRenderer`：
  - 横向 `LinearLayout` 容器 + 每项 `widget_item.xml`（ImageView 图标 + TextView 名称小字）。
  - 图标用 `RemoteViews.setImageViewBitmap`（drawable→Bitmap，全版本兼容）。
  - 每项 `PendingIntent.getActivity` 用 `packageManager.getLaunchIntentForPackage(pkg)` 启动。
  - 顶部「刷新」按钮 → `PendingIntent.getBroadcast` 自定义 action，接收后调 `refresh()` 再 `onUpdate`。
  - 包失效/图标读取失败 → 占位图标；数量不足 N 时用占位/留空。
- `WidgetConfigActivity`：`appwidget-provider` 声明 `android:configure`；Premium 可改 iconCount/颜色/圆角/图标大小/sortMode，写入 `WidgetConfigEntity`；免费版直接使用默认配置返回。

### 5.6 主界面（Compose，3 Tab）
- `MainActivity` 用 `Scaffold` + `NavigationBar` 三段切换（不引入 navigation 库，用 `when(selectedTab)`）。
- Tab1 榜单：`StatsViewModel` 收集 Room `Flow<List<UsageCacheEntity>>`，`LazyColumn` 展示排名、图标、7天/30天次数、时长；每项长按/菜单提供「隐藏」（免费）与「置顶」（Premium，未解锁弹 PremiumGate）。
- Tab2 小组件管理：`WidgetPreviewScreen` 用静态预览图展示小组件效果 + 「添加小组件」引导说明；Premium 展示多配置入口。
- Tab3 设置：权重滑块（Premium）、排序模式切换（Premium）、批量过滤开关（系统/游戏，Premium）、黑名单管理、清除数据、隐私政策、购买入口（PremiumGate）、关于。
- `PremiumGate` / `PremiumState`：`StubBillingManager` 的 `StateFlow<Boolean>` 驱动功能锁与解锁提示。

### 5.7 内购桩
- `BillingManager` 接口：`val isPremium: StateFlow<Boolean>`、`suspend fun purchase(): Boolean`、`suspend fun restore(): Boolean`。
- `StubBillingManager`：`purchase()` 延迟后把 `SettingsEntity.isPremium=true` 落库并更新 StateFlow；`restore()` 读库刷新；`isPremium` 初值来自 `ConfigRepository`。
- 未来替换为真实 `BillingClient` 时仅需实现同一接口，UI 层零改动。

### 5.8 隐私页 / CSV 导出 / 定时刷新
- `PrivacyPolicyScreen`：内置中文隐私说明 + 外链 `https://soulscop.com`（静态托管，URL 占位，上线前替换为真实地址）。
- `CsvExporter`（Premium）：把当前缓存榜单写 CSV 到 `getExternalFilesDir(DIRECTORY_DOWNLOADS)/mostused_stats.csv`，经 `FileProvider`（`file_paths.xml`）触发 `ACTION_SEND` 分享；不申请存储权限。
- 定时刷新：`RefreshWorker`（`PeriodicWorkRequestBuilder` 24h）读取 `autoRefreshEnabled`，调用 `UsageStatsRepository.refresh()` 并触发小组件更新；在 `MostUsedApp` 用 WorkManager `enqueueUniquePeriodicWork` 登记。

### 5.9 文案与国际化（策略）
- MVP 阶段：**中文单语**（默认），全部 UI 文案（Compose 界面、小组件 RemoteViews 占位/刷新提示、权限引导、隐私政策等）放 `values/strings.xml`，代码/XML 一律 `@string` 引用，禁止硬编码。
- 国际化延后：待所有功能完成并验收通过后，再新增 `values-xx` 目录与语言切换（语言范围届时再定）。因文案已资源化，后续加语言只需补 `values-xx` 目录，无需改代码。
- 本次不实现：`LocaleHelper`、`SettingsEntity.languageCode`、设置页语言切换。

## 6. 假设与决策（Assumptions & Decisions）

1. **包可见性**：不用 `QUERY_ALL_PACKAGES`；在 Manifest 声明 `<queries><intent><action MAIN/><category LAUNCHER/></intent></queries>` 以获得启动器可见性，从而读取名称/图标并 `getLaunchIntentForPackage`。
2. **图标位图**：统一转 Bitmap 用 `setImageViewBitmap`，兼容 minSdk 21。
3. **权限申请**：`PACKAGE_USAGE_STATS` 无法弹窗，一律跳系统设置手动开启；用 AppOps 检测而非查询结果是否为空。
4. **CompileSdk**：用 36（本机已装），targetSdk 35（PRD 要求），不下载 android-35 platform。
5. **存储**：全部 Room，不引入 DataStore；隐私/商店文案按 PRD 第 7 节敏感词规避。
6. **商店对接**：Play 商品 `premium_unlock`、$1.99、Data Safety、隐私外链均为上线期配置项，MVP 代码层面只留接口/文案占位，不在本地环境真实扣费测试。
7. **文案/国际化**：MVP 中文单语，文案全部资源化（`strings.xml`），不硬编码；多语言国际化延后至功能全部完成并验收通过后再做。

## 7. 验收 / 验证步骤（Verification）

1. 构建：`.\gradlew.bat :app:assembleDebug` 成功产出 `app/build/outputs/apk/debug/app-debug.apk`。
2. 静态校验：无 `QUERY_ALL_PACKAGES`；Manifest 含 usage stats 权限、appwidget receiver、`<queries>`；`updatePeriodMillis=0`。
3. 权限流程：未授权进引导页 → 跳系统设置 → 授权后「重新检测」进入主界面；关闭权限后小组件显示占位文案。
4. 统计逻辑：模拟打开若干 App，刷新后榜单排序正确；黑名单隐藏生效；白名单置顶排最前；免费版小组件固定 Top4 且点击可启动。
5. Premium：桩解锁后权重/排序/批量过滤/CSV 导出/定时刷新/小组件自定义（4/6/8、颜色圆角）生效。
6. 健壮性：无使用记录 / 包失效 / 图标失败均有占位，不崩溃。
7. 文案资源化：全部 UI 文案入 `strings.xml`（中文默认），无硬编码字符串（静态扫描），为后续国际化预留结构。

## 8. 实施顺序（Implementation Order）

1. 工程脚手架 + Gradle 配置 + 空壳可编译。
2. 数据层（Room entity/DAO/DB + ConfigRepository）。
3. domain（UsageStatsProvider / AppInfoResolver / ScoreCalculator / AppFilter）+ UsageStatsRepository。
4. 权限引导 + 权限工具 + MainActivity 路由。
5. 桌面小组件（provider/renderer/config + 布局 + manifest）。
6. 主界面 Compose 3 Tab + ViewModel + PremiumGate。
7. 内购桩 + PremiumState 接线。
8. CSV 导出 / 定时刷新 / 隐私页 / 收尾文案。
9. 文案资源化（全部文案入 `strings.xml`，中文，无硬编码）。
10. 构建验证 + 自测清单核对。
