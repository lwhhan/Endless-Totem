package com.wujin.totem.mixin;

import com.wujin.totem.item.EndlessBoxItem;
import com.wujin.totem.item.EndlessTotemItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

/**
 * 无尽图腾耐久 tooltip 管理。
 *
 * 背景：无尽图腾想「始终在悬停 tooltip 里显示一行当前耐久」，但 MC 1.20.1
 * 原版的耐久行（item.durability）只在物品受损伤（isDamaged）时才自动追加，
 * 损伤后又可能与 mod 手动追加的一行重复，行为不稳定。
 *
 * 方案：这里在 ItemStack#getTooltipLines 返回前，把无尽图腾的原版
 * item.durability 自动行移除；EndlessTotemItem#appendHoverText 再统一、
 * 无条件地补一行当前耐久。这样满耐久/受损任意状态都恰好一行，不缺失、不重复。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackDurabilityMixin {

	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void endlessTotem_stripVanillaDurabilityLine(@Nullable Player player, TooltipFlag flag,
			CallbackInfoReturnable<List<Component>> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		// 无尽图腾和无尽盒都用自定义翻译键自己加耐久行，原版 item.durability 行需要屏蔽掉
		if (!(stack.getItem() instanceof EndlessTotemItem)
				&& !(stack.getItem() instanceof EndlessBoxItem)) {
			return;
		}

		List<Component> lines = cir.getReturnValue();
		Iterator<Component> it = lines.iterator();
		while (it.hasNext()) {
			Component c = it.next();
			// 原版自动耐久行是 TranslatableContents，key 为 item.durability
			if (c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
					&& "item.durability".equals(tc.getKey())) {
				it.remove();
			}
		}
	}
}