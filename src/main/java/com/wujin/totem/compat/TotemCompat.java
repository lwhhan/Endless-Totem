package com.wujin.totem.compat;

import com.wujin.totem.EndlessTotemMod;
import com.wujin.totem.item.EnchantTotemItem;
import com.wujin.totem.item.EndlessBoxItem;
import com.wujin.totem.item.EndlessTotemItem;
import com.wujin.totem.item.InfiniteTotemItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 三种图腾的公共复活逻辑，以及第三方模组（Charm of Undying / Inventory Totem）的软兼容入口。
 *
 * <p>这里的所有兼容都是「检测到才适配」：没有安装对应模组时不会加载任何相关类，
 * 也不会产生硬依赖。模组本体不依赖 trinkets、collective 等任何第三方前置。</p>
 */
public final class TotemCompat {

	/** 三种图腾的类型，id 用于发网络包 / 注册第三方效果提供器 */
	public enum Kind {
		ENDLESS("endless_totem:endless_totem"),
		INFINITE("endless_totem:infinite_totem"),
		ENCHANT("endless_totem:enchant_totem"),
		BOX("endless_totem:endless_box");

		public final String id;

		Kind(String id) {
			this.id = id;
		}
	}

	private TotemCompat() {
	}

	/** 判断物品栈是否为本模组的图腾，返回对应类型；不是或耐久已耗尽则返回 null */
	@Nullable
	public static Kind kindOf(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		Item item = stack.getItem();
		Kind kind;
		if (item instanceof EndlessTotemItem) {
			kind = Kind.ENDLESS;
		} else if (item instanceof InfiniteTotemItem) {
			kind = Kind.INFINITE;
		} else if (item instanceof EnchantTotemItem) {
			kind = Kind.ENCHANT;
		} else if (item instanceof EndlessBoxItem) {
			kind = Kind.BOX;
		} else {
			return null;
		}

		// 耐久已经见底（例如 0/99）的物品视为失效，不能再触发复活
		if (isDepleted(stack)) {
			return null;
		}
		return kind;
	}

	/** 可损坏物品的耐久是否已经耗尽（damage 达到上限） */
	public static boolean isDepleted(ItemStack stack) {
		int max = stack.getMaxDamage();
		return max > 0 && stack.getDamageValue() >= max;
	}

	/** 各图腾的复活增益 */
	public static List<MobEffectInstance> effectsOf(Kind kind) {
		return switch (kind) {
			// 无尽图腾：生命恢复 3、抗火、抗性提升 2、伤害吸收 2
			case ENDLESS -> List.of(
				new MobEffectInstance(MobEffects.REGENERATION, 400, 2),
				new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0),
				new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1),
				new MobEffectInstance(MobEffects.ABSORPTION, 400, 1));
			// 无限图腾：生命恢复 5、抗性提升 5、伤害吸收 5、抗火
			case INFINITE -> List.of(
				new MobEffectInstance(MobEffects.REGENERATION, 900, 4),
				new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 800, 4),
				new MobEffectInstance(MobEffects.ABSORPTION, 800, 4),
				new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
			// 附魔图腾（一次性）：效果同无限但统一减半为 20 秒，伤害吸收 2 分钟
			case ENCHANT -> List.of(
				new MobEffectInstance(MobEffects.REGENERATION, 400, 4),
				new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 4),
				new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4),
				new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
			// 无尽盒：与原版不死图腾完全一致——生命恢复 II 45 秒、伤害吸收 II 5 秒、抗火 40 秒，
			// 没有抗性提升（原版图腾本身就没有）
			case BOX -> List.of(
				new MobEffectInstance(MobEffects.REGENERATION, 900, 1),
				new MobEffectInstance(MobEffects.ABSORPTION, 100, 1),
				new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
		};
	}

	/** 只施加复活增益（回血、清效果、加 buff），不处理消耗 */
	public static void applyEffects(LivingEntity entity, Kind kind) {
		entity.setHealth(1.0F);
		entity.removeAllEffects();
		for (MobEffectInstance effect : effectsOf(kind)) {
			entity.addEffect(effect);
		}
	}

	/** 按类型消耗图腾：无尽扣 1 点耐久（扣满消失），无尽盒扣 1 点耐久（扣满停在 0/99 保留），无限不消耗，附魔整体消耗 1 个 */
	public static void consume(ItemStack stack, Kind kind, LivingEntity entity) {
		if (entity instanceof Player player && player.isCreative()) {
			return;
		}
		switch (kind) {
			// 无尽图腾：扣满 99 点后整体消耗，用完即消失
			case ENDLESS -> damageOne(stack, entity, true);
			// 无尽盒：扣满后停在 0/99，物品保留但不再生效，可以继续用不死图腾充能
			case BOX -> damageOne(stack, entity, false);
			case INFINITE -> {
				// 无限耐久，不消耗
			}
			case ENCHANT -> stack.shrink(1);
		}
	}

	/**
	 * 扣 1 点耐久。
	 *
	 * <p>不用 {@code ItemStack#hurt}，是因为不同版本对「damage 达到上限」的处理不一致。
	 * 这里把「扣满之后怎么办」交给调用方决定：</p>
	 *
	 * @param destroyWhenEmpty {@code true} = 扣满后整体消耗 1 个（无尽图腾，用完即消失）；
	 *                         {@code false} = 扣满后停在 0/99 保留物品（无尽盒，之后不再生效）
	 */
	private static void damageOne(ItemStack stack, LivingEntity entity, boolean destroyWhenEmpty) {
		int max = stack.getMaxDamage();
		if (max <= 0) {
			return;
		}
		int next = stack.getDamageValue() + 1;
		if (next >= max) {
			if (destroyWhenEmpty) {
				stack.shrink(1);
				if (entity instanceof ServerPlayer sp) {
					sp.level().playSound(null, sp.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
				}
			} else {
				// 无尽盒耐久归零：物品留下，只是不再生效（也不播破坏音效，东西并没有坏掉）
				stack.setDamageValue(max);
			}
		} else {
			stack.setDamageValue(next);
		}
	}

	/**
	 * 供 Charm of Undying 的 modifyStack 使用：该回调没有实体上下文，
	 * 只能直接改 damage / 数量，无法走 hurt 的统计与音效。
	 */
	public static void consumeForCharm(ItemStack stack, Kind kind) {
		switch (kind) {
			case ENDLESS -> {
				int max = stack.getMaxDamage();
				if (max <= 0) {
					return;
				}
				int next = stack.getDamageValue() + 1;
				if (next >= max) {
					stack.shrink(1);
				} else {
					stack.setDamageValue(next);
				}
			}
			// 无尽盒：扣满后停在 0/99 保留物品，不像无尽图腾那样消失
			case BOX -> {
				int max = stack.getMaxDamage();
				if (max <= 0) {
					return;
				}
				int next = stack.getDamageValue() + 1;
				stack.setDamageValue(Math.min(next, max));
			}
			case INFINITE -> {
				// 无限耐久，不消耗
			}
			case ENCHANT -> stack.shrink(1);
		}
	}

	/** 播放复活特效：自定义网络包 + 图腾使用音效 */
	public static void playFx(LivingEntity entity, Kind kind) {
		if (entity instanceof ServerPlayer sp) {
			FriendlyByteBuf buf = PacketByteBufs.create();
			buf.writeUtf(kind.id);
			ServerPlayNetworking.send(sp, EndlessTotemMod.TOTEM_FX_CHANNEL, buf);
			sp.level().playSound(null, sp.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	/** 完整复活流程：增益 + 消耗 + 特效 */
	public static boolean revive(LivingEntity entity, ItemStack stack, Kind kind) {
		applyEffects(entity, kind);
		consume(stack, kind, entity);
		playFx(entity, kind);
		return true;
	}

	/**
	 * 模组初始化时调用：按已加载的模组注册软兼容。
	 * 未安装时直接跳过，不加载任何第三方类，也不产生前置依赖。
	 */
	public static void registerCompat() {
		if (FabricLoader.getInstance().isModLoaded("charmofundying")) {
			try {
				CharmOfUndyingCompat.init();
			} catch (Throwable t) {
				EndlessTotemMod.LOGGER.warn("[无尽图腾] Charm of Undying 兼容初始化失败：{}", t.toString());
			}
		}

		if (FabricLoader.getInstance().isModLoaded("inventorytotem")) {
			try {
				InventoryTotemCompat.init();
			} catch (Throwable t) {
				EndlessTotemMod.LOGGER.warn("[无尽图腾] Inventory Totem 兼容初始化失败：{}", t.toString());
			}
		}
	}
}
