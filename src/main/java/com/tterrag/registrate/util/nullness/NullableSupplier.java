package com.tterrag.registrate.util.nullness;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link Supplier} whose value may be {@code null}.
 *
 * @deprecated Use {@link Supplier} together with the appropriate nullness annotation, or
 *             {@link NonNullSupplier} when a value is required.
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface NullableSupplier<T> extends Supplier<T> {

    @Override
    @Nullable
    T get();

    /**
     * Gets the value, requiring it to be non-null.
     */
    default T getNonNull() {
        return getNonNull(() -> "Unexpected null value from supplier");
    }

    default T getNonNull(NonNullSupplier<String> errorMsg) {
        T res = get();
        Objects.requireNonNull(res, errorMsg);
        return res;
    }

    /**
     * Adapts this supplier to a non-null supplier.
     */
    default NonNullSupplier<T> asNonNull() {
        return () -> getNonNull();
    }

    default NonNullSupplier<T> asNonNull(NonNullSupplier<String> errorMsg) {
        return () -> getNonNull(errorMsg);
    }
}
