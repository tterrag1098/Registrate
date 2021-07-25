package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tterrag.registrate.AbstractRegistrate;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.loot.EntityLootTables;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor
public class RegistrateEntityLootTables extends EntityLootTables implements RegistrateLootTables {
    
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;
    
    @Override
    protected void addTables() {
        callback.accept(this);
    }
    
    @Override
    protected Iterable<EntityType<?>> getKnownEntities() {
        return parent.<EntityType<?>>getAll(EntityType.class).stream().map(Supplier::get).collect(Collectors.toList());
    }

    @Override
    protected boolean isNonLiving(EntityType<?> entitytype) {
        return entitytype.getCategory() == EntityClassification.MISC; // TODO open this to customization?
    }

    // @formatter:off
    // GENERATED START

    @Override
    public void add(EntityType<?> type, LootTable.Builder table) { super.add(type, table); }

    @Override
    public void add(ResourceLocation id, LootTable.Builder table) { super.add(id, table); }

    // GENERATED END
}
