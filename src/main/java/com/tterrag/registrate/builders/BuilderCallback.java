package com.tterrag.registrate.builders;

import java.util.function.Supplier;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A callback passed to {@link Builder builders} from the owning {@link AbstractRegistrate} which will add a registration for the built entry that lazily creates and registers it.
 */
@FunctionalInterface
public interface BuilderCallback {

    /**
     * Accept a built entry, to later be constructed and registered.
     * 
     * @param <R>
     *            The registry type to which the entry will be registered
     * @param <T>
     *            The type of the entry
     * @param name
     *            The name of the entry
     * @param type
     *            A {@link Class} representing the registry type
     * @param factory
     *            A {@link Supplier} that will create the entry
     * @return A {@link RegistryEntry} that will supply the registered entry
     */
    <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, Class<? super R> type, NonNullSupplier<? extends T> factory);

}
