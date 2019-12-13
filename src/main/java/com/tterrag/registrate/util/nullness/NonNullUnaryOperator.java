package com.tterrag.registrate.util.nullness;

@FunctionalInterface
public interface NonNullUnaryOperator<T> extends NonNullFunction<T, T> {
    
    static <T> NonNullUnaryOperator<T> identity() {
        return t -> t;
    }
}
