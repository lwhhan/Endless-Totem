package com.wujin.totem.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 无限图腾：同样具备免疫死亡的能力。
 * - 无限耐久：复活时不消耗耐久、永不损坏（不可损坏物品，无剩余耐久显示）；
 * - 免疫死亡后提供：生命恢复 5、抗性提升 5、伤害吸收 5、抗火；
 * - 背包内不显示耐久条（isBarVisible 返回 false），tooltip 也不显示剩余耐久。
 */
public class InfiniteTotemItem extends Item {

	public InfiniteTotemItem(Properties properties) {
		super(properties);
	}

	// 无限图腾：与无尽图腾一致，背包内隐藏耐久条（仅保留悬停 Tooltip 的剩余耐久显示）
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return false;
	}
}