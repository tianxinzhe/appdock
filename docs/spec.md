# MostUsed Widget — 开发规格说明书（Spec）

> 版本：V1.0（MVP）｜包名：`com.soulscop.mostusedwidget`｜语言：中文（默认），国际化延后

## 1. 产品定位与目标

- **定位**：本地统计 App 使用热度 + 桌面小组件快捷启动的 Android 工具 App。
- **口号**：Privacy-first local app usage widget | No cloud, no ads。
- **目标用户**：海外 Android 效率用户、桌面洁癖者、折腾玩家、不想换 Launcher、注重隐私/反感广告者。
- **商业模式**：免费基础版 + 一次性买断高级版（`premium_unlock`，$1.99）。

## 2. 范围

### 2.1 范围内（In Scope）
1. 权限引导（使用情况访问权限）。
2. 本地热度统计（7 天/30 天启动次数、前台时长、最近打开）。
3. 桌面小组件（免费 Top4，Premium 4/6/8 + 外观自定义 + 多配置）。
4. 热度榜单、黑名单隐藏、白名单置顶（Premium）。
5. 设置（权重/排序/批量过滤/清除数据/隐私/购买/关于）。
6. 内购桩（接口 + 本地 mock，预留真实 Billing）。
7. CSV 导出（Premium）、每日自动刷新（Premium）。
8. 内置隐私政策页 + 外链。
9. 文案资源化：全部 UI 文案走 `strings.xml`（中文默认），代码不硬编码；国际化（多语言）延后至功能全部完成后再做。

### 2.2 范围外（Out of Scope）
- 不修改/不移动/不排序系统桌面图标，不改桌面布局。
- 不做云端同步、账号、数据上传。
- 不做家长管控、监控、远程查看。
- 不做广告/追踪 SDK。
- 不支持鸿蒙（仅 Google Play Android）。
- 真实 Billing 扣费接入（本次仅接口 + 桩，商店配置属上线期）。

## 3. 非功能约束与合规

- `minSdk=21`（Android 5.0，UsageStats 最低），`targetSdk=35`（Android 15），`compileSdk=36`。
- **不申请 `QUERY_ALL_PACKAGES`**；用 `<queries>`（MAIN+LAUNCHER intent）解决包可见性。
- 数据全部本地（Room），不上传任何服务器。
- 小组件 `updatePeriodMillis=0`，仅手动/事件刷新，控制耗电。
- 后台任务适配电池优化（可选引导，不强求）。
- 商店文案规避 `auto arrange desktop icons / track / monitor` 等敏感词。

## 4. 权限

| 权限 | 用途 | 备注 |
| --- | --- | --- |
| `PACKAGE_USAGE_STATS` | 读取使用记录，统计热度 | 特殊权限，跳系统设置手动开 |
| `PackageManager` | 按包名读名称/图标 | 不遍历全部 App |
| `AppWidget` | 小组件注册/渲染/刷新/点击 | 标准 API |
| 本地存储 | Room 保存统计/配置/黑白名单 | 仅本地 |

## 5. 技术栈（决策锁定）

| 项 | 选择 |
| --- | --- |
| 语言 | Kotlin |
| 主界面 UI | Jetpack Compose + Material 3 |
| 小组件 | RemoteViews（XML，平台限制） |
| 构建 | Gradle Wrapper 8.11.1 + AGP 8.9.2 |
| Kotlin/Compose | Kotlin 2.0.21 + compose 插件 2.0.21 |
| 持久化 | Room 2.6.1 + KSP 2.0.21-1.0.27 |
| Compose BOM | 2024.10.01 |
| 生命周期/Activity/Core | 2.8.7 / 1.9.3 / 1.15.0 |
| 后台 | WorkManager 2.9.1 |
| 内购 | 接口 + 本地桩（无第三方库） |
| 文案/国际化 | 中文单语（`values/strings.xml`），国际化延后 | 文案不硬编码，预留多语言结构 |

## 6. 系统架构与模块

```
MostUsedApp(Application) → 初始化 Room + 仓库 + PremiumState + WorkManager
MainActivity → 权限路由 → 主界面 Scaffold(3 Tab)
   ├─ Tab1 榜单     HeatListScreen + StatsViewModel
   ├─ Tab2 小组件   WidgetManageScreen / WidgetPreviewScreen
   └─ Tab3 设置     SettingsScreen + SettingsViewModel

数据层：UsageStatsProvider → AppInfoResolver → AppFilter → ScoreCalculator → UsageStatsRepository → Room
小组件：MostUsedWidgetProvider → WidgetRenderer(RemoteViews) → Room 读 Top N
```

## 7. 数据模型（Room，5 张表）

| 表 | 字段 | 说明 |
| --- | --- | --- |
| `usage_cache` | packageName(PK), appLabel, launchCount7d, launchCount30d, foregroundTime7d, foregroundTime30d, lastUsedTime, score, lastUpdated | 统计缓存 |
| `hidden_app` | packageName(PK) | 黑名单 |
| `pinned_app` | packageName(PK), sortOrder | 白名单置顶 |
| `settings` | id=0, isPremium, w1, w2, decayFactorDays, sortMode, excludeSystem, excludeGames, autoRefreshEnabled, languageCode | 全局设置单行 |
| `widget_config` | widgetId(PK), sortMode, w1, w2, decayFactorDays, iconCount, bgColor, textColor, cornerRadiusDp, iconSizeDp, templateId | 每小组件配置 |

`languageCode` 为空 = 跟随系统；`sortMode` ∈ `LAUNCH_COUNT / FOREGROUND_TIME / LAST_USED`。

## 8. 核心业务逻辑

### 8.1 统计读取
- 权限检测：`AppOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS)`。
- 时长：`queryUsageStats(INTERVAL_DAILY, 30d)` 聚合 `totalTimeInForeground`、`lastTimeUsed`。
- 启动次数：`queryEvents(7d/30d)` 统计 `MOVE_TO_FOREGROUND` 事件。

### 8.2 热度公式
- 免费固定：`score = launchCount7d * 2`。
- Premium 按 `sortMode`：
  - `LAUNCH_COUNT`：`score = w1*launch7d + w2*launch30d + 100*exp(-ageDays/decayFactorDays)`
  - `FOREGROUND_TIME`：`score = foregroundTime7d`
  - `LAST_USED`：`score = lastUsedTime`
- `w1/w2/decayFactorDays` 由滑块调整（Premium）。

### 8.3 过滤与排序
1. 剔除自身包名、黑名单、系统（excludeSystem）、游戏（excludeGames）。
2. 白名单置顶按 `sortOrder` 排最前，其余按 score 降序。
3. 写 Room 缓存；UI/小组件读缓存。

### 8.4 刷新触发
- 手动点击刷新；添加小组件时；Premium 每日定时（WorkManager 24h）；首次授权成功后。

## 9. 功能规格（免费 / Premium）

| 模块 | 免费版 | Premium |
| --- | --- | --- |
| 权限引导 | ✅ | ✅ |
| 热度统计（固定公式） | ✅（近7天×2） | ✅（可自定义） |
| 小组件 | Top4 固定样式 | 4/6/8 + 颜色/圆角/图标大小/布局模板 + 多配置 |
| 榜单 | 查看 + 隐藏 | + 置顶 |
| 批量过滤 | ❌ | 一键屏蔽系统/游戏 |
| 排序模式 | ❌ | 次数/时长/最近 |
| 数据导出 | ❌ | CSV |
| 自动刷新 | ❌ | 每日定时 |
| 文案资源化（中文） | ✅ | ✅ |

## 10. UI/UX 规范
- Material 3，低饱和度，适配暗/亮主题。
- 小组件简洁、文字少、快速启动优先。
- 无弹窗广告/推送。
- 无权限/无数据/加载失败均有友好占位，不空白不崩溃。

## 11. 文案与国际化（策略）
- MVP 阶段：**中文单语**（默认），全部 UI 文案（Compose 界面、小组件 RemoteViews 占位/刷新提示、权限引导、隐私政策等）放 `values/strings.xml`，代码/XML 一律 `@string` 引用，禁止硬编码。
- 国际化延后：待所有功能完成并验收通过后，再新增 `values-xx` 目录与语言切换。语言范围届时再定（此前曾列 10 语：en/es/de/fr/pt/it/ja/ko/zh-rCN/zh-rTW，可作参考）。
- 本次不实现：`LocaleHelper`、`SettingsEntity.languageCode`、设置页语言切换。

## 12. 验收标准（Definition of Done）
1. `assembleDebug` 成功产出 APK。
2. 无 `QUERY_ALL_PACKAGES`；Manifest 权限/receiver/`<queries>` 正确；`updatePeriodMillis=0`。
3. 权限流程正常，关闭权限后小组件显示占位提示。
4. 统计正确，黑名单生效，白名单置顶，免费 Top4 小组件可点击启动。
5. Premium 桩解锁后全部高级项生效；恢复购买（桩）可读回状态。
6. 无崩溃、不疯狂耗电。
7. 全部文案已资源化（中文默认），无硬编码字符串，为后续国际化预留结构。
