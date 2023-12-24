package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class EntityEntry<T extends Entity> extends RegistryEntry<EntityType<?>,EntityType<T>> {

    public EntityEntry(AbstractRegistrate<?> owner, DeferredHolder<EntityType<?>, EntityType<T>> delegate) {
        super(owner, delegate);
    }

    public @Nullable T create(Level world) {
        return get().create(world);
    }

    public boolean is(Entity t) {
        return t != null && t.getType() == get();
    }

    public static <T extends Entity> EntityEntry<T> cast(RegistryEntry<EntityType<?>,EntityType<T>> entry) {
        return RegistryEntry.cast(EntityEntry.class, entry);
    }
}
