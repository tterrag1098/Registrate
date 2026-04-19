package com.tterrag.registrate.providers.loot;

import net.minecraft.core.WritableRegistry;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

import java.util.Map;

public interface RegistrateLootTables extends LootTableSubProvider {
    default void validate(WritableRegistry<LootTable> tables, ValidationContextSource validationContext) {}
}
