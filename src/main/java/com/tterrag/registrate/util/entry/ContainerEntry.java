package com.tterrag.registrate.util.entry;

import java.util.function.Consumer;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.NetworkHooks;

public class ContainerEntry<T extends Container> extends RegistryEntry<ContainerType<T>> {

    public ContainerEntry(AbstractRegistrate<?> owner, RegistryObject<ContainerType<T>> delegate) {
        super(owner, delegate);
    }

    public T create(int windowId, PlayerInventory playerInv) {
        return get().create(windowId, playerInv);
    }

    public IContainerProvider asProvider() {
        return (window, playerinv, $) -> create(window, playerinv);
    }

    public void open(ServerPlayerEntity player, ITextComponent displayName) {
        open(player, displayName, asProvider());
    }

    public void open(ServerPlayerEntity player, ITextComponent displayName, Consumer<PacketBuffer> extraData) {
        open(player, displayName, asProvider(), extraData);
    }

    public void open(ServerPlayerEntity player, ITextComponent displayName, IContainerProvider provider) {
        NetworkHooks.openGui(player, new SimpleNamedContainerProvider(provider, displayName));
    }

    public void open(ServerPlayerEntity player, ITextComponent displayName, IContainerProvider provider, Consumer<PacketBuffer> extraData) {
        NetworkHooks.openGui(player, new SimpleNamedContainerProvider(provider, displayName), extraData);
    }
}
