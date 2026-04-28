package com.tterrag.registrate.providers;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Generated;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import lombok.Getter;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
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
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;

public class RegistrateRecipeProvider extends RecipeProvider implements RegistrateProvider, RecipeOutput {

    private final AbstractRegistrate<?> owner;
    @Getter(onMethod_ = {@Deprecated(forRemoval = true)})
    private @Nullable HolderLookup.Provider provider = null;

    public RegistrateRecipeProvider(AbstractRegistrate<?> owner, PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
        this.owner = owner;
    }

    public HolderLookup.Provider getRegistries() {
        Objects.requireNonNull(this.provider, "Attempting to resolve registries before provider is being run");
        return this.provider;
    }

    public HolderLookup<Item> itemLookup() {
        return getRegistries().lookupOrThrow(Registries.ITEM);
    }

    public HolderLookup<Block> blockLookup() {
        return getRegistries().lookupOrThrow(Registries.BLOCK);
    }

    public HolderLookup<EntityType<?>> entityLookup() {
        return getRegistries().lookupOrThrow(Registries.ENTITY_TYPE);
    }

    public <T> Holder<T> resolve(ResourceKey<T> key) {
        return provider.lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

	@Override
    protected CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider provider) {
        this.provider = provider;
        return super.run(output, provider);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Nullable
    private RecipeOutput callback;

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @org.jetbrains.annotations.Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (callback == null) {
            throw new IllegalStateException("Cannot accept recipes outside of a call to registerRecipes");
        }
        callback.accept(id, recipe, advancement, conditions);
    }

    @Override
    public Advancement.Builder advancement() {
        if (callback == null) {
            throw new IllegalStateException("Cannot get advancement outside of a call to registerRecipes");
        }
        return callback.advancement();
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        this.callback = recipeOutput;
        owner.genData(ProviderType.RECIPE, this);
        this.callback = null;
    }

    public ResourceLocation safeId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(owner.getModid(), safeName(id));
    }

    public ResourceLocation safeId(DataIngredient source) {
        return safeId(source.getId());
    }

    public ResourceLocation safeId(ItemLike registryEntry) {
        return safeId(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
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
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(category, output.get())
                .define('X', source.toVanilla());
        if (small) {
            builder.pattern("XX").pattern("XX");
        } else {
            builder.pattern("XXX").pattern("XXX").pattern("XXX");
        }
        builder.unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(output.get()));
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
        return ShapelessRecipeBuilder.shapeless(category, result.get(), amount)
            .requires(source.toVanilla(), required)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this));
    }

    public <T extends ItemLike> void singleItem(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {
        singleItemUnfinished(source, category, result, required, amount).save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void planks(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        singleItemUnfinished(source, category, result, 1, 4)
            .group("planks")
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void stairs(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shaped(category, result.get(), 4)
            .pattern("X  ").pattern("XX ").pattern("XXX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, category, result);
        }
    }

    public <T extends ItemLike> void slab(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shaped(category, result.get(), 6)
            .pattern("XXX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, category, result, 2);
        }
    }

    public <T extends ItemLike> void fence(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get(), 3)
            .pattern("W#W").pattern("W#W")
            .define('W', source.toVanilla())
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void fenceGate(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get())
            .pattern("#W#").pattern("#W#")
            .define('W', source.toVanilla())
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void wall(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        ShapedRecipeBuilder.shaped(category, result.get(), 6)
            .pattern("XXX").pattern("XXX")
            .define('X', source.toVanilla())
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
        stonecutting(source, category, result);
    }

    public <T extends ItemLike> void door(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get(), 3)
            .pattern("XX").pattern("XX").pattern("XX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void trapDoor(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get(), 2)
            .pattern("XXX").pattern("XXX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
            .save(this, safeId(result.get()));
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link RecipeProvider#buildAdvancement} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public CompletableFuture<?> buildAdvancement(CachedOutput output, HolderLookup.Provider registries, AdvancementHolder advancement) { return super.buildAdvancement(output, registries, advancement); }

    /** Generated override to expose protected method: {@link RecipeProvider#buildAdvancement} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public CompletableFuture<?> buildAdvancement(CachedOutput output, HolderLookup.Provider registries, AdvancementHolder advancement, net.neoforged.neoforge.common.conditions.ICondition... conditions) { return super.buildAdvancement(output, registries, advancement, conditions); }

    /** Generated override to expose protected method: {@link RecipeProvider#buildRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public void buildRecipes(RecipeOutput p_recipeOutput, HolderLookup.Provider holderLookup) { super.buildRecipes(p_recipeOutput, holderLookup); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateForEnabledBlockFamilies} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public void generateForEnabledBlockFamilies(RecipeOutput enabledFeatures, FeatureFlagSet p_251836_) { super.generateForEnabledBlockFamilies(enabledFeatures, p_251836_); }

    /** Generated override to expose protected method: {@link RecipeProvider#netheriteSmithing} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void netheriteSmithing(RecipeOutput recipeOutput, Item ingredientItem, RecipeCategory category, Item resultItem) { RecipeProvider.netheriteSmithing(recipeOutput, ingredientItem, category, resultItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#trimSmithing} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void trimSmithing(RecipeOutput recipeOutput, Item ingredientItem, ResourceLocation location) { RecipeProvider.trimSmithing(recipeOutput, ingredientItem, location); }

    /** Generated override to expose protected method: {@link RecipeProvider#twoByTwoPacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void twoByTwoPacker(RecipeOutput recipeOutput, RecipeCategory category, ItemLike packed, ItemLike unpacked) { RecipeProvider.twoByTwoPacker(recipeOutput, category, packed, unpacked); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void threeByThreePacker(RecipeOutput recipeOutput, RecipeCategory category, ItemLike packed, ItemLike unpacked, String criterionName) { RecipeProvider.threeByThreePacker(recipeOutput, category, packed, unpacked, criterionName); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void threeByThreePacker(RecipeOutput recipeOutput, RecipeCategory category, ItemLike packed, ItemLike unpacked) { RecipeProvider.threeByThreePacker(recipeOutput, category, packed, unpacked); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLog} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void planksFromLog(RecipeOutput recipeOutput, ItemLike planks, TagKey<Item> logs, int resultCount) { RecipeProvider.planksFromLog(recipeOutput, planks, logs, resultCount); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLogs} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void planksFromLogs(RecipeOutput recipeOutput, ItemLike planks, TagKey<Item> logs, int result) { RecipeProvider.planksFromLogs(recipeOutput, planks, logs, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodFromLogs} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void woodFromLogs(RecipeOutput recipeOutput, ItemLike wood, ItemLike log) { RecipeProvider.woodFromLogs(recipeOutput, wood, log); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodenBoat} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void woodenBoat(RecipeOutput recipeOutput, ItemLike boat, ItemLike material) { RecipeProvider.woodenBoat(recipeOutput, boat, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#chestBoat} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void chestBoat(RecipeOutput recipeOutput, ItemLike boat, ItemLike material) { RecipeProvider.chestBoat(recipeOutput, boat, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#buttonBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder buttonBuilder(ItemLike button, Ingredient material) { return RecipeProvider.buttonBuilder(button, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#doorBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder doorBuilder(ItemLike door, Ingredient material) { return RecipeProvider.doorBuilder(door, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder fenceBuilder(ItemLike fence, Ingredient material) { return RecipeProvider.fenceBuilder(fence, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceGateBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder fenceGateBuilder(ItemLike fenceGate, Ingredient material) { return RecipeProvider.fenceGateBuilder(fenceGate, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void pressurePlate(RecipeOutput recipeOutput, ItemLike pressurePlate, ItemLike material) { RecipeProvider.pressurePlate(recipeOutput, pressurePlate, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlateBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike pressurePlate, Ingredient material) { return RecipeProvider.pressurePlateBuilder(category, pressurePlate, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#slab} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void slab(RecipeOutput recipeOutput, RecipeCategory category, ItemLike slab, ItemLike material) { RecipeProvider.slab(recipeOutput, category, slab, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#slabBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder slabBuilder(RecipeCategory category, ItemLike slab, Ingredient material) { return RecipeProvider.slabBuilder(category, slab, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stairBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder stairBuilder(ItemLike stairs, Ingredient material) { return RecipeProvider.stairBuilder(stairs, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#trapdoorBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder trapdoorBuilder(ItemLike trapdoor, Ingredient material) { return RecipeProvider.trapdoorBuilder(trapdoor, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#signBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder signBuilder(ItemLike sign, Ingredient material) { return RecipeProvider.signBuilder(sign, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#hangingSign} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void hangingSign(RecipeOutput recipeOutput, ItemLike sign, ItemLike material) { RecipeProvider.hangingSign(recipeOutput, sign, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#colorBlockWithDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void colorBlockWithDye(RecipeOutput recipeOutput, List<Item> dyes, List<Item> dyeableItems, String group) { RecipeProvider.colorBlockWithDye(recipeOutput, dyes, dyeableItems, group); }

    /** Generated override to expose protected method: {@link RecipeProvider#carpet} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void carpet(RecipeOutput recipeOutput, ItemLike carpet, ItemLike material) { RecipeProvider.carpet(recipeOutput, carpet, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#bedFromPlanksAndWool} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void bedFromPlanksAndWool(RecipeOutput recipeOutput, ItemLike bed, ItemLike wool) { RecipeProvider.bedFromPlanksAndWool(recipeOutput, bed, wool); }

    /** Generated override to expose protected method: {@link RecipeProvider#banner} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void banner(RecipeOutput recipeOutput, ItemLike banner, ItemLike material) { RecipeProvider.banner(recipeOutput, banner, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassFromGlassAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void stainedGlassFromGlassAndDye(RecipeOutput recipeOutput, ItemLike stainedGlass, ItemLike dye) { RecipeProvider.stainedGlassFromGlassAndDye(recipeOutput, stainedGlass, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromStainedGlass} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void stainedGlassPaneFromStainedGlass(RecipeOutput recipeOutput, ItemLike stainedGlassPane, ItemLike stainedGlass) { RecipeProvider.stainedGlassPaneFromStainedGlass(recipeOutput, stainedGlassPane, stainedGlass); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromGlassPaneAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void stainedGlassPaneFromGlassPaneAndDye(RecipeOutput recipeOutput, ItemLike stainedGlassPane, ItemLike dye) { RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(recipeOutput, stainedGlassPane, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#coloredTerracottaFromTerracottaAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void coloredTerracottaFromTerracottaAndDye(RecipeOutput recipeOutput, ItemLike terracotta, ItemLike dye) { RecipeProvider.coloredTerracottaFromTerracottaAndDye(recipeOutput, terracotta, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#concretePowder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void concretePowder(RecipeOutput recipeOutput, ItemLike concretePowder, ItemLike dye) { RecipeProvider.concretePowder(recipeOutput, concretePowder, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#candle} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void candle(RecipeOutput recipeOutput, ItemLike candle, ItemLike dye) { RecipeProvider.candle(recipeOutput, candle, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#wall} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void wall(RecipeOutput recipeOutput, RecipeCategory category, ItemLike wall, ItemLike material) { RecipeProvider.wall(recipeOutput, category, wall, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#wallBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder wallBuilder(RecipeCategory category, ItemLike wall, Ingredient material) { return RecipeProvider.wallBuilder(category, wall, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#polished} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void polished(RecipeOutput recipeOutput, RecipeCategory category, ItemLike result, ItemLike material) { RecipeProvider.polished(recipeOutput, category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#polishedBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient material) { return RecipeProvider.polishedBuilder(category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#cut} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void cut(RecipeOutput recipeOutput, RecipeCategory category, ItemLike cutResult, ItemLike material) { RecipeProvider.cut(recipeOutput, category, cutResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#cutBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike cutResult, Ingredient material) { return RecipeProvider.cutBuilder(category, cutResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseled} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void chiseled(RecipeOutput recipeOutput, RecipeCategory category, ItemLike chiseledResult, ItemLike material) { RecipeProvider.chiseled(recipeOutput, category, chiseledResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#mosaicBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void mosaicBuilder(RecipeOutput recipeOutput, RecipeCategory category, ItemLike result, ItemLike material) { RecipeProvider.mosaicBuilder(recipeOutput, category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseledBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike chiseledResult, Ingredient material) { return RecipeProvider.chiseledBuilder(category, chiseledResult, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void stonecutterResultFromBase(RecipeOutput recipeOutput, RecipeCategory category, ItemLike result, ItemLike material) { RecipeProvider.stonecutterResultFromBase(recipeOutput, category, result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void stonecutterResultFromBase(RecipeOutput recipeOutput, RecipeCategory category, ItemLike result, ItemLike material, int resultCount) { RecipeProvider.stonecutterResultFromBase(recipeOutput, category, result, material, resultCount); }

    /** Generated override to expose protected method: {@link RecipeProvider#smeltingResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void smeltingResultFromBase(RecipeOutput recipeOutput, ItemLike result, ItemLike ingredient) { RecipeProvider.smeltingResultFromBase(recipeOutput, result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike template, TagKey<Item> baseMaterial) { RecipeProvider.copySmithingTemplate(recipeOutput, template, baseMaterial); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike template, ItemLike baseItem) { RecipeProvider.copySmithingTemplate(recipeOutput, template, baseItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void copySmithingTemplate(RecipeOutput recipeOutput, ItemLike template, Ingredient baseItem) { RecipeProvider.copySmithingTemplate(recipeOutput, template, baseItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void waxRecipes(RecipeOutput recipeOutput, FeatureFlagSet requiredFeatures) { RecipeProvider.waxRecipes(recipeOutput, requiredFeatures); }

    /** Generated override to expose protected method: {@link RecipeProvider#grate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void grate(RecipeOutput recipeOutput, Block grateBlock, Block material) { RecipeProvider.grate(recipeOutput, grateBlock, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#copperBulb} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void copperBulb(RecipeOutput recipeOutput, Block bulbBlock, Block material) { RecipeProvider.copperBulb(recipeOutput, bulbBlock, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static void generateRecipes(RecipeOutput recipeOutput, BlockFamily blockFamily, FeatureFlagSet requiredFeatures) { RecipeProvider.generateRecipes(recipeOutput, blockFamily, requiredFeatures); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBaseBlock} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Block getBaseBlock(BlockFamily family, BlockFamily.Variant variant) { return RecipeProvider.getBaseBlock(family, variant); }

    /** Generated override to expose protected method: {@link RecipeProvider#insideOf} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) { return RecipeProvider.insideOf(block); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) { return RecipeProvider.has(count, item); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) { return RecipeProvider.has(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) { return RecipeProvider.has(tag); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... items) { return RecipeProvider.inventoryTrigger(items); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) { return RecipeProvider.inventoryTrigger(predicates); }

    /** Generated override to expose protected method: {@link RecipeProvider#getHasName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static String getHasName(ItemLike itemLike) { return RecipeProvider.getHasName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getItemName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static String getItemName(ItemLike itemLike) { return RecipeProvider.getItemName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSimpleRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static String getSimpleRecipeName(ItemLike itemLike) { return RecipeProvider.getSimpleRecipeName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getConversionRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static String getConversionRecipeName(ItemLike result, ItemLike ingredient) { return RecipeProvider.getConversionRecipeName(result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSmeltingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static String getSmeltingRecipeName(ItemLike itemLike) { return RecipeProvider.getSmeltingRecipeName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBlastingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Tue, 28 Apr 2026 01:45:28 GMT")
    public static String getBlastingRecipeName(ItemLike itemLike) { return RecipeProvider.getBlastingRecipeName(itemLike); }

    // GENERATED END
}
