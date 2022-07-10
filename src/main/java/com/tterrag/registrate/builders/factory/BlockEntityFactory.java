package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface BlockEntityFactory<@NonnullType BLOCK_ENTITY extends BlockEntity>
{
	@Nonnull BLOCK_ENTITY create(@Nonnull BlockEntityType<BLOCK_ENTITY> blockEntityType, @Nonnull BlockPos pos, @Nonnull BlockState blockState);
}