package com.wujin.totem.client;

import com.wujin.totem.EndlessTotemMod;
import com.wujin.totem.ModParticles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.wujin.totem.client.particle.InfiniteTotemParticle;

/**
 * 客户端初始化：注册蓝色粒子工厂，并接收服务端发来的复活特效自定义网络包。
 * <p>收到后（切回主线程执行）：<ol>
 * <li>在本地玩家周身生成亮蓝色复活粒子；</li>
 * <li>用网络包携带的图腾 id 取出对应 ItemStack，调用
 * {@code GameRenderer#displayItemActivation} 播放该图腾「自身图标」的眼前漂浮升起动画
 * （替代 vanilla 固定显示的原版不死图腾）。</li></ol>
 * 附魔图腾本身就是原版不死图腾贴图 + 附魔流光，无尽图腾显示蓝黑自绘图标，
 * 无限图腾显示无限图标——每种图腾各自显示自己的样子。</p>
 */
public class EndlessTotemModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 注册无限图腾 / 附魔图腾的蓝色粒子
		ParticleFactoryRegistry.getInstance().register(ModParticles.INFINITE_TOTEM, InfiniteTotemParticle.Factory::new);

		// 接收复活特效网络包：切回主线程后触发眼前图腾动画 + 蓝色粒子
		ClientPlayNetworking.registerGlobalReceiver(EndlessTotemMod.TOTEM_FX_CHANNEL, (client, handler, buf, responseSender) -> {
			String totemId = buf.readUtf();
			client.execute(() -> {
				var player = client.player;
				if (player == null) {
					return;
				}

				// 1) 生成蓝色复活粒子
				this.spawnInfiniteParticles(player, client.level);

				// 2) 眼前图标动画：仅本地玩家自己播放
				var item = BuiltInRegistries.ITEM.get(new ResourceLocation(totemId));
				if (item != null) {
					client.gameRenderer.displayItemActivation(new ItemStack(item));
				}
			});
		});
	}

	// 在复活实体周身生成扩散上升的蓝色光点，营造"无限图腾"复活特效
	private void spawnInfiniteParticles(net.minecraft.world.entity.Entity entity, ClientLevel level) {
		if (entity == null || level == null) {
			return;
		}
		// 中心一道上升光柱
		level.addParticle(ModParticles.INFINITE_TOTEM,
			entity.getX(), entity.getY() + 0.8, entity.getZ(), 0.0, 0.07, 0.0);
		// 周身扩散飞散的蓝色光点
		for (int i = 0; i < 60; ++i) {
			double dx = level.random.nextGaussian() * 0.25;
			double dz = level.random.nextGaussian() * 0.25;
			double dy = level.random.nextDouble() * 0.5;
			level.addParticle(ModParticles.INFINITE_TOTEM,
				entity.getX() + dx, entity.getY() + 0.3 + dy, entity.getZ() + dz,
				dx, 0.08 + dy * 0.12, dz);
		}
	}
}