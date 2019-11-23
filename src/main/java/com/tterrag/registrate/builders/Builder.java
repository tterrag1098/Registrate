package com.tterrag.registrate.builders;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface Builder<R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> {

    RegistryObject<T> register();
    
    Registrate getOwner();

    P getParent();

    String getName();
    
    default Supplier<T> get(Class<? super R> registryType) {
        return () -> getOwner().<R, T>get(getName(), registryType).get();
    }
    
    @SuppressWarnings("unchecked")
    default <D extends RegistrateProvider> S addData(ProviderType<D> type, Class<? super R> clazz, Consumer<DataGenContext<D, R, T>> cons) {
        getOwner().addDataGenerator(getName(), type, prov -> cons.accept(DataGenContext.from(prov, this, clazz)));
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    default <R2 extends IForgeRegistryEntry<R2>, T2 extends R2, P2, S2 extends Builder<R2, T2, P2, S2>> S2 transform(Function<S, S2> func) {
        return func.apply((S) this);
    }

    default P build() {
        register(); // Ignore return value
        return getParent();
    }
}
