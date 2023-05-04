package com.tterrag.registrate.util.entry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * Wraps a {@link RegistryObject}, providing a cleaner API with null-safe access, and registrate-specific extensions such as {@link #getSibling(ResourceKey)}.
 *
 * @param <T>
 *            The type of the entry
 */
@EqualsAndHashCode(of = "delegate")
public class RegistryEntry<T> implements NonNullSupplier<T> {

    private static RegistryEntry<?> EMPTY; static {
        try {
            // Safe to call with null here and only here
            @SuppressWarnings({ "null", "unchecked", "rawtypes" })
            RegistryEntry<?> ret = new RegistryEntry(null, (RegistryObject) ObfuscationReflectionHelper.findMethod(RegistryObject.class, "empty").invoke(null));
            EMPTY = ret;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> RegistryEntry<T> empty() {
        @SuppressWarnings("unchecked")
        RegistryEntry<T> t = (RegistryEntry<T>) EMPTY;
        return t;
    }

    private interface Exclusions<T> {

        T get();

        RegistryObject<T> filter(Predicate<? super T> predicate);
        
        public void updateReference(IForgeRegistry<? extends T> registry);
    }

    private final AbstractRegistrate<?> owner;
    @Delegate(excludes = Exclusions.class)
    private final @Nullable RegistryObject<T> delegate;

    @SuppressWarnings("unused")
    public RegistryEntry(AbstractRegistrate<?> owner, RegistryObject<T> delegate) {
        if (EMPTY != null && owner == null)
            throw new NullPointerException("Owner must not be null");
        if (EMPTY != null && delegate == null)
            throw new NullPointerException("Delegate must not be null");
        this.owner = owner;
        this.delegate = delegate;
    }

    private static final Method _updateReference_Registry = ObfuscationReflectionHelper.findMethod(RegistryObject.class, "updateReference", IForgeRegistry.class);
    private static final Method _updateReference_Event = ObfuscationReflectionHelper.findMethod(RegistryObject.class, "updateReference", RegisterEvent.class);

    /**
     * Update the underlying entry manually from the given registry.
     * 
     * @param event
     *            The registry to pull the entry from.
     */
    @Deprecated
    public void updateReference(IForgeRegistry<? super T> event) {
        RegistryObject<T> delegate = this.delegate;
        try {
            _updateReference_Registry.invoke(Objects.requireNonNull(delegate, "Registry entry is empty"), event);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Update the underlying entry manually from the given registry.
     * 
     * @param event
     *            The register event to pull the entry from.
     */
    public void updateReference(RegisterEvent event) {
        RegistryObject<T> delegate = this.delegate;
        try {
            _updateReference_Event.invoke(Objects.requireNonNull(delegate, "Registry entry is empty"), event);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
        return delegate == null ? null : delegate.orElse(null);
    }

    public <R, E extends R> RegistryEntry<E> getSibling(ResourceKey<? extends Registry<R>> registryType) {
        return this == EMPTY ? empty() : owner.get(getId().getPath(), registryType);
    }

    public <R, E extends R> RegistryEntry<E> getSibling(IForgeRegistry<R> registry) {
        return getSibling(registry.getRegistryKey());
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
