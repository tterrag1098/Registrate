package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface EnchantmentFactory<@NonnullType ENCHANTMENT extends Enchantment>
{
	@Nonnull ENCHANTMENT create(@Nonnull Enchantment.Rarity rarity, @Nonnull EnchantmentCategory category, @Nonnull EquipmentSlot... slots);
}