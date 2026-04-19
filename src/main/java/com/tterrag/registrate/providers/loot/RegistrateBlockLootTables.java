package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import javax.annotation.Generated;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegistrateBlockLootTables extends BlockLootSubProvider implements RegistrateLootTables {
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateBlockLootTables> callback;

    private final HolderLookup<Item> itemLookup;
    private final HolderLookup<Block> blockLookup;
    private final HolderLookup<EntityType<?>> entityLookup;

    public RegistrateBlockLootTables(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<RegistrateBlockLootTables> callback) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        this.parent = parent;
        this.callback = callback;
        itemLookup = registries.lookupOrThrow(Registries.ITEM);
        blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        entityLookup = registries.lookupOrThrow(Registries.ENTITY_TYPE);
    }

    @Override
    protected void generate() {
        callback.accept(this);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return parent.getAll(Registries.BLOCK).stream().map(Supplier::get).collect(Collectors.toList());
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

    /** Generated override to expose protected method: {@link BlockLootSubProvider#applyExplosionDecay} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike type, FunctionUserBuilder<T> builder) { return super.applyExplosionDecay(type, builder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#applyExplosionCondition} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike type, ConditionUserBuilder<T> builder) { return super.applyExplosionCondition(type, builder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSelfDropDispatchTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public static LootTable.Builder createSelfDropDispatchTable(Block original, LootItemCondition.Builder condition, LootPoolEntryContainer.Builder<?> entry) { return BlockLootSubProvider.createSelfDropDispatchTable(original, condition, entry); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchDispatchTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSilkTouchDispatchTable(Block original, LootPoolEntryContainer.Builder<?> entry) { return super.createSilkTouchDispatchTable(original, entry); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsDispatchTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createShearsDispatchTable(Block original, LootPoolEntryContainer.Builder<?> entry) { return super.createShearsDispatchTable(original, entry); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchOrShearsDispatchTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSilkTouchOrShearsDispatchTable(Block original, LootPoolEntryContainer.Builder<?> entry) { return super.createSilkTouchOrShearsDispatchTable(original, entry); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block original, ItemLike drop) { return super.createSingleItemTableWithSilkTouch(original, drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSingleItemTable(ItemLike drop, NumberProvider count) { return super.createSingleItemTable(drop, count); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block original, ItemLike drop, NumberProvider count) { return super.createSingleItemTableWithSilkTouch(original, drop, count); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchOnlyTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSilkTouchOnlyTable(ItemLike drop) { return super.createSilkTouchOnlyTable(drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createPotFlowerItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createPotFlowerItemTable(ItemLike flower) { return super.createPotFlowerItemTable(flower); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSlabItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createSlabItemTable(Block slab) { return super.createSlabItemTable(slab); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createNameableBlockEntityTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createNameableBlockEntityTable(Block drop) { return super.createNameableBlockEntityTable(drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShulkerBoxDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createShulkerBoxDrop(Block shulkerBox) { return super.createShulkerBoxDrop(shulkerBox); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCopperOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createCopperOreDrops(Block block) { return super.createCopperOreDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createLapisOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createLapisOreDrops(Block block) { return super.createLapisOreDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createRedstoneOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createRedstoneOreDrops(Block block) { return super.createRedstoneOreDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBannerDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createBannerDrop(Block original) { return super.createBannerDrop(original); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBeeNestDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createBeeNestDrop(Block original) { return super.createBeeNestDrop(original); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBeeHiveDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createBeeHiveDrop(Block original) { return super.createBeeHiveDrop(original); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCaveVinesDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createCaveVinesDrop(Block original) { return super.createCaveVinesDrop(original); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCopperGolemStatueBlock} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createCopperGolemStatueBlock(Block block) { return super.createCopperGolemStatueBlock(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createOreDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createOreDrop(Block original, Item drop) { return super.createOreDrop(original, drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMushroomBlockDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createMushroomBlockDrop(Block original, ItemLike drop) { return super.createMushroomBlockDrop(original, drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createGrassDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createGrassDrops(Block original) { return super.createGrassDrops(original); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsOnlyDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createShearsOnlyDrop(ItemLike drop) { return super.createShearsOnlyDrop(drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsOrSilkTouchOnlyDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike drop) { return super.createShearsOrSilkTouchOnlyDrop(drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMultifaceBlockDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder condition) { return super.createMultifaceBlockDrops(block, condition); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMultifaceBlockDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createMultifaceBlockDrops(Block block) { return super.createMultifaceBlockDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMossyCarpetBlockDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createMossyCarpetBlockDrops(Block block) { return super.createMossyCarpetBlockDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createLeavesDrops(Block original, Block sapling, float... saplingChances) { return super.createLeavesDrops(original, sapling, saplingChances); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createOakLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createOakLeavesDrops(Block original, Block sapling, float... saplingChances) { return super.createOakLeavesDrops(original, sapling, saplingChances); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMangroveLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createMangroveLeavesDrops(Block block) { return super.createMangroveLeavesDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCropDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createCropDrops(Block original, Item cropDrop, Item seedDrop, LootItemCondition.Builder isMaxAge) { return super.createCropDrops(original, cropDrop, seedDrop, isMaxAge); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoublePlantShearsDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createDoublePlantShearsDrop(Block block) { return super.createDoublePlantShearsDrop(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoublePlantWithSeedDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block drop) { return super.createDoublePlantWithSeedDrops(block, drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCandleDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createCandleDrops(Block block) { return super.createCandleDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCandleCakeDrops} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public static LootTable.Builder createCandleCakeDrops(Block candle) { return BlockLootSubProvider.createCandleCakeDrops(candle); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#addNetherVinesDropTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void addNetherVinesDropTable(Block vineBlock, Block plantBlock) { super.addNetherVinesDropTable(vineBlock, plantBlock); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoorTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public LootTable.Builder createDoorTable(Block block) { return super.createDoorTable(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropPottedContents} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void dropPottedContents(Block potted) { super.dropPottedContents(potted); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#otherWhenSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void otherWhenSilkTouch(Block block, Block other) { super.otherWhenSilkTouch(block, other); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropOther} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void dropOther(Block block, ItemLike drop) { super.dropOther(block, drop); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropWhenSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void dropWhenSilkTouch(Block block) { super.dropWhenSilkTouch(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropSelf} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void dropSelf(Block block) { super.dropSelf(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Sun, 19 Apr 2026 10:15:02 GMT")
    public void add(Block block, LootTable.Builder builder) { super.add(block, builder); }

    // GENERATED END
}
