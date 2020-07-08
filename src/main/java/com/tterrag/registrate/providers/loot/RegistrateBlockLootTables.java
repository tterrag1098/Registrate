package com.tterrag.registrate.providers.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tterrag.registrate.AbstractRegistrate;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
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

    public static LootTable.Builder dropping(IItemProvider p_218546_0_) { return BlockLootTables.dropping(p_218546_0_); }

    public static LootTable.Builder dropping(Block p_218494_0_, ILootCondition.IBuilder p_218494_1_, LootEntry.Builder<?> p_218494_2_) { return BlockLootTables.dropping(p_218494_0_, p_218494_1_, p_218494_2_); }

    public static LootTable.Builder droppingWithSilkTouch(Block p_218519_0_, LootEntry.Builder<?> p_218519_1_) { return BlockLootTables.droppingWithSilkTouch(p_218519_0_, p_218519_1_); }

    public static LootTable.Builder droppingWithShears(Block p_218511_0_, LootEntry.Builder<?> p_218511_1_) { return BlockLootTables.droppingWithShears(p_218511_0_, p_218511_1_); }

    public static LootTable.Builder droppingWithSilkTouchOrShears(Block p_218535_0_, LootEntry.Builder<?> p_218535_1_) { return BlockLootTables.droppingWithSilkTouchOrShears(p_218535_0_, p_218535_1_); }

    public static LootTable.Builder droppingWithSilkTouch(Block p_218515_0_, IItemProvider p_218515_1_) { return BlockLootTables.droppingWithSilkTouch(p_218515_0_, p_218515_1_); }

    public static LootTable.Builder droppingRandomly(IItemProvider p_218463_0_, IRandomRange p_218463_1_) { return BlockLootTables.droppingRandomly(p_218463_0_, p_218463_1_); }

    public static LootTable.Builder droppingWithSilkTouchOrRandomly(Block p_218530_0_, IItemProvider p_218530_1_, IRandomRange p_218530_2_) { return BlockLootTables.droppingWithSilkTouchOrRandomly(p_218530_0_, p_218530_1_, p_218530_2_); }

    public static LootTable.Builder onlyWithSilkTouch(IItemProvider p_218561_0_) { return BlockLootTables.onlyWithSilkTouch(p_218561_0_); }

    public static LootTable.Builder droppingAndFlowerPot(IItemProvider p_218523_0_) { return BlockLootTables.droppingAndFlowerPot(p_218523_0_); }

    public static LootTable.Builder droppingSlab(Block p_218513_0_) { return BlockLootTables.droppingSlab(p_218513_0_); }

    public static LootTable.Builder droppingWithName(Block p_218481_0_) { return BlockLootTables.droppingWithName(p_218481_0_); }

    public static LootTable.Builder droppingWithContents(Block p_218544_0_) { return BlockLootTables.droppingWithContents(p_218544_0_); }

    public static LootTable.Builder droppingWithPatterns(Block p_218559_0_) { return BlockLootTables.droppingWithPatterns(p_218559_0_); }

    public static LootTable.Builder droppingItemWithFortune(Block p_218476_0_, Item p_218476_1_) { return BlockLootTables.droppingItemWithFortune(p_218476_0_, p_218476_1_); }

    public static LootTable.Builder droppingItemRarely(Block p_218491_0_, IItemProvider p_218491_1_) { return BlockLootTables.droppingItemRarely(p_218491_0_, p_218491_1_); }

    public static LootTable.Builder droppingSeeds(Block p_218570_0_) { return BlockLootTables.droppingSeeds(p_218570_0_); }

    public static LootTable.Builder droppingByAge(Block p_218475_0_, Item p_218475_1_) { return BlockLootTables.droppingByAge(p_218475_0_, p_218475_1_); }

    public static LootTable.Builder onlyWithShears(IItemProvider p_218486_0_) { return BlockLootTables.onlyWithShears(p_218486_0_); }

    public static LootTable.Builder droppingWithChancesAndSticks(Block p_218540_0_, Block p_218540_1_, float... p_218540_2_) { return BlockLootTables.droppingWithChancesAndSticks(p_218540_0_, p_218540_1_, p_218540_2_); }

    public static LootTable.Builder droppingWithChancesSticksAndApples(Block p_218526_0_, Block p_218526_1_, float... p_218526_2_) { return BlockLootTables.droppingWithChancesSticksAndApples(p_218526_0_, p_218526_1_, p_218526_2_); }

    public static LootTable.Builder droppingAndBonusWhen(Block p_218541_0_, Item p_218541_1_, Item p_218541_2_, ILootCondition.IBuilder p_218541_3_) { return BlockLootTables.droppingAndBonusWhen(p_218541_0_, p_218541_1_, p_218541_2_, p_218541_3_); }

    @Override
    public void registerLootTable(Block blockIn, LootTable.Builder table) { super.registerLootTable(blockIn, table); }

    // GENERATED END
}
