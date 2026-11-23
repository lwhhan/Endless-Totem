package com.wujin.totem.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 三图腾共用的复活粒子：亮蓝色光点，短生命周期、缓缓淡出，围绕身体扩散上升。
 * <p>注意：项目使用 Mojang 官方映射，粒子基类是 {@code TextureSheetParticle}
 * （Yarn 草映射中才叫 SpriteParticle）。</p>
 */
@Environment(EnvType.CLIENT)
public class InfiniteTotemParticle extends TextureSheetParticle {

	private InfiniteTotemParticle(ClientLevel level, double x, double y, double z,
			double xd, double yd, double zd, SpriteSet sprites) {
		super(level, x, y, z);
		this.lifetime = 46;
		// 无限图腾主题亮蓝色
		this.rCol = 0.10f;
		this.gCol = 0.55f;
		this.bCol = 1.00f;
		this.alpha = 0.92f;
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.quadSize = 0.34f;
		this.hasPhysics = false;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.age > 28) {
			this.setAlpha(1.0f - (float) (this.age - 28) / (this.lifetime - 28));
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Factory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level,
				double x, double y, double z, double xd, double yd, double zd) {
			return new InfiniteTotemParticle(level, x, y, z, xd, yd, zd, this.sprites);
		}
	}
}