package com.wujin.totem.compat;

import com.wujin.totem.EndlessTotemMod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Charm of Undying 软兼容：把三种图腾注册进它的 {@code TotemProviders}，
 * 这样图腾放进 Trinkets 的 charm 饰品槽后也能触发复活。
 *
 * <p>全部通过反射 + 动态代理完成，编译期不引用对方任何类，未安装该模组时不会加载。</p>
 */
public final class CharmOfUndyingCompat {

	private static final String PROVIDERS_CLASS = "com.illusivesoulworks.charmofundying.common.TotemProviders";
	private static final String PROVIDER_INTERFACE = "com.illusivesoulworks.charmofundying.common.ITotemEffectProvider";

	private CharmOfUndyingCompat() {
	}

	public static void init() throws Exception {
		Class<?> providerInterface = Class.forName(PROVIDER_INTERFACE);
		Class<?> providersClass = Class.forName(PROVIDERS_CLASS);
		Method put = providersClass.getMethod("putEffectProvider", String.class, providerInterface);

		for (TotemCompat.Kind kind : TotemCompat.Kind.values()) {
			Object provider = createProvider(providerInterface, kind);
			put.invoke(null, kind.id, provider);
		}

		EndlessTotemMod.LOGGER.info("[无尽图腾] 已注册 Charm of Undying 图腾效果提供器（charm 饰品槽可触发复活）");
	}

	private static Object createProvider(Class<?> providerInterface, TotemCompat.Kind kind) {
		ClassLoader loader = providerInterface.getClassLoader();
		return Proxy.newProxyInstance(loader, new Class<?>[]{providerInterface}, (proxy, method, args) -> {
			switch (method.getName()) {
				// 不挡虚空等「无视无敌」伤害，与主副手路径保持一致
				case "bypassInvul":
					return false;
				// 该回调没有实体上下文，只做消耗；耐久已见底的物品不再消耗，也不会复活
				case "modifyStack": {
					ItemStack stack = (ItemStack) args[0];
					if (!TotemCompat.isDepleted(stack)) {
						TotemCompat.consumeForCharm(stack, kind);
					}
					return null;
				}
				case "applyEffects": {
					LivingEntity entity = (LivingEntity) args[0];
					// DamageSource 由对方传入，当前三种图腾的效果不依赖伤害来源
					DamageSource ignored = (DamageSource) args[1];
					// args[2] 是 modifyStack 之前的副本，用它判断原始耐久是否已经耗尽
					ItemStack original = (ItemStack) args[2];
					if (TotemCompat.isDepleted(original)) {
						return false;
					}
					TotemCompat.applyEffects(entity, kind);
					TotemCompat.playFx(entity, kind);
					return true;
				}
				case "toString":
					return "EndlessTotemProvider[" + kind.id + "]";
				case "hashCode":
					return System.identityHashCode(proxy);
				case "equals":
					return proxy == args[0];
				default:
					return null;
			}
		});
	}
}
