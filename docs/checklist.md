# MostUsed Widget — 验收清单（Checklist）

## 1. 构建与合规
- [ ] `.\gradlew.bat :app:assembleDebug` 成功产出 `app-debug.apk`
- [ ] Manifest 无 `QUERY_ALL_PACKAGES`
- [ ] Manifest 含 `<queries>`（MAIN + LAUNCHER）、`PACKAGE_USAGE_STATS`、appwidget receiver、`android:configure`
- [ ] 小组件 `updatePeriodMillis=0`
- [ ] `minSdk=21` / `targetSdk=35` / `compileSdk=36`

## 2. 权限流程
- [ ] 首启进入权限引导页，文案含隐私说明
- [ ] 「前往开启权限」跳转 `ACTION_USAGE_ACCESS_SETTINGS`
- [ ] 「已开启，重新检测」可正确刷新权限状态
- [ ] 关闭权限后小组件显示占位提示

## 3. 统计与排序
- [ ] 7 天/30 天启动次数、前台时长聚合正确
- [ ] 免费公式 `score = launch7d * 2` 生效
- [ ] Premium 三种排序模式（次数/时长/最近）生效
- [ ] 权重 w1/w2、衰减因子滑块生效
- [ ] 黑名单隐藏生效；白名单置顶排最前
- [ ] 批量屏蔽系统/游戏（Premium）生效

## 4. 桌面小组件
- [ ] 4×2 布局正确，图标 + 名称渲染
- [ ] 点击图标可启动对应 App
- [ ] 刷新按钮可手动刷新
- [ ] 包失效/图标失败用占位图标
- [ ] 免费版固定 Top4；Premium 4/6/8 及外观自定义生效

## 5. 主界面
- [ ] 三 Tab（榜单/小组件/设置）正常切换
- [ ] 榜单支持隐藏、置顶操作
- [ ] 设置页各配置项可用，Premium 项被正确锁定/解锁

## 6. 内购桩
- [ ] 购买后解锁 Premium 并持久化
- [ ] 恢复购买可读回状态
- [ ] UI 面向 `BillingManager` 接口，无硬编码耦合

## 7. 数据与后台
- [ ] CSV 导出可写入并分享
- [ ] 每日自动刷新（Premium）生效
- [ ] 清除数据功能可用
- [ ] 无异常耗电、无崩溃

## 8. 文案资源化
- [ ] 全部 UI 文案入 `strings.xml`（中文默认）
- [ ] 无硬编码字符串
- [ ] 为后续国际化预留结构（加 `values-xx` 即可，无需改代码）

## 9. 占位与健壮性
- [ ] 无使用记录、无权限、加载失败均有友好占位
- [ ] 隐私政策页可访问 + 外链占位正确
