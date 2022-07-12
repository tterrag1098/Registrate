package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface MenuScreenFactory<@NonnullType MENU extends AbstractContainerMenu, @NonnullType SCREEN extends Screen & MenuAccess<MENU>>
{
	@Nonnull SCREEN create(@Nonnull MENU menu, @Nonnull Inventory playerInventory, @Nonnull Component titleComponent);
}