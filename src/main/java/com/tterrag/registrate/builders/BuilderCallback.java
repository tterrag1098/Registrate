package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

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
     * @param builder
     *            The builder performing this callback
     * @param factory
     *            A {@link NonNullSupplier} that will create the entry
     * @param entryFactory
     *            A {@link NonNullFunction} which accepts the entry delegate and returns a {@link RegistryEntry} wrapper
     * @return A {@link RegistryEntry} that will supply the registered entry
     */
    @Deprecated
    <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> factory, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory);

    @SuppressWarnings("null")
    default <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> factory, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
        return accept(name, RegistryManager.ACTIVE.<R>getRegistry(type).getRegistrySuperType(), builder, factory, entryFactory);
    }

    /**
     * Accept a built entry, to later be constructed and registered. Uses the default {@link RegistryEntry#RegistryEntry(AbstractRegistrate, RegistryObject) RegistryEntry factory}.
     * 
     * @param <R>
     *            The registry type to which the entry will be registered
     * @param <T>
     *            The type of the entry
     * @param name
     *            The name of the entry
     * @param type
     *            A {@link Class} representing the registry type
     * @param builder
     *            The builder performing this callback
     * @param factory
     *            A {@link NonNullSupplier} that will create the entry
     * @return A {@link RegistryEntry} that will supply the registered entry
     */
    default <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> factory) {
        return accept(name, type, builder, factory, delegate -> new RegistryEntry<>(builder.getOwner(), delegate));
    }

    @FunctionalInterface
    public interface NewBuilderCallback extends BuilderCallback {

        @SuppressWarnings("null")
        @Deprecated
        @Override
        default <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> factory, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
            return accept(name, RegistryManager.ACTIVE.<R>getRegistry(type).getRegistryKey(), builder, factory, entryFactory);
        }

        @Override
        <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> factory, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory);
    }
}
