package com.tterrag.registrate.util.entry;

import javax.annotation.Nullable;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class LazyRegistryEntry<R, T extends R> implements NonNullSupplier<T> {
    
    @Nullable
    private NonNullSupplier<? extends RegistryEntry<R, T>> supplier;
    @Nullable
    private RegistryEntry<R, T> value;

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
