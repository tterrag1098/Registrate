package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class NoConfigBuilder<O extends AbstractRegistrate<O>, R extends IForgeRegistryEntry<R>, T extends R, P> extends AbstractBuilder<O, R, T, P, NoConfigBuilder<O, R, T, P>> {
    
    private final NonNullSupplier<T> factory;

    public NoConfigBuilder(O owner, P parent, String name, BuilderCallback<O> callback, ResourceKey<? extends Registry<R>> registryType, NonNullSupplier<T> factory) {
        super(owner, parent, name, callback, registryType);
        this.factory = factory;
    }

    @Deprecated
    public NoConfigBuilder(O owner, P parent, String name, BuilderCallback<O> callback, Class<? super R> registryType, NonNullSupplier<T> factory) {
        super(owner, parent, name, callback, registryType);
        this.factory = factory;
    }

    @Override
    protected @NonnullType T createEntry() {
        return factory.get();
    }
}