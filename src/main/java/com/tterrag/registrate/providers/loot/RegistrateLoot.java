package com.tterrag.registrate.providers.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface RegistrateLoot extends Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {

    default void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationresults) {
    }

}
