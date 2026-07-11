package com.tterrag.registrate.util.entry;

import java.util.function.Consumer;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MenuEntry<T extends AbstractContainerMenu> extends RegistryEntry<MenuType<?>, MenuType<T>> {

    public MenuEntry(AbstractRegistrate<?> owner, DeferredHolder<MenuType<?>, MenuType<T>> delegate) {
        super(owner, delegate);
    }

    public T create(int windowId, Inventory playerInv) {
        return get().create(windowId, playerInv);
    }

    public MenuConstructor asProvider() {
        return (window, playerinv, $) -> create(window, playerinv);
    }

    public void open(ServerPlayer player, Component displayName) {
        open(player, displayName, asProvider());
    }

    public void open(ServerPlayer player, Component displayName, Consumer<RegistryFriendlyByteBuf> extraData) {
        open(player, displayName, asProvider(), extraData);
    }

    public void open(ServerPlayer player, Component displayName, MenuConstructor provider) {
        player.openMenu(new SimpleMenuProvider(provider, displayName));
    }

    public void open(ServerPlayer player, Component displayName, MenuConstructor provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        player.openMenu(new SimpleMenuProvider(provider, displayName), extraData);
    }
}
