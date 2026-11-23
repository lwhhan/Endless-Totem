package com.wujin.totem.recipe;

import com.wujin.totem.EndlessTotemMod;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 无尽盒合成配方：工作台 3x3 摆成十字 —— 中心一个箱子，上下左右各一个不死图腾，四角留空。
 *
 * <pre>
 *      [图腾]
 * [图腾][箱子][图腾]
 *      [图腾]
 * </pre>
 *
 * <p>产出是 <b>0/99</b> 耐久的空无尽盒：默认没有耐久，必须先收纳不死图腾才能生效。
 * 用自定义配方而不是 shaped 配方，是因为 1.20.1 的 shaped 配方 result 不支持写 damage，
 * 而这里必须让合成预览和取出结果都精确等于 0/99。</p>
 */
public class EndlessBoxRecipe extends CustomRecipe {

	public EndlessBoxRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		// 十字形占满 3x3，背包的 2x2 合成格放不下
		if (container.getWidth() != 3 || container.getHeight() != 3) {
			return false;
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				ItemStack stack = container.getItem(x + y * 3);
				boolean isCenter = x == 1 && y == 1;
				boolean isCross = (x == 1) != (y == 1); // 上下左右四个格子，异或为真

				if (isCenter) {
					if (!stack.is(Items.CHEST)) {
						return false;
					}
				} else if (isCross) {
					if (!stack.is(Items.TOTEM_OF_UNDYING)) {
						return false;
					}
				} else if (!stack.isEmpty()) {
					// 四角必须留空
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		return createResult();
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return createResult();
	}

	private static ItemStack createResult() {
		ItemStack stack = new ItemStack(EndlessTotemMod.ENDLESS_BOX);
		int max = stack.getMaxDamage();
		if (max > 0) {
			// damage = max 就是「剩余耐久 0」，合成出来是空的 0/99，必须先收纳不死图腾才生效
			stack.setDamageValue(max);
		}
		return stack;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return EndlessTotemMod.ENDLESS_BOX_RECIPE;
	}
}
