package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RegistrateEntityLootTables extends VanillaEntityLoot implements RegistrateLootTables {

    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;

    @Override
    public void generate() {
        callback.accept(this);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return parent.getAll(Registries.ENTITY_TYPE).stream().map(Supplier::get);
    }

    // @formatter:off
    // GENERATED START

    @Override
    public void add(EntityType<?> p_124372_, LootTable.Builder p_124373_) { super.add(p_124372_, p_124373_); }

    @Override
    public void add(EntityType<?> entityType, ResourceLocation p_124381_, LootTable.Builder p_124382_) { super.add(entityType, p_124381_, p_124382_); }

    // GENERATED END
}
