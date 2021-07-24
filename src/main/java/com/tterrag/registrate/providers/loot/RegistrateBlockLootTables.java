package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RegistrateBlockLoot extends BlockLoot implements RegistrateLoot {

    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateBlockLoot> callback;

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

    public static <T> T withExplosionDecay(ItemLike item, FunctionUserBuilder<T> function) {
        return BlockLoot.applyExplosionDecay(item, function);
    }

    public static <T> T withSurvivesExplosion(ItemLike item, ConditionUserBuilder<T> condition) {
        return BlockLoot.applyExplosionCondition(item, condition);
    }

    public static LootTable.Builder dropping(ItemLike item) {
        return BlockLoot.createSelfDropDispatchTable(item, );
    }

    public static LootTable.Builder dropping(Block block, LootItemCondition.Builder conditionBuilder, LootPoolEntryContainer.Builder<?> entry) {
        return BlockLoot.createSelfDropDispatchTable(block, conditionBuilder, entry);
    }

    public static LootTable.Builder droppingWithSilkTouch(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLoot.createSilkTouchDispatchTable(block, builder);
    }

    public static LootTable.Builder droppingWithShears(Block block, LootPoolEntryContainer.Builder<?> noShearAlternativeEntry) {
        return BlockLoot.createShearsDispatchTable(block, noShearAlternativeEntry);
    }

    public static LootTable.Builder droppingWithSilkTouchOrShears(Block block, LootPoolEntryContainer.Builder<?> alternativeLootEntry) {
        return BlockLoot.createSilkTouchOrShearsDispatchTable(block, alternativeLootEntry);
    }

    public static LootTable.Builder droppingWithSilkTouch(Block block, ItemLike noSilkTouch) {
        return BlockLoot.createSingleItemTableWithSilkTouch(block, noSilkTouch);
    }

    public static LootTable.Builder droppingRandomly(ItemLike item, NumberProvider range) {
        return BlockLoot.createSingleItemTable(item, range);
    }

    public static LootTable.Builder droppingWithSilkTouchOrRandomly(Block block, ItemLike item, NumberProvider range) {
        return BlockLoot.createSingleItemTableWithSilkTouch(block, item, range);
    }

    public static LootTable.Builder onlyWithSilkTouch(ItemLike item) {
        return BlockLoot.createSilkTouchOnlyTable(item);
    }

    public static LootTable.Builder droppingAndFlowerPot(ItemLike flower) {
        return BlockLoot.createPotFlowerItemTable(flower);
    }

    public static LootTable.Builder droppingSlab(Block slab) {
        return BlockLoot.createSlabItemTable(slab);
    }

    public static LootTable.Builder droppingWithName(Block block) {
        return BlockLoot.createNameableBlockEntityTable(block);
    }

    public static LootTable.Builder droppingWithContents(Block shulker) {
        return BlockLoot.createShulkerBoxDrop(shulker);
    }

    public static LootTable.Builder droppingWithPatterns(Block banner) {
        return BlockLoot.createBannerDrop(banner);
    }

    public static LootTable.Builder droppingItemWithFortune(Block block, Item item) {
        return BlockLoot.createOreDrop(block, item);
    }

    public static LootTable.Builder droppingItemRarely(Block block, ItemLike item) {
        return BlockLoot.createMushroomBlockDrop(block, item);
    }

    public static LootTable.Builder droppingSeeds(Block block) {
        return BlockLoot.createGrassDrops(block);
    }

    public static LootTable.Builder droppingByAge(Block stemFruit, Item item) {
        return BlockLoot.createStemDrops(stemFruit, item);
    }

    public static LootTable.Builder onlyWithShears(ItemLike item) {
        return BlockLoot.createShearsOnlyDrop(item);
    }

    public static LootTable.Builder droppingWithChancesAndSticks(Block block, Block sapling, float... chances) {
        return BlockLoot.createLeavesDrops(block, sapling, chances);
    }

    public static LootTable.Builder droppingWithChancesSticksAndApples(Block block, Block sapling, float... chances) {
        return BlockLoot.createOakLeavesDrops(block, sapling, chances);
    }

    public static LootTable.Builder droppingAndBonusWhen(Block block, Item itemConditional, Item withBonus, LootItemCondition.Builder conditionBuilder) {
        return BlockLoot.createCropDrops(block, itemConditional, withBonus, conditionBuilder);
    }

    @Override
    public void add(Block blockIn, LootTable.Builder table) {
        super.add(blockIn, table);
    }

    // GENERATED END
}
