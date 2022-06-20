package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tterrag.registrate.AbstractRegistrate;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.loot.LootTable;

@RequiredArgsConstructor
public class RegistrateEntityLootTables extends EntityLoot implements RegistrateLootTables {
    
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;
    
    @Override
    protected void addTables() {
        callback.accept(this);
    }
    
    @Override
    protected Iterable<EntityType<?>> getKnownEntities() {
        return parent.getAll(Registry.ENTITY_TYPE_REGISTRY).stream().map(Supplier::get).collect(Collectors.toList());
    }

    @Override
    protected boolean isNonLiving(EntityType<?> entitytype) {
        return entitytype.getCategory() == MobCategory.MISC; // TODO open this to customization?
    }

    // @formatter:off
    // GENERATED START

    @Override
    public void add(EntityType<?> p_124372_, LootTable.Builder p_124373_) { super.add(p_124372_, p_124373_); }

    @Override
    public void add(ResourceLocation p_124381_, LootTable.Builder p_124382_) { super.add(p_124381_, p_124382_); }

    // GENERATED END
}
