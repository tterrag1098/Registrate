package com.tterrag.registrate.util;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class CreativeModeTabModifier implements CreativeModeTab.Output {
    private final Supplier<FeatureFlagSet> flags;
    private final BooleanSupplier hasPermissions;
    private final BiConsumer<ItemStack, CreativeModeTab.TabVisibility> acceptFunc;

    @ApiStatus.Internal
    public CreativeModeTabModifier(Supplier<FeatureFlagSet> flags, BooleanSupplier hasPermissions, BiConsumer<ItemStack, CreativeModeTab.TabVisibility> acceptFunc) {
        this.flags = flags;
        this.hasPermissions = hasPermissions;
        this.acceptFunc = acceptFunc;
    }

    public FeatureFlagSet getFlags() {
        return flags.get();
    }

    public boolean hasPermissions() {
        return hasPermissions.getAsBoolean();
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        acceptFunc.accept(stack, visibility);
    }

    public void accept(Supplier<? extends ItemLike> item, CreativeModeTab.TabVisibility visibility) {
        accept(item.get(), visibility);
    }

    public void accept(Supplier<? extends ItemLike> item) {
        accept(item.get());
    }
}
