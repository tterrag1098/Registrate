package com.tterrag.registrate.util.entry;

import java.util.Optional;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class FluidEntry<T extends ForgeFlowingFluid> extends RegistryEntry<T> {

    private final @Nullable BlockEntry<? extends Block> block;

    public FluidEntry(AbstractRegistrate<?> owner, RegistryObject<T> delegate) {
        super(owner, delegate);
        BlockEntry<? extends Block> block = null;
        try {
            block = BlockEntry.cast(getSibling(ForgeRegistries.BLOCKS));
        } catch (IllegalArgumentException e) {} // TODO add way to get entry optionally
        this.block = block;
    }

    @Override
    public <R extends IForgeRegistryEntry<? super T>> boolean is(R entry) {
        return get().isEquivalentTo((Fluid) entry);
    }

    @SuppressWarnings({ "unchecked", "null" })
    <S extends ForgeFlowingFluid> S getSource() {
        return (S) get().getStillFluid();
    }

    @SuppressWarnings({ "unchecked", "null" })
    <B extends Block> Optional<B> getBlock() {
        return (Optional<B>) Optional.ofNullable(block).map(RegistryEntry::get);
    }

    @SuppressWarnings({ "unchecked", "null" })
    <I extends Item> Optional<I> getBucket() {
        return Optional.ofNullable((I) get().getFilledBucket());
    }
}
