package com.tterrag.registrate.util;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.util.LazyLoadedValue;

public class NonNullLazyValue<T> extends LazyLoadedValue<T> implements NonNullSupplier<T> {

    public NonNullLazyValue(NonNullSupplier<T> supplier) {
        super(supplier);
    }

    public @NonnullType T getValue() {
        return super.get();
    }
}
