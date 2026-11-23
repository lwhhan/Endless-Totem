package com.wujin.totem;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * 自定义粒子类型注册。
 * 目前仅一个：{@code infinite_totem}——无尽 / 无限 / 附魔 三个图腾共用的亮蓝色复活粒子
 * （服务端均广播实体事件 100，客户端渲染蓝色光点），与蓝色系图标一致。
 */
public final class ModParticles {

	/** 三图腾复活特效共用的亮蓝色粒子 */
	public static final SimpleParticleType INFINITE_TOTEM = FabricParticleTypes.simple();

	private ModParticles() {
	}

	/** 在模组初始化时调用，向游戏注册粒子类型 */
	public static void register() {
		Registry.register(BuiltInRegistries.PARTICLE_TYPE,
			new ResourceLocation(EndlessTotemMod.MOD_ID, "infinite_totem"), INFINITE_TOTEM);
	}
}