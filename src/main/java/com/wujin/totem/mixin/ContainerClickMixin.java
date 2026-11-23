package com.wujin.totem.mixin;

import com.wujin.totem.item.EndlessBoxItem;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 无尽盒收纳不死图腾：
 * 鼠标左键拿起无尽盒（物品悬浮在光标上，即菜单的 carried stack），
 * 再右键背包中的普通不死图腾 → 普通图腾消失 1 个，无尽盒耐久 +1。
 *
 * <p>注意：这个收纳功能只属于「无尽盒」。无尽图腾（强化效果那件）已经移除充能，
 * 不能再用不死图腾补充耐久。</p>
 *
 * 拦截 AbstractContainerMenu#clicked 的 HEAD：
 * - 只处理 PICKUP + 右键（button == 1）；
 * - 光标物品必须是受损的无尽图腾；
 * - 被点击槽位必须是普通不死图腾且允许取出；
 * - 处理完取消原版逻辑，避免原版把无尽图腾放进该槽位。
 * 客户端与服务端都会执行同一判断，保证本地预测与服务端结果一致。
 */
@Mixin(AbstractContainerMenu.class)
public class ContainerClickMixin {

	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	private void endlessTotem_chargeFromInventory(int slotId, int button, ClickType clickType, Player player,
			CallbackInfo ci) {
		// 只处理「鼠标右键点击槽位」
		if (clickType != ClickType.PICKUP || button != 1) {
			return;
		}

		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

		// 点击菜单外（slotId 为 -999）或非法槽位直接放行
		if (slotId < 0 || slotId >= menu.slots.size()) {
			return;
		}

		// 光标上拿着的必须是有损耗的无尽盒
		ItemStack carried = menu.getCarried();
		if (!(carried.getItem() instanceof EndlessBoxItem) || carried.getDamageValue() <= 0) {
			return;
		}

		Slot slot = menu.slots.get(slotId);
		if (!slot.mayPickup(player)) {
			return;
		}

		// 被点击的槽位里必须是普通不死图腾
		ItemStack slotStack = slot.getItem();
		if (!slotStack.is(Items.TOTEM_OF_UNDYING)) {
			return;
		}

		// 消耗 1 个普通不死图腾，光标上的无尽盒恢复 1 点耐久
		slot.remove(1);
		slot.setChanged();
		carried.setDamageValue(carried.getDamageValue() - 1);
		menu.setCarried(carried);

		if (!player.level().isClientSide) {
			menu.broadcastChanges();
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
		}

		// 取消原版右键放置逻辑，避免原版把无尽盒放进该槽位
		ci.cancel();
	}
}
