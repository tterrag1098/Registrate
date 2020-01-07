package com.tterrag.registrate.providers;

import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A context bean passed to data generator callbacks. Contains the provider itself, as well as the entry that data is being created for, and some metadata about the entry.
 *
 * @param <T>
 *            Provider type
 * @param <R>
 *            Type of the registry to which the entry belongs
 * @param <E>
 *            Type of the object for which data is being generated
 */
@Value
public class DataGenContext<R extends IForgeRegistryEntry<R>, E extends R> {

    @SuppressWarnings("null")
    @Getter(AccessLevel.NONE)
    NonNullSupplier<E> entry;
    String name;
    ResourceLocation id;

    @SuppressWarnings("null")
    public @NonnullType E getEntry() {
        return entry.get();
    }

    public static <R extends IForgeRegistryEntry<R>, E extends R> DataGenContext<R, E> from(Builder<R, E, ?, ?> builder, Class<? super R> clazz) {
        return new DataGenContext<R, E>(NonNullSupplier.of(builder.getOwner().<R, E>get(builder.getName(), clazz)), builder.getName(),
                new ResourceLocation(builder.getOwner().getModid(), builder.getName()));
    }
}
