package com.tterrag.registrate.util.nullness;

import java.util.Objects;
import java.util.function.Supplier;

public interface NullableSupplier<@NullableType T> extends Supplier<T> {
    
    @Override
    T get();

    default T getNonNull() {
        return getNonNull("Unexpected null value from supplier");
    }
    
    default T getNonNull(String errorMsg) {
        T res = get();
        Objects.requireNonNull(res, errorMsg);
        return res;
    }
    
    default NonNullSupplier<T> asNonNull() {
        return () -> getNonNull();
    }
}
