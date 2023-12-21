package com.tterrag.registrate.util.entry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Wraps a {@link DeferredHolder}, providing a cleaner API with null-safe access, and registrate-specific extensions such as {@link #getSibling(ResourceKey)}.
 *
 * @param <T>
 *            The type of the entry
 */
@EqualsAndHashCode(of = "delegate")
public class RegistryEntry<T> implements NonNullSupplier<T> {

    private interface Exclusions<T> {

        T get();

        DeferredHolder<? super T, T> filter(Predicate<? super T> predicate);
        
        public void updateReference(Registry<? extends T> registry);
    }

    private final AbstractRegistrate<?> owner;
    @Delegate(excludes = Exclusions.class)
    private final @Nullable DeferredHolder<? super T, T> delegate;

    @SuppressWarnings("unused")
    public RegistryEntry(AbstractRegistrate<?> owner, DeferredHolder<? super T, T> delegate) {
        if (owner == null)
            throw new NullPointerException("Owner must not be null");
        if (delegate == null)
            throw new NullPointerException("Delegate must not be null");
        this.owner = owner;
        this.delegate = delegate;
    }

    //TODO Remove
//    /**
//     * Update the underlying entry manually from the given registry.
//     *
//     * @param registry
//     *            The registry to pull the entry from.
//     */
//    @Deprecated
//    public void updateReference(Registry<? super T> registry) {
//        DeferredHolder<? super T, T> delegate = this.delegate;
//        try {
//            if(registry == delegate.get)){
//                registry.getHolder()
//            }
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Update the underlying entry manually from the given registry.
//     *
//     * @param event
//     *            The register event to pull the entry from.
//     */
//    public void updateReference(RegisterEvent event) {
//        updateReference((Registry<? super T>) event.getRegistry());
//    }

    /**
     * Get the entry, throwing an exception if it is not present for any reason.
     * 
     * @return The (non-null) entry
     */
    @Override
    public @NonnullType T get() {
        DeferredHolder<? super T,T> delegate = this.delegate;
        return Objects.requireNonNull(getUnchecked(), () -> delegate == null ? "Registry entry is empty" : "Registry entry not present: " + delegate.getId());
    }

    /**
     * Get the entry without performing any checks.
     * 
     * @return The (nullable) entry
     */
    public @Nullable T getUnchecked() {
        DeferredHolder<? super T, T> delegate = this.delegate;
        return delegate == null ? null : delegate.get();
    }

    public <R, E extends R> RegistryEntry<E> getSibling(ResourceKey<? extends Registry<R>> registryType) {
        return owner.get(getId().getPath(), registryType);
    }

    public <R, E extends R> RegistryEntry<E> getSibling(Registry<R> registry) {
        return getSibling(registry.key());
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
    public Optional<RegistryEntry<T>> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (predicate.test(get())) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    public <R> boolean is(R entry) {
        return get() == entry;
    }

    @SuppressWarnings("unchecked")
    protected static <E extends RegistryEntry<?>> E cast(Class<? super E> clazz, RegistryEntry<?> entry) {
        if (clazz.isInstance(entry)) {
            return (E) entry;
        }
        throw new IllegalArgumentException("Could not convert RegistryEntry: expecting " + clazz + ", found " + entry.getClass());
    }
}
