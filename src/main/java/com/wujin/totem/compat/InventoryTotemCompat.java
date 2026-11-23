package com.wujin.totem.compat;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Inventory Totem 软兼容：该模组让「背包里的不死图腾」也能触发复活，
 * 但它硬编码只认原版 {@code Items.TOTEM_OF_UNDYING}，不认识本模组的图腾。
 *
 * <p>这里补上同样的行为：当背包（非主副手）里有三种图腾之一时也能复活。
 * 只在检测到该模组时才注册，未安装时完全不影响原版手感。</p>
 */
public final class InventoryTotemCompat {

	private InventoryTotemCompat() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) {
				return true;
			}

			// 无视无敌的伤害（虚空、/kill）不触发，与主副手路径一致
			if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				return true;
			}

			// 主副手已有图腾时交给 TotemReviveMixin，避免同一件物品被处理两次
			for (InteractionHand hand : InteractionHand.values()) {
				if (TotemCompat.kindOf(player.getItemInHand(hand)) != null) {
					return true;
				}
			}

			Inventory inventory = player.getInventory();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				ItemStack stack = inventory.getItem(i);
				TotemCompat.Kind kind = TotemCompat.kindOf(stack);
				if (kind != null) {
					TotemCompat.revive(player, stack, kind);
					// 返回 false 阻止死亡
					return false;
				}
			}

			return true;
		});

		com.wujin.totem.EndlessTotemMod.LOGGER.info("[无尽图腾] 已注册 Inventory Totem 兼容（背包内图腾可触发复活）");
	}
}
