package com.wujin.totem.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 附魔图腾：与普通不死图腾类似，免疫死亡，但为一次性消耗品。
 * - 复活后整体消耗 1 个（{@code shrink(1)}），用一次即消失，不依赖创造模式；
 *   刻意不设 durability，避免背包里多出一条耐久条；
 * - 效果与无限图腾相同（等级一致），但所有效果统一减半为 20 秒，
 *   伤害吸收（金色血量）改为 2 分钟；
 * - 材质采用普通不死图腾样式 + 附魔流光（isFoil 返回 true，端上渲染附魔光泽）；
 * - 复活特效用蓝色粒子（byte 100），由客户端 Mixin（client source）负责渲染。
 */
public class EnchantTotemItem extends Item {

	public EnchantTotemItem(Properties properties) {
		super(properties);
	}

	// 附魔图腾：携带原版"附魔流光"效果（类似附魔金苹果的流动光泽）
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}