package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tterrag.registrate.Registrate;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.loot.EntityLootTables;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;

@RequiredArgsConstructor
public class RegistrateEntityLootTables extends EntityLootTables implements RegistrateLootTables {
    
    private final Registrate parent;
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
        return entitytype.getClassification() == EntityClassification.MISC; // TODO open this to customization?
    }
    
    // @formatter:off
    // GENERATED START

    @Override
    public void func_218582_a(EntityType<?> p_218582_1_, LootTable.Builder p_218582_2_) { super.func_218582_a(p_218582_1_, p_218582_2_); }

    @Override
    public void func_218585_a(ResourceLocation p_218585_1_, LootTable.Builder p_218585_2_) { super.func_218585_a(p_218585_1_, p_218585_2_); }

    // GENERATED END
}
