package com.tterrag.registrate.providers.loot;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;

public interface RegistrateLootTables extends Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {

    default void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationresults) {}
    
}
