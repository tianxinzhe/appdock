# AppDock 测试文档（Testing）

> 本文档描述 AppDock（MostUsed Widget）MVP 的单元测试范围、用例与结果。

## 1. 概述

- 测试对象：`com.soulscop.mostusedwidget` 的核心业务逻辑。
- 测试类型：**JVM 单元测试**（`app/src/test/java/`，本地运行，不依赖真机/模拟器）。
- 测试框架：JUnit 4（`junit:junit:4.13.2`）。
- 运行命令：`.\gradlew.bat :app:testDebugUnitTest`。

## 2. 测试范围

| 模块 | 文件 | 可测性 | 说明 |
| --- | --- | --- | --- |
| 热度公式 | `domain/ScoreCalculator.kt` | ✅ 纯函数 | 免费/高级、三种排序、衰减 |
| 过滤逻辑 | `domain/AppFilter.kt` | ✅ 纯函数 | 自身/黑名单/系统/游戏 |
| 统计读取 | `domain/UsageStatsProvider.kt` | ⚠️ 需设备 | 依赖 `UsageStatsManager` |
| 数据层 | `data/local/*` + `repository/*` | ⚠️ 需设备 | 依赖 Room / Android |
| 小组件 | `widget/*` | ⚠️ 需设备 | 依赖 `RemoteViews` / `AppWidgetManager` |

> 上表标注「⚠️ 需设备」的部分属于**仪器测试（Instrumented Test）**范畴，需在真机/模拟器上运行，本次未覆盖（见 §6 后续建议）。

## 3. 测试环境

| 项 | 值 |
| --- | --- |
| JDK | 17 |
| Gradle | 8.13（Wrapper） |
| Kotlin | 2.0.21 |
| 测试框架 | JUnit 4.13.2 |
| 测试目录 | `app/src/test/java/com/soulscop/mostusedwidget/domain/` |

## 4. 测试用例

### 4.1 ScoreCalculatorTest（热度公式，5 例）

| 用例 | 输入/前置 | 预期 |
| --- | --- | --- |
| `freeFormula_usesLaunch7dTimesTwo` | 免费版，launch7d=5 | score = 10.0（=5×2） |
| `premiumLaunchCount_combinesWeightsAndDecay` | 高级版 LAUNCH_COUNT，w1=2/w2=0.5/decay=7，launch7d=10/launch30d=20，lastUsed=now | score = 2×10 + 0.5×20 + 100 = 130.0 |
| `premiumForegroundTime_usesForegroundTime7d` | 高级版 FOREGROUND_TIME，foregroundTime7d=5000 | score = 5000.0 |
| `premiumLastUsed_usesLastUsedTimestamp` | 高级版 LAST_USED，lastUsed=1234567890 | score = 1234567890.0 |
| `decayTerm_decreasesWithAge` | 高级版，w1=w2=0/decay=7，lastUsed=now 与 7 天前对比 | now→100.0；7 天前→100×e⁻¹≈36.79 |

### 4.2 AppFilterTest（过滤逻辑，6 例）

| 用例 | 输入/前置 | 预期 |
| --- | --- | --- |
| `excludesSelfPackage` | 包名 == 自身包名 | 排除（false） |
| `excludesHiddenPackage` | 包名在黑名单 | 排除（false） |
| `excludesSystemWhenEnabled` | excludeSystem=true 且 isSystem=true | 排除（false） |
| `excludesGameWhenEnabled` | excludeGames=true 且 isGame=true | 排除（false） |
| `includesNormalPackage` | 普通包名，无过滤 | 包含（true） |
| `includesSystemWhenNotExcluded` | excludeSystem=false 且 isSystem=true | 包含（true） |

## 5. 测试结果

执行 `.\gradlew.bat :app:testDebugUnitTest`，**BUILD SUCCESSFUL**。

| 测试类 | 用例数 | 失败 | 错误 | 跳过 |
| --- | --- | --- | --- | --- |
| `ScoreCalculatorTest` | 5 | 0 | 0 | 0 |
| `AppFilterTest` | 6 | 0 | 0 | 0 |
| **合计** | **11** | **0** | **0** | **0** |

报告产物：
- HTML 报告：`app/build/reports/tests/testDebugUnitTest/index.html`
- XML 结果：`app/build/test-results/testDebugUnitTest/*.xml`

## 6. 后续建议

以下功能需真机/模拟器补充**仪器测试**（`app/src/androidTest/` + Robolectric/Espresso）：

1. `UsageStatsProvider`：权限检测、7/30 天聚合正确性（需授权 + 真实使用数据）。
2. `UsageStatsRepository.refresh()`：全链路「读取 → 过滤 → 算分 → 写缓存」。
3. 小组件：`WidgetRenderer` 渲染、点击启动、刷新广播、占位。
4. 内购桩：`StubBillingManager.purchase()/restore()` 持久化状态。
5. CSV 导出：`CsvExporter` 写入 `getExternalFilesDir` 并触发分享。

> 若需覆盖上述模块，可引入 `androidx.test` + `Robolectric`（本地）或 `ConnectedAndroidTest`（真机）。
