package com.tterrag.registrate.util.entry;

import java.util.Optional;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FluidEntry<T extends BaseFlowingFluid> extends RegistryEntry<T> {

    private final @Nullable BlockEntry<? extends Block> block;

    public FluidEntry(AbstractRegistrate<?> owner, DeferredHolder<? super T, T> delegate) {
        super(owner, delegate);
        BlockEntry<? extends Block> block = null;
        try {
            block = BlockEntry.cast(getSibling(BuiltInRegistries.BLOCK));
        } catch (IllegalArgumentException e) {} // TODO add way to get entry optionally
        this.block = block;
    }

    @Override
    public <R> boolean is(R entry) {
        return get().isSame((Fluid) entry);
    }

    @SuppressWarnings("unchecked")
    public <S extends BaseFlowingFluid> S getSource() {
        return (S) get().getSource();
    }

    public FluidType getType() {
        return get().getFluidType();
    }

    @SuppressWarnings({ "unchecked", "null" })
    public <B extends Block> Optional<B> getBlock() {
        return (Optional<B>) Optional.ofNullable(block).map(RegistryEntry::get);
    }

    @SuppressWarnings({ "unchecked", "null" })
    public <I extends Item> Optional<I> getBucket() {
        return Optional.ofNullable((I) get().getBucket());
    }
}
