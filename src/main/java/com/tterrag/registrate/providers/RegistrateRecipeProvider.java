package com.tterrag.registrate.providers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Generated;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;

public class RegistrateRecipeProvider extends RecipeProvider implements RegistrateProvider, RecipeOutput {

    private final AbstractRegistrate<?> owner;

    public RegistrateRecipeProvider(AbstractRegistrate<?> owner, PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
        this.owner = owner;
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
        callback.accept(id, recipe, advancement);
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
        return new ResourceLocation(owner.getModid(), safeName(id));
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

    private static final String SMELTING_NAME = "smelting";
    @SuppressWarnings("null")
    private static final ImmutableMap<RecipeSerializer<? extends AbstractCookingRecipe>, String> COOKING_TYPE_NAMES = ImmutableMap.<RecipeSerializer<? extends AbstractCookingRecipe>, String>builder()
            .put(RecipeSerializer.SMELTING_RECIPE, SMELTING_NAME)
            .put(RecipeSerializer.BLASTING_RECIPE, "blasting")
            .put(RecipeSerializer.SMOKING_RECIPE, "smoking")
            .put(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, "campfire")
            .build();

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, RecipeSerializer<S> serializer, AbstractCookingRecipe.Factory<S> factory) {
        cooking(source, category, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer, factory);
    }

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, String typeName, RecipeSerializer<S> serializer, AbstractCookingRecipe.Factory<S> factory) {
        SimpleCookingRecipeBuilder.generic(source, category, result.get(), experience, cookingTime, serializer, factory)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
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
        SingleItemRecipeBuilder.stonecutting(source, category, result.get(), resultAmount)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
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
                .define('X', source);
        if (small) {
            builder.pattern("XX").pattern("XX");
        } else {
            builder.pattern("XXX").pattern("XXX").pattern("XXX");
        }
        builder.unlockedBy("has_" + safeName(source), source.getCritereon(this))
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
            .requires(source, required)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this));
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
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, category, result);
        }
    }

    public <T extends ItemLike> void slab(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shaped(category, result.get(), 6)
            .pattern("XXX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, category, result, 2);
        }
    }

    public <T extends ItemLike> void fence(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get(), 3)
            .pattern("W#W").pattern("W#W")
            .define('W', source)
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void fenceGate(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get())
            .pattern("#W#").pattern("#W#")
            .define('W', source)
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void wall(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        ShapedRecipeBuilder.shaped(category, result.get(), 6)
            .pattern("XXX").pattern("XXX")
            .define('X', source)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
        stonecutting(source, category, result);
    }

    public <T extends ItemLike> void door(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get(), 3)
            .pattern("XX").pattern("XX").pattern("XX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }

    public <T extends ItemLike> void trapDoor(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(category, result.get(), 2)
            .pattern("XXX").pattern("XXX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link RecipeProvider#buildAdvancement} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public CompletableFuture<?> buildAdvancement(CachedOutput p_253674_, AdvancementHolder p_301116_) { return super.buildAdvancement(p_253674_, p_301116_); }

    /** Generated override to expose protected method: {@link RecipeProvider#buildAdvancement} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public CompletableFuture<?> buildAdvancement(CachedOutput p_253674_, AdvancementHolder p_301116_, net.neoforged.neoforge.common.conditions.ICondition... conditions) { return super.buildAdvancement(p_253674_, p_301116_, conditions); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateForEnabledBlockFamilies} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public void generateForEnabledBlockFamilies(RecipeOutput p_301146_, FeatureFlagSet p_251836_) { super.generateForEnabledBlockFamilies(p_301146_, p_251836_); }

    /** Generated override to expose protected method: {@link RecipeProvider#netheriteSmithing} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void netheriteSmithing(RecipeOutput p_300886_, Item p_250046_, RecipeCategory p_248986_, Item p_250389_) { RecipeProvider.netheriteSmithing(p_300886_, p_250046_, p_248986_, p_250389_); }

    /** Generated override to expose protected method: {@link RecipeProvider#trimSmithing} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void trimSmithing(RecipeOutput p_301332_, Item p_285461_, ResourceLocation p_285044_) { RecipeProvider.trimSmithing(p_301332_, p_285461_, p_285044_); }

    /** Generated override to expose protected method: {@link RecipeProvider#twoByTwoPacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void twoByTwoPacker(RecipeOutput p_301022_, RecipeCategory p_250881_, ItemLike p_252184_, ItemLike p_249710_) { RecipeProvider.twoByTwoPacker(p_301022_, p_250881_, p_252184_, p_249710_); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void threeByThreePacker(RecipeOutput p_301298_, RecipeCategory p_259247_, ItemLike p_259376_, ItemLike p_259717_, String p_260308_) { RecipeProvider.threeByThreePacker(p_301298_, p_259247_, p_259376_, p_259717_, p_260308_); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void threeByThreePacker(RecipeOutput p_301302_, RecipeCategory p_259186_, ItemLike p_259360_, ItemLike p_259263_) { RecipeProvider.threeByThreePacker(p_301302_, p_259186_, p_259360_, p_259263_); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLog} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void planksFromLog(RecipeOutput p_301020_, ItemLike p_259052_, TagKey<Item> p_259045_, int p_259471_) { RecipeProvider.planksFromLog(p_301020_, p_259052_, p_259045_, p_259471_); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLogs} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void planksFromLogs(RecipeOutput p_301221_, ItemLike p_259193_, TagKey<Item> p_259818_, int p_259807_) { RecipeProvider.planksFromLogs(p_301221_, p_259193_, p_259818_, p_259807_); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodFromLogs} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void woodFromLogs(RecipeOutput p_301095_, ItemLike p_126004_, ItemLike p_126005_) { RecipeProvider.woodFromLogs(p_301095_, p_126004_, p_126005_); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodenBoat} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void woodenBoat(RecipeOutput p_301315_, ItemLike p_126023_, ItemLike p_126024_) { RecipeProvider.woodenBoat(p_301315_, p_126023_, p_126024_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chestBoat} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void chestBoat(RecipeOutput p_300996_, ItemLike p_236373_, ItemLike p_236374_) { RecipeProvider.chestBoat(p_300996_, p_236373_, p_236374_); }

    /** Generated override to expose protected method: {@link RecipeProvider#buttonBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder buttonBuilder(ItemLike p_176659_, Ingredient p_176660_) { return RecipeProvider.buttonBuilder(p_176659_, p_176660_); }

    /** Generated override to expose protected method: {@link RecipeProvider#doorBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder doorBuilder(ItemLike p_176671_, Ingredient p_176672_) { return RecipeProvider.doorBuilder(p_176671_, p_176672_); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder fenceBuilder(ItemLike p_176679_, Ingredient p_176680_) { return RecipeProvider.fenceBuilder(p_176679_, p_176680_); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceGateBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder fenceGateBuilder(ItemLike p_176685_, Ingredient p_176686_) { return RecipeProvider.fenceGateBuilder(p_176685_, p_176686_); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void pressurePlate(RecipeOutput p_301211_, ItemLike p_176692_, ItemLike p_176693_) { RecipeProvider.pressurePlate(p_301211_, p_176692_, p_176693_); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlateBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder pressurePlateBuilder(RecipeCategory p_251447_, ItemLike p_251989_, Ingredient p_249211_) { return RecipeProvider.pressurePlateBuilder(p_251447_, p_251989_, p_249211_); }

    /** Generated override to expose protected method: {@link RecipeProvider#slab} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void slab(RecipeOutput p_300949_, RecipeCategory p_251848_, ItemLike p_249368_, ItemLike p_252133_) { RecipeProvider.slab(p_300949_, p_251848_, p_249368_, p_252133_); }

    /** Generated override to expose protected method: {@link RecipeProvider#slabBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder slabBuilder(RecipeCategory p_251707_, ItemLike p_251284_, Ingredient p_248824_) { return RecipeProvider.slabBuilder(p_251707_, p_251284_, p_248824_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stairBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder stairBuilder(ItemLike p_176711_, Ingredient p_176712_) { return RecipeProvider.stairBuilder(p_176711_, p_176712_); }

    /** Generated override to expose protected method: {@link RecipeProvider#trapdoorBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder trapdoorBuilder(ItemLike p_176721_, Ingredient p_176722_) { return RecipeProvider.trapdoorBuilder(p_176721_, p_176722_); }

    /** Generated override to expose protected method: {@link RecipeProvider#signBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder signBuilder(ItemLike p_176727_, Ingredient p_176728_) { return RecipeProvider.signBuilder(p_176727_, p_176728_); }

    /** Generated override to expose protected method: {@link RecipeProvider#hangingSign} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void hangingSign(RecipeOutput p_301272_, ItemLike p_252355_, ItemLike p_250437_) { RecipeProvider.hangingSign(p_301272_, p_252355_, p_250437_); }

    /** Generated override to expose protected method: {@link RecipeProvider#colorBlockWithDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void colorBlockWithDye(RecipeOutput p_301260_, List<Item> p_289675_, List<Item> p_289672_, String p_289641_) { RecipeProvider.colorBlockWithDye(p_301260_, p_289675_, p_289672_, p_289641_); }

    /** Generated override to expose protected method: {@link RecipeProvider#carpet} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void carpet(RecipeOutput p_300953_, ItemLike p_176718_, ItemLike p_176719_) { RecipeProvider.carpet(p_300953_, p_176718_, p_176719_); }

    /** Generated override to expose protected method: {@link RecipeProvider#bedFromPlanksAndWool} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void bedFromPlanksAndWool(RecipeOutput p_301140_, ItemLike p_126075_, ItemLike p_126076_) { RecipeProvider.bedFromPlanksAndWool(p_301140_, p_126075_, p_126076_); }

    /** Generated override to expose protected method: {@link RecipeProvider#banner} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void banner(RecipeOutput p_301322_, ItemLike p_126083_, ItemLike p_126084_) { RecipeProvider.banner(p_301322_, p_126083_, p_126084_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassFromGlassAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void stainedGlassFromGlassAndDye(RecipeOutput p_300895_, ItemLike p_126087_, ItemLike p_126088_) { RecipeProvider.stainedGlassFromGlassAndDye(p_300895_, p_126087_, p_126088_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromStainedGlass} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void stainedGlassPaneFromStainedGlass(RecipeOutput p_301233_, ItemLike p_126091_, ItemLike p_126092_) { RecipeProvider.stainedGlassPaneFromStainedGlass(p_301233_, p_126091_, p_126092_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromGlassPaneAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void stainedGlassPaneFromGlassPaneAndDye(RecipeOutput p_301038_, ItemLike p_126095_, ItemLike p_126096_) { RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(p_301038_, p_126095_, p_126096_); }

    /** Generated override to expose protected method: {@link RecipeProvider#coloredTerracottaFromTerracottaAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void coloredTerracottaFromTerracottaAndDye(RecipeOutput p_300948_, ItemLike p_126099_, ItemLike p_126100_) { RecipeProvider.coloredTerracottaFromTerracottaAndDye(p_300948_, p_126099_, p_126100_); }

    /** Generated override to expose protected method: {@link RecipeProvider#concretePowder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void concretePowder(RecipeOutput p_301177_, ItemLike p_126103_, ItemLike p_126104_) { RecipeProvider.concretePowder(p_301177_, p_126103_, p_126104_); }

    /** Generated override to expose protected method: {@link RecipeProvider#candle} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void candle(RecipeOutput p_301024_, ItemLike p_176544_, ItemLike p_176545_) { RecipeProvider.candle(p_301024_, p_176544_, p_176545_); }

    /** Generated override to expose protected method: {@link RecipeProvider#wall} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void wall(RecipeOutput p_301176_, RecipeCategory p_251148_, ItemLike p_250499_, ItemLike p_249970_) { RecipeProvider.wall(p_301176_, p_251148_, p_250499_, p_249970_); }

    /** Generated override to expose protected method: {@link RecipeProvider#wallBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder wallBuilder(RecipeCategory p_249083_, ItemLike p_250754_, Ingredient p_250311_) { return RecipeProvider.wallBuilder(p_249083_, p_250754_, p_250311_); }

    /** Generated override to expose protected method: {@link RecipeProvider#polished} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void polished(RecipeOutput p_301100_, RecipeCategory p_248719_, ItemLike p_250032_, ItemLike p_250021_) { RecipeProvider.polished(p_301100_, p_248719_, p_250032_, p_250021_); }

    /** Generated override to expose protected method: {@link RecipeProvider#polishedBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static RecipeBuilder polishedBuilder(RecipeCategory p_249131_, ItemLike p_251242_, Ingredient p_251412_) { return RecipeProvider.polishedBuilder(p_249131_, p_251242_, p_251412_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cut} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void cut(RecipeOutput p_301189_, RecipeCategory p_252306_, ItemLike p_249686_, ItemLike p_251100_) { RecipeProvider.cut(p_301189_, p_252306_, p_249686_, p_251100_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cutBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static ShapedRecipeBuilder cutBuilder(RecipeCategory p_250895_, ItemLike p_251147_, Ingredient p_251563_) { return RecipeProvider.cutBuilder(p_250895_, p_251147_, p_251563_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseled} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void chiseled(RecipeOutput p_300911_, RecipeCategory p_251604_, ItemLike p_251049_, ItemLike p_252267_) { RecipeProvider.chiseled(p_300911_, p_251604_, p_251049_, p_252267_); }

    /** Generated override to expose protected method: {@link RecipeProvider#mosaicBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void mosaicBuilder(RecipeOutput p_301088_, RecipeCategory p_248788_, ItemLike p_251925_, ItemLike p_252242_) { RecipeProvider.mosaicBuilder(p_301088_, p_248788_, p_251925_, p_252242_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseledBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static ShapedRecipeBuilder chiseledBuilder(RecipeCategory p_251755_, ItemLike p_249782_, Ingredient p_250087_) { return RecipeProvider.chiseledBuilder(p_251755_, p_249782_, p_250087_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void stonecutterResultFromBase(RecipeOutput p_301035_, RecipeCategory p_248911_, ItemLike p_251265_, ItemLike p_250033_) { RecipeProvider.stonecutterResultFromBase(p_301035_, p_248911_, p_251265_, p_250033_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void stonecutterResultFromBase(RecipeOutput p_301195_, RecipeCategory p_250609_, ItemLike p_251254_, ItemLike p_249666_, int p_251462_) { RecipeProvider.stonecutterResultFromBase(p_301195_, p_250609_, p_251254_, p_249666_, p_251462_); }

    /** Generated override to expose protected method: {@link RecipeProvider#smeltingResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void smeltingResultFromBase(RecipeOutput p_300916_, ItemLike p_176741_, ItemLike p_176742_) { RecipeProvider.smeltingResultFromBase(p_300916_, p_176741_, p_176742_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void copySmithingTemplate(RecipeOutput p_300986_, ItemLike p_266974_, TagKey<Item> p_267283_) { RecipeProvider.copySmithingTemplate(p_300986_, p_266974_, p_267283_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void copySmithingTemplate(RecipeOutput p_301265_, ItemLike p_267133_, ItemLike p_267023_) { RecipeProvider.copySmithingTemplate(p_301265_, p_267133_, p_267023_); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void waxRecipes(RecipeOutput p_301254_, FeatureFlagSet p_313879_) { RecipeProvider.waxRecipes(p_301254_, p_313879_); }

    /** Generated override to expose protected method: {@link RecipeProvider#grate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void grate(RecipeOutput p_309012_, Block p_309021_, Block p_309140_) { RecipeProvider.grate(p_309012_, p_309021_, p_309140_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copperBulb} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void copperBulb(RecipeOutput p_308971_, Block p_309026_, Block p_308866_) { RecipeProvider.copperBulb(p_308971_, p_309026_, p_308866_); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static void generateRecipes(RecipeOutput p_301014_, BlockFamily p_176582_, FeatureFlagSet p_313799_) { RecipeProvider.generateRecipes(p_301014_, p_176582_, p_313799_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBaseBlock} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Block getBaseBlock(BlockFamily p_176524_, BlockFamily.Variant p_176525_) { return RecipeProvider.getBaseBlock(p_176524_, p_176525_); }

    /** Generated override to expose protected method: {@link RecipeProvider#insideOf} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block p_125980_) { return RecipeProvider.insideOf(p_125980_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints p_176521_, ItemLike p_176522_) { return RecipeProvider.has(p_176521_, p_176522_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike p_125978_) { return RecipeProvider.has(p_125978_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> p_206407_) { return RecipeProvider.has(p_206407_); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... p_299111_) { return RecipeProvider.inventoryTrigger(p_299111_); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... p_126012_) { return RecipeProvider.inventoryTrigger(p_126012_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getHasName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static String getHasName(ItemLike p_176603_) { return RecipeProvider.getHasName(p_176603_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getItemName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static String getItemName(ItemLike p_176633_) { return RecipeProvider.getItemName(p_176633_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSimpleRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static String getSimpleRecipeName(ItemLike p_176645_) { return RecipeProvider.getSimpleRecipeName(p_176645_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getConversionRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static String getConversionRecipeName(ItemLike p_176518_, ItemLike p_176519_) { return RecipeProvider.getConversionRecipeName(p_176518_, p_176519_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSmeltingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static String getSmeltingRecipeName(ItemLike p_176657_) { return RecipeProvider.getSmeltingRecipeName(p_176657_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBlastingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sat, 23 Dec 2023 10:56:17 GMT")
    public static String getBlastingRecipeName(ItemLike p_176669_) { return RecipeProvider.getBlastingRecipeName(p_176669_); }

    // GENERATED END
}
