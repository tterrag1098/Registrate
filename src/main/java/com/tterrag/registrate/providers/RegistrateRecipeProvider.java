package com.tterrag.registrate.providers;

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
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        return safeId(ForgeRegistries.ITEMS.getKey(registryEntry.asItem()));
    }

    public String safeName(ResourceLocation id) {
        return id.getPath().replace('/', '_');
    }

    public String safeName(DataIngredient source) {
        return safeName(source.getId());
    }

    public String safeName(ItemLike registryEntry) {
        return safeName(ForgeRegistries.ITEMS.getKey(registryEntry.asItem()));
    }

    public static final int DEFAULT_SMELT_TIME = 200;
    public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
    public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
    public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;

    private static final String SMELTING_NAME = "smelting";
    @SuppressWarnings("null")
    private static final ImmutableMap<SimpleCookingSerializer<?>, String> COOKING_TYPE_NAMES = ImmutableMap.<SimpleCookingSerializer<?>, String>builder()
            .put((SimpleCookingSerializer<?>) RecipeSerializer.SMELTING_RECIPE, SMELTING_NAME)
            .put((SimpleCookingSerializer<?>) RecipeSerializer.BLASTING_RECIPE, "blasting")
            .put((SimpleCookingSerializer<?>) RecipeSerializer.SMOKING_RECIPE, "smoking")
            .put((SimpleCookingSerializer<?>) RecipeSerializer.CAMPFIRE_COOKING_RECIPE, "campfire")
            .build();

    public <T extends ItemLike> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, SimpleCookingSerializer<?> serializer) {
        cooking(source, category, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer);
    }

    public <T extends ItemLike> void cooking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime, String typeName, SimpleCookingSerializer<?> serializer) {
        SimpleCookingRecipeBuilder.generic(source, category, result.get(), experience, cookingTime, serializer)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        smelting(source, category, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, (SimpleCookingSerializer<?>) RecipeSerializer.SMELTING_RECIPE);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        blasting(source, category, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, (SimpleCookingSerializer<?>) RecipeSerializer.BLASTING_RECIPE);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        smoking(source, category, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, (SimpleCookingSerializer<?>) RecipeSerializer.SMOKING_RECIPE);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {
        campfire(source, category, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, (SimpleCookingSerializer<?>) RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
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
    // GENERATED START

    @Override
    public CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) { return super.saveAdvancement(output, finishedRecipe, advancementJson); }

    @Override
    public CompletableFuture<?> buildAdvancement(CachedOutput p_253674_, ResourceLocation p_254102_, Advancement.Builder p_253712_) { return super.buildAdvancement(p_253674_, p_254102_, p_253712_); }

    @Override
    public void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> p_249188_, FeatureFlagSet p_251836_) { super.generateForEnabledBlockFamilies(p_249188_, p_251836_); }

    public static void oreSmelting(Consumer<FinishedRecipe> p_250654_, List<ItemLike> p_250172_, RecipeCategory p_250588_, ItemLike p_251868_, float p_250789_, int p_252144_, String p_251687_) { RecipeProvider.oreSmelting(p_250654_, p_250172_, p_250588_, p_251868_, p_250789_, p_252144_, p_251687_); }

    public static void oreBlasting(Consumer<FinishedRecipe> p_248775_, List<ItemLike> p_251504_, RecipeCategory p_248846_, ItemLike p_249735_, float p_248783_, int p_250303_, String p_251984_) { RecipeProvider.oreBlasting(p_248775_, p_251504_, p_248846_, p_249735_, p_248783_, p_250303_, p_251984_); }

    public static void oreCooking(Consumer<FinishedRecipe> p_250791_, RecipeSerializer<? extends AbstractCookingRecipe> p_251817_, List<ItemLike> p_249619_, RecipeCategory p_251154_, ItemLike p_250066_, float p_251871_, int p_251316_, String p_251450_, String p_249236_) { RecipeProvider.oreCooking(p_250791_, p_251817_, p_249619_, p_251154_, p_250066_, p_251871_, p_251316_, p_251450_, p_249236_); }

    public static void netheriteSmithing(Consumer<FinishedRecipe> p_251614_, Item p_250046_, RecipeCategory p_248986_, Item p_250389_) { RecipeProvider.netheriteSmithing(p_251614_, p_250046_, p_248986_, p_250389_); }

    public static void twoByTwoPacker(Consumer<FinishedRecipe> p_248860_, RecipeCategory p_250881_, ItemLike p_252184_, ItemLike p_249710_) { RecipeProvider.twoByTwoPacker(p_248860_, p_250881_, p_252184_, p_249710_); }

    public static void threeByThreePacker(Consumer<FinishedRecipe> p_259036_, RecipeCategory p_259247_, ItemLike p_259376_, ItemLike p_259717_, String p_260308_) { RecipeProvider.threeByThreePacker(p_259036_, p_259247_, p_259376_, p_259717_, p_260308_); }

    public static void threeByThreePacker(Consumer<FinishedRecipe> p_260012_, RecipeCategory p_259186_, ItemLike p_259360_, ItemLike p_259263_) { RecipeProvider.threeByThreePacker(p_260012_, p_259186_, p_259360_, p_259263_); }

    public static void planksFromLog(Consumer<FinishedRecipe> p_259712_, ItemLike p_259052_, TagKey<Item> p_259045_, int p_259471_) { RecipeProvider.planksFromLog(p_259712_, p_259052_, p_259045_, p_259471_); }

    public static void planksFromLogs(Consumer<FinishedRecipe> p_259910_, ItemLike p_259193_, TagKey<Item> p_259818_, int p_259807_) { RecipeProvider.planksFromLogs(p_259910_, p_259193_, p_259818_, p_259807_); }

    public static void woodFromLogs(Consumer<FinishedRecipe> p_126003_, ItemLike p_126004_, ItemLike p_126005_) { RecipeProvider.woodFromLogs(p_126003_, p_126004_, p_126005_); }

    public static void woodenBoat(Consumer<FinishedRecipe> p_126022_, ItemLike p_126023_, ItemLike p_126024_) { RecipeProvider.woodenBoat(p_126022_, p_126023_, p_126024_); }

    public static void chestBoat(Consumer<FinishedRecipe> p_236372_, ItemLike p_236373_, ItemLike p_236374_) { RecipeProvider.chestBoat(p_236372_, p_236373_, p_236374_); }

    public static RecipeBuilder buttonBuilder(ItemLike p_176659_, Ingredient p_176660_) { return RecipeProvider.buttonBuilder(p_176659_, p_176660_); }

    public static RecipeBuilder doorBuilder(ItemLike p_176671_, Ingredient p_176672_) { return RecipeProvider.doorBuilder(p_176671_, p_176672_); }

    public static RecipeBuilder fenceBuilder(ItemLike p_176679_, Ingredient p_176680_) { return RecipeProvider.fenceBuilder(p_176679_, p_176680_); }

    public static RecipeBuilder fenceGateBuilder(ItemLike p_176685_, Ingredient p_176686_) { return RecipeProvider.fenceGateBuilder(p_176685_, p_176686_); }

    public static void pressurePlate(Consumer<FinishedRecipe> p_176691_, ItemLike p_176692_, ItemLike p_176693_) { RecipeProvider.pressurePlate(p_176691_, p_176692_, p_176693_); }

    public static RecipeBuilder pressurePlateBuilder(RecipeCategory p_251447_, ItemLike p_251989_, Ingredient p_249211_) { return RecipeProvider.pressurePlateBuilder(p_251447_, p_251989_, p_249211_); }

    public static void slab(Consumer<FinishedRecipe> p_248880_, RecipeCategory p_251848_, ItemLike p_249368_, ItemLike p_252133_) { RecipeProvider.slab(p_248880_, p_251848_, p_249368_, p_252133_); }

    public static RecipeBuilder slabBuilder(RecipeCategory p_251707_, ItemLike p_251284_, Ingredient p_248824_) { return RecipeProvider.slabBuilder(p_251707_, p_251284_, p_248824_); }

    public static RecipeBuilder stairBuilder(ItemLike p_176711_, Ingredient p_176712_) { return RecipeProvider.stairBuilder(p_176711_, p_176712_); }

    public static RecipeBuilder trapdoorBuilder(ItemLike p_176721_, Ingredient p_176722_) { return RecipeProvider.trapdoorBuilder(p_176721_, p_176722_); }

    public static RecipeBuilder signBuilder(ItemLike p_176727_, Ingredient p_176728_) { return RecipeProvider.signBuilder(p_176727_, p_176728_); }

    public static void hangingSign(Consumer<FinishedRecipe> p_250663_, ItemLike p_252355_, ItemLike p_250437_) { RecipeProvider.hangingSign(p_250663_, p_252355_, p_250437_); }

    public static void coloredWoolFromWhiteWoolAndDye(Consumer<FinishedRecipe> p_126062_, ItemLike p_126063_, ItemLike p_126064_) { RecipeProvider.coloredWoolFromWhiteWoolAndDye(p_126062_, p_126063_, p_126064_); }

    public static void carpet(Consumer<FinishedRecipe> p_176717_, ItemLike p_176718_, ItemLike p_176719_) { RecipeProvider.carpet(p_176717_, p_176718_, p_176719_); }

    public static void coloredCarpetFromWhiteCarpetAndDye(Consumer<FinishedRecipe> p_126070_, ItemLike p_126071_, ItemLike p_126072_) { RecipeProvider.coloredCarpetFromWhiteCarpetAndDye(p_126070_, p_126071_, p_126072_); }

    public static void bedFromPlanksAndWool(Consumer<FinishedRecipe> p_126074_, ItemLike p_126075_, ItemLike p_126076_) { RecipeProvider.bedFromPlanksAndWool(p_126074_, p_126075_, p_126076_); }

    public static void bedFromWhiteBedAndDye(Consumer<FinishedRecipe> p_126078_, ItemLike p_126079_, ItemLike p_126080_) { RecipeProvider.bedFromWhiteBedAndDye(p_126078_, p_126079_, p_126080_); }

    public static void banner(Consumer<FinishedRecipe> p_126082_, ItemLike p_126083_, ItemLike p_126084_) { RecipeProvider.banner(p_126082_, p_126083_, p_126084_); }

    public static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> p_126086_, ItemLike p_126087_, ItemLike p_126088_) { RecipeProvider.stainedGlassFromGlassAndDye(p_126086_, p_126087_, p_126088_); }

    public static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> p_126090_, ItemLike p_126091_, ItemLike p_126092_) { RecipeProvider.stainedGlassPaneFromStainedGlass(p_126090_, p_126091_, p_126092_); }

    public static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> p_126094_, ItemLike p_126095_, ItemLike p_126096_) { RecipeProvider.stainedGlassPaneFromGlassPaneAndDye(p_126094_, p_126095_, p_126096_); }

    public static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> p_126098_, ItemLike p_126099_, ItemLike p_126100_) { RecipeProvider.coloredTerracottaFromTerracottaAndDye(p_126098_, p_126099_, p_126100_); }

    public static void concretePowder(Consumer<FinishedRecipe> p_126102_, ItemLike p_126103_, ItemLike p_126104_) { RecipeProvider.concretePowder(p_126102_, p_126103_, p_126104_); }

    public static void candle(Consumer<FinishedRecipe> p_176543_, ItemLike p_176544_, ItemLike p_176545_) { RecipeProvider.candle(p_176543_, p_176544_, p_176545_); }

    public static void wall(Consumer<FinishedRecipe> p_251034_, RecipeCategory p_251148_, ItemLike p_250499_, ItemLike p_249970_) { RecipeProvider.wall(p_251034_, p_251148_, p_250499_, p_249970_); }

    public static RecipeBuilder wallBuilder(RecipeCategory p_249083_, ItemLike p_250754_, Ingredient p_250311_) { return RecipeProvider.wallBuilder(p_249083_, p_250754_, p_250311_); }

    public static void polished(Consumer<FinishedRecipe> p_251348_, RecipeCategory p_248719_, ItemLike p_250032_, ItemLike p_250021_) { RecipeProvider.polished(p_251348_, p_248719_, p_250032_, p_250021_); }

    public static RecipeBuilder polishedBuilder(RecipeCategory p_249131_, ItemLike p_251242_, Ingredient p_251412_) { return RecipeProvider.polishedBuilder(p_249131_, p_251242_, p_251412_); }

    public static void cut(Consumer<FinishedRecipe> p_248712_, RecipeCategory p_252306_, ItemLike p_249686_, ItemLike p_251100_) { RecipeProvider.cut(p_248712_, p_252306_, p_249686_, p_251100_); }

    public static ShapedRecipeBuilder cutBuilder(RecipeCategory p_250895_, ItemLike p_251147_, Ingredient p_251563_) { return RecipeProvider.cutBuilder(p_250895_, p_251147_, p_251563_); }

    public static void chiseled(Consumer<FinishedRecipe> p_250120_, RecipeCategory p_251604_, ItemLike p_251049_, ItemLike p_252267_) { RecipeProvider.chiseled(p_250120_, p_251604_, p_251049_, p_252267_); }

    public static void mosaicBuilder(Consumer<FinishedRecipe> p_249200_, RecipeCategory p_248788_, ItemLike p_251925_, ItemLike p_252242_) { RecipeProvider.mosaicBuilder(p_249200_, p_248788_, p_251925_, p_252242_); }

    public static ShapedRecipeBuilder chiseledBuilder(RecipeCategory p_251755_, ItemLike p_249782_, Ingredient p_250087_) { return RecipeProvider.chiseledBuilder(p_251755_, p_249782_, p_250087_); }

    public static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_251589_, RecipeCategory p_248911_, ItemLike p_251265_, ItemLike p_250033_) { RecipeProvider.stonecutterResultFromBase(p_251589_, p_248911_, p_251265_, p_250033_); }

    public static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_249145_, RecipeCategory p_250609_, ItemLike p_251254_, ItemLike p_249666_, int p_251462_) { RecipeProvider.stonecutterResultFromBase(p_249145_, p_250609_, p_251254_, p_249666_, p_251462_); }

    public static void smeltingResultFromBase(Consumer<FinishedRecipe> p_176740_, ItemLike p_176741_, ItemLike p_176742_) { RecipeProvider.smeltingResultFromBase(p_176740_, p_176741_, p_176742_); }

    public static void nineBlockStorageRecipes(Consumer<FinishedRecipe> p_249580_, RecipeCategory p_251203_, ItemLike p_251689_, RecipeCategory p_251376_, ItemLike p_248771_) { RecipeProvider.nineBlockStorageRecipes(p_249580_, p_251203_, p_251689_, p_251376_, p_248771_); }

    public static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> p_250488_, RecipeCategory p_250885_, ItemLike p_251651_, RecipeCategory p_250874_, ItemLike p_248576_, String p_250171_, String p_249386_) { RecipeProvider.nineBlockStorageRecipesWithCustomPacking(p_250488_, p_250885_, p_251651_, p_250874_, p_248576_, p_250171_, p_249386_); }

    public static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> p_250320_, RecipeCategory p_248979_, ItemLike p_249101_, RecipeCategory p_252036_, ItemLike p_250886_, String p_248768_, String p_250847_) { RecipeProvider.nineBlockStorageRecipesRecipesWithCustomUnpacking(p_250320_, p_248979_, p_249101_, p_252036_, p_250886_, p_248768_, p_250847_); }

    public static void cookRecipes(Consumer<FinishedRecipe> p_126007_, String p_126008_, RecipeSerializer<? extends AbstractCookingRecipe> p_250529_, int p_126010_) { RecipeProvider.cookRecipes(p_126007_, p_126008_, p_250529_, p_126010_); }

    public static void simpleCookingRecipe(Consumer<FinishedRecipe> p_249398_, String p_249709_, RecipeSerializer<? extends AbstractCookingRecipe> p_251876_, int p_249258_, ItemLike p_250669_, ItemLike p_250224_, float p_252138_) { RecipeProvider.simpleCookingRecipe(p_249398_, p_249709_, p_251876_, p_249258_, p_250669_, p_250224_, p_252138_); }

    public static void waxRecipes(Consumer<FinishedRecipe> p_176611_) { RecipeProvider.waxRecipes(p_176611_); }

    public static void generateRecipes(Consumer<FinishedRecipe> p_176581_, BlockFamily p_176582_) { RecipeProvider.generateRecipes(p_176581_, p_176582_); }

    public static Block getBaseBlock(BlockFamily p_176524_, BlockFamily.Variant p_176525_) { return RecipeProvider.getBaseBlock(p_176524_, p_176525_); }

    public static EnterBlockTrigger.TriggerInstance insideOf(Block p_125980_) { return RecipeProvider.insideOf(p_125980_); }

    public static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints p_176521_, ItemLike p_176522_) { return RecipeProvider.has(p_176521_, p_176522_); }

    public static InventoryChangeTrigger.TriggerInstance has(ItemLike p_125978_) { return RecipeProvider.has(p_125978_); }

    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> p_206407_) { return RecipeProvider.has(p_206407_); }

    public static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... p_126012_) { return RecipeProvider.inventoryTrigger(p_126012_); }

    public static String getHasName(ItemLike p_176603_) { return RecipeProvider.getHasName(p_176603_); }

    public static String getItemName(ItemLike p_176633_) { return RecipeProvider.getItemName(p_176633_); }

    public static String getSimpleRecipeName(ItemLike p_176645_) { return RecipeProvider.getSimpleRecipeName(p_176645_); }

    public static String getConversionRecipeName(ItemLike p_176518_, ItemLike p_176519_) { return RecipeProvider.getConversionRecipeName(p_176518_, p_176519_); }

    public static String getSmeltingRecipeName(ItemLike p_176657_) { return RecipeProvider.getSmeltingRecipeName(p_176657_); }

    public static String getBlastingRecipeName(ItemLike p_176669_) { return RecipeProvider.getBlastingRecipeName(p_176669_); }

    // GENERATED END
}
