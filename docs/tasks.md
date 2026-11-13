# MostUsed Widget — 实施任务清单（Tasks）

> 顺序即依赖顺序；每任务「完成标准」满足后才进入下一任务。包名 `com.soulscop.mostusedwidget`，代码根目录 `app/src/main/kotlin/com/soulscop/mostusedwidget/`。

## 阶段 A：工程脚手架
- [ ] **T1 工程骨架**
  - 文件：`settings.gradle.kts`、`build.gradle.kts`、`gradle.properties`、`local.properties`、`gradle/libs.versions.toml`、`gradle/wrapper/*`、`gradlew*`、`app/build.gradle.kts`、`AndroidManifest.xml`（空壳）、`MostUsedApp.kt`、`MainActivity.kt`（空壳 Compose）、`res/values/strings.xml`/`themes.xml`/`colors.xml`、mipmap 图标。
  - 完成标准：`.\gradlew.bat :app:assembleDebug` 成功；`sdk.dir` 指向 `C:\Users\yuan_weiping\AppData\Local\Android\Sdk`；compileSdk 36 / minSdk 21 / targetSdk 35。

## 阶段 B：数据层
- [ ] **T2 Room 数据层**
  - 文件：`data/local/AppDatabase.kt`、5 个 Entity、`data/local/dao/*.kt`、`repository/ConfigRepository.kt`。
  - 完成标准：实体与表结构符合 spec §7；DAO 可 CRUD；`ConfigRepository` 暴露 `Flow`；`getWidgetConfig(widgetId)` 返回默认值（免费：iconCount 4、默认色、LAUNCH_COUNT、w1=2/w2=0）。

## 阶段 C：统计核心
- [ ] **T3 统计读取 + 算分 + 过滤**
  - 文件：`domain/UsageStatsProvider.kt`、`domain/AppInfoResolver.kt`、`domain/ScoreCalculator.kt`、`domain/AppFilter.kt`、`util/SortMode.kt`、`repository/UsageStatsRepository.kt`。
  - 完成标准：7d/30d 启动次数与前台时长可正确聚合；公式符合 spec §8.2；过滤与置顶排序符合 §8.3；`refresh()` 全链路可写缓存。

## 阶段 D：权限引导
- [ ] **T4 权限引导 + 路由**
  - 文件：`util/UsageStatsPermission.kt`、`ui/permission/PermissionGuideScreen.kt`、改 `MainActivity.kt` 路由。
  - 完成标准：未授权进引导页 → 跳 `ACTION_USAGE_ACCESS_SETTINGS` → 「重新检测」；已授权进主界面；AppOps 检测正确。

## 阶段 E：桌面小组件
- [ ] **T5 小组件**
  - 文件：`widget/MostUsedWidgetProvider.kt`、`widget/WidgetRenderer.kt`、`widget/WidgetConfigActivity.kt`、`util/WidgetUtils.kt`、`res/layout/widget_most_used.xml`、`res/layout/widget_item.xml`、`res/xml/most_used_widget_info.xml`、`res/drawable/ic_app_placeholder.xml`、Manifest 注册 receiver + `<queries>`。
  - 完成标准：4×2 布局；读 Top N 渲染图标+名称；点击启动；刷新按钮广播；无权限/图标失败占位；`updatePeriodMillis=0`；`android:configure` 指向配置页。

## 阶段 F：主界面
- [ ] **T6 Compose 三 Tab + ViewModel**
  - 文件：`ui/theme/*`、`ui/components/*`（AppRow/EmptyState/PremiumGate）、`ui/stats/HeatListScreen.kt`+`StatsViewModel.kt`、`ui/widget/WidgetManageScreen.kt`+`WidgetPreviewScreen.kt`、`ui/settings/SettingsScreen.kt`+`SettingsViewModel.kt`、`ui/settings/PrivacyPolicyScreen.kt`。
  - 完成标准：三 Tab 切换；榜单展示/隐藏/置顶；设置页各开关与滑块；PremiumGate 拦截未解锁功能。

## 阶段 G：内购桩
- [ ] **T7 内购桩 + PremiumState**
  - 文件：`billing/BillingManager.kt`、`billing/StubBillingManager.kt`、`billing/PremiumState.kt`。
  - 完成标准：`isPremium` StateFlow 驱动功能锁；`purchase()`/`restore()` 落库 `settings.isPremium`；UI 层零耦合（面向接口）。

## 阶段 H：导出 / 定时 / 文案资源化
- [ ] **T8 CSV 导出 + 定时刷新**
  - 文件：`util/CsvExporter.kt`、`res/xml/file_paths.xml`、`RefreshWorker`（WorkManager）、`MostUsedApp` 登记唯一周期任务。
  - 完成标准：CSV 写入 `getExternalFilesDir(DIRECTORY_DOWNLOADS)` 并经 FileProvider 分享；每日 `enqueueUniquePeriodicWork` 生效（Premium 且 autoRefreshEnabled）。

- [ ] **T9 文案资源化（中文）**
  - 文件：`res/values/strings.xml`（中文默认）；所有 Compose/XML 文案改为 `@string`，禁止硬编码。
  - 完成标准：全部 UI 文案入 `strings.xml`（中文）；无硬编码字符串；为后续国际化预留结构（届时加 `values-xx` 即可，无需改代码）。

## 阶段 I：验证
- [ ] **T10 构建与自测**
  - 完成标准：`assembleDebug` 通过；按 `checklist.md` 全项核对通过。
