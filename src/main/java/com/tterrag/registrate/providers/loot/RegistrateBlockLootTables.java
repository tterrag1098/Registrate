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

    public static <T> T withExplosionDecay(IItemProvider item, ILootFunctionConsumer<T> function) { return BlockLootTables.applyExplosionDecay(item, function); }

    public static <T> T withSurvivesExplosion(IItemProvider item, ILootConditionConsumer<T> condition) { return BlockLootTables.applyExplosionCondition(item, condition); }

    public static LootTable.Builder dropping(IItemProvider item) { return BlockLootTables.createSingleItemTable(item); }

    public static LootTable.Builder dropping(Block block, ILootCondition.IBuilder conditionBuilder, LootEntry.Builder<?> p_218494_2_) { return BlockLootTables.createSelfDropDispatchTable(block, conditionBuilder, p_218494_2_); }

    public static LootTable.Builder droppingWithSilkTouch(Block block, LootEntry.Builder<?> builder) { return BlockLootTables.createSilkTouchDispatchTable(block, builder); }

    public static LootTable.Builder droppingWithShears(Block block, LootEntry.Builder<?> noShearAlternativeEntry) { return BlockLootTables.createShearsDispatchTable(block, noShearAlternativeEntry); }

    public static LootTable.Builder droppingWithSilkTouchOrShears(Block block, LootEntry.Builder<?> alternativeLootEntry) { return BlockLootTables.createSilkTouchOrShearsDispatchTable(block, alternativeLootEntry); }

    public static LootTable.Builder droppingWithSilkTouch(Block block, IItemProvider noSilkTouch) { return BlockLootTables.createSingleItemTableWithSilkTouch(block, noSilkTouch); }

    public static LootTable.Builder droppingRandomly(IItemProvider item, IRandomRange range) { return BlockLootTables.createSingleItemTable(item, range); }

    public static LootTable.Builder droppingWithSilkTouchOrRandomly(Block block, IItemProvider item, IRandomRange range) { return BlockLootTables.createSingleItemTableWithSilkTouch(block, item, range); }

    public static LootTable.Builder onlyWithSilkTouch(IItemProvider item) { return BlockLootTables.createSilkTouchOnlyTable(item); }

    public static LootTable.Builder droppingAndFlowerPot(IItemProvider flower) { return BlockLootTables.createPotFlowerItemTable(flower); }

    public static LootTable.Builder droppingSlab(Block slab) { return BlockLootTables.createSlabItemTable(slab); }

    public static LootTable.Builder droppingWithName(Block block) { return BlockLootTables.createNameableBlockEntityTable(block); }

    public static LootTable.Builder droppingWithContents(Block shulker) { return BlockLootTables.createShulkerBoxDrop(shulker); }

    public static LootTable.Builder droppingWithPatterns(Block banner) { return BlockLootTables.createBannerDrop(banner); }

    public static LootTable.Builder droppingItemWithFortune(Block block, Item item) { return BlockLootTables.createOreDrop(block, item); }

    public static LootTable.Builder droppingItemRarely(Block block, IItemProvider item) { return BlockLootTables.createMushroomBlockDrop(block, item); }

    public static LootTable.Builder droppingSeeds(Block block) { return BlockLootTables.createGrassDrops(block); }

    public static LootTable.Builder droppingByAge(Block stemFruit, Item item) { return BlockLootTables.createStemDrops(stemFruit, item); }

    public static LootTable.Builder onlyWithShears(IItemProvider item) { return BlockLootTables.createShearsOnlyDrop(item); }

    public static LootTable.Builder droppingWithChancesAndSticks(Block block, Block sapling, float... chances) { return BlockLootTables.createLeavesDrops(block, sapling, chances); }

    public static LootTable.Builder droppingWithChancesSticksAndApples(Block block, Block sapling, float... chances) { return BlockLootTables.createOakLeavesDrops(block, sapling, chances); }

    public static LootTable.Builder droppingAndBonusWhen(Block block, Item itemConditional, Item withBonus, ILootCondition.IBuilder conditionBuilder) { return BlockLootTables.createCropDrops(block, itemConditional, withBonus, conditionBuilder); }

    @Override
    public void add(Block blockIn, LootTable.Builder table) { super.add(blockIn, table); }

    // GENERATED END
}
