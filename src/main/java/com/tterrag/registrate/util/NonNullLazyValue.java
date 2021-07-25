package com.tterrag.registrate.util;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.util.LazyValue;

public class NonNullLazyValue<T> extends LazyValue<T> implements NonNullSupplier<T> {

    public NonNullLazyValue(NonNullSupplier<T> supplier) {
        super(supplier);
    }

    /**
     * This conflicts with mojmaps as of 1.16.
     * <p>
     * Do not use this if your project is on mojmaps, and probably best to just avoid it entirely.
     */
    @Deprecated
    @Override
    public @NonnullType T get() {
        return super.get();
    }

    @Override
    public @NonnullType T get() {
        return super.get();
    }
}
