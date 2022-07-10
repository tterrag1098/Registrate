package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface BlockItemFactory<@NonnullType BLOCK extends Block, @NonnullType ITEM extends Item>
{
	@Nonnull ITEM create(@Nonnull BLOCK block, @Nonnull Item.Properties properties);

	static <BLOCK extends Block> BlockItemFactory<BLOCK, BlockItem> blockItem()
	{
		return BlockItem::new;
	}
}