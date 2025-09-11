package com.tterrag.registrate.providers.generators;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.experimental.Delegate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.CheckReturnValue;
import javax.annotation.Generated;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class RegistrateRecipeProvider extends RecipeProvider implements RecipeOutput {

    private final RegistrateRecipeRunner runner;

    @Delegate
    private final RecipeOutput outputDelegated;

    private final HolderLookup<Item> itemLookup;
    private final HolderLookup<Block> blockLookup;
    private final HolderLookup<EntityType<?>> entityLookup;

    public RegistrateRecipeProvider(RegistrateRecipeRunner runner, HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.runner = runner;
        this.outputDelegated = output;
        itemLookup = registries.lookupOrThrow(Registries.ITEM);
        blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        entityLookup = registries.lookupOrThrow(Registries.ENTITY_TYPE);
    }

    @Override
    public void buildRecipes() {
        runner.provider = this;
        runner.owner.genData(ProviderType.RECIPE, this);
        runner.provider = null;
    }

    public HolderLookup.Provider registries() {
        return registries;
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

    public <T> Holder<T> resolve(ResourceKey<T> key) {
        return registries.lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    public ResourceLocation safeId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(runner.owner.getModid(), safeName(id));
    }

    public ResourceLocation safeId(DataIngredient source) {
        return safeId(source.getId());
    }

    public ResourceLocation safeId(ItemLike registryEntry) {
        return safeId(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public ResourceKey<Recipe<?>> safeKey(ResourceLocation id) {
        return ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(runner.owner.getModid(), safeName(id)));
    }

    public ResourceKey<Recipe<?>> safeKey(DataIngredient source) {
        return safeKey(source.getId());
    }

    public ResourceKey<Recipe<?>> safeKey(ItemLike registryEntry) {
        return safeKey(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public String safeName(ResourceLocation id) {
        return id.getPath().replace('/', '_');
    }

    public String safeName(DataIngredient source) {
        return safeName(source.getId());
    }

    public String safeName(ItemLike registryEntry) {
        return safeName(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public static final int DEFAULT_SMELT_TIME = 200;
    public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
    public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
    public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;

    private static final ImmutableMap<RecipeSerializer<? extends AbstractCookingRecipe>, String> COOKING_TYPE_NAMES = ImmutableMap.<RecipeSerializer<? extends AbstractCookingRecipe>, String>builder()
            .put(RecipeSerializer.SMELTING_RECIPE, "smelting")
            .put(RecipeSerializer.BLASTING_RECIPE, "blasting")
            .put(RecipeSerializer.SMOKING_RECIPE, "smoking")
            .put(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, "campfire")
            .build();

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, RecipeSerializer<S> serializer, AbstractCookingRecipe.Factory<S> factory) {
        cooking(source, category, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer, factory);
    }

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, String typeName, RecipeSerializer<S> serializer, AbstractCookingRecipe.Factory<S> factory) {
        SimpleCookingRecipeBuilder.generic(source.toVanilla(), category, result.get(), experience, cookingTime, serializer, factory)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        smelting(source, category, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        blasting(source, category, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        smoking(source, category, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.SMOKING_RECIPE, SmokingRecipe::new);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        campfire(source, category, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.CAMPFIRE_COOKING_RECIPE, CampfireCookingRecipe::new);
    }

    public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        stonecutting(source, category, result, 1);
    }

    public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int resultAmount) {
        SingleItemRecipeBuilder.stonecutting(source.toVanilla(), category, result.get(), resultAmount)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_stonecutting");
    }

    public <T extends ItemLike> void smeltingAndBlasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float xp) {
        smelting(source, category, result, xp);
        blasting(source, category, result, xp);
    }

    public <T extends ItemLike> void food(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float xp) {
        smelting(source, category, result, xp);
        smoking(source, category, result, xp);
        campfire(source, category, result, xp);
    }

    public <T extends ItemLike> void square(DataIngredient source, RecipeCategory category, Supplier<? extends T> output, boolean small) {
        ShapedRecipeBuilder builder = shaped(category, output.get())
                .define('X', source.toVanilla());
        if (small) {
            builder.pattern("XX").pattern("XX");
        } else {
            builder.pattern("XXX").pattern("XXX").pattern("XXX");
        }
        builder.unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(output.get()));
    }

    /**
     * @param <T>
     * @param source
     * @param output
     * @deprecated Broken, use {@link #storage(NonNullSupplier, RecipeCategory, NonNullSupplier)} or {@link #storage(DataIngredient, RecipeCategory, NonNullSupplier, DataIngredient, NonNullSupplier)}.
     */
    @Deprecated
    public <T extends ItemLike> void storage(DataIngredient source, RecipeCategory category, NonNullSupplier<? extends T> output) {
        square(source, category, output, false);
        // This is backwards, but leaving in for binary compat
        singleItemUnfinished(source, category, output, 1, 9)
                .save(this, safeId(source) + "_from_" + safeName(output.get()));
    }

    public <T extends ItemLike> void storage(NonNullSupplier<? extends T> source, RecipeCategory category, NonNullSupplier<? extends T> output) {
        storage(DataIngredient.items(source), category, source, DataIngredient.items(output), output);
    }

    public <T extends ItemLike> void storage(DataIngredient sourceIngredient, RecipeCategory category, NonNullSupplier<? extends T> source, DataIngredient outputIngredient, NonNullSupplier<? extends T> output) {
        square(sourceIngredient, category, output, false);
        singleItemUnfinished(outputIngredient, category, source, 1, 9)
                .save(this, safeId(sourceIngredient) + "_from_" + safeName(output.get()));
    }

    @CheckReturnValue
    public <T extends ItemLike> ShapelessRecipeBuilder singleItemUnfinished(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {
        return shapeless(category, result.get(), amount)
                .requires(source.toVanilla(), required)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this));
    }

    public <T extends ItemLike> void singleItem(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {
        singleItemUnfinished(source, category, result, required, amount).save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void planks(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        singleItemUnfinished(source, category, result, 1, 4)
                .group("planks")
                .save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void stairs(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        shaped(category, result.get(), 4)
                .pattern("X  ").pattern("XX ").pattern("XXX")
                .define('X', source.toVanilla())
                .group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
        if (stone) {
            stonecutting(source, category, result);
        }
    }

    public <T extends ItemLike> void slab(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        shaped(category, result.get(), 6)
                .pattern("XXX")
                .define('X', source.toVanilla())
                .group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
        if (stone) {
            stonecutting(source, category, result, 2);
        }
    }

    public <T extends ItemLike> void fence(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 3)
                .pattern("W#W").pattern("W#W")
                .define('W', source.toVanilla())
                .define('#', Tags.Items.RODS_WOODEN)
                .group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void fenceGate(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get())
                .pattern("#W#").pattern("#W#")
                .define('W', source.toVanilla())
                .define('#', Tags.Items.RODS_WOODEN)
                .group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void wall(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        shaped(category, result.get(), 6)
                .pattern("XXX").pattern("XXX")
                .define('X', source.toVanilla())
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
        stonecutting(source, category, result);
    }

    public <T extends ItemLike> void door(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 3)
                .pattern("XX").pattern("XX").pattern("XX")
                .define('X', source.toVanilla())
                .group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void trapDoor(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 2)
                .pattern("XXX").pattern("XXX")
                .define('X', source.toVanilla())
                .group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeKey(result.get()));
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link RecipeProvider#generateForEnabledBlockFamilies} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void generateForEnabledBlockFamilies(FeatureFlagSet enabledFeatures) { super.generateForEnabledBlockFamilies(enabledFeatures); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreSmelting} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void oreSmelting(List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) { super.oreSmelting(ingredients, category, result, experience, cookingTime, group); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreBlasting} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void oreBlasting(List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) { super.oreBlasting(ingredients, category, result, experience, cookingTime, group); }

    /** Generated override to expose protected method: {@link RecipeProvider#netheriteSmithing} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void netheriteSmithing(Item ingredientItem, RecipeCategory category, Item resultItem) { super.netheriteSmithing(ingredientItem, category, resultItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#trimSmithing} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void trimSmithing(Item template, ResourceKey<TrimPattern> pattern, ResourceKey<Recipe<?>> recipe) { super.trimSmithing(template, pattern, recipe); }

    /** Generated override to expose protected method: {@link RecipeProvider#twoByTwoPacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void twoByTwoPacker(RecipeCategory category, ItemLike packed, ItemLike unpacked) { super.twoByTwoPacker(category, packed, unpacked); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked, String criterionName) { super.threeByThreePacker(category, packed, unpacked, criterionName); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void threeByThreePacker(RecipeCategory category, ItemLike packed, ItemLike unpacked) { super.threeByThreePacker(category, packed, unpacked); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLog} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void planksFromLog(ItemLike planks, TagKey<Item> logs, int resultCount) { super.planksFromLog(planks, logs, resultCount); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLogs} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void planksFromLogs(ItemLike planks, TagKey<Item> logs, int result) { super.planksFromLogs(planks, logs, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodFromLogs} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void woodFromLogs(ItemLike wood, ItemLike log) { super.woodFromLogs(wood, log); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodenBoat} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void woodenBoat(ItemLike boat, ItemLike material) { super.woodenBoat(boat, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#chestBoat} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void chestBoat(ItemLike boat, ItemLike material) { super.chestBoat(boat, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#buttonBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder buttonBuilder(ItemLike button, Ingredient material) { return super.buttonBuilder(button, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#doorBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder doorBuilder(ItemLike door, Ingredient material) { return super.doorBuilder(door, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder fenceBuilder(ItemLike fence, Ingredient material) { return super.fenceBuilder(fence, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceGateBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder fenceGateBuilder(ItemLike fenceGate, Ingredient material) { return super.fenceGateBuilder(fenceGate, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void pressurePlate(ItemLike pressurePlate, ItemLike material) { super.pressurePlate(pressurePlate, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlateBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike pressurePlate, Ingredient material) { return super.pressurePlateBuilder(category, pressurePlate, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#slab} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void slab(RecipeCategory category, ItemLike slab, ItemLike material) { super.slab(category, slab, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#slabBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder slabBuilder(RecipeCategory category, ItemLike slab, Ingredient material) { return super.slabBuilder(category, slab, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stairBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder stairBuilder(ItemLike stairs, Ingredient material) { return super.stairBuilder(stairs, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#trapdoorBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder trapdoorBuilder(ItemLike trapdoor, Ingredient material) { return super.trapdoorBuilder(trapdoor, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#signBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder signBuilder(ItemLike sign, Ingredient material) { return super.signBuilder(sign, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#hangingSign} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void hangingSign(ItemLike sign, ItemLike material) { super.hangingSign(sign, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#colorItemWithDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void colorItemWithDye(List<Item> dyeItems, List<Item> dyeableItems, String group, RecipeCategory category) { super.colorItemWithDye(dyeItems, dyeableItems, group, category); }

    /** Generated override to expose protected method: {@link RecipeProvider#carpet} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void carpet(ItemLike carpet, ItemLike material) { super.carpet(carpet, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#bedFromPlanksAndWool} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void bedFromPlanksAndWool(ItemLike bed, ItemLike wool) { super.bedFromPlanksAndWool(bed, wool); }

    /** Generated override to expose protected method: {@link RecipeProvider#banner} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void banner(ItemLike banner, ItemLike material) { super.banner(banner, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassFromGlassAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void stainedGlassFromGlassAndDye(ItemLike stainedGlass, ItemLike dye) { super.stainedGlassFromGlassAndDye(stainedGlass, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#dryGhast} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void dryGhast(ItemLike dryGhast) { super.dryGhast(dryGhast); }

    /** Generated override to expose protected method: {@link RecipeProvider#harness} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void harness(ItemLike harness, ItemLike wool) { super.harness(harness, wool); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromStainedGlass} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void stainedGlassPaneFromStainedGlass(ItemLike stainedGlassPane, ItemLike stainedGlass) { super.stainedGlassPaneFromStainedGlass(stainedGlassPane, stainedGlass); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromGlassPaneAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void stainedGlassPaneFromGlassPaneAndDye(ItemLike stainedGlassPane, ItemLike dye) { super.stainedGlassPaneFromGlassPaneAndDye(stainedGlassPane, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#coloredTerracottaFromTerracottaAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void coloredTerracottaFromTerracottaAndDye(ItemLike terracotta, ItemLike dye) { super.coloredTerracottaFromTerracottaAndDye(terracotta, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#concretePowder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void concretePowder(ItemLike concretePowder, ItemLike dye) { super.concretePowder(concretePowder, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#candle} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void candle(ItemLike candle, ItemLike dye) { super.candle(candle, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#wall} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void wall(RecipeCategory category, ItemLike wall, ItemLike material) { super.wall(category, wall, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#wallBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder wallBuilder(RecipeCategory category, ItemLike wall, Ingredient material) { return super.wallBuilder(category, wall, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#polished} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void polished(RecipeCategory category, ItemLike result, ItemLike material) { super.polished(category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#polishedBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient material) { return super.polishedBuilder(category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#cut} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void cut(RecipeCategory category, ItemLike cutResult, ItemLike material) { super.cut(category, cutResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#cutBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike cutResult, Ingredient material) { return super.cutBuilder(category, cutResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseled} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void chiseled(RecipeCategory category, ItemLike chiseledResult, ItemLike material) { super.chiseled(category, chiseledResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#mosaicBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike material) { super.mosaicBuilder(category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseledBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike chiseledResult, Ingredient material) { return super.chiseledBuilder(category, chiseledResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material) { super.stonecutterResultFromBase(category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material, int resultCount) { super.stonecutterResultFromBase(category, result, material, resultCount); }

    /** Generated override to expose protected method: {@link RecipeProvider#smeltingResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void smeltingResultFromBase(ItemLike result, ItemLike ingredient) { super.smeltingResultFromBase(result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#nineBlockStorageRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed) { super.nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void copySmithingTemplate(ItemLike template, ItemLike baseItem) { super.copySmithingTemplate(template, baseItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void copySmithingTemplate(ItemLike template, Ingredient baseItem) { super.copySmithingTemplate(template, baseItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void waxRecipes(FeatureFlagSet requiredFeatures) { super.waxRecipes(requiredFeatures); }

    /** Generated override to expose protected method: {@link RecipeProvider#grate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void grate(Block grateBlock, Block material) { super.grate(grateBlock, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#copperBulb} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void copperBulb(Block bulbBlock, Block material) { super.copperBulb(bulbBlock, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#suspiciousStew} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void suspiciousStew(Item flowerItem, SuspiciousEffectHolder effect) { super.suspiciousStew(flowerItem, effect); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public void generateRecipes(BlockFamily blockFamily, FeatureFlagSet requiredFeatures) { super.generateRecipes(blockFamily, requiredFeatures); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBaseBlock} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public Block getBaseBlock(BlockFamily family, BlockFamily.Variant variant) { return super.getBaseBlock(family, variant); }

    /** Generated override to expose protected method: {@link RecipeProvider#insideOf} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) { return RecipeProvider.insideOf(block); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) { return super.has(count, item); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) { return super.has(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) { return super.has(tag); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... items) { return RecipeProvider.inventoryTrigger(items); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) { return RecipeProvider.inventoryTrigger(predicates); }

    /** Generated override to expose protected method: {@link RecipeProvider#getHasName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static String getHasName(ItemLike itemLike) { return RecipeProvider.getHasName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getItemName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static String getItemName(ItemLike itemLike) { return RecipeProvider.getItemName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSimpleRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static String getSimpleRecipeName(ItemLike itemLike) { return RecipeProvider.getSimpleRecipeName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getConversionRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static String getConversionRecipeName(ItemLike result, ItemLike ingredient) { return RecipeProvider.getConversionRecipeName(result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSmeltingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static String getSmeltingRecipeName(ItemLike itemLike) { return RecipeProvider.getSmeltingRecipeName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBlastingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public static String getBlastingRecipeName(ItemLike itemLike) { return RecipeProvider.getBlastingRecipeName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#tag} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public Ingredient tag(TagKey<Item> tag) { return super.tag(tag); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) { return super.shaped(category, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) { return super.shaped(category, result, count); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStack result) { return super.shapeless(category, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) { return super.shapeless(category, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 20 Jul 2025 06:59:06 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) { return super.shapeless(category, result, count); }

    // GENERATED END

}
