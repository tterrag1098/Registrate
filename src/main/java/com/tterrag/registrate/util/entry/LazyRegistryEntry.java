package com.tterrag.registrate.util.entry;

import org.jspecify.annotations.Nullable;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class LazyRegistryEntry<R, T extends R> implements NonNullSupplier<T> {

    private @Nullable NonNullSupplier<? extends RegistryEntry<R, T>> supplier;
    private @Nullable RegistryEntry<R, T> value;

    public LazyRegistryEntry(NonNullSupplier<? extends RegistryEntry<R, T>> supplier) {
        this.supplier = supplier;
    }
    
    @Override
    public T get() {
        NonNullSupplier<? extends RegistryEntry<R, T>> supplier = this.supplier;
        if (supplier != null) {
            this.value = supplier.get();
            this.supplier = null;
        }
        return this.value.get();
    }
}
