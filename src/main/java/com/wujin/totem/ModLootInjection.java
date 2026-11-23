package com.wujin.totem;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

/**
 * 附魔图腾的箱子战利品注入。
 *
 * 目标：让「附魔图腾」在箱子里的产出位置与刷新率，和原版「附魔金苹果」一致。
 * 实现：对 1.20.1 所有含附魔金苹果的箱子战利品表，各自新增一条独立 roll——
 * 每个箱子以等于该表附魔金苹果出现概率的几率，掉落 1 个附魔图腾。
 * 概率通过「空条目」权重反向控制实现（附魔图腾权重 1，空条目权重让
 * 1/(1+空权重) ≈ 该表附魔金苹果概率）。
 *
 * 注意：1.20.1（fabric-api 0.92.9）的 LootTableEvents.MODIFY 回调签名是
 * (ResourceManager, LootManager, Identifier, LootTable.Builder, LootTableSource)，
 * 通过 LootTable.Builder.withPool(...) 追加池，故 lambda 采用隐式类型推断。
 */
public final class ModLootInjection {

	private static final ResourceLocation ENCHANT_TOTEM =
		new ResourceLocation(EndlessTotemMod.MOD_ID, "enchant_totem");

	private ModLootInjection() {
	}

	public static void register() {
		// 目标箱子表 → 空条目权重。期望概率 = 1/(1+weight)，对齐该表附魔金苹果概率。
		// abandoned_mineshaft ~1.4% → 1/71
		// ancient_city       ~8.4% → 1/12
		// bastion_treasure   ~5.9% → 1/17
		// desert_pyramid     ~2.4% → 1/42
		// ruined_portal      ~1.5% → 1/67
		// simple_dungeon     ~2.8% → 1/36
		// woodland_mansion   ~3.1% → 1/33
		addPool("chests/abandoned_mineshaft", 70);
		addPool("chests/ancient_city", 11);
		addPool("chests/bastion_treasure", 16);
		addPool("chests/desert_pyramid", 41);
		addPool("chests/ruined_portal", 66);
		addPool("chests/simple_dungeon", 35);
		addPool("chests/woodland_mansion", 32);
	}

	private static void addPool(String chestPath, int emptyWeight) {
		// loot table 实际资源 id 是 minecraft:chests/xxx，与回调的 id 参数（ResourceLocation）比较
		ResourceLocation id = new ResourceLocation("minecraft", chestPath);
		LootTableEvents.MODIFY.register((resourceManager, lootManager, tableId, tableBuilder, source) -> {
			// 只改内置战利品表，避免误伤数据包自定义表；再校验目标表 id
			if (!source.isBuiltin() || !id.equals(tableId)) {
				return;
			}
			LootPool.Builder pool = LootPool.lootPool();
			pool.add(LootItem.lootTableItem(
					BuiltInRegistries.ITEM.get(ENCHANT_TOTEM)).setWeight(1));
			pool.add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
			tableBuilder.withPool(pool);
		});
	}
}