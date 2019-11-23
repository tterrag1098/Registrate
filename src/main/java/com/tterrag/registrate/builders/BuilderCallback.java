package com.tterrag.registrate.builders;

import java.util.function.Supplier;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

@FunctionalInterface
public interface BuilderCallback {

    <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> accept(String name, Class<? super R> type, Supplier<? extends T> factory);

}
