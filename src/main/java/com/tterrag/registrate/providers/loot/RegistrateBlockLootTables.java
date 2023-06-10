package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
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
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link BlockLootSubProvider#applyExplosionDecay} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike p_248695_, FunctionUserBuilder<T> p_248548_) { return super.applyExplosionDecay(p_248695_, p_248548_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#applyExplosionCondition} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike p_249717_, ConditionUserBuilder<T> p_248851_) { return super.applyExplosionCondition(p_249717_, p_248851_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSelfDropDispatchTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createSelfDropDispatchTable(Block p_252253_, LootItemCondition.Builder p_248764_, LootPoolEntryContainer.Builder<?> p_249146_) { return BlockLootSubProvider.createSelfDropDispatchTable(p_252253_, p_248764_, p_249146_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchDispatchTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createSilkTouchDispatchTable(Block p_250203_, LootPoolEntryContainer.Builder<?> p_252089_) { return BlockLootSubProvider.createSilkTouchDispatchTable(p_250203_, p_252089_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsDispatchTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createShearsDispatchTable(Block p_252195_, LootPoolEntryContainer.Builder<?> p_250102_) { return BlockLootSubProvider.createShearsDispatchTable(p_252195_, p_250102_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchOrShearsDispatchTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block p_250539_, LootPoolEntryContainer.Builder<?> p_251459_) { return BlockLootSubProvider.createSilkTouchOrShearsDispatchTable(p_250539_, p_251459_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block p_249305_, ItemLike p_251905_) { return super.createSingleItemTableWithSilkTouch(p_249305_, p_251905_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createSingleItemTable(ItemLike p_251584_, NumberProvider p_249865_) { return super.createSingleItemTable(p_251584_, p_249865_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSingleItemTableWithSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createSingleItemTableWithSilkTouch(Block p_251449_, ItemLike p_248558_, NumberProvider p_250047_) { return super.createSingleItemTableWithSilkTouch(p_251449_, p_248558_, p_250047_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSilkTouchOnlyTable} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createSilkTouchOnlyTable(ItemLike p_252216_) { return BlockLootSubProvider.createSilkTouchOnlyTable(p_252216_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createPotFlowerItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createPotFlowerItemTable(ItemLike p_249395_) { return super.createPotFlowerItemTable(p_249395_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createSlabItemTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createSlabItemTable(Block p_251313_) { return super.createSlabItemTable(p_251313_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createNameableBlockEntityTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createNameableBlockEntityTable(Block p_252291_) { return super.createNameableBlockEntityTable(p_252291_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShulkerBoxDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createShulkerBoxDrop(Block p_252164_) { return super.createShulkerBoxDrop(p_252164_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCopperOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createCopperOreDrops(Block p_251306_) { return super.createCopperOreDrops(p_251306_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createLapisOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createLapisOreDrops(Block p_251511_) { return super.createLapisOreDrops(p_251511_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createRedstoneOreDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createRedstoneOreDrops(Block p_251906_) { return super.createRedstoneOreDrops(p_251906_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBannerDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createBannerDrop(Block p_249810_) { return super.createBannerDrop(p_249810_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBeeNestDrop} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createBeeNestDrop(Block p_250988_) { return BlockLootSubProvider.createBeeNestDrop(p_250988_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createBeeHiveDrop} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createBeeHiveDrop(Block p_248770_) { return BlockLootSubProvider.createBeeHiveDrop(p_248770_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCaveVinesDrop} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createCaveVinesDrop(Block p_251070_) { return BlockLootSubProvider.createCaveVinesDrop(p_251070_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createOreDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createOreDrop(Block p_250450_, Item p_249745_) { return super.createOreDrop(p_250450_, p_249745_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMushroomBlockDrop} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createMushroomBlockDrop(Block p_249959_, ItemLike p_249315_) { return super.createMushroomBlockDrop(p_249959_, p_249315_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createGrassDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createGrassDrops(Block p_252139_) { return super.createGrassDrops(p_252139_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createShearsOnlyDrop} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createShearsOnlyDrop(ItemLike p_250684_) { return BlockLootSubProvider.createShearsOnlyDrop(p_250684_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMultifaceBlockDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createMultifaceBlockDrops(Block p_249088_, LootItemCondition.Builder p_251535_) { return super.createMultifaceBlockDrops(p_249088_, p_251535_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createLeavesDrops(Block p_250088_, Block p_250731_, float... p_248949_) { return super.createLeavesDrops(p_250088_, p_250731_, p_248949_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createOakLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createOakLeavesDrops(Block p_249535_, Block p_251505_, float... p_250753_) { return super.createOakLeavesDrops(p_249535_, p_251505_, p_250753_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createMangroveLeavesDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createMangroveLeavesDrops(Block p_251103_) { return super.createMangroveLeavesDrops(p_251103_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCropDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createCropDrops(Block p_249457_, Item p_248599_, Item p_251915_, LootItemCondition.Builder p_252202_) { return super.createCropDrops(p_249457_, p_248599_, p_251915_, p_252202_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoublePlantShearsDrop} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createDoublePlantShearsDrop(Block p_248678_) { return BlockLootSubProvider.createDoublePlantShearsDrop(p_248678_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoublePlantWithSeedDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createDoublePlantWithSeedDrops(Block p_248590_, Block p_248735_) { return super.createDoublePlantWithSeedDrops(p_248590_, p_248735_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCandleDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createCandleDrops(Block p_250896_) { return super.createCandleDrops(p_250896_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createPetalsDrops} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createPetalsDrops(Block p_273240_) { return super.createPetalsDrops(p_273240_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createCandleCakeDrops} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public static LootTable.Builder createCandleCakeDrops(Block p_250280_) { return BlockLootSubProvider.createCandleCakeDrops(p_250280_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#addNetherVinesDropTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void addNetherVinesDropTable(Block p_252269_, Block p_250696_) { super.addNetherVinesDropTable(p_252269_, p_250696_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#createDoorTable} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public LootTable.Builder createDoorTable(Block p_252166_) { return super.createDoorTable(p_252166_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropPottedContents} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void dropPottedContents(Block p_251064_) { super.dropPottedContents(p_251064_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#otherWhenSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void otherWhenSilkTouch(Block p_249932_, Block p_252053_) { super.otherWhenSilkTouch(p_249932_, p_252053_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropOther} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void dropOther(Block p_248885_, ItemLike p_251883_) { super.dropOther(p_248885_, p_251883_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropWhenSilkTouch} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void dropWhenSilkTouch(Block p_250855_) { super.dropWhenSilkTouch(p_250855_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#dropSelf} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void dropSelf(Block p_249181_) { super.dropSelf(p_249181_); }

    /** Generated override to expose protected method: {@link BlockLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateBlockLootTables", date = "Fri, 9 Jun 2023 03:54:51 GMT")
    public void add(Block p_250610_, LootTable.Builder p_249817_) { super.add(p_250610_, p_249817_); }

    // GENERATED END
}
