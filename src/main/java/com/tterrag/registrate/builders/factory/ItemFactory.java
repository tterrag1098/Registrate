package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ItemFactory<@NonnullType ITEM extends Item>
{
	ItemFactory<Item> ITEM = Item::new;

	@Nonnull ITEM create(@Nonnull Item.Properties properties);
}