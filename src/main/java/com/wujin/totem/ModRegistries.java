package com.wujin.totem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * 集中注册本模组新增物品与创造分类页；方块/菜单相关（制造台）已删除。
 * 均以 Mojang 官方映射注册，供主类初始化。
 */
public final class ModRegistries {
	public static final String MOD_ID = EndlessTotemMod.MOD_ID;

	/** 「无尽图腾」自定义创造分类页：收纳无尽图腾 / 无限图腾 / 附魔图腾 / 无尽盒 */
	public static final CreativeModeTab ENDLESS_TOTEM_TAB = FabricItemGroup.builder()
		.icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(id("endless_totem"))))
		.title(Component.translatable("itemGroup.endless_totem"))
		.displayItems((displayContext, entries) -> {
			entries.accept(BuiltInRegistries.ITEM.get(id("endless_totem")));
			entries.accept(BuiltInRegistries.ITEM.get(id("infinite_totem")));
			entries.accept(BuiltInRegistries.ITEM.get(id("enchant_totem")));
			entries.accept(BuiltInRegistries.ITEM.get(id("endless_box")));
		})
		.build();

	private ModRegistries() {
	}

	/** 注册「无尽图腾」创造分类页（主类 onInitialize 中调用） */
	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
			id("endless_totem"), ENDLESS_TOTEM_TAB);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}