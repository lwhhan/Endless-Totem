package com.wujin.totem.mixin;

import com.wujin.totem.item.EndlessTotemItem;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 合成预览修正：让合成结果槽里的无尽图腾直接显示 1/99。
 *
 * 普通 shaped 配方只能产出满耐久物品，玩家取出时才会由 EndlessTotemItem#onCraftedBy
 * 把 damage 改成 98，因此结果槽预览会先显示 99/99。
 * 这里在结果槽重新计算完成（slotsChanged TAIL）后，把预览物品的 damage 补上，
 * 让工作台（CraftingMenu）和背包 2x2 合成格（InventoryMenu）都显示 1/99。
 */
@Mixin({InventoryMenu.class, CraftingMenu.class})
public class CraftingPreviewMixin {

	@Shadow
	@Final
	private ResultContainer resultSlots;

	@Inject(method = "slotsChanged", at = @At("TAIL"))
	private void endlessTotem_fixCraftPreview(Container container, CallbackInfo ci) {
		ItemStack stack = this.resultSlots.getItem(0);
		if (stack.getItem() instanceof EndlessTotemItem
				&& stack.getMaxDamage() > 0
				&& stack.getDamageValue() == 0) {
			stack.setDamageValue(stack.getMaxDamage() - 1);
			this.resultSlots.setChanged();
		}
	}
}
