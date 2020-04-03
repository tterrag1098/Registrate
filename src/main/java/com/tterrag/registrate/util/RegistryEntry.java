package com.tterrag.registrate.util;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import lombok.experimental.Delegate;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Wraps a {@link RegistryObject}, providing a cleaner API with null-safe access, and registrate-specific extensions such as {@link #getSibling(Class)}.
 *
 * @param <T>
 *            The type of the entry
 */
public class RegistryEntry<T extends IForgeRegistryEntry<? super T>> implements NonNullSupplier<T> {

    @SuppressWarnings("null") // Safe to call with null here and only here
    private static RegistryEntry<?> EMPTY = new RegistryEntry<>(new Registrate("dummy") {}, null);

    private static <T extends IForgeRegistryEntry<? super T>> RegistryEntry<T> empty() {
        @SuppressWarnings("unchecked")
        RegistryEntry<T> t = (RegistryEntry<T>) EMPTY;
        return t;
    }

    private interface Exclusions<T extends IForgeRegistryEntry<? super T>> {

        T get();

        RegistryObject<T> filter(Predicate<? super T> predicate);
    }

    private final AbstractRegistrate<?> owner;
    @Delegate(excludes = Exclusions.class)
    private final @Nullable RegistryObject<T> delegate;

    @SuppressWarnings("unused")
    public RegistryEntry(AbstractRegistrate<?> owner, RegistryObject<T> delegate) {
        if (EMPTY != null && delegate == null)
            throw new NullPointerException("Delegate must not be null");
        this.owner = owner;
        this.delegate = delegate;
    }

    /**
     * Get the entry, throwing an exception if it is not present for any reason.
     * 
     * @return The (non-null) entry
     */
    @Override
    public @NonnullType T get() {
        RegistryObject<T> delegate = this.delegate;
        return Objects.requireNonNull(getUnchecked(), () -> delegate == null ? "Registry entry is empty" : "Registry entry not present: " + delegate.getId());
    }

    /**
     * Get the entry without performing any checks.
     * 
     * @return The (nullable) entry
     */
    public @Nullable T getUnchecked() {
        RegistryObject<T> delegate = this.delegate;
        return delegate == null ? null : delegate.get();
    }
    
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>, E extends R> RegistryEntry<E> getSibling(Class<? super R> registryType) {
        return owner.get(getId().getPath(), (Class<R>) registryType);
    }
    
    public <R extends IForgeRegistryEntry<R>, E extends R> RegistryEntry<E> getSibling(IForgeRegistry<R> registry) {
        return getSibling(registry.getRegistrySuperType());
    }

    /**
     * If an entry is present, and the entry matches the given predicate, return an {@link RegistryEntry} describing the value, otherwise return an empty {@link RegistryEntry}.
     *
     * @param predicate
     *            a {@link Predicate predicate} to apply to the entry, if present
     * @return an {@link RegistryEntry} describing the value of this {@link RegistryEntry} if the entry is present and matches the given predicate, otherwise an empty {@link RegistryEntry}
     * @throws NullPointerException
     *             if the predicate is null
     */
    public RegistryEntry<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent() || predicate.test(get())) {
            return this;
        }
        return empty();
    }
}
