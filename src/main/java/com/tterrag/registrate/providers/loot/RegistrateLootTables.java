package com.tterrag.registrate.providers.loot;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.ValidationResults;

public interface RegistrateLootTables extends Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {

    default void validate(Map<ResourceLocation, LootTable> map, ValidationResults validationresults) {}
    
}
