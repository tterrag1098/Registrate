package com.tterrag.registrate.util.nullness;

import java.util.function.Supplier;

@FunctionalInterface
public interface NonNullSupplier<@NonnullType T> extends Supplier<T> {
    
    @Override
    T get();

    static <T> NonNullSupplier<T> of(Supplier<@NullableType T> sup) {
        return ((NullableSupplier<T>)sup::get).asNonNull();
    }
}
