package com.wujin.totem;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import com.wujin.totem.item.EndlessBoxItem;
import com.wujin.totem.item.EndlessTotemItem;
import com.wujin.totem.item.EnchantTotemItem;
import com.wujin.totem.item.InfiniteTotemItem;
import com.wujin.totem.recipe.EndlessBoxRecipe;
import com.wujin.totem.recipe.EndlessTotemRecipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndlessTotemMod implements ModInitializer {
	public static final String MOD_ID = "endless_totem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// 复活特效专用自定义网络包通道：服务端把"要显示哪个图腾图标"发给本地客户端，
	// 客户端收到后在主线程调用 GameRenderer#displayItemActivation 播放眼前漂浮动画。
	// 不在 vanilla 实体事件字节（如 35）上做，避免客户端不认识自定义字节导致特效丢失。
	public static final ResourceLocation TOTEM_FX_CHANNEL = new ResourceLocation(MOD_ID, "totem_fx");

	// 物品实例提前创建为静态字段，供自定义配方（EndlessTotemRecipe）直接引用产出物
	public static final Item ENDLESS_TOTEM =
		new EndlessTotemItem(new FabricItemSettings().stacksTo(1).durability(99));
	public static final Item INFINITE_TOTEM =
		new InfiniteTotemItem(new FabricItemSettings().stacksTo(1));
	public static final Item ENCHANT_TOTEM =
		new EnchantTotemItem(new FabricItemSettings().stacksTo(1));
	// 无尽盒：存放不死图腾的盒子，耐久 99，复活效果与原版不死图腾一致
	public static final Item ENDLESS_BOX =
		new EndlessBoxItem(new FabricItemSettings().stacksTo(1).durability(99));

	// 自定义合成配方序列化器：产出直接是 1/99 的无尽图腾，避免合成预览显示 99/99
	public static RecipeSerializer<EndlessTotemRecipe> ENDLESS_TOTEM_RECIPE;

	// 无尽盒的自定义合成配方序列化器：产出直接是 1/99 的无尽盒
	public static RecipeSerializer<EndlessBoxRecipe> ENDLESS_BOX_RECIPE;

	@Override
	public void onInitialize() {
		// 注册无尽图腾：99 耐久，免疫死亡后消耗 1 点耐久，悬停 Tooltip 始终显示剩余耐久
		Registry.register(BuiltInRegistries.ITEM, id("endless_totem"), ENDLESS_TOTEM);

		// 注册无限图腾：不可损坏（无耐久），复活不消耗耐久、永不损坏
		Registry.register(BuiltInRegistries.ITEM, id("infinite_totem"), INFINITE_TOTEM);

		// 注册附魔图腾：一次性消耗品（复活后整体消耗 1 个），效果同无限但时间减半统一 20 秒、伤害吸收 2 分钟
		Registry.register(BuiltInRegistries.ITEM, id("enchant_totem"), ENCHANT_TOTEM);

		// 注册无尽盒：耐久 99，右键可收入普通不死图腾补充耐久，复活效果同原版不死图腾
		Registry.register(BuiltInRegistries.ITEM, id("endless_box"), ENDLESS_BOX);

		// 注册无尽图腾的自定义合成配方（4 个不死图腾 2x2 → 1 个 1/99 的无尽图腾）
		ENDLESS_TOTEM_RECIPE = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			id("endless_totem"),
			new SimpleCraftingRecipeSerializer<>(EndlessTotemRecipe::new));

		// 注册无尽盒的自定义合成配方（3x3 十字：中心箱子 + 上下左右各 1 个不死图腾 → 1 个 1/99 的无尽盒）
		ENDLESS_BOX_RECIPE = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			id("endless_box"),
			new SimpleCraftingRecipeSerializer<>(EndlessBoxRecipe::new));

		// 注册自定义粒子类型（无限/附魔图腾蓝色复活特效）
		ModParticles.register();

		// 注册「无尽图腾」创造分类页（制造台已删除）
		ModRegistries.register();

		// 注册附魔图腾的箱子战利品生成：与附魔金苹果同位置、同刷新率
		ModLootInjection.register();

		// 第三方模组软兼容：仅在检测到对应模组时注册，未安装时不产生任何前置依赖
		com.wujin.totem.compat.TotemCompat.registerCompat();

		LOGGER.info("无尽图腾模组已加载！");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
