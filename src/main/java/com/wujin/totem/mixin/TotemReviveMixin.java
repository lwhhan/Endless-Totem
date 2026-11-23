package com.wujin.totem.mixin;

import com.wujin.totem.compat.TotemCompat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class TotemReviveMixin {

	// 在 checkTotemDeathProtection（官方映射名）入口处拦截：如果主/副手持有无尽图腾、无限图腾或附魔图腾，则执行自定义复活逻辑
	@Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
	private void endlessTotem_checkTotemRevive(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;

		// 无法被不死图腾抵挡的伤害（如虚空、/kill），同样不触发
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return;
		}

		// 依次检查主手、副手
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = self.getItemInHand(hand);
			TotemCompat.Kind kind = TotemCompat.kindOf(stack);

			if (kind != null) {
				TotemCompat.revive(self, stack, kind);
				// setReturnValue 只设置返回值，方法仍会继续往下执行。
				// 装了 Charm of Undying 时，它在 InteractionHand.values() 调用点还有一处注入，
				// 会再走一次 useTotem（重复扣耐久 + 重复施加效果）。
				// cancel() 直接中断方法，保证一次死亡只结算一次。
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}
		}
	}
}
