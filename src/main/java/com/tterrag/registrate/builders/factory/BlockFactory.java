package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface BlockFactory<@NonnullType BLOCK extends Block>
{
	BlockFactory<Block> BLOCK = Block::new;

	@Nonnull BLOCK create(@Nonnull BlockBehaviour.Properties properties);
}