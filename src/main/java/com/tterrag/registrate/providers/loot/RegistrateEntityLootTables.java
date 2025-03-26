package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Generated;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistrateEntityLootTables extends EntityLootSubProvider implements RegistrateLootTables {

    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;

    public RegistrateEntityLootTables(HolderLookup.Provider p_346214_, AbstractRegistrate<?> parent, Consumer<RegistrateEntityLootTables> callback) {
        super(FeatureFlags.REGISTRY.allFlags(), p_346214_);
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    public void generate() {
        callback.accept(this);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return parent.getAll(Registries.ENTITY_TYPE).stream().map(Supplier::get);
    }

    public HolderLookup.Provider getRegistries() {
        return this.registries;
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link EntityLootSubProvider#killedByFrog} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Wed, 26 Mar 2025 08:35:35 GMT")
    public LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> p_361765_) { return super.killedByFrog(p_361765_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Wed, 26 Mar 2025 08:35:35 GMT")
    public void add(EntityType<?> p_248740_, LootTable.Builder p_249440_) { super.add(p_248740_, p_249440_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Wed, 26 Mar 2025 08:35:35 GMT")
    public void add(EntityType<?> p_252130_, ResourceKey<LootTable> p_335943_, LootTable.Builder p_249357_) { super.add(p_252130_, p_335943_, p_249357_); }

    // GENERATED END
}
