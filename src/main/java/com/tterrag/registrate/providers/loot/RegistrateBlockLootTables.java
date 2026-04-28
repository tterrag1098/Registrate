package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.entity.EntityType;
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

import javax.annotation.Generated;

public class RegistrateBlockLootTables extends VanillaBlockLoot implements RegistrateLootTables {
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateBlockLootTables> callback;

    private final HolderLookup<Item> itemLookup;
    private final HolderLookup<Block> blockLookup;
    private final HolderLookup<EntityType<?>> entityLookup;

    public RegistrateBlockLootTables(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<RegistrateBlockLootTables> callback) {
        super(provider);
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
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike item, FunctionUserBuilder<T> functionBuilder) { return super.applyExplosionDecay(item, functionBuilder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#applyExplosionCondition} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike item, ConditionUserBuilder<T> conditionBuilder) { return super.applyExplosionCondition(item, conditionBuilder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchDispatchTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) { return super.createSilkTouchDispatchTable(block, builder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsDispatchTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) { return super.createShearsDispatchTable(block, builder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchOrShearsDispatchTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) { return super.createSilkTouchOrShearsDispatchTable(block, builder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item) { return super.createSingleItemTableWithSilkTouch(block, item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSingleItemTable(ItemLike item, NumberProvider count) { return super.createSingleItemTable(item, count); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike item, NumberProvider count) { return super.createSingleItemTableWithSilkTouch(block, item, count); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchOnlyTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSilkTouchOnlyTable(ItemLike item) { return super.createSilkTouchOnlyTable(item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createPotFlowerItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createPotFlowerItemTable(ItemLike item) { return super.createPotFlowerItemTable(item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSlabItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createSlabItemTable(Block block) { return super.createSlabItemTable(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createNameableBlockEntityTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createNameableBlockEntityTable(Block block) { return super.createNameableBlockEntityTable(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShulkerBoxDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createShulkerBoxDrop(Block block) { return super.createShulkerBoxDrop(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCopperOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createCopperOreDrops(Block block) { return super.createCopperOreDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createLapisOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createLapisOreDrops(Block block) { return super.createLapisOreDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createRedstoneOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createRedstoneOreDrops(Block block) { return super.createRedstoneOreDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBannerDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createBannerDrop(Block block) { return super.createBannerDrop(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBeeNestDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createBeeNestDrop(Block block) { return super.createBeeNestDrop(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBeeHiveDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createBeeHiveDrop(Block block) { return super.createBeeHiveDrop(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCaveVinesDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createCaveVinesDrop(Block block) { return super.createCaveVinesDrop(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createOreDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createOreDrop(Block block, Item item) { return super.createOreDrop(block, item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMushroomBlockDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createMushroomBlockDrop(Block block, ItemLike item) { return super.createMushroomBlockDrop(block, item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createGrassDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createGrassDrops(Block block) { return super.createGrassDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsOnlyDrop} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public static LootTable.Builder createShearsOnlyDrop(ItemLike item) { return BlockLootSubProvider.createShearsOnlyDrop(item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMultifaceBlockDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder builder) { return super.createMultifaceBlockDrops(block, builder); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createLeavesDrops(Block leavesBlock, Block saplingBlock, float... chances) { return super.createLeavesDrops(leavesBlock, saplingBlock, chances); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createOakLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createOakLeavesDrops(Block oakLeavesBlock, Block saplingBlock, float... chances) { return super.createOakLeavesDrops(oakLeavesBlock, saplingBlock, chances); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMangroveLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createMangroveLeavesDrops(Block block) { return super.createMangroveLeavesDrops(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCropDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createCropDrops(Block cropBlock, Item grownCropItem, Item seedsItem, LootItemCondition.Builder dropGrownCropCondition) { return super.createCropDrops(cropBlock, grownCropItem, seedsItem, dropGrownCropCondition); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoublePlantShearsDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createDoublePlantShearsDrop(Block sheared) { return super.createDoublePlantShearsDrop(sheared); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoublePlantWithSeedDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block sheared) { return super.createDoublePlantWithSeedDrops(block, sheared); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCandleDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createCandleDrops(Block candleBlock) { return super.createCandleDrops(candleBlock); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createPetalsDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createPetalsDrops(Block petalBlock) { return super.createPetalsDrops(petalBlock); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCandleCakeDrops} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public static LootTable.Builder createCandleCakeDrops(Block candleCakeBlock) { return BlockLootSubProvider.createCandleCakeDrops(candleCakeBlock); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#addNetherVinesDropTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void addNetherVinesDropTable(Block vines, Block plant) { super.addNetherVinesDropTable(vines, plant); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoorTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public LootTable.Builder createDoorTable(Block doorBlock) { return super.createDoorTable(doorBlock); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropPottedContents} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void dropPottedContents(Block flowerPot) { super.dropPottedContents(flowerPot); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#otherWhenSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void otherWhenSilkTouch(Block block, Block other) { super.otherWhenSilkTouch(block, other); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropOther} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void dropOther(Block block, ItemLike item) { super.dropOther(block, item); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropWhenSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void dropWhenSilkTouch(Block block) { super.dropWhenSilkTouch(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropSelf} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void dropSelf(Block block) { super.dropSelf(block); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Tue, 28 Apr 2026 01:43:43 GMT")
    public void add(Block block, LootTable.Builder builder) { super.add(block, builder); }

    // GENERATED END
}
