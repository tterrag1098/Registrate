package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tterrag.registrate.AbstractRegistrate;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.loot.ILootConditionConsumer;
import net.minecraft.loot.ILootFunctionConsumer;
import net.minecraft.loot.IRandomRange;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.IItemProvider;

@RequiredArgsConstructor
public class RegistrateBlockLootTables extends BlockLootTables implements RegistrateLootTables {
    
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateBlockLootTables> callback;
    
    @Override
    protected void addTables() {
        callback.accept(this);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return parent.getAll(Block.class).stream().map(Supplier::get).collect(Collectors.toList());
    }

    // @formatter:off
    // GENERATED START

    public static <T> T withExplosionDecay(IItemProvider item, ILootFunctionConsumer<T> function) { return BlockLootTables.withExplosionDecay(item, function); }

    public static <T> T withSurvivesExplosion(IItemProvider item, ILootConditionConsumer<T> condition) { return BlockLootTables.withSurvivesExplosion(item, condition); }

    public static LootTable.Builder dropping(IItemProvider item) { return BlockLootTables.dropping(item); }

    public static LootTable.Builder dropping(Block block, ILootCondition.IBuilder conditionBuilder, LootEntry.Builder<?> p_218494_2_) { return BlockLootTables.dropping(block, conditionBuilder, p_218494_2_); }

    public static LootTable.Builder droppingWithSilkTouch(Block block, LootEntry.Builder<?> builder) { return BlockLootTables.droppingWithSilkTouch(block, builder); }

    public static LootTable.Builder droppingWithShears(Block block, LootEntry.Builder<?> noShearAlternativeEntry) { return BlockLootTables.droppingWithShears(block, noShearAlternativeEntry); }

    public static LootTable.Builder droppingWithSilkTouchOrShears(Block block, LootEntry.Builder<?> alternativeLootEntry) { return BlockLootTables.droppingWithSilkTouchOrShears(block, alternativeLootEntry); }

    public static LootTable.Builder droppingWithSilkTouch(Block block, IItemProvider noSilkTouch) { return BlockLootTables.droppingWithSilkTouch(block, noSilkTouch); }

    public static LootTable.Builder droppingRandomly(IItemProvider item, IRandomRange range) { return BlockLootTables.droppingRandomly(item, range); }

    public static LootTable.Builder droppingWithSilkTouchOrRandomly(Block block, IItemProvider item, IRandomRange range) { return BlockLootTables.droppingWithSilkTouchOrRandomly(block, item, range); }

    public static LootTable.Builder onlyWithSilkTouch(IItemProvider item) { return BlockLootTables.onlyWithSilkTouch(item); }

    public static LootTable.Builder droppingAndFlowerPot(IItemProvider flower) { return BlockLootTables.droppingAndFlowerPot(flower); }

    public static LootTable.Builder droppingSlab(Block slab) { return BlockLootTables.droppingSlab(slab); }

    public static LootTable.Builder droppingWithName(Block block) { return BlockLootTables.droppingWithName(block); }

    public static LootTable.Builder droppingWithContents(Block shulker) { return BlockLootTables.droppingWithContents(shulker); }

    public static LootTable.Builder droppingWithPatterns(Block banner) { return BlockLootTables.droppingWithPatterns(banner); }

    public static LootTable.Builder droppingItemWithFortune(Block block, Item item) { return BlockLootTables.droppingItemWithFortune(block, item); }

    public static LootTable.Builder droppingItemRarely(Block block, IItemProvider item) { return BlockLootTables.droppingItemRarely(block, item); }

    public static LootTable.Builder droppingSeeds(Block block) { return BlockLootTables.droppingSeeds(block); }

    public static LootTable.Builder droppingByAge(Block stemFruit, Item item) { return BlockLootTables.droppingByAge(stemFruit, item); }

    public static LootTable.Builder onlyWithShears(IItemProvider item) { return BlockLootTables.onlyWithShears(item); }

    public static LootTable.Builder droppingWithChancesAndSticks(Block block, Block sapling, float... chances) { return BlockLootTables.droppingWithChancesAndSticks(block, sapling, chances); }

    public static LootTable.Builder droppingWithChancesSticksAndApples(Block block, Block sapling, float... chances) { return BlockLootTables.droppingWithChancesSticksAndApples(block, sapling, chances); }

    public static LootTable.Builder droppingAndBonusWhen(Block block, Item itemConditional, Item withBonus, ILootCondition.IBuilder conditionBuilder) { return BlockLootTables.droppingAndBonusWhen(block, itemConditional, withBonus, conditionBuilder); }

    @Override
    public void registerLootTable(Block blockIn, LootTable.Builder table) { super.registerLootTable(blockIn, table); }

    // GENERATED END
}
