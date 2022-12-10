package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

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

    public static LootTable.Builder createSheepTable(ItemLike p_249422_) { return EntityLootSubProvider.createSheepTable(p_249422_); }

    @Override
    public boolean canHaveLootTable(EntityType<?> p_249029_) { return super.canHaveLootTable(p_249029_); }

    @Override
    public LootItemCondition.Builder killedByFrogVariant(FrogVariant p_249403_) { return super.killedByFrogVariant(p_249403_); }

    @Override
    public void add(EntityType<?> p_248740_, LootTable.Builder p_249440_) { super.add(p_248740_, p_249440_); }

    @Override
    public void add(EntityType<?> p_252130_, ResourceLocation p_251706_, LootTable.Builder p_249357_) { super.add(p_252130_, p_251706_, p_249357_); }

    // GENERATED END
}
