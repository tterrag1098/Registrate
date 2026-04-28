package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Generated;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RegistrateEntityLootTables extends VanillaEntityLoot implements RegistrateLootTables {

    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;

    private final HolderLookup<Item> itemLookup;
    private final HolderLookup<Block> blockLookup;
    private final HolderLookup<EntityType<?>> entityLookup;

    public RegistrateEntityLootTables(HolderLookup.Provider p_346214_, AbstractRegistrate<?> parent, Consumer<RegistrateEntityLootTables> callback) {
        super(p_346214_);
        this.parent = parent;
        this.callback = callback;
        itemLookup = registries.lookupOrThrow(Registries.ITEM);
        blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        entityLookup = registries.lookupOrThrow(Registries.ENTITY_TYPE);
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

    public HolderLookup<Item> itemLookup() {
        return itemLookup;
    }

    public HolderLookup<Block> blockLookup() {
        return blockLookup;
    }

    public HolderLookup<EntityType<?>> entityLookup() {
        return entityLookup;
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link EntityLootSubProvider#createSheepTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Tue, 28 Apr 2026 01:12:07 GMT")
    public static LootTable.Builder createSheepTable(ItemLike p_249422_) { return EntityLootSubProvider.createSheepTable(p_249422_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#canHaveLootTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Tue, 28 Apr 2026 01:12:07 GMT")
    public boolean canHaveLootTable(EntityType<?> p_249029_) { return super.canHaveLootTable(p_249029_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#killedByFrogVariant} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Tue, 28 Apr 2026 01:12:07 GMT")
    public LootItemCondition.Builder killedByFrogVariant(ResourceKey<FrogVariant> p_335676_) { return super.killedByFrogVariant(p_335676_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Tue, 28 Apr 2026 01:12:07 GMT")
    public void add(EntityType<?> p_248740_, LootTable.Builder p_249440_) { super.add(p_248740_, p_249440_); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Tue, 28 Apr 2026 01:12:07 GMT")
    public void add(EntityType<?> p_252130_, ResourceKey<LootTable> p_335943_, LootTable.Builder p_249357_) { super.add(p_252130_, p_335943_, p_249357_); }

    // GENERATED END
}
