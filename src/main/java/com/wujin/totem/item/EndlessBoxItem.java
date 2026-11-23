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
 * 无尽盒：一个用来存放不死图腾的盒子。
 * - 耐久上限 99，<b>默认是 0/99 的空盒</b>，必须先收纳不死图腾才能生效；
 * - 耐久 ≥ 1 才生效，耐久为 0 时不生效、但也<b>不会消失</b>（可以继续收纳图腾补充）；
 * - 复活效果与原版不死图腾完全一致（生命恢复 II 45 秒 / 伤害吸收 II 5 秒 / 抗火 40 秒），不带抗性提升；
 * - 收纳：在背包界面用鼠标左键拿起无尽盒（悬浮在光标上），右键背包中的普通不死图腾
 *   → 普通图腾消失 1 个，无尽盒耐久 +1。逻辑由 ContainerClickMixin 拦截
 *   AbstractContainerMenu#clicked 实现；
 * - 背包内不显示耐久条，悬停 Tooltip 始终显示一行剩余耐久；
 * - 与 Charm of Undying（饰品槽）、Inventory Totem（背包）软兼容，走 TotemCompat 统一注册。
 */
public class EndlessBoxItem extends Item {

	public EndlessBoxItem(Properties properties) {
		super(properties);
	}

	// 背包内隐藏图标耐久条，只保留悬停 Tooltip 的剩余耐久显示
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return false;
	}

	// 悬停 Tooltip：只显示一行「耐久度 当前/上限」，满耐久与受损都显示。
	// 用自定义翻译键（非 item.durability），避免被 ItemStackDurabilityMixin 一并删掉。
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		int maxDurability = stack.getMaxDamage();
		int current = maxDurability - stack.getDamageValue();
		if (current < 0) {
			current = 0;
		}
		tooltip.add(Component.translatable("item.endless_totem.endless_box.durability", current, maxDurability));
	}

	/**
	 * 合成产出：初始耐久为 0/99（空盒），必须先收纳不死图腾才能生效。
	 * 配方本身已经直接产出 0/99，这里是双保险，只处理全新满耐久的产出。
	 */
	@Override
	public void onCraftedBy(ItemStack stack, Level level, Player player) {
		super.onCraftedBy(stack, level, player);
		if (stack.getMaxDamage() > 0 && stack.getDamageValue() == 0) {
			stack.setDamageValue(stack.getMaxDamage());
		}
	}
}
