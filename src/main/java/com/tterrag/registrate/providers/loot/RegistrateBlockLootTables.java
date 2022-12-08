package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
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
public class RegistrateBlockLootTables extends VanillaBlockLoot implements RegistrateLootTables {
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateBlockLootTables> callback;

    @Override
    protected void generate() {
        callback.accept(this);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return parent.getAll(Registries.BLOCK).stream().map(Supplier::get).collect(Collectors.toList());
    }

    // @formatter:off
    // GENERATED START

    public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike p_236222_, FunctionUserBuilder<T> p_236223_) { return super.applyExplosionDecay(p_236222_, p_236223_); }

    public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike p_236225_, ConditionUserBuilder<T> p_236226_) { return super.applyExplosionCondition(p_236225_, p_236226_); }

    public LootTable.Builder createSingleItemTable(ItemLike p_124127_) { return super.createSingleItemTable(p_124127_); }

    public static LootTable.Builder createSelfDropDispatchTable(Block p_124172_, LootItemCondition.Builder p_124173_, LootPoolEntryContainer.Builder<?> p_124174_) { return BlockLootSubProvider.createSelfDropDispatchTable(p_124172_, p_124173_, p_124174_); }

    public static LootTable.Builder createSilkTouchDispatchTable(Block p_124169_, LootPoolEntryContainer.Builder<?> p_124170_) { return BlockLootSubProvider.createSilkTouchDispatchTable(p_124169_, p_124170_); }

    public static LootTable.Builder createShearsDispatchTable(Block p_124268_, LootPoolEntryContainer.Builder<?> p_124269_) { return BlockLootSubProvider.createShearsDispatchTable(p_124268_, p_124269_); }

    public static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block p_124284_, LootPoolEntryContainer.Builder<?> p_124285_) { return BlockLootSubProvider.createSilkTouchOrShearsDispatchTable(p_124284_, p_124285_); }

    public LootTable.Builder createSingleItemTableWithSilkTouch(Block p_124258_, ItemLike p_124259_) { return super.createSingleItemTableWithSilkTouch(p_124258_, p_124259_); }

    public LootTable.Builder createSingleItemTable(ItemLike p_176040_, NumberProvider p_176041_) { return super.createSingleItemTable(p_176040_, p_176041_); }

    public LootTable.Builder createSingleItemTableWithSilkTouch(Block p_176043_, ItemLike p_176044_, NumberProvider p_176045_) { return super.createSingleItemTableWithSilkTouch(p_176043_, p_176044_, p_176045_); }

    public static LootTable.Builder createSilkTouchOnlyTable(ItemLike p_124251_) { return BlockLootSubProvider.createSilkTouchOnlyTable(p_124251_); }

    public LootTable.Builder createPotFlowerItemTable(ItemLike p_124271_) { return super.createPotFlowerItemTable(p_124271_); }

    public LootTable.Builder createSlabItemTable(Block p_124291_) { return super.createSlabItemTable(p_124291_); }

    public LootTable.Builder createNameableBlockEntityTable(Block p_124293_) { return super.createNameableBlockEntityTable(p_124293_); }

    public LootTable.Builder createShulkerBoxDrop(Block p_124295_) { return super.createShulkerBoxDrop(p_124295_); }

    public LootTable.Builder createCopperOreDrops(Block p_176047_) { return super.createCopperOreDrops(p_176047_); }

    public LootTable.Builder createLapisOreDrops(Block p_176049_) { return super.createLapisOreDrops(p_176049_); }

    public LootTable.Builder createRedstoneOreDrops(Block p_176051_) { return super.createRedstoneOreDrops(p_176051_); }

    public LootTable.Builder createBannerDrop(Block p_124297_) { return super.createBannerDrop(p_124297_); }

    public static LootTable.Builder createBeeNestDrop(Block p_124299_) { return BlockLootSubProvider.createBeeNestDrop(p_124299_); }

    public static LootTable.Builder createBeeHiveDrop(Block p_124301_) { return BlockLootSubProvider.createBeeHiveDrop(p_124301_); }

    public static LootTable.Builder createCaveVinesDrop(Block p_176053_) { return BlockLootSubProvider.createCaveVinesDrop(p_176053_); }

    public LootTable.Builder createOreDrop(Block p_124140_, Item p_124141_) { return super.createOreDrop(p_124140_, p_124141_); }

    public LootTable.Builder createMushroomBlockDrop(Block p_124278_, ItemLike p_124279_) { return super.createMushroomBlockDrop(p_124278_, p_124279_); }

    public LootTable.Builder createGrassDrops(Block p_124303_) { return super.createGrassDrops(p_124303_); }

    public LootTable.Builder createStemDrops(Block p_124255_, Item p_124256_) { return super.createStemDrops(p_124255_, p_124256_); }

    public LootTable.Builder createAttachedStemDrops(Block p_124275_, Item p_124276_) { return super.createAttachedStemDrops(p_124275_, p_124276_); }

    public static LootTable.Builder createShearsOnlyDrop(ItemLike p_124287_) { return BlockLootSubProvider.createShearsOnlyDrop(p_124287_); }

    public LootTable.Builder createMultifaceBlockDrops(Block p_236228_, LootItemCondition.Builder p_236229_) { return super.createMultifaceBlockDrops(p_236228_, p_236229_); }

    public LootTable.Builder createLeavesDrops(Block p_124158_, Block p_124159_, float... p_124160_) { return super.createLeavesDrops(p_124158_, p_124159_, p_124160_); }

    public LootTable.Builder createOakLeavesDrops(Block p_124264_, Block p_124265_, float... p_124266_) { return super.createOakLeavesDrops(p_124264_, p_124265_, p_124266_); }

    public LootTable.Builder createMangroveLeavesDrops(Block p_236249_) { return super.createMangroveLeavesDrops(p_236249_); }

    public LootTable.Builder createCropDrops(Block p_124143_, Item p_124144_, Item p_124145_, LootItemCondition.Builder p_124146_) { return super.createCropDrops(p_124143_, p_124144_, p_124145_, p_124146_); }

    public static LootTable.Builder createDoublePlantShearsDrop(Block p_124305_) { return BlockLootSubProvider.createDoublePlantShearsDrop(p_124305_); }

    public LootTable.Builder createDoublePlantWithSeedDrops(Block p_124261_, Block p_124262_) { return super.createDoublePlantWithSeedDrops(p_124261_, p_124262_); }

    public LootTable.Builder createCandleDrops(Block p_176057_) { return super.createCandleDrops(p_176057_); }

    public static LootTable.Builder createCandleCakeDrops(Block p_176059_) { return BlockLootSubProvider.createCandleCakeDrops(p_176059_); }

    @Override public void dropSelf(Block p_249181_) { super.dropSelf(p_249181_); }

    @Override public void add(Block p_124166_, LootTable.Builder p_124167_) { super.add(p_124166_, p_124167_); }

    @Override public<T extends Comparable<T>&StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block p_252154_, Property<T> p_250272_, T p_250292_) { return super.createSinglePropConditionTable(p_252154_,p_250272_,p_250292_); }

    @Override public void addNetherVinesDropTable(Block p_252269_, Block p_250696_) { super.addNetherVinesDropTable(p_252269_,p_250696_); }

    @Override public LootTable.Builder createDoorTable(Block p_252166_) { return super.createDoorTable(p_252166_); }

    @Override public void dropPottedContents(Block p_251064_) { super.dropPottedContents(p_251064_); }

    @Override public void otherWhenSilkTouch(Block p_249932_, Block p_252053_) { super.otherWhenSilkTouch(p_249932_,p_252053_); }

    @Override public void dropOther(Block p_248885_, ItemLike p_251883_) { super.dropOther(p_248885_,p_251883_); }

    @Override public void dropWhenSilkTouch(Block p_250855_) { super.dropWhenSilkTouch(p_250855_); }

    // GENERATED END
}
