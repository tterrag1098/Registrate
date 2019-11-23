package com.tterrag.registrate.builders;

import java.util.function.Consumer;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

@RequiredArgsConstructor
public abstract class AbstractBuilder<R extends IForgeRegistryEntry<R>, T extends R, P, S extends AbstractBuilder<R, T, P, S>> implements Builder<R, T, P, S> {
    
    @Getter(onMethod = @__({ @Override }))
    private final Registrate owner;
    @Getter(onMethod = @__({ @Override }))
    private final P parent;
    @Getter(onMethod = @__({ @Override }))
    private final String name;
    @Getter(AccessLevel.PROTECTED)
    private final BuilderCallback callback;
    private final Class<? super R> registryType;
    
    protected abstract T createEntry();
    
    @Override
    public RegistryObject<T> register() {
        return callback.accept(name, registryType, this::createEntry);
    }
    
    public T get() {
        return getOwner().<R, T>get(getName(), registryType).get();
    }
    
    public <D extends RegistrateProvider> S addData(ProviderType<D> type, Consumer<DataGenContext<D, R, T>> cons) {
        return addData(type, registryType, cons);
    }
}
