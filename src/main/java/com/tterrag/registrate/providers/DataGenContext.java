package com.tterrag.registrate.providers;

import java.util.function.Supplier;

import com.tterrag.registrate.builders.Builder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Value
public class DataGenContext<T extends RegistrateProvider, R extends IForgeRegistryEntry<R>, E extends R> {

    T provider;
    @Getter(AccessLevel.NONE)
    Supplier<E> entry;
    String name;
    ResourceLocation id;

    public E getEntry() {
        return entry.get();
    }

    public static <P extends RegistrateProvider, R extends IForgeRegistryEntry<R>, E extends R> DataGenContext<P, R, E> from(P prov, Builder<R, E, ?, ?> builder, Class<? super R> clazz) {
        return new DataGenContext<>(prov, builder.getOwner().<R, E>get(builder.getName(), clazz), builder.getName(),
                new ResourceLocation(builder.getOwner().getModid(), builder.getName()));
    }
}
