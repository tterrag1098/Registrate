package com.tterrag.registrate.providers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Generated;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;

public class RegistrateRecipeProvider extends RecipeProvider implements RegistrateProvider, Consumer<FinishedRecipe> {

    private final AbstractRegistrate<?> owner;

    public RegistrateRecipeProvider(AbstractRegistrate<?> owner, PackOutput output) {
        super(output);
        this.owner = owner;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Nullable
    private Consumer<FinishedRecipe> callback;

    @Override
    public void accept(@Nullable FinishedRecipe t) {
        if (callback == null) {
            throw new IllegalStateException("Cannot accept recipes outside of a call to registerRecipes");
        }
        callback.accept(t);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        this.callback = consumer;
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

    public <T extends ItemLike> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, RecipeSerializer<? extends AbstractCookingRecipe> serializer) {
        cooking(source, category, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer);
    }

    public <T extends ItemLike> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, String typeName, RecipeSerializer<? extends AbstractCookingRecipe> serializer) {
        SimpleCookingRecipeBuilder.generic(source, category, result.get(), experience, cookingTime, serializer)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        smelting(source, category, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.SMELTING_RECIPE);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        blasting(source, category, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.BLASTING_RECIPE);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        smoking(source, category, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.SMOKING_RECIPE);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        campfire(source, category, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
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

    /** Generated override to expose protected method: {@link RecipeProvider#saveAdvancement} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) { return super.saveAdvancement(output, finishedRecipe, advancementJson); }

    /** Generated override to expose protected method: {@link RecipeProvider#buildAdvancement} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public CompletableFuture<?> buildAdvancement(CachedOutput p_253674_, ResourceLocation p_254102_, Advancement.Builder p_253712_) { return super.buildAdvancement(p_253674_, p_254102_, p_253712_); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateForEnabledBlockFamilies} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> p_249188_, FeatureFlagSet p_251836_) { super.generateForEnabledBlockFamilies(p_249188_, p_251836_); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreSmelting} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void oreSmelting(Consumer<FinishedRecipe> p_250654_, List<ItemLike> p_250172_, RecipeCategory p_250588_, ItemLike p_251868_, float p_250789_, int p_252144_, String p_251687_) { RecipeProvider.oreSmelting(p_250654_, p_250172_, p_250588_, p_251868_, p_250789_, p_252144_, p_251687_); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreBlasting} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void oreBlasting(Consumer<FinishedRecipe> p_248775_, List<ItemLike> p_251504_, RecipeCategory p_248846_, ItemLike p_249735_, float p_248783_, int p_250303_, String p_251984_) { RecipeProvider.oreBlasting(p_248775_, p_251504_, p_248846_, p_249735_, p_248783_, p_250303_, p_251984_); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreCooking} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void oreCooking(Consumer<FinishedRecipe> p_250791_, RecipeSerializer<? extends AbstractCookingRecipe> p_251817_, List<ItemLike> p_249619_, RecipeCategory p_251154_, ItemLike p_250066_, float p_251871_, int p_251316_, String p_251450_, String p_249236_) { RecipeProvider.oreCooking(p_250791_, p_251817_, p_249619_, p_251154_, p_250066_, p_251871_, p_251316_, p_251450_, p_249236_); }

    /** Generated override to expose protected method: {@link RecipeProvider#netheriteSmithing} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void netheriteSmithing(Consumer<FinishedRecipe> p_251614_, Item p_250046_, RecipeCategory p_248986_, Item p_250389_) { RecipeProvider.netheriteSmithing(p_251614_, p_250046_, p_248986_, p_250389_); }

    /** Generated override to expose protected method: {@link RecipeProvider#trimSmithing} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void trimSmithing(Consumer<FinishedRecipe> p_285086_, Item p_285461_, ResourceLocation p_285044_) { RecipeProvider.trimSmithing(p_285086_, p_285461_, p_285044_); }

    /** Generated override to expose protected method: {@link RecipeProvider#twoByTwoPacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void twoByTwoPacker(Consumer<FinishedRecipe> p_248860_, RecipeCategory p_250881_, ItemLike p_252184_, ItemLike p_249710_) { RecipeProvider.twoByTwoPacker(p_248860_, p_250881_, p_252184_, p_249710_); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void threeByThreePacker(Consumer<FinishedRecipe> p_259036_, RecipeCategory p_259247_, ItemLike p_259376_, ItemLike p_259717_, String p_260308_) { RecipeProvider.threeByThreePacker(p_259036_, p_259247_, p_259376_, p_259717_, p_260308_); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void threeByThreePacker(Consumer<FinishedRecipe> p_260012_, RecipeCategory p_259186_, ItemLike p_259360_, ItemLike p_259263_) { RecipeProvider.threeByThreePacker(p_260012_, p_259186_, p_259360_, p_259263_); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLog} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void planksFromLog(Consumer<FinishedRecipe> p_259712_, ItemLike p_259052_, TagKey<Item> p_259045_, int p_259471_) { RecipeProvider.planksFromLog(p_259712_, p_259052_, p_259045_, p_259471_); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLogs} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void planksFromLogs(Consumer<FinishedRecipe> p_259910_, ItemLike p_259193_, TagKey<Item> p_259818_, int p_259807_) { RecipeProvider.planksFromLogs(p_259910_, p_259193_, p_259818_, p_259807_); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodFromLogs} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void woodFromLogs(Consumer<FinishedRecipe> p_126003_, ItemLike p_126004_, ItemLike p_126005_) { RecipeProvider.woodFromLogs(p_126003_, p_126004_, p_126005_); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodenBoat} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void woodenBoat(Consumer<FinishedRecipe> p_126022_, ItemLike p_126023_, ItemLike p_126024_) { RecipeProvider.woodenBoat(p_126022_, p_126023_, p_126024_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chestBoat} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void chestBoat(Consumer<FinishedRecipe> p_236372_, ItemLike p_236373_, ItemLike p_236374_) { RecipeProvider.chestBoat(p_236372_, p_236373_, p_236374_); }

    /** Generated override to expose protected method: {@link RecipeProvider#buttonBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder buttonBuilder(ItemLike p_176659_, Ingredient p_176660_) { return RecipeProvider.buttonBuilder(p_176659_, p_176660_); }

    /** Generated override to expose protected method: {@link RecipeProvider#doorBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder doorBuilder(ItemLike p_176671_, Ingredient p_176672_) { return RecipeProvider.doorBuilder(p_176671_, p_176672_); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder fenceBuilder(ItemLike p_176679_, Ingredient p_176680_) { return RecipeProvider.fenceBuilder(p_176679_, p_176680_); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceGateBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder fenceGateBuilder(ItemLike p_176685_, Ingredient p_176686_) { return RecipeProvider.fenceGateBuilder(p_176685_, p_176686_); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void pressurePlate(Consumer<FinishedRecipe> p_176691_, ItemLike p_176692_, ItemLike p_176693_) { RecipeProvider.pressurePlate(p_176691_, p_176692_, p_176693_); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlateBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder pressurePlateBuilder(RecipeCategory p_251447_, ItemLike p_251989_, Ingredient p_249211_) { return RecipeProvider.pressurePlateBuilder(p_251447_, p_251989_, p_249211_); }

    /** Generated override to expose protected method: {@link RecipeProvider#slab} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void slab(Consumer<FinishedRecipe> p_248880_, RecipeCategory p_251848_, ItemLike p_249368_, ItemLike p_252133_) { RecipeProvider.slab(p_248880_, p_251848_, p_249368_, p_252133_); }

    /** Generated override to expose protected method: {@link RecipeProvider#slabBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder slabBuilder(RecipeCategory p_251707_, ItemLike p_251284_, Ingredient p_248824_) { return RecipeProvider.slabBuilder(p_251707_, p_251284_, p_248824_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stairBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder stairBuilder(ItemLike p_176711_, Ingredient p_176712_) { return RecipeProvider.stairBuilder(p_176711_, p_176712_); }

    /** Generated override to expose protected method: {@link RecipeProvider#trapdoorBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder trapdoorBuilder(ItemLike p_176721_, Ingredient p_176722_) { return RecipeProvider.trapdoorBuilder(p_176721_, p_176722_); }

    /** Generated override to expose protected method: {@link RecipeProvider#signBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder signBuilder(ItemLike p_176727_, Ingredient p_176728_) { return RecipeProvider.signBuilder(p_176727_, p_176728_); }

    /** Generated override to expose protected method: {@link RecipeProvider#hangingSign} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void hangingSign(Consumer<FinishedRecipe> p_250663_, ItemLike p_252355_, ItemLike p_250437_) { RecipeProvider.hangingSign(p_250663_, p_252355_, p_250437_); }

    /** Generated override to expose protected method: {@link RecipeProvider#colorBlockWithDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void colorBlockWithDye(Consumer<FinishedRecipe> p_289666_, List<Item> p_289675_, List<Item> p_289672_, String p_289641_) { RecipeProvider.colorBlockWithDye(p_289666_, p_289675_, p_289672_, p_289641_); }

    /** Generated override to expose protected method: {@link RecipeProvider#carpet} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void carpet(Consumer<FinishedRecipe> p_176717_, ItemLike p_176718_, ItemLike p_176719_) { RecipeProvider.carpet(p_176717_, p_176718_, p_176719_); }

    /** Generated override to expose protected method: {@link RecipeProvider#bedFromPlanksAndWool} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void bedFromPlanksAndWool(Consumer<FinishedRecipe> p_126074_, ItemLike p_126075_, ItemLike p_126076_) { RecipeProvider.bedFromPlanksAndWool(p_126074_, p_126075_, p_126076_); }

    /** Generated override to expose protected method: {@link RecipeProvider#banner} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void banner(Consumer<FinishedRecipe> p_126082_, ItemLike p_126083_, ItemLike p_126084_) { RecipeProvider.banner(p_126082_, p_126083_, p_126084_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassFromGlassAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> p_126086_, ItemLike p_126087_, ItemLike p_126088_) { RecipeProvider.stainedGlassFromGlassAndDye(p_126086_, p_126087_, p_126088_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromStainedGlass} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> p_126090_, ItemLike p_126091_, ItemLike p_126092_) { RecipeProvider.stainedGlassPaneFromStainedGlass(p_126090_, p_126091_, p_126092_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromGlassPaneAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> p_126094_, ItemLike p_126095_, ItemLike p_126096_) { RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(p_126094_, p_126095_, p_126096_); }

    /** Generated override to expose protected method: {@link RecipeProvider#coloredTerracottaFromTerracottaAndDye} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> p_126098_, ItemLike p_126099_, ItemLike p_126100_) { RecipeProvider.coloredTerracottaFromTerracottaAndDye(p_126098_, p_126099_, p_126100_); }

    /** Generated override to expose protected method: {@link RecipeProvider#concretePowder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void concretePowder(Consumer<FinishedRecipe> p_126102_, ItemLike p_126103_, ItemLike p_126104_) { RecipeProvider.concretePowder(p_126102_, p_126103_, p_126104_); }

    /** Generated override to expose protected method: {@link RecipeProvider#candle} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void candle(Consumer<FinishedRecipe> p_176543_, ItemLike p_176544_, ItemLike p_176545_) { RecipeProvider.candle(p_176543_, p_176544_, p_176545_); }

    /** Generated override to expose protected method: {@link RecipeProvider#wall} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void wall(Consumer<FinishedRecipe> p_251034_, RecipeCategory p_251148_, ItemLike p_250499_, ItemLike p_249970_) { RecipeProvider.wall(p_251034_, p_251148_, p_250499_, p_249970_); }

    /** Generated override to expose protected method: {@link RecipeProvider#wallBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder wallBuilder(RecipeCategory p_249083_, ItemLike p_250754_, Ingredient p_250311_) { return RecipeProvider.wallBuilder(p_249083_, p_250754_, p_250311_); }

    /** Generated override to expose protected method: {@link RecipeProvider#polished} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void polished(Consumer<FinishedRecipe> p_251348_, RecipeCategory p_248719_, ItemLike p_250032_, ItemLike p_250021_) { RecipeProvider.polished(p_251348_, p_248719_, p_250032_, p_250021_); }

    /** Generated override to expose protected method: {@link RecipeProvider#polishedBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static RecipeBuilder polishedBuilder(RecipeCategory p_249131_, ItemLike p_251242_, Ingredient p_251412_) { return RecipeProvider.polishedBuilder(p_249131_, p_251242_, p_251412_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cut} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void cut(Consumer<FinishedRecipe> p_248712_, RecipeCategory p_252306_, ItemLike p_249686_, ItemLike p_251100_) { RecipeProvider.cut(p_248712_, p_252306_, p_249686_, p_251100_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cutBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static ShapedRecipeBuilder cutBuilder(RecipeCategory p_250895_, ItemLike p_251147_, Ingredient p_251563_) { return RecipeProvider.cutBuilder(p_250895_, p_251147_, p_251563_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseled} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void chiseled(Consumer<FinishedRecipe> p_250120_, RecipeCategory p_251604_, ItemLike p_251049_, ItemLike p_252267_) { RecipeProvider.chiseled(p_250120_, p_251604_, p_251049_, p_252267_); }

    /** Generated override to expose protected method: {@link RecipeProvider#mosaicBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void mosaicBuilder(Consumer<FinishedRecipe> p_249200_, RecipeCategory p_248788_, ItemLike p_251925_, ItemLike p_252242_) { RecipeProvider.mosaicBuilder(p_249200_, p_248788_, p_251925_, p_252242_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseledBuilder} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static ShapedRecipeBuilder chiseledBuilder(RecipeCategory p_251755_, ItemLike p_249782_, Ingredient p_250087_) { return RecipeProvider.chiseledBuilder(p_251755_, p_249782_, p_250087_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_251589_, RecipeCategory p_248911_, ItemLike p_251265_, ItemLike p_250033_) { RecipeProvider.stonecutterResultFromBase(p_251589_, p_248911_, p_251265_, p_250033_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_249145_, RecipeCategory p_250609_, ItemLike p_251254_, ItemLike p_249666_, int p_251462_) { RecipeProvider.stonecutterResultFromBase(p_249145_, p_250609_, p_251254_, p_249666_, p_251462_); }

    /** Generated override to expose protected method: {@link RecipeProvider#smeltingResultFromBase} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void smeltingResultFromBase(Consumer<FinishedRecipe> p_176740_, ItemLike p_176741_, ItemLike p_176742_) { RecipeProvider.smeltingResultFromBase(p_176740_, p_176741_, p_176742_); }

    /** Generated override to expose protected method: {@link RecipeProvider#nineBlockStorageRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void nineBlockStorageRecipes(Consumer<FinishedRecipe> p_249580_, RecipeCategory p_251203_, ItemLike p_251689_, RecipeCategory p_251376_, ItemLike p_248771_) { RecipeProvider.nineBlockStorageRecipes(p_249580_, p_251203_, p_251689_, p_251376_, p_248771_); }

    /** Generated override to expose protected method: {@link RecipeProvider#nineBlockStorageRecipesWithCustomPacking} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> p_250488_, RecipeCategory p_250885_, ItemLike p_251651_, RecipeCategory p_250874_, ItemLike p_248576_, String p_250171_, String p_249386_) { RecipeProvider.nineBlockStorageRecipesWithCustomPacking(p_250488_, p_250885_, p_251651_, p_250874_, p_248576_, p_250171_, p_249386_); }

    /** Generated override to expose protected method: {@link RecipeProvider#nineBlockStorageRecipesRecipesWithCustomUnpacking} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> p_250320_, RecipeCategory p_248979_, ItemLike p_249101_, RecipeCategory p_252036_, ItemLike p_250886_, String p_248768_, String p_250847_) { RecipeProvider.nineBlockStorageRecipesRecipesWithCustomUnpacking(p_250320_, p_248979_, p_249101_, p_252036_, p_250886_, p_248768_, p_250847_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void copySmithingTemplate(Consumer<FinishedRecipe> p_267061_, ItemLike p_266974_, TagKey<Item> p_267283_) { RecipeProvider.copySmithingTemplate(p_267061_, p_266974_, p_267283_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void copySmithingTemplate(Consumer<FinishedRecipe> p_266734_, ItemLike p_267133_, ItemLike p_267023_) { RecipeProvider.copySmithingTemplate(p_266734_, p_267133_, p_267023_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cookRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void cookRecipes(Consumer<FinishedRecipe> p_126007_, String p_126008_, RecipeSerializer<? extends AbstractCookingRecipe> p_250529_, int p_126010_) { RecipeProvider.cookRecipes(p_126007_, p_126008_, p_250529_, p_126010_); }

    /** Generated override to expose protected method: {@link RecipeProvider#simpleCookingRecipe} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void simpleCookingRecipe(Consumer<FinishedRecipe> p_249398_, String p_249709_, RecipeSerializer<? extends AbstractCookingRecipe> p_251876_, int p_249258_, ItemLike p_250669_, ItemLike p_250224_, float p_252138_) { RecipeProvider.simpleCookingRecipe(p_249398_, p_249709_, p_251876_, p_249258_, p_250669_, p_250224_, p_252138_); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void waxRecipes(Consumer<FinishedRecipe> p_176611_) { RecipeProvider.waxRecipes(p_176611_); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateRecipes} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static void generateRecipes(Consumer<FinishedRecipe> p_176581_, BlockFamily p_176582_) { RecipeProvider.generateRecipes(p_176581_, p_176582_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBaseBlock} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static Block getBaseBlock(BlockFamily p_176524_, BlockFamily.Variant p_176525_) { return RecipeProvider.getBaseBlock(p_176524_, p_176525_); }

    /** Generated override to expose protected method: {@link RecipeProvider#insideOf} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static EnterBlockTrigger.TriggerInstance insideOf(Block p_125980_) { return RecipeProvider.insideOf(p_125980_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints p_176521_, ItemLike p_176522_) { return RecipeProvider.has(p_176521_, p_176522_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static InventoryChangeTrigger.TriggerInstance has(ItemLike p_125978_) { return RecipeProvider.has(p_125978_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> p_206407_) { return RecipeProvider.has(p_206407_); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... p_126012_) { return RecipeProvider.inventoryTrigger(p_126012_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getHasName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static String getHasName(ItemLike p_176603_) { return RecipeProvider.getHasName(p_176603_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getItemName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static String getItemName(ItemLike p_176633_) { return RecipeProvider.getItemName(p_176633_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSimpleRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static String getSimpleRecipeName(ItemLike p_176645_) { return RecipeProvider.getSimpleRecipeName(p_176645_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getConversionRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static String getConversionRecipeName(ItemLike p_176518_, ItemLike p_176519_) { return RecipeProvider.getConversionRecipeName(p_176518_, p_176519_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSmeltingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static String getSmeltingRecipeName(ItemLike p_176657_) { return RecipeProvider.getSmeltingRecipeName(p_176657_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBlastingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Fri, 9 Jun 2023 04:03:23 GMT")
    public static String getBlastingRecipeName(ItemLike p_176669_) { return RecipeProvider.getBlastingRecipeName(p_176669_); }

    // GENERATED END
}
