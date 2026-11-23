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
 * 无尽图腾合成配方：4 个不死图腾摆成 2x2 正方形 → 1 个无尽图腾，产出直接是 1/99 耐久。
 *
 * <p>用自定义配方而不是普通 shaped 配方，是因为 1.20.1 的 shaped 配方 result 不支持写 damage，
 * 只能在玩家取出时用 onCraftedBy 补救，导致合成预览先显示 99/99。
 * 这里让 {@link #getResultItem} 和 {@link #assemble} 都返回 1/99，预览和取出完全一致。</p>
 */
public class EndlessTotemRecipe extends CustomRecipe {

	public EndlessTotemRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		int width = container.getWidth();
		int height = container.getHeight();
		if (width < 2 || height < 2) {
			return false;
		}

		// 允许 2x2（背包合成格）和 3x3（工作台）里任意位置的 2x2 正方形摆放
		for (int x = 0; x <= width - 2; x++) {
			for (int y = 0; y <= height - 2; y++) {
				if (matchesAt(container, x, y)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean matchesAt(CraftingContainer container, int originX, int originY) {
		int width = container.getWidth();
		int height = container.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ItemStack stack = container.getItem(x + y * width);
				boolean inside = x >= originX && x < originX + 2 && y >= originY && y < originY + 2;

				if (inside) {
					if (!stack.is(Items.TOTEM_OF_UNDYING)) {
						return false;
					}
				} else if (!stack.isEmpty()) {
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
		ItemStack stack = new ItemStack(EndlessTotemMod.ENDLESS_TOTEM);
		int max = stack.getMaxDamage();
		if (max > 0) {
			stack.setDamageValue(max - 1);
		}
		return stack;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 2 && height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return EndlessTotemMod.ENDLESS_TOTEM_RECIPE;
	}
}
