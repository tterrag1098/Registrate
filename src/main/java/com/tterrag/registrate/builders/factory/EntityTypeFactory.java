package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface EntityTypeFactory<@NonnullType ENTITY extends Entity> extends EntityType.EntityFactory<ENTITY>
{
	@Override @Nonnull ENTITY create(@Nonnull EntityType<ENTITY> entityType, @Nonnull Level level);
}