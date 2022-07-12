package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface MenuFactory<@NonnullType MENU extends AbstractContainerMenu>
{
	MENU create(@Nullable MenuType<? extends MENU> menuType, int windowId, @Nonnull Inventory playerInventory);

	static <@NonnullType MENU extends AbstractContainerMenu> WithBuffer<MENU> withIgnoredBuffer(MenuFactory<MENU> menuFactory)
	{
		return (menuType, windowId, playerInventory, buffer) -> menuFactory.create(menuType, windowId, playerInventory);
	}

	@FunctionalInterface
	interface WithBuffer<@NonnullType MENU extends AbstractContainerMenu>
	{
		MENU create(@Nullable MenuType<? extends MENU> menuType, int windowId, @NotNull Inventory playerInventory, @Nonnull FriendlyByteBuf buffer);
	}
}