package com.wujin.totem.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 无尽图腾：与不死图腾功能相似，免疫死亡。
 * - 耐久 99，免疫死亡后消耗 1 点耐久；
 * - 免疫死亡后提供：生命恢复 3、抗火、抗性提升 2、伤害吸收 2；
 * - 背包内不显示图标耐久条（isBarVisible 返回 false）；
 * - Tooltip 耐久显示规则：始终显示一行「耐久度 x/y」，满耐久与受损都显示。
 *   自家耐久行用自定义翻译键 item.endless_totem.endless_totem.durability，
 *   与 ItemStackDurabilityMixin 屏蔽的"原版 item.durability 自动行"区分开，
 *   避免 mixin 把我们自己加的那行也删掉。
 *
 * 合成：4 个不死图腾 2x2 正方形合成 1 个无尽图腾，产出初始耐久为 1/99。
 * 注意：v42 起「右键收入不死图腾」的充能功能已迁移到「无尽盒」（EndlessBoxItem），
 *       无尽图腾本身不再能补充耐久，用完即消耗。
 */
public class EndlessTotemItem extends Item {

	public EndlessTotemItem(Properties properties) {
		super(properties);
	}

	// 无尽图腾：背包内隐藏图标耐久条
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return false;
	}

	// 悬停 Tooltip：无条件始终显示一行「耐久度 当前/上限」，满耐久与受损都显示。
	// 原版自动追加的耐久行由 ItemStackDurabilityMixin 屏蔽，避免任何状态出现两行。
	// 注意：这里用自定义翻译键（非 item.durability），否则会被 ItemStackDurabilityMixin 一并删掉。
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		int maxDurability = stack.getMaxDamage();
		int current = maxDurability - stack.getDamageValue();
		if (current < 0) {
			current = 0;
		}
		tooltip.add(Component.translatable("item.endless_totem.endless_totem.durability", current, maxDurability));
	}

	/**
	 * 合成产出：4 个不死图腾正方形合成后，初始耐久为 1/99。
	 * 配方本身只能产出满耐久物品，这里在玩家从合成结果槽取出的瞬间把 damage 设为 98。
	 * 只处理满耐久的全新产出，避免影响其它途径获得的物品。
	 */
	@Override
	public void onCraftedBy(ItemStack stack, Level level, Player player) {
		super.onCraftedBy(stack, level, player);
		if (stack.getMaxDamage() > 0 && stack.getDamageValue() == 0) {
			stack.setDamageValue(stack.getMaxDamage() - 1);
		}
	}
}
