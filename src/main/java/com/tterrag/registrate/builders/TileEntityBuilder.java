package com.tterrag.registrate.builders;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityBuilder<T extends TileEntity, P> extends AbstractBuilder<TileEntityType<?>, TileEntityType<T>, P, TileEntityBuilder<T, P>> {
    
    private final Supplier<? extends T> factory;
    private final Set<Supplier<? extends Block>> validBlocks = new HashSet<>();

    public TileEntityBuilder(Registrate owner, P parent, String name, BuilderCallback callback, Supplier<? extends T> factory) {
        super(owner, parent, name, callback, TileEntityType.class);
        this.factory = factory;
    }
    
    public TileEntityBuilder<T, P> validBlock(Supplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }

    @Override
    protected TileEntityType<T> createEntry() {
        return TileEntityType.Builder.<T>create(factory, validBlocks.stream().map(Supplier::get).toArray(Block[]::new))
                .build(null);
    }
}
