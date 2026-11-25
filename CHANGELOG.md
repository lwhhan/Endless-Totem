# 无尽图腾模组 (Endless Totem) 变更记录

模组 id：`endless_totem`

## v51 (2026-11-23)
- 修复：GitHub Actions 第 2 次运行仍失败，定位到真正原因
  - 现象：Build 步骤只跑 8 秒就退出，前 6 步全部 success
  - 报错：`Could not resolve net.fabricmc:fabric-loom:1.17.21. Dependency requires at least JVM runtime version 21. This build uses a Java 17 JVM.`
  - 根因：`fabric-loom` 1.17 起要求运行 Gradle 的 JVM 至少为 21，而 workflow 里配的是 JDK 17
  - 修复：workflow 的 `Set up JDK` 改为 **21**
  - 说明：产物字节码仍由 `options.release = 17` 控制，玩家侧 Java 17 即可，`fabric.mod.json` 的 `java >= 17` 不变
- 更新：README「从源码构建」补充"构建需要 JDK 21"的说明

## v49 (2026-11-23)
- 修复：GitHub Actions 首次运行失败（38 秒退出，未进入编译阶段）
  - 根因：`gradlew` 提交进仓库时是 `100644`（Windows 不记录可执行位），
    而 `gradle/actions/setup-gradle@v4` 会执行 `./gradlew --version` 探测版本，
    原来的 `chmod +x` 放在它之后，导致 `Permission denied`
  - 修复：把 `chmod +x gradlew` 提到 checkout 之后第一步，早于 setup-gradle
- 修复：`setup-gradle` 会读取 wrapper 的 `distributionUrl` 预取发行版，
  原 sed 换源步骤也在它之后，等于让它去访问国外不通的腾讯镜像
  - 修复：sed 换源同样提前到 setup-gradle 之前，并顺带关闭 `validateDistributionUrl`、
    把 `retries` 从 0 调到 3
- 优化：构建后把产物统一收集到 `dist/`，排除 `-sources.jar` 和 `-dev.jar`，
  上传与 Release 都只取 `dist/*.jar`
- 优化：`./gradlew build` 加 `--no-daemon`

## v48 (2026-11-23)
- 新增：`fabric.mod.json` 补回 `contact` 字段，指向仓库 https://github.com/lwhhan/Endless-Totem
- 更新：README 顶部加仓库链接

## v47 (2026-11-23)
- 变更：许可证由 CC0 1.0 换成 **MIT**（`LICENSE` 全文替换，版权方 lwhhan，年份 2026）
- 变更：`fabric.mod.json` 的 `license` 改为 `MIT`，`authors` 改为 `lwhhan`
- 变更：移除 `fabric.mod.json` 的 `contact` 字段（GitHub 仓库尚未创建，避免留下无效链接，建好后补回）
- 更新：README 许可证段落同步为 MIT

## v46 (2026-11-23)
- 清理：删除根目录 4 个制造台贴图生成脚本（`tools_gen_workbench_textures*.py`）
  - 制造台功能早已移除，这些脚本属于历史遗留，上传 GitHub 前清掉避免仓库杂乱
  - 删除前已备份到项目外的 `_archive/制造台贴图脚本备份_20261123/`

## v45 (2026-11-23)
- 新增：**README.md** —— 按当前实际内容重写（四种物品的耐久/效果/获取方式、合成图示、无尽盒收纳流程、软兼容清单、环境要求、构建与项目结构）
- 新增：GitHub 发布配套文件
  - `.gitignore`（排除 build/.gradle/run/.idea 等）
  - `.gitattributes`（统一换行符，jar/png 标记为二进制）
  - `.github/workflows/build.yml`（push/PR/打 tag 自动构建，tag 触发自动挂 Release）
- 变更：**产物 jar 名改为「中文名_英文名」**，例如 `无尽图腾_EndlessTotem-1.0.0.jar`
  - `gradle.properties` 的 `archives_base_name` 改为 `无尽图腾_EndlessTotem`
  - `build.gradle` 补 `base { archivesName = ... }`（Gradle 9 已移除旧的 `archivesBaseName` 属性，不补不生效）
- 变更：`fabric.mod.json` 游戏内显示名改为「无尽图腾 Endless Totem」，描述更新为四种物品，补 `homepage` / `sources` / `issues` 链接
- 修复：`gradlew` 补执行权限（原为 644，clone 后无法直接运行）

## v44 (2026-11-21)
- 精简：**无尽盒悬停提示只保留一行耐久显示**
  - 删除「耐久为 0，暂不生效」红字提示
  - 删除「手持时免疫死亡，效果同不死图腾」效果说明
  - 删除「右键可收入普通不死图腾补充耐久」用法提示
  - `EndlessBoxItem.appendHoverText` 只剩耐久度一行，`ChatFormatting` import 一并移除
  - zh_cn / en_us 同步删除 `endless_box.empty` / `endless_box.tooltip` / `endless_box.tooltip1` 三个键

## v43 (2026-11-16)
- 变更：**无尽盒默认耐久改为 0/99（空盒）**，合成出来必须先收纳不死图腾才能用
  - 合成产出、合成预览、配方书预览全部统一为 0/99
- 变更：**无尽盒耐久判定改为「≥1 才生效」**
  - 耐久 0 时不再触发复活（`TotemCompat.isDepleted` 统一拦截，主副手 / 背包 / 饰品槽三条路径一致）
  - 耐久 0 时物品**不会消失**，继续拿普通不死图腾右键即可补充
  - 耐久 0 时 Tooltip 用红字提示「耐久为 0，暂不生效」
- 变更：**无尽盒扣耐久到 0 时不再整体消耗**（无尽图腾仍然是扣满消失）
  - `TotemCompat.damageOne` 新增 `destroyWhenEmpty` 参数：无尽图腾传 true、无尽盒传 false
  - `TotemCompat.consumeForCharm`（Charm of Undying 路径）同步分开处理
- 核实：无尽盒效果数值确认为 Java 版原版不死图腾的真实数值 —— 生命恢复 II **45 秒**、伤害吸收 II **5 秒**、抗火 I **40 秒**（三者时长本来就不同，不是同一个时长）

## v42 (2026-11-16)
- 新增：**无尽盒**（`endless_box`）—— 一个专门用来存放不死图腾的盒子
  - 贴图：以无尽图腾的像素图为基础整体改色为 `#9ee08d`（浅绿），保留原有 5 级明暗层次
  - 耐久上限 99，复活后扣 1 点耐久，扣到上限整体消耗 1 个（与无尽图腾同一套 `damageOne` 逻辑）
  - 复活效果**与原版不死图腾完全一致**：生命恢复 II 45 秒、伤害吸收 II 5 秒、抗火 40 秒，无抗性提升
  - 合成：工作台 3x3 十字形 —— 中心 1 个箱子 + 上下左右各 1 个不死图腾，四角留空，产出 1/99
  - 收纳：背包界面左键拿起无尽盒，右键背包里的普通不死图腾 → 图腾 -1，无尽盒耐久 +1
  - 悬停 Tooltip 显示剩余耐久，并附一行收纳用法提示
- 变更：**「右键收入不死图腾」的充能功能从无尽图腾迁移到无尽盒**
  - 无尽图腾不再能用普通不死图腾补充耐久，用完即消耗（强化效果的定位更清晰）
- 兼容：无尽盒同样走 `TotemCompat` 统一注册，自动覆盖三条复活路径
  - 主副手（`TotemReviveMixin`）
  - Inventory Totem 背包路径（`InventoryTotemCompat`）
  - Charm of Undying 饰品槽（`CharmOfUndyingCompat` 遍历 `Kind.values()` 自动注册 + `trinkets:charm/charm` 标签已加入无尽盒）
- 无尽盒已加入模组创造分类页

## v41 (2026-11-15)
- 调整：无尽图腾复活效果中的**抗性提升**由 III 降为 II（`DAMAGE_RESISTANCE` amplifier 2 → 1）
  - 其余三项不变：生命恢复 III（10 秒）、抗火（30 秒）、伤害吸收 II（20 秒）
  - 主副手、Inventory Totem 背包、Charm 饰品槽三条路径共用 `TotemCompat.effectsOf`，改动一次性覆盖全部

## v40 (2026-11-15)
- 修复（重要）：装了 **Charm of Undying** 时，一次死亡会结算两次复活
  - 根因：`TotemReviveMixin` 在 `checkTotemDeathProtection` 的 HEAD 注入里只调了 `cir.setReturnValue(true)`。
    `setReturnValue` 只是设置返回值，**方法仍会继续往下执行**；Charm 在 `InteractionHand.values()` 调用点还有一处注入，
    于是又走了一次 `useTotem`（再扣 1 点耐久 + 再施加一遍效果）
  - 改为 `cir.setReturnValue(true); cir.cancel();`，直接中断方法，保证一次死亡只结算一次
- 修正：附魔图腾的实现说明与实际一致（复活后整体消耗 1 个，刻意不设 durability，背包里不会多出耐久条）
- 代码审查结论：其余 16 个 Java 文件、mixins 配置、配方/标签 JSON、模型与粒子资源均未发现编译或逻辑错误

## v39 (2026-11-01)
- 修复：无尽图腾出现 **0/99** 且耐久耗尽后仍然能继续复活的问题
  - 根因：之前用 `ItemStack#hurt` 扣耐久，damage 达到上限时的处理不稳定，可能把 damage 直接写成 99（显示 0/99）而没有消耗物品
  - 改为手动扣耐久：damage 到上限时直接整体消耗 1 个，**绝不让 damage 停在上限**
  - `TotemCompat.kindOf` 增加耐久耗尽判断：`damage >= maxDamage` 的物品不再被识别为有效图腾
  - 三条复活路径（主副手 / Inventory Totem 背包 / Charm 饰品槽）全部生效：0/99 的物品不会触发复活
  - Charm 路径额外处理：`modifyStack` 对已耗尽物品不消耗，`applyEffects` 返回 false 不复活
  - 旧存档里已经存在的 0/99 物品，可以用普通不死图腾充能修复（damage 99 → 98，变回 1/99）
- 耐久耗尽消耗时会播放物品损坏音效（`ITEM_BREAK`）

## v38 (2026-10-31)
- 修复：合成预览一直显示 **99/99**、取出后才变 1/99 的问题
  - 根因：v36 的 `CraftingPreviewMixin` 只在客户端 `slotsChanged` 末尾改了一次结果槽，随后被服务端同步的结果槽数据包覆盖
  - 改为**自定义配方** `EndlessTotemRecipe`：`getResultItem` 和 `assemble` 都直接返回 1/99 的无尽图腾，预览、取出、服务端同步完全一致
  - 配方类型改为 `endless_totem:endless_totem`（`SimpleCraftingRecipeSerializer`），配方 JSON 只保留 `category`
  - 匹配逻辑支持 2x2 背包合成格，也支持 3x3 工作台里任意位置的 2x2 正方形摆放，且要求其余格子为空
- 停用 v36 的 `CraftingPreviewMixin`（已从 `endless_totem.mixins.json` 移除注册，源文件保留不再加载）
- `EndlessTotemMod` 中三种图腾改为静态字段，供自定义配方直接引用产出物

## v37 (2026-10-31)
- 新增：与 **Charm of Undying**（`charmofundying`）软兼容
  - 三种图腾现在可以放进 Trinkets 的 charm 饰品槽（提供 `data/trinkets/tags/items/charm/charm.json`，`replace: false` 合并）
  - 通过反射调用对方的 `TotemProviders.putEffectProvider` + `Proxy` 动态实现 `ITotemEffectProvider`，注册三种图腾的复活效果
  - `bypassInvul()` 返回 `false`，与主副手路径一致：虚空等「无视无敌」伤害不触发
  - 饰品槽触发时：无尽图腾扣 1 点耐久、无限图腾不消耗、附魔图腾整体消耗
  - 编译期不引用对方任何类，**未安装时不会加载、也不会成为前置**
- 新增：与 **Inventory Totem**（`inventorytotem`）软兼容
  - 该模组让背包里的图腾也能复活，但硬编码只认原版不死图腾
  - 补上同样行为：背包里有三种图腾之一时同样触发复活（`ServerLivingEntityEvents.ALLOW_DEATH`）
  - 主/副手已有图腾时交给原 mixin 处理，避免同一件物品被处理两次
  - 同样只在检测到该模组时注册，未安装不影响原版手感
- 重构：把三种图腾的复活逻辑（增益 / 消耗 / 特效）抽到 `com.wujin.totem.compat.TotemCompat`，`TotemReviveMixin` 只负责遍历主副手
- 修复隐患：`TotemReviveMixin` 原先在 mixin 类内用 `@Unique` 私有方法，现已改为调用公共工具类，便于第三方兼容路径复用同一套效果

## v36 (2026-10-31)
- 修正：合成结果槽的预览物品现在直接显示 **1/99**（之前预览是 99/99，取出后才变 1/99）
- 新增 `CraftingPreviewMixin`：在 `InventoryMenu`（背包 2x2 合成格）和 `CraftingMenu`（工作台）的 `slotsChanged` 末尾，把结果槽里满耐久的无尽图腾补成 damage 98
- 配方本身仍是普通 shaped 配方，`onCraftedBy` 保留作兜底

## v35 (2026-10-30)
- 修正 v34 的充能方式：不是主手/副手右键，而是**背包界面内**操作
  - 鼠标左键拿起无尽图腾（物品悬浮在光标上，carried stack）
  - 右键背包中的普通不死图腾 → 普通图腾消失 1 个，无尽图腾耐久 +1
- 新增 `ContainerClickMixin`：拦截 `AbstractContainerMenu#clicked` 的 `PICKUP + 右键`，处理完取消原版放置逻辑
- 满耐久时右键不会消耗普通图腾
- 移除 v34 的主副手 `use` 充能逻辑与 tooltip 提示行

## v34 (2026-10-30)
- 新增：无尽图腾合成配方 —— 4 个不死图腾 2x2 正方形摆放 → 1 个无尽图腾
- 新增：合成产出的无尽图腾初始耐久为 **1/99**（`onCraftedBy` 中把 damage 设为 98）
- （v35 已修正）原充能方式为主副手右键，已改为背包界面点击

## v33 (2026-10-10)
- 稳健性修复：战利品表 id 不再引用 `BuiltInLootTables` 常量
- 项目采用官方映射 `officialMojangMappings()`，该常量在 mojmap 下字段名与 javadoc(yarn) 不一致，有编译隐患
- 改为直接用 `new ResourceLocation("minecraft", "chests/xxx")` 构造表 id，与回调参数（ResourceLocation）比对，彻底绕开常量
- 功能不变：附魔图腾与世界同位置、同刷新率

## v32 (2026-10-06)
- 修复：v31 编译失败——`LootTableEvents.MODIFY` 回调签名写错
- 1.20.1（fabric-api 0.92.9）该回调是 **5 参数** `(ResourceManager, LootManager, Identifier, LootTable.Builder, LootTableSource)`，原代码误用旧版 4 参导航，导致 lambda 参数类型不匹配
- 改用隐式 lambda 推断，通过 `tableBuilder.withPool(pool)` 追加战利品池，并按 `source.isBuiltin()` 只修内置表
- 功能与 v31 一致：附魔图腾刷在箱子里，与附魔金苹果同位置、同刷新率

## v31 (2026-10-05)
- 新增：附魔图腾拥有世界生成方式——刷在箱子里
- 位置与刷新率与**附魔金苹果完全一致**，覆盖 1.20.1 含附魔金苹果的全部箱子战利品表：
  - 废弃矿井 abandoned_mineshaft（~1.4%）
  - 古代城市 ancient_city（~8.4%）
  - 堡垒遗迹宝箱 bastion_treasure（~5.9%）
  - 沙漠神殿 desert_pyramid（~2.4%）
  - 废弃传送门 ruined_portal（~1.5%）
  - 简单地牢 simple_dungeon（~2.8%）
  - 林地府邸 woodland_mansion（~3.1%）
- 实现：新增 `ModLootInjection`，用 Fabric Loot API（LootTableEvents.MODIFY）为上述表各注入一条独立 roll，箱子以等于该表附魔金苹果的概率掉 1 个附魔图腾，不影响附魔金苹果本身

## v1 (2026-09-10)
- 基于 Fabric 官方 1.20.1 模板（Mojang 官方映射）搭建工程
- 新增「无尽图腾」：免疫死亡，耐久 99，复活消耗 1 点耐久；复活后提供 生命恢复 V / 抗火 / 抗性提升 II；背包内**不显示**耐久条
- 新增「∞图腾·无限图腾」：免疫死亡，**无限耐久**，复活**不消耗耐久**、永不损坏；复活后提供 生命恢复 II / 伤害吸收 II / 抗火；背包内显示耐久条（始终满格）
- 两图腾均未添加合成配方，仅作为普通物品注册，放入「工具与实用物品」创意标签页
- 图标：黑中透蓝图腾，造型取自原版不死图腾，蓝色清晰轮廓（无尽=深沉蓝黑，无限=明亮亮青眼）
## v2 (2026-09-11)
- 修复：补充缺失的 mod 图标 icon.png（取无尽图腾贴图放大至 128x128），Mod 列表不再显示空白图标

## v3 (2026-09-11)
- 修复：fabric.mod.json 的 name/description 改为英文（mod元数据不走lang翻译，避免非ASCII乱码）
- 修复：fabric-api 依赖从通配 * 锁定为 >=0.92.12+1.20.1，避免匹配到不兼容版本
- 改进：重绘 128x128 高清 mod 图标 icon.png（黑中透蓝图腾、蓝色清晰轮廓），替换原 16x16 放大模糊版
- 重构：TotemReviveMixin 抽取 revive() 私有方法收敛两段重复复活逻辑

## v4 (2026-09-11)
- 修复：gradle wrapper 下载源改为腾讯云镜像（gradle-9.5.1-bin.zip），加速国内下载
- 修复：编译错误——官方映射（officialMojangMappings）下 `Item.Properties` 设置耐久的方法名是 `durability(int)`，将 `maxDamage(...)` 全部改为 `durability(...)`
- 调整：两个图腾背包内均不显示耐久条（isBarVisible 返回 false），仅保留鼠标悬停 Tooltip 显示的耐久文字

## v5 (2026-09-12)
- 修复：运行时加载报兼容错误（Incompatible mods found）——原声明需要 fabricloader>=0.19.5、fabric-api>=0.92.12，但用户环境安装的是 fabricloader 0.19.3、fabric-api 0.92.9+1.20.1
- 将 fabric.mod.json 的 depends 降级为 fabricloader>=0.19.3、fabric-api>=0.92.9+1.20.1
- gradle.properties 编译依赖同步降为 loader_version=0.19.3、fabric_api_version=0.92.9+1.20.1，与运行时环境一致

## v6 (2026-09-12)
- 修复：Mixin 注入失败（Critical injection failure，No refMap loaded）——注入目标方法名使用了 Yarn 映射名 `tryUseTotem`，与官方映射（officialMojangMappings）下的 `checkTotemDeathProtection` 不符
- 将 @Inject method 改为官方映射名 `checkTotemDeathProtection`
- 在 endless_totem.mixins.json 补充 refmap 声明 `endless_totem.refmap.json`（loom 编译时自动生成）

## v7 (2026-09-13)
- 调整：删除两个图腾在背包内 Tooltip 的自定义介绍文字（appendHoverText），仅保留系统自带的剩余耐久显示（如"耐久度 x/y"）

## v8 (2026-09-13)
- 调整：无尽图腾复活效果改为 生命恢复 3、抗火、抗性提升 3、伤害吸收 2
- 调整：无限图腾复活效果改为 生命恢复 5、抗性提升 5、伤害吸收 5、抗火
- 调整：无限图腾移除 durability（改为不可损坏物品），Tooltip 不再显示剩余耐久（无限耐久无显示意义）；无尽图腾保留 99 耐久并显示剩余耐久

## v9 (2026-09-14)
- 新增方块「灵魂合成工作台」（crafting_workbench）：外观为深蓝轮廓 + 浅蓝内芯的工作台式方块
- 功能与工作台一致：右键打开 9×9（81）输入网格 + 右侧 1 个合成输出格的界面，下方为玩家背包；关闭界面时输入格物品自动归还玩家
- 已预留合成配方接口：`CraftingWorkbenchMenu#updateResult()` 当前为空实现、输出格暂不可取物，后续在此接入 9×9 配方
- 方块/方块实体/菜单/客户端屏幕架构：`CraftingWorkbenchBlock`、`CraftingWorkbenchBlockEntity`、`WorkbenchContainer`（82 槽）、`CraftingWorkbenchMenu`、`CraftingWorkbenchScreen`，统一由 `ModRegistries` 集中注册
- 新增方块贴图（深蓝轮廓 + 浅蓝 3×3 网格顶面）、GUI 背景贴图（208×312，与槽位坐标对齐）及 blockstate / model / lang 翻译

## v10 (2026-09-15)
- 修复：MenuType 编译错误——`CraftingWorkbenchMenu` 新增两参构造 `(int containerId, Inventory playerInventory)`，供 `new MenuType<>(（id, inv) -> new CraftingWorkbenchMenu(id, inv)）` 工厂签名匹配（此前仅有三参带容器构造，导致无法推断类型变量 T）
- 新增「无尽图腾」自定义创造分类页：收纳 无尽图腾 / 无限图腾 / 灵魂合成工作台，图标用无尽图腾；两个图腾与工作台不再加入原版"工具与实用物品"页
- 翻译：新增 `itemGroup.endless_totem`（中文「无尽图腾」/ 英文「Endless Totem」）

## v11 (2026-09-15)
- 修复：MenuType 仍报"无法推断类型变量 T"——改用显式泛型 `new MenuType<CraftingWorkbenchMenu>(...)` 去掉菱形 `<>`。原因：菱形类型推断与 lambda 返回类型推断在 javac 无法同时完成，与构造参数个数无关
- 调整：方块「灵魂合成工作台」改名为「无尽合成台」（中文/英文 lang 值同步改，lang 键 `block.endless_totem.crafting_workbench` 不变）

## v12 (2026-09-16)
- 修复：MenuType 编译错误——Minecraft 1.20 起 `MenuType` 构造器需要两个参数 `(MenuSupplier, FeatureFlagSet)`，补上第二参 `FeatureFlags.DEFAULT_FLAGS`（此前只传工厂单参，报"构造器无法应用到给定类型"）

## v13 (2026-09-16)
- 美术重做（按用户要求做成「原版工作台的蓝色版」）：
  - 方块贴图重绘——顶面深蓝底板 + 3×3 凹陷工作格 + 亮蓝高光；侧面蓝木板 + 台沿；底面深蓝地板
  - GUI 背景重绘——深蓝面板木纹、凹槽式槽位、亮蓝 9×9 输入格、更亮的输出格 + 指向箭头、下方灰色背包/快捷栏凹槽
  - 槽位坐标与 Java 布局保持不变（9×9 输入 + 右侧输出 + 下方背包）

## v14 (2026-09-17)
- UI 布局修复：物品栏不再"飞走"——背包槽起 y 由 232 上移 184、快捷栏 290 上移 242，紧贴 9×9 合成区正下方（消除 v13 中间约 52px 空档）；GUI 尺寸由 208×312 压缩为 208×266，整窗紧凑居中；重绘 GUI 贴图对齐新坐标（新增 `tools_gen_workbench_textures_v3.py`）
- 新增无限图腾专属复活特效：注册自定义粒子类型 `infinite_totem`（亮蓝色光点）与自定义复活实体事件(100)；无限图腾复活时广播(100)，客户端播放扩散上升的蓝色光点；无尽图腾仍沿用原版不死图腾的绿色复活特效(35)
- 涉及文件：`CraftingWorkbenchMenu`（物品栏坐标）、`CraftingWorkbenchScreen`（IMAGE_HEIGHT=266）、新增 `ModParticles`、`InfiniteTotemParticle`、`textures/particle/infinite_totem_spark.png`、`assets/endless_totem/particles/infinite_totem.json`、`TotemReviveMixin`（按图腾区分特效事件）

## v15 (2026-09-18)
- 修复 v14 编译错误：main source set 的 `TotemReviveMixin` 引用了客户端类 `ClientLevel`，而 Loom 主编译不含客户端类，报 `程序包 net.minecraft.client.multiplayer 不存在`
- 拆分修复：无限图腾的蓝色粒子渲染逻辑移到 client source set 的专属 Mixin `ClientTotemFxMixin`（可正常引用 `ClientLevel`）；新增客户端专属 `endless_totem.client.mixins.json`，并在 `fabric.mod.json` 新增 `client` 键使其仅客户端加载；main 侧 `TotemReviveMixin` 只保留服务端复活逻辑（无尽=35 沿用原版绿特效、无限=100 广播自定义事件）

## v16 (2026-09-20)
- GUI 背景改为「原版工作台暖木棕」风格（参考 ToolLeveling 的"自绘 GUI 贴图 + blit 全图"做法，参考其绘制方式但不照搬其冷色调）
  - 配色：深棕木纹面板 + 米褐色 3D 凹槽（左上高光 / 右下阴影），彻底替换 v14/v15 的蓝色调
  - 布局与坐标不变：9×9 网格 (8,18)、输出格 (176,90)、背包 (8,184) / 快捷栏 (8,242)，GUI 仍为 208×266
  - 方块贴图保持不变（仍为蓝色工作台）
  - 新增生成脚本 `tools_gen_workbench_textures_v4.py`

## v17 (2026-09-21)
- 修复 v16 编译错误（compileClientJava）：无限图腾粒子类用了 **Yarn 映射名 `SpriteParticle`**，本项目全程 Mojang 官方映射，官方映射中粒子基类是 `TextureSheetParticle`，导致 `找不到符号` 报错
- 同步修正粒子基类构造调用：官方 `Particle` 构造为 `(ClientLevel, x, y, z)`，`xd/yd/zd` 需在构造体内手动赋值（不复用错误写法）
- 涉及文件：`InfiniteTotemParticle.java`（extends `TextureSheetParticle`，其余字段/方法官方名不变）

## v18 (2026-09-25)
- 修复：恢复无尽图腾鼠标悬停 Tooltip 的剩余耐久显示。根因——`isBarVisible=false` 不仅隐藏背包图标耐久条，同时也会让原版默认 tooltip 不再渲染 `item.durability`（耐久度 x/y）。修复：在 `appendHoverText` 中手动用 `item.durability` 键补回耐久文字，图标条仍隐藏
- 新增「附魔图腾」（`enchant_totem`）：效果与无限图腾一致（等级相同），但所有效果统一减半为 20 秒，伤害吸收（金色血量）改为 2 分钟；无限耐久、复活不消耗耐久；复活特效用蓝色粒子(100)；配套 item 注册、`EnchantTotemItem`、lang 中英文翻译、item 模型、紫色附魔风贴图，并加入创造分类页
- 删除「无尽合成台」（制造台）功能及全部相关文件：
  - 删除源码：`CraftingWorkbenchBlock` / `CraftingWorkbenchBlockEntity` / `CraftingWorkbenchMenu` / `WorkbenchContainer` / `CraftingWorkbenchScreen`
  - `ModRegistries` 移除方块/方块物品/方块实体/菜单类型注册，仅保留「无尽图腾」创造分类页；`EndlessTotemModClient` 移除 `MenuScreens.register`；`EndlessTotemMod` 同步移除制造台注释
  - 删除资源：blockstate、block/item model、方块贴图、GUI 背景贴图、lang 中 `block.endless_totem.crafting_workbench` 键
  - 制造台源码完整保留于 v17 zip 备份，可随时恢复
- 涉及文件：`EndlessTotemItem` / 新增 `EnchantTotemItem` / `EndlessTotemMod` / `ModRegistries` / `EndlessTotemModClient` / `TotemReviveMixin` / lang / item 模型与贴图 / CHANGELOG

## v19 (2026-09-26)
- 调整：附魔图腾由「无限耐久」改为「一次性消耗品」——耐久改为 1，复活消耗 1 点耐久后直接消失（同原版不死图腾）；`TotemReviveMixin` 中附魔图腾分支 consumeDurability 由 false 改为 true
- 调整：附魔图腾材质改为「普通不死图腾样式 + 附魔流光」——item model 的 layer0 直接指向原版 `minecraft:item/totem_of_undying` 贴图；新增 `EnchantTotemItem.isFoil()` 返回 true，端上渲染附魔金苹果式流动光泽；删除 v18 自绘的紫色调贴图 `textures/item/enchant_totem.png`
- 涉及文件：`EnchantTotemItem` / `EndlessTotemMod`（durability(1)）/ `TotemReviveMixin`（消耗耐久）/ `models/item/enchant_totem.json`（指向原版不死图腾）/ 删除本地贴图

## v20 (2026-09-28)
- 修复：无尽图腾 Tooltip 出现两行耐久（两个 98/99）。根因——MC 1.20.1 原版在物品受损伤（`isDamaged`）时会自动追加一条 `item.durability`（不受 `isBarVisible` 影响），与 v18 手动补的行叠加导致重复。修复：`appendHoverText` 只在满耐久（未受损伤）时补一条，受损伤后交给原版显示，任何状态恰好一行
- 修复：附魔图腾显示 `-1/1` 且可多次使用。根因——v19 给它配了 `durability(1)` 并走 `stack.hurt` 扣耐久，耐久逻辑出现越界、且未按预期一次性爆掉。修复：附魔图腾改为**无耐久、不可损坏**（注册去掉 `durability(1)`），复活时在 `TotemReviveMixin` 中按 `CONSUME_ITEM` 整体 `stack.shrink(1)` 消耗掉，如同原版不死图腾「用一次就爆掉」，不再显示耐久度
- 涉及文件：`EndlessTotemItem` / `EnchantTotemItem`（无改动，注册调整）/ `EndlessTotemMod`（附魔图腾去掉 durability）/ `TotemReviveMixin`（消耗方式改为 0 不消耗 / 1 扣耐久 / 2 整体消耗）

## v21 (2026-09-29)
- 修复：v20 编译错误——`LivingEntity` 无 `isCreative()` 方法（仅 `Player` 有）。修正为 `self instanceof Player player && player.isCreative()` 判断后整体消耗；补充 `Player` import
- 涉及文件：`TotemReviveMixin`

## v22 (2026-09-30)
- 调整：无尽图腾复活特效由「原版不死图腾绿色爆裂(35)」改为「自定义蓝色粒子(100)」，与蓝色系图标一致；现在 无尽 / 无限 / 附魔 三个图腾复活特效统一为蓝色粒子，不再出现原版绿色特效
- 涉及文件：`TotemReviveMixin`（无尽特效事件 35→100）/ `ModParticles` / `ClientTotemFxMixin` / `InfiniteTotemParticle`（注释同步为三图腾共用）

## v30 (2026-10-05)
- 修复：无尽图腾「耐久仍不显示」——v28/v29 自家手动加的耐久行也一直没显示
  - 根因：`EndlessTotemItem#appendHoverText` 加的耐久行使用了原版翻译键 `item.durability`，而 `ItemStackDurabilityMixin` 会把 **所有** key 为 `item.durability` 的行删掉（本意只删原版自动行），结果把我们自己加的那行也一并删除了，任何状态都看不到耐久
  - 修复：自家耐久行改用自定义翻译键 `item.endless_totem.endless_totem.durability`（en_us/zh_cn 均已新增），与原版 `item.durability` 区分，mixin 只清理原版自动行
- 涉及文件：`EndlessTotemItem`（翻译键）/ `lang/en_us.json`、`lang/zh_cn.json`（新增 durable 键）

## v29 (2026-10-04)
- 修复：v28 启动崩溃——`ItemStackDurabilityMixin` 注入方法签名错误
  - 根因：1.20.1 官方映射下 `ItemStack#getTooltipLines` 第一个参数是 `Player`（而非 `BlockGetter`），mixin 注入时报 `Invalid descriptor` 导致 game 启动崩溃
  - 修正：注入方法第一个参数由 `@Nullable BlockGetter level` 改为 `@Nullable Player player`
- 涉及文件：`ItemStackDurabilityMixin`

## v28 (2026-10-04)
- 修复：无尽图腾「耐久不显示」——悬停 tooltip 在满耐久或受损时都可能看不到耐久行
  - 根因：此前靠「满耐久时补一行、受损时交给原版自动显示」的两段式逻辑，在自定义 modded 物品上受损后的原版自动行不可靠，导致任一状态都可能缺失
  - 现改为彻底接管：`EndlessTotemItem#appendHoverText` **无条件始终**补一行「耐久度 当前/99」，满耐久与受损都稳定显示当前剩余耐久
  - 新增 `ItemStackDurabilityMixin`：在 `ItemStack#getTooltipLines` 返回前移除无尽图腾的原版 `item.durability` 自动行，保证任意状态 tooltip 恰好一行、不缺失也不重复
- 涉及文件：`EndlessTotemItem`（appendHoverText 逻辑）/ 新增 `ItemStackDurabilityMixin` / `endless_totem.mixins.json`（注册新 mixin）

## v27 (2026-10-03)
- 修复：三图腾复活时「眼前飘起的图腾」动画此前固定显示原版不死图腾（触碰复活瞬间眼前浮现的原版图腾闪光），现改为每种图腾触发时眼前显示**其自身图标**的漂浮升起动画
  - 无尽图腾 → 眼前飘「无尽图腾」自己的蓝黑图标
  - 无限图腾 → 眼前飘「无限图腾」自己的图标
  - 附魔图腾 → 眼前飘「附魔图腾」图标（原版不死图腾外观 + 附魔流光，正是其本身样子）
- 实现（采用自定义网络包方案，彻底绕开 vanilla 字节限制）：
  - vanilla 客户端只认识少量固定实体事件字节（如 35=不死图腾），对自定义字节一律忽略。因此服务端不再广播字节事件（此前试过的 101/102/103 方案因字节不被识别而完全不触发动画）
  - 采用 1.20.1 旧版网络包 API：通道 `endless_totem:totem_fx`（`ResourceLocation`），载荷为触发复活的那支图腾的注册 id（`PacketByteBuf#writeUtf`）
  - 服务端 `TotemReviveMixin`：复活成功时用 `ServerPlayNetworking.send(sp, TOTEM_FX_CHANNEL, buf)` 向本地玩家发送图腾 id，并保留 TOTEM_USE 音效
  - 客户端 `EndlessTotemModClient`：`ClientPlayNetworking.registerGlobalReceiver` 接收包，在切回主线程后生成蓝色粒子 + 调用 `GameRenderer#displayItemActivation(对应图腾ItemStack)` 播放对应图标的眼前动画
  - 移除废弃的字节监听 mixin（`ClientTotemFxMixin`）及其 client mixins 配置
- 涉及文件：`TotemReviveMixin`（广播字节 → 发自定义网络包）/ `EndlessTotemMod`（新增 `TOTEM_FX_CHANNEL` 通道常量）/ `EndlessTotemModClient`（旧版接收器 + 粒子 + 眼前动画）/ 移除 `TotemFxPayload`、`ClientTotemFxMixin`、`endless_totem.client.mixins.json`
- 编译修复（1.20.1 专属）：
  - 按 id 取物品的方法为 `BuiltInRegistries.ITEM.get(ResourceLocation)`（`getValue` 是 Yarn 名，编不过）
  - 1.20.1 用旧版 `ServerPlayNetworking` + `PacketByteBuf` 网络包 API；`CustomPacketPayload / StreamCodec / PayloadTypeRegistry` 是 1.20.2+ 新 API，1.20.1 不存在
