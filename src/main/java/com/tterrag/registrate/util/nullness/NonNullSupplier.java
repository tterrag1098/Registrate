package com.tterrag.registrate.util.nullness;

import net.neoforged.neoforge.common.util.Lazy;

import java.util.Objects;
import java.util.function.Supplier;


@FunctionalInterface
public interface NonNullSupplier<@NonnullType T> extends Supplier<T> {
    
    @Override
    T get();

    static <T> NonNullSupplier<T> of(Supplier<@NullableType T> sup) {
        return of(sup, () -> "Unexpected null value from supplier");
    }
    
    static <T> NonNullSupplier<T> of(Supplier<@NullableType T> sup, NonNullSupplier<String> errorMsg) {
        return () -> {
            T res = sup.get();
            Objects.requireNonNull(res, errorMsg);
            return res;
        };
    }

    default NonNullSupplier<T> lazy() {
        return lazy(this);
    }

    static <T> NonNullSupplier<T> lazy(Supplier<@NonnullType T> sup) {
        return Lazy.of(sup)::get;
    }
}
