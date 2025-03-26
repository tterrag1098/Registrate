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

    public RegistrateRecipeProvider(RegistrateRecipeRunner runner, HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.runner = runner;
        this.outputDelegated = output;
    }

    @Override
    public void buildRecipes() {
        runner.provider = this;
        runner.owner.genData(ProviderType.RECIPE, this);
        runner.provider = null;
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
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void generateForEnabledBlockFamilies(FeatureFlagSet p_251836_) { super.generateForEnabledBlockFamilies(p_251836_); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreSmelting} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void oreSmelting(List<ItemLike> p_250172_, RecipeCategory p_250588_, ItemLike p_251868_, float p_250789_, int p_252144_, String p_251687_) { super.oreSmelting(p_250172_, p_250588_, p_251868_, p_250789_, p_252144_, p_251687_); }

    /** Generated override to expose protected method: {@link RecipeProvider#oreBlasting} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void oreBlasting(List<ItemLike> p_251504_, RecipeCategory p_248846_, ItemLike p_249735_, float p_248783_, int p_250303_, String p_251984_) { super.oreBlasting(p_251504_, p_248846_, p_249735_, p_248783_, p_250303_, p_251984_); }

    /** Generated override to expose protected method: {@link RecipeProvider#netheriteSmithing} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void netheriteSmithing(Item p_250046_, RecipeCategory p_248986_, Item p_250389_) { super.netheriteSmithing(p_250046_, p_248986_, p_250389_); }

    /** Generated override to expose protected method: {@link RecipeProvider#trimSmithing} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void trimSmithing(Item p_285461_, ResourceKey<TrimPattern> p_379766_, ResourceKey<Recipe<?>> p_399566_) { super.trimSmithing(p_285461_, p_379766_, p_399566_); }

    /** Generated override to expose protected method: {@link RecipeProvider#twoByTwoPacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void twoByTwoPacker(RecipeCategory p_250881_, ItemLike p_252184_, ItemLike p_249710_) { super.twoByTwoPacker(p_250881_, p_252184_, p_249710_); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void threeByThreePacker(RecipeCategory p_259247_, ItemLike p_259376_, ItemLike p_259717_, String p_260308_) { super.threeByThreePacker(p_259247_, p_259376_, p_259717_, p_260308_); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void threeByThreePacker(RecipeCategory p_259186_, ItemLike p_259360_, ItemLike p_259263_) { super.threeByThreePacker(p_259186_, p_259360_, p_259263_); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLog} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void planksFromLog(ItemLike p_259052_, TagKey<Item> p_259045_, int p_259471_) { super.planksFromLog(p_259052_, p_259045_, p_259471_); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLogs} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void planksFromLogs(ItemLike p_259193_, TagKey<Item> p_259818_, int p_259807_) { super.planksFromLogs(p_259193_, p_259818_, p_259807_); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodFromLogs} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void woodFromLogs(ItemLike p_126004_, ItemLike p_126005_) { super.woodFromLogs(p_126004_, p_126005_); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodenBoat} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void woodenBoat(ItemLike p_126023_, ItemLike p_126024_) { super.woodenBoat(p_126023_, p_126024_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chestBoat} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void chestBoat(ItemLike p_236373_, ItemLike p_236374_) { super.chestBoat(p_236373_, p_236374_); }

    /** Generated override to expose protected method: {@link RecipeProvider#buttonBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder buttonBuilder(ItemLike p_176659_, Ingredient p_176660_) { return super.buttonBuilder(p_176659_, p_176660_); }

    /** Generated override to expose protected method: {@link RecipeProvider#doorBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder doorBuilder(ItemLike p_176671_, Ingredient p_176672_) { return super.doorBuilder(p_176671_, p_176672_); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder fenceBuilder(ItemLike p_176679_, Ingredient p_176680_) { return super.fenceBuilder(p_176679_, p_176680_); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceGateBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder fenceGateBuilder(ItemLike p_176685_, Ingredient p_176686_) { return super.fenceGateBuilder(p_176685_, p_176686_); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void pressurePlate(ItemLike p_176692_, ItemLike p_176693_) { super.pressurePlate(p_176692_, p_176693_); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlateBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder pressurePlateBuilder(RecipeCategory p_251447_, ItemLike p_251989_, Ingredient p_249211_) { return super.pressurePlateBuilder(p_251447_, p_251989_, p_249211_); }

    /** Generated override to expose protected method: {@link RecipeProvider#slab} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void slab(RecipeCategory p_251848_, ItemLike p_249368_, ItemLike p_252133_) { super.slab(p_251848_, p_249368_, p_252133_); }

    /** Generated override to expose protected method: {@link RecipeProvider#slabBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder slabBuilder(RecipeCategory p_251707_, ItemLike p_251284_, Ingredient p_248824_) { return super.slabBuilder(p_251707_, p_251284_, p_248824_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stairBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder stairBuilder(ItemLike p_176711_, Ingredient p_176712_) { return super.stairBuilder(p_176711_, p_176712_); }

    /** Generated override to expose protected method: {@link RecipeProvider#trapdoorBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder trapdoorBuilder(ItemLike p_176721_, Ingredient p_176722_) { return super.trapdoorBuilder(p_176721_, p_176722_); }

    /** Generated override to expose protected method: {@link RecipeProvider#signBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder signBuilder(ItemLike p_176727_, Ingredient p_176728_) { return super.signBuilder(p_176727_, p_176728_); }

    /** Generated override to expose protected method: {@link RecipeProvider#hangingSign} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void hangingSign(ItemLike p_252355_, ItemLike p_250437_) { super.hangingSign(p_252355_, p_250437_); }

    /** Generated override to expose protected method: {@link RecipeProvider#colorBlockWithDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void colorBlockWithDye(List<Item> p_289675_, List<Item> p_289672_, String p_289641_) { super.colorBlockWithDye(p_289675_, p_289672_, p_289641_); }

    /** Generated override to expose protected method: {@link RecipeProvider#carpet} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void carpet(ItemLike p_176718_, ItemLike p_176719_) { super.carpet(p_176718_, p_176719_); }

    /** Generated override to expose protected method: {@link RecipeProvider#bedFromPlanksAndWool} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void bedFromPlanksAndWool(ItemLike p_126075_, ItemLike p_126076_) { super.bedFromPlanksAndWool(p_126075_, p_126076_); }

    /** Generated override to expose protected method: {@link RecipeProvider#banner} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void banner(ItemLike p_126083_, ItemLike p_126084_) { super.banner(p_126083_, p_126084_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassFromGlassAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void stainedGlassFromGlassAndDye(ItemLike p_126087_, ItemLike p_126088_) { super.stainedGlassFromGlassAndDye(p_126087_, p_126088_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromStainedGlass} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void stainedGlassPaneFromStainedGlass(ItemLike p_126091_, ItemLike p_126092_) { super.stainedGlassPaneFromStainedGlass(p_126091_, p_126092_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromGlassPaneAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void stainedGlassPaneFromGlassPaneAndDye(ItemLike p_126095_, ItemLike p_126096_) { super.stainedGlassPaneFromGlassPaneAndDye(p_126095_, p_126096_); }

    /** Generated override to expose protected method: {@link RecipeProvider#coloredTerracottaFromTerracottaAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void coloredTerracottaFromTerracottaAndDye(ItemLike p_126099_, ItemLike p_126100_) { super.coloredTerracottaFromTerracottaAndDye(p_126099_, p_126100_); }

    /** Generated override to expose protected method: {@link RecipeProvider#concretePowder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void concretePowder(ItemLike p_126103_, ItemLike p_126104_) { super.concretePowder(p_126103_, p_126104_); }

    /** Generated override to expose protected method: {@link RecipeProvider#candle} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void candle(ItemLike p_176544_, ItemLike p_176545_) { super.candle(p_176544_, p_176545_); }

    /** Generated override to expose protected method: {@link RecipeProvider#wall} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void wall(RecipeCategory p_251148_, ItemLike p_250499_, ItemLike p_249970_) { super.wall(p_251148_, p_250499_, p_249970_); }

    /** Generated override to expose protected method: {@link RecipeProvider#wallBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder wallBuilder(RecipeCategory p_249083_, ItemLike p_250754_, Ingredient p_250311_) { return super.wallBuilder(p_249083_, p_250754_, p_250311_); }

    /** Generated override to expose protected method: {@link RecipeProvider#polished} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void polished(RecipeCategory p_248719_, ItemLike p_250032_, ItemLike p_250021_) { super.polished(p_248719_, p_250032_, p_250021_); }

    /** Generated override to expose protected method: {@link RecipeProvider#polishedBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public RecipeBuilder polishedBuilder(RecipeCategory p_249131_, ItemLike p_251242_, Ingredient p_251412_) { return super.polishedBuilder(p_249131_, p_251242_, p_251412_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cut} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void cut(RecipeCategory p_252306_, ItemLike p_249686_, ItemLike p_251100_) { super.cut(p_252306_, p_249686_, p_251100_); }

    /** Generated override to expose protected method: {@link RecipeProvider#cutBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapedRecipeBuilder cutBuilder(RecipeCategory p_250895_, ItemLike p_251147_, Ingredient p_251563_) { return super.cutBuilder(p_250895_, p_251147_, p_251563_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseled} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void chiseled(RecipeCategory p_251604_, ItemLike p_251049_, ItemLike p_252267_) { super.chiseled(p_251604_, p_251049_, p_252267_); }

    /** Generated override to expose protected method: {@link RecipeProvider#mosaicBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void mosaicBuilder(RecipeCategory p_248788_, ItemLike p_251925_, ItemLike p_252242_) { super.mosaicBuilder(p_248788_, p_251925_, p_252242_); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseledBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapedRecipeBuilder chiseledBuilder(RecipeCategory p_251755_, ItemLike p_249782_, Ingredient p_250087_) { return super.chiseledBuilder(p_251755_, p_249782_, p_250087_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void stonecutterResultFromBase(RecipeCategory p_248911_, ItemLike p_251265_, ItemLike p_250033_) { super.stonecutterResultFromBase(p_248911_, p_251265_, p_250033_); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void stonecutterResultFromBase(RecipeCategory p_250609_, ItemLike p_251254_, ItemLike p_249666_, int p_251462_) { super.stonecutterResultFromBase(p_250609_, p_251254_, p_249666_, p_251462_); }

    /** Generated override to expose protected method: {@link RecipeProvider#smeltingResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void smeltingResultFromBase(ItemLike p_176741_, ItemLike p_176742_) { super.smeltingResultFromBase(p_176741_, p_176742_); }

    /** Generated override to expose protected method: {@link RecipeProvider#nineBlockStorageRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void nineBlockStorageRecipes(RecipeCategory p_250083_, ItemLike p_250042_, RecipeCategory p_248977_, ItemLike p_251911_) { super.nineBlockStorageRecipes(p_250083_, p_250042_, p_248977_, p_251911_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void copySmithingTemplate(ItemLike p_350799_, ItemLike p_365321_) { super.copySmithingTemplate(p_350799_, p_365321_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void copySmithingTemplate(ItemLike p_266974_, Ingredient p_360677_) { super.copySmithingTemplate(p_266974_, p_360677_); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void waxRecipes(FeatureFlagSet p_313879_) { super.waxRecipes(p_313879_); }

    /** Generated override to expose protected method: {@link RecipeProvider#grate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void grate(Block p_309021_, Block p_309140_) { super.grate(p_309021_, p_309140_); }

    /** Generated override to expose protected method: {@link RecipeProvider#copperBulb} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void copperBulb(Block p_309026_, Block p_308866_) { super.copperBulb(p_309026_, p_308866_); }

    /** Generated override to expose protected method: {@link RecipeProvider#suspiciousStew} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void suspiciousStew(Item p_360920_, SuspiciousEffectHolder p_361278_) { super.suspiciousStew(p_360920_, p_361278_); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public void generateRecipes(BlockFamily p_176582_, FeatureFlagSet p_313799_) { super.generateRecipes(p_176582_, p_313799_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBaseBlock} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public Block getBaseBlock(BlockFamily p_176524_, BlockFamily.Variant p_176525_) { return super.getBaseBlock(p_176524_, p_176525_); }

    /** Generated override to expose protected method: {@link RecipeProvider#insideOf} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block p_125980_) { return RecipeProvider.insideOf(p_125980_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints p_176521_, ItemLike p_176522_) { return super.has(p_176521_, p_176522_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike p_125978_) { return super.has(p_125978_); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> p_206407_) { return super.has(p_206407_); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... p_299111_) { return RecipeProvider.inventoryTrigger(p_299111_); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... p_126012_) { return RecipeProvider.inventoryTrigger(p_126012_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getHasName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static String getHasName(ItemLike p_176603_) { return RecipeProvider.getHasName(p_176603_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getItemName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static String getItemName(ItemLike p_176633_) { return RecipeProvider.getItemName(p_176633_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSimpleRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static String getSimpleRecipeName(ItemLike p_176645_) { return RecipeProvider.getSimpleRecipeName(p_176645_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getConversionRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static String getConversionRecipeName(ItemLike p_176518_, ItemLike p_176519_) { return RecipeProvider.getConversionRecipeName(p_176518_, p_176519_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSmeltingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static String getSmeltingRecipeName(ItemLike p_176657_) { return RecipeProvider.getSmeltingRecipeName(p_176657_); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBlastingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public static String getBlastingRecipeName(ItemLike p_176669_) { return RecipeProvider.getBlastingRecipeName(p_176669_); }

    /** Generated override to expose protected method: {@link RecipeProvider#tag} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public Ingredient tag(TagKey<Item> p_364630_) { return super.tag(p_364630_); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory p_360632_, ItemLike p_365035_) { return super.shaped(p_360632_, p_365035_); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory p_363994_, ItemLike p_365113_, int p_362095_) { return super.shaped(p_363994_, p_365113_, p_362095_); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory p_364602_, ItemStack p_361999_) { return super.shapeless(p_364602_, p_361999_); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory p_364319_, ItemLike p_364774_) { return super.shapeless(p_364319_, p_364774_); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Wed, 26 Mar 2025 08:32:44 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory p_362256_, ItemLike p_363786_, int p_365368_) { return super.shapeless(p_362256_, p_363786_, p_365368_); }

    // GENERATED END

}
