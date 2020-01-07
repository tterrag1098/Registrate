package com.tterrag.registrate.util;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.util.LazyLoadBase;

public class LazyValue<T> extends LazyLoadBase<T> implements NonNullSupplier<T> {

    public LazyValue(NonNullSupplier<T> supplier) {
        super(supplier);
    }

    @Override
    public @NonnullType T get() {
        return getValue();
    }
}
