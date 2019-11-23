package com.tterrag.registrate.builders;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityBuilder<T extends TileEntity, P> extends AbstractBuilder<TileEntityType<?>, TileEntityType<T>, P, TileEntityBuilder<T, P>> {
    
    private final TileEntityType.Builder<T> builder;

    public TileEntityBuilder(Registrate owner, P parent, String name, BuilderCallback callback, Supplier<T> factory) {
        super(owner, parent, name, callback, TileEntityType.class);
        this.builder = TileEntityType.Builder.create(factory);
    }
    
    public TileEntityBuilder<T, P> apply(Consumer<TileEntityType.Builder<T>> cons) {
        cons.accept(builder);
        return this;
    }

    @Override
    protected TileEntityType<T> createEntry() {
        return builder.build(null);
    }
}
