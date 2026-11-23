# 无尽图腾 · Endless Totem

> Minecraft 1.20.1 · Fabric · 让你一条命可以死很多次
>
> 仓库：https://github.com/lwhhan/Endless-Totem

一个 Fabric 模组，在保留原版不死图腾手感的前提下，加了三种进阶图腾和一个「图腾收纳盒」。
所有复活逻辑都走同一套统一注册，主手、副手、背包、饰品槽四条路径效果一致。

---

## 内容一览

| 物品 | 耐久 / 消耗 | 复活后效果 | 获取方式 |
| --- | --- | --- | --- |
| **无尽图腾** | 99 点耐久，每次复活 -1，扣满整体消失 | 生命恢复 III 20s · 抗火 30s · 抗性提升 II 20s · 伤害吸收 II 20s | 2×2 摆放 4 个不死图腾 |
| **∞图腾** | 无限耐久，永不消耗 | 生命恢复 V 45s · 抗性提升 V 40s · 伤害吸收 V 40s · 抗火 40s | 暂无合成配方，创造模式获取 |
| **附魔图腾** | 一次性，复活后消耗 1 个 | 生命恢复 V 20s · 抗性提升 V 20s · 伤害吸收 V 120s · 抗火 20s | 箱子战利品，刷新率对齐附魔金苹果 |
| **无尽盒** | 99 点耐久，**默认 0/99**，≥1 才生效，0 时不生效也不消失 | 生命恢复 II 45s · 伤害吸收 II 5s · 抗火 40s（与原版不死图腾完全一致） | 3×3 十字：中心 1 个箱子 + 上下左右各 1 个不死图腾 |

> 效果等级按原版显示习惯：III = 3 级，V = 5 级。数值与原版一致，复活时统一把血量设为 1 并清除身上所有状态。

## 合成与获取

**无尽图腾**（2×2，背包合成格或工作台任意 2×2 区域均可）

```
[不死图腾] [不死图腾]
[不死图腾] [不死图腾]
```

产出 1 个 **1/99** 的无尽图腾，可以直接用。

**无尽盒**（3×3 十字，四角必须留空）

```
[　]      [不死图腾] [　]
[不死图腾] [ 箱子 ]  [不死图腾]
[　]      [不死图腾] [　]
```

产出 1 个 **0/99** 的空盒，必须先装图腾才能生效。

**附魔图腾**：不走合成，注入到 1.20.1 里所有含附魔金苹果的箱子战利品表，
每个表的出现概率与该表附魔金苹果的概率对齐：

`废弃矿井` `远古城市` `堡垒遗迹宝藏` `沙漠神殿` `废弃传送门` `地牢` `林地府邸`

## 无尽盒怎么用

1. 合成出来的无尽盒是 **0/99**，此时不生效。
2. 在背包界面**左键拿起**无尽盒（让它悬浮在鼠标光标上）。
3. **右键**背包里的普通不死图腾 → 图腾 -1，无尽盒耐久 +1。
4. 耐久 ≥ 1 即可正常复活；扣到 0 时物品**不会消失**，继续装图腾就能接着用。

耐久条在背包里是隐藏的，悬停时显示一行「耐久度 当前 / 上限」。

## 兼容性

全部为**软兼容**：检测到才适配，没装就完全跳过，不产生任何硬前置。

| 模组 | 效果 |
| --- | --- |
| [Charm of Undying](https://modrinth.com/mod/charm-of-undying) | 饰品槽里的本模组图腾同样能触发复活 |
| [Inventory Totem](https://modrinth.com/mod/inventory-totem) | 背包里的本模组图腾同样能触发复活 |
| Trinkets | 已把四种图腾加入 `trinkets:charm/charm` 标签 |

主手 / 副手 / 背包 / 饰品槽四条路径共用同一套判定，不会出现「一次死亡结算两次」的问题。

## 环境要求

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.20.1 |
| Fabric Loader | ≥ 0.19.3 |
| Fabric API | ≥ 0.92.9+1.20.1 |
| Java | 17 |

## 安装

1. 装好 Fabric Loader 和 Fabric API。
2. 把 `无尽图腾_EndlessTotem-x.x.x.jar` 丢进 `.minecraft/mods/`。
3. 启动游戏，物品在「无尽图腾」创造分类页里。

## 从源码构建

```bash
./gradlew build
# Windows
gradlew.bat build
```

产物在 `build/libs/`。

> 国内网络如果下载 Gradle 发行包慢，可以把 `gradle/wrapper/gradle-wrapper.properties`
> 里的 `distributionUrl` 换成腾讯云镜像：
> `https\://mirrors.cloud.tencent.com/gradle/gradle-9.5.1-bin.zip`

## 项目结构

```
src/main/java/com/wujin/totem/
├── EndlessTotemMod.java        模组入口：物品与配方注册
├── ModRegistries.java          创造分类页
├── ModParticles.java           自定义粒子类型
├── ModLootInjection.java       附魔图腾的箱子战利品注入
├── compat/
│   ├── TotemCompat.java        统一复活逻辑 + 软兼容注册入口
│   ├── CharmOfUndyingCompat.java
│   └── InventoryTotemCompat.java
├── item/                       四种图腾物品
├── recipe/                     两个自定义配方
└── mixin/                      复活、耐久显示、收纳交互

src/client/java/com/wujin/totem/client/
├── EndlessTotemModClient.java  客户端入口：粒子注册 + 复活特效网络包
└── particle/                   蓝色复活粒子
```

## 更新日志

见 [CHANGELOG.md](CHANGELOG.md)。

## 许可证

[MIT](LICENSE) © 2026 lwhhan
