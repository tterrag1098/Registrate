package com.tterrag.registrate.providers;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RegistrateRecipeProvider extends RecipeProvider {

    public static class Runner extends RecipeProvider.Runner implements RegistrateProvider {

        private final AbstractRegistrate<?> owner;

        @org.jetbrains.annotations.Nullable
        private RegistrateRecipeProvider provider;

        protected Runner(AbstractRegistrate<?> owner, PackOutput p_365369_, CompletableFuture<HolderLookup.Provider> p_361563_) {
            super(p_365369_, p_361563_);
            this.owner = owner;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider p_362946_, RecipeOutput p_365274_) {
            return new RegistrateRecipeProvider(this, p_362946_, p_365274_);
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public LogicalSide getSide() {
            return LogicalSide.SERVER;
        }

        public RegistrateRecipeProvider getRecipeProvider() {
            if (provider == null) throw new IllegalStateException("Recipe Provider is not available now");
            return provider;
        }

    }

    private final Runner runner;

    public RegistrateRecipeProvider(Runner runner, HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.runner = runner;
    }

    @Override
    public void buildRecipes() {
        runner.provider = this;
        runner.owner.genData(ProviderType.RECIPE, runner);
        runner.provider = null;
    }

    public <T> Holder<T> resolve(ResourceKey<T> key) {
        return registries.lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    public ResourceKey<Recipe<?>> safeId(ResourceLocation id) {
        return ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(runner.owner.getModid(), safeName(id)));
    }

    public ResourceKey<Recipe<?>> safeId(DataIngredient source) {
        return safeId(source.getId());
    }

    public ResourceKey<Recipe<?>> safeId(ItemLike registryEntry) {
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
                .save(this.output, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
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
                .save(this.output, safeId(result.get()) + "_from_" + safeName(source) + "_stonecutting");
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
                .save(this.output, safeId(output.get()));
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
                .save(this.output, safeId(source) + "_from_" + safeName(output.get()));
    }

    public <T extends ItemLike> void storage(NonNullSupplier<? extends T> source, RecipeCategory category, NonNullSupplier<? extends T> output) {
        storage(DataIngredient.items(source), category, source, DataIngredient.items(output), output);
    }

    public <T extends ItemLike> void storage(DataIngredient sourceIngredient, RecipeCategory category, NonNullSupplier<? extends T> source, DataIngredient outputIngredient, NonNullSupplier<? extends T> output) {
        square(sourceIngredient, category, output, false);
        singleItemUnfinished(outputIngredient, category, source, 1, 9)
                .save(this.output, safeId(sourceIngredient) + "_from_" + safeName(output.get()));
    }

    @CheckReturnValue
    public <T extends ItemLike> ShapelessRecipeBuilder singleItemUnfinished(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {
        return shapeless(category, result.get(), amount)
            .requires(source.toVanilla(), required)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this));
    }

    public <T extends ItemLike> void singleItem(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {
        singleItemUnfinished(source, category, result, required, amount).save(this.output, safeId(result.get()));
    }

    public <T extends ItemLike> void planks(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        singleItemUnfinished(source, category, result, 1, 4)
            .group("planks")
                .save(this.output, safeId(result.get()));
    }

    public <T extends ItemLike> void stairs(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        shaped(category, result.get(), 4)
            .pattern("X  ").pattern("XX ").pattern("XXX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this.output, safeId(result.get()));
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
                .save(this.output, safeId(result.get()));
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
                .save(this.output, safeId(result.get()));
    }

    public <T extends ItemLike> void fenceGate(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get())
            .pattern("#W#").pattern("#W#")
            .define('W', source.toVanilla())
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this.output, safeId(result.get()));
    }

    public <T extends ItemLike> void wall(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        shaped(category, result.get(), 6)
            .pattern("XXX").pattern("XXX")
            .define('X', source.toVanilla())
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this.output, safeId(result.get()));
        stonecutting(source, category, result);
    }

    public <T extends ItemLike> void door(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 3)
            .pattern("XX").pattern("XX").pattern("XX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this.output, safeId(result.get()));
    }

    public <T extends ItemLike> void trapDoor(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 2)
            .pattern("XXX").pattern("XXX")
            .define('X', source.toVanilla())
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this.output, safeId(result.get()));
    }

    // generated overrides to expose protected methods

    @Override
    public void generateForEnabledBlockFamilies(FeatureFlagSet p_251836_) {
        super.generateForEnabledBlockFamilies(p_251836_);
    }

    @Override
    public void oneToOneConversionRecipe(ItemLike p_176558_, ItemLike p_176559_, @org.jetbrains.annotations.Nullable String p_176560_) {
        super.oneToOneConversionRecipe(p_176558_, p_176559_, p_176560_);
    }

    @Override
    public void oneToOneConversionRecipe(ItemLike p_176553_, ItemLike p_176554_, @org.jetbrains.annotations.Nullable String p_176555_, int p_362382_) {
        super.oneToOneConversionRecipe(p_176553_, p_176554_, p_176555_, p_362382_);
    }

    @Override
    public void oreSmelting(List<ItemLike> p_250172_, RecipeCategory p_250588_, ItemLike p_251868_, float p_250789_, int p_252144_, String p_251687_) {
        super.oreSmelting(p_250172_, p_250588_, p_251868_, p_250789_, p_252144_, p_251687_);
    }

    @Override
    public void oreBlasting(List<ItemLike> p_251504_, RecipeCategory p_248846_, ItemLike p_249735_, float p_248783_, int p_250303_, String p_251984_) {
        super.oreBlasting(p_251504_, p_248846_, p_249735_, p_248783_, p_250303_, p_251984_);
    }

    @Override
    public <T extends AbstractCookingRecipe> void oreCooking(RecipeSerializer<T> p_251817_, AbstractCookingRecipe.Factory<T> p_312707_, List<ItemLike> p_249619_, RecipeCategory p_251154_, ItemLike p_250066_, float p_251871_, int p_251316_, String p_251450_, String p_249236_) {
        super.oreCooking(p_251817_, p_312707_, p_249619_, p_251154_, p_250066_, p_251871_, p_251316_, p_251450_, p_249236_);
    }

    @Override
    public void netheriteSmithing(Item p_250046_, RecipeCategory p_248986_, Item p_250389_) {
        super.netheriteSmithing(p_250046_, p_248986_, p_250389_);
    }

    @Override
    public void trimSmithing(Item p_285461_, ResourceKey<Recipe<?>> p_379766_) {
        super.trimSmithing(p_285461_, p_379766_);
    }

    @Override
    public void twoByTwoPacker(RecipeCategory p_250881_, ItemLike p_252184_, ItemLike p_249710_) {
        super.twoByTwoPacker(p_250881_, p_252184_, p_249710_);
    }

    @Override
    public void threeByThreePacker(RecipeCategory p_259247_, ItemLike p_259376_, ItemLike p_259717_, String p_260308_) {
        super.threeByThreePacker(p_259247_, p_259376_, p_259717_, p_260308_);
    }

    @Override
    public void threeByThreePacker(RecipeCategory p_259186_, ItemLike p_259360_, ItemLike p_259263_) {
        super.threeByThreePacker(p_259186_, p_259360_, p_259263_);
    }

    @Override
    public void planksFromLog(ItemLike p_259052_, TagKey<Item> p_259045_, int p_259471_) {
        super.planksFromLog(p_259052_, p_259045_, p_259471_);
    }

    @Override
    public void planksFromLogs(ItemLike p_259193_, TagKey<Item> p_259818_, int p_259807_) {
        super.planksFromLogs(p_259193_, p_259818_, p_259807_);
    }

    @Override
    public void woodFromLogs(ItemLike p_126004_, ItemLike p_126005_) {
        super.woodFromLogs(p_126004_, p_126005_);
    }

    @Override
    public void woodenBoat(ItemLike p_126023_, ItemLike p_126024_) {
        super.woodenBoat(p_126023_, p_126024_);
    }

    @Override
    public void chestBoat(ItemLike p_236373_, ItemLike p_236374_) {
        super.chestBoat(p_236373_, p_236374_);
    }

    @Override
    public RecipeBuilder buttonBuilder(ItemLike p_176659_, Ingredient p_176660_) {
        return super.buttonBuilder(p_176659_, p_176660_);
    }

    @Override
    public RecipeBuilder doorBuilder(ItemLike p_176671_, Ingredient p_176672_) {
        return super.doorBuilder(p_176671_, p_176672_);
    }

    @Override
    public RecipeBuilder fenceBuilder(ItemLike p_176679_, Ingredient p_176680_) {
        return super.fenceBuilder(p_176679_, p_176680_);
    }

    @Override
    public RecipeBuilder fenceGateBuilder(ItemLike p_176685_, Ingredient p_176686_) {
        return super.fenceGateBuilder(p_176685_, p_176686_);
    }

    @Override
    public void pressurePlate(ItemLike p_176692_, ItemLike p_176693_) {
        super.pressurePlate(p_176692_, p_176693_);
    }

    @Override
    public RecipeBuilder pressurePlateBuilder(RecipeCategory p_251447_, ItemLike p_251989_, Ingredient p_249211_) {
        return super.pressurePlateBuilder(p_251447_, p_251989_, p_249211_);
    }

    @Override
    public void slab(RecipeCategory p_251848_, ItemLike p_249368_, ItemLike p_252133_) {
        super.slab(p_251848_, p_249368_, p_252133_);
    }

    @Override
    public RecipeBuilder slabBuilder(RecipeCategory p_251707_, ItemLike p_251284_, Ingredient p_248824_) {
        return super.slabBuilder(p_251707_, p_251284_, p_248824_);
    }

    @Override
    public RecipeBuilder stairBuilder(ItemLike p_176711_, Ingredient p_176712_) {
        return super.stairBuilder(p_176711_, p_176712_);
    }

    @Override
    public RecipeBuilder trapdoorBuilder(ItemLike p_176721_, Ingredient p_176722_) {
        return super.trapdoorBuilder(p_176721_, p_176722_);
    }

    @Override
    public RecipeBuilder signBuilder(ItemLike p_176727_, Ingredient p_176728_) {
        return super.signBuilder(p_176727_, p_176728_);
    }

    @Override
    public void hangingSign(ItemLike p_252355_, ItemLike p_250437_) {
        super.hangingSign(p_252355_, p_250437_);
    }

    @Override
    public void colorBlockWithDye(List<Item> p_289675_, List<Item> p_289672_, String p_289641_) {
        super.colorBlockWithDye(p_289675_, p_289672_, p_289641_);
    }

    @Override
    public void colorWithDye(List<Item> p_361753_, List<Item> p_362611_, @org.jetbrains.annotations.Nullable Item p_361184_, String p_362682_, RecipeCategory p_365350_) {
        super.colorWithDye(p_361753_, p_362611_, p_361184_, p_362682_, p_365350_);
    }

    @Override
    public void carpet(ItemLike p_176718_, ItemLike p_176719_) {
        super.carpet(p_176718_, p_176719_);
    }

    @Override
    public void bedFromPlanksAndWool(ItemLike p_126075_, ItemLike p_126076_) {
        super.bedFromPlanksAndWool(p_126075_, p_126076_);
    }

    @Override
    public void banner(ItemLike p_126083_, ItemLike p_126084_) {
        super.banner(p_126083_, p_126084_);
    }

    @Override
    public void stainedGlassFromGlassAndDye(ItemLike p_126087_, ItemLike p_126088_) {
        super.stainedGlassFromGlassAndDye(p_126087_, p_126088_);
    }

    @Override
    public void stainedGlassPaneFromStainedGlass(ItemLike p_126091_, ItemLike p_126092_) {
        super.stainedGlassPaneFromStainedGlass(p_126091_, p_126092_);
    }

    @Override
    public void stainedGlassPaneFromGlassPaneAndDye(ItemLike p_126095_, ItemLike p_126096_) {
        super.stainedGlassPaneFromGlassPaneAndDye(p_126095_, p_126096_);
    }

    @Override
    public void coloredTerracottaFromTerracottaAndDye(ItemLike p_126099_, ItemLike p_126100_) {
        super.coloredTerracottaFromTerracottaAndDye(p_126099_, p_126100_);
    }

    @Override
    public void concretePowder(ItemLike p_126103_, ItemLike p_126104_) {
        super.concretePowder(p_126103_, p_126104_);
    }

    @Override
    public void candle(ItemLike p_176544_, ItemLike p_176545_) {
        super.candle(p_176544_, p_176545_);
    }

    @Override
    public void wall(RecipeCategory p_251148_, ItemLike p_250499_, ItemLike p_249970_) {
        super.wall(p_251148_, p_250499_, p_249970_);
    }

    @Override
    public RecipeBuilder wallBuilder(RecipeCategory p_249083_, ItemLike p_250754_, Ingredient p_250311_) {
        return super.wallBuilder(p_249083_, p_250754_, p_250311_);
    }

    @Override
    public void polished(RecipeCategory p_248719_, ItemLike p_250032_, ItemLike p_250021_) {
        super.polished(p_248719_, p_250032_, p_250021_);
    }

    @Override
    public RecipeBuilder polishedBuilder(RecipeCategory p_249131_, ItemLike p_251242_, Ingredient p_251412_) {
        return super.polishedBuilder(p_249131_, p_251242_, p_251412_);
    }

    @Override
    public void cut(RecipeCategory p_252306_, ItemLike p_249686_, ItemLike p_251100_) {
        super.cut(p_252306_, p_249686_, p_251100_);
    }

    @Override
    public ShapedRecipeBuilder cutBuilder(RecipeCategory p_250895_, ItemLike p_251147_, Ingredient p_251563_) {
        return super.cutBuilder(p_250895_, p_251147_, p_251563_);
    }

    @Override
    public void chiseled(RecipeCategory p_251604_, ItemLike p_251049_, ItemLike p_252267_) {
        super.chiseled(p_251604_, p_251049_, p_252267_);
    }

    @Override
    public void mosaicBuilder(RecipeCategory p_248788_, ItemLike p_251925_, ItemLike p_252242_) {
        super.mosaicBuilder(p_248788_, p_251925_, p_252242_);
    }

    @Override
    public ShapedRecipeBuilder chiseledBuilder(RecipeCategory p_251755_, ItemLike p_249782_, Ingredient p_250087_) {
        return super.chiseledBuilder(p_251755_, p_249782_, p_250087_);
    }

    @Override
    public void stonecutterResultFromBase(RecipeCategory p_248911_, ItemLike p_251265_, ItemLike p_250033_) {
        super.stonecutterResultFromBase(p_248911_, p_251265_, p_250033_);
    }

    @Override
    public void stonecutterResultFromBase(RecipeCategory p_250609_, ItemLike p_251254_, ItemLike p_249666_, int p_251462_) {
        super.stonecutterResultFromBase(p_250609_, p_251254_, p_249666_, p_251462_);
    }

    @Override
    public void smeltingResultFromBase(ItemLike p_176741_, ItemLike p_176742_) {
        super.smeltingResultFromBase(p_176741_, p_176742_);
    }

    @Override
    public void nineBlockStorageRecipes(RecipeCategory p_250083_, ItemLike p_250042_, RecipeCategory p_248977_, ItemLike p_251911_) {
        super.nineBlockStorageRecipes(p_250083_, p_250042_, p_248977_, p_251911_);
    }

    @Override
    public void nineBlockStorageRecipesWithCustomPacking(RecipeCategory p_250885_, ItemLike p_251651_, RecipeCategory p_250874_, ItemLike p_248576_, String p_250171_, String p_249386_) {
        super.nineBlockStorageRecipesWithCustomPacking(p_250885_, p_251651_, p_250874_, p_248576_, p_250171_, p_249386_);
    }

    @Override
    public void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory p_248979_, ItemLike p_249101_, RecipeCategory p_252036_, ItemLike p_250886_, String p_248768_, String p_250847_) {
        super.nineBlockStorageRecipesRecipesWithCustomUnpacking(p_248979_, p_249101_, p_252036_, p_250886_, p_248768_, p_250847_);
    }

    @Override
    public void nineBlockStorageRecipes(RecipeCategory p_251203_, ItemLike p_251689_, RecipeCategory p_251376_, ItemLike p_248771_, String p_364391_, @org.jetbrains.annotations.Nullable String p_361531_, String p_363105_, @org.jetbrains.annotations.Nullable String p_365446_) {
        super.nineBlockStorageRecipes(p_251203_, p_251689_, p_251376_, p_248771_, p_364391_, p_361531_, p_363105_, p_365446_);
    }

    @Override
    public void copySmithingTemplate(ItemLike p_350799_, ItemLike p_365321_) {
        super.copySmithingTemplate(p_350799_, p_365321_);
    }

    @Override
    public void copySmithingTemplate(ItemLike p_266974_, Ingredient p_360677_) {
        super.copySmithingTemplate(p_266974_, p_360677_);
    }

    @Override
    public <T extends AbstractCookingRecipe> void cookRecipes(String p_126008_, RecipeSerializer<T> p_250529_, AbstractCookingRecipe.Factory<T> p_312449_, int p_126010_) {
        super.cookRecipes(p_126008_, p_250529_, p_312449_, p_126010_);
    }

    @Override
    public <T extends AbstractCookingRecipe> void simpleCookingRecipe(String p_249709_, RecipeSerializer<T> p_251876_, AbstractCookingRecipe.Factory<T> p_312056_, int p_249258_, ItemLike p_250669_, ItemLike p_250224_, float p_252138_) {
        super.simpleCookingRecipe(p_249709_, p_251876_, p_312056_, p_249258_, p_250669_, p_250224_, p_252138_);
    }

    @Override
    public void waxRecipes(FeatureFlagSet p_313879_) {
        super.waxRecipes(p_313879_);
    }

    @Override
    public void grate(Block p_309021_, Block p_309140_) {
        super.grate(p_309021_, p_309140_);
    }

    @Override
    public void copperBulb(Block p_309026_, Block p_308866_) {
        super.copperBulb(p_309026_, p_308866_);
    }

    @Override
    public void suspiciousStew(Item p_360920_, SuspiciousEffectHolder p_361278_) {
        super.suspiciousStew(p_360920_, p_361278_);
    }

    @Override
    public void generateRecipes(BlockFamily p_176582_, FeatureFlagSet p_313799_) {
        super.generateRecipes(p_176582_, p_313799_);
    }

    @Override
    public Block getBaseBlock(BlockFamily p_176524_, BlockFamily.Variant p_176525_) {
        return super.getBaseBlock(p_176524_, p_176525_);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints p_176521_, ItemLike p_176522_) {
        return super.has(p_176521_, p_176522_);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike p_125978_) {
        return super.has(p_125978_);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> p_206407_) {
        return super.has(p_206407_);
    }

    @Override
    public Ingredient tag(TagKey<Item> p_364630_) {
        return super.tag(p_364630_);
    }

    @Override
    public ShapedRecipeBuilder shaped(RecipeCategory p_360632_, ItemLike p_365035_) {
        return super.shaped(p_360632_, p_365035_);
    }

    @Override
    public ShapedRecipeBuilder shaped(RecipeCategory p_363994_, ItemLike p_365113_, int p_362095_) {
        return super.shaped(p_363994_, p_365113_, p_362095_);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory p_364602_, ItemStack p_361999_) {
        return super.shapeless(p_364602_, p_361999_);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory p_364319_, ItemLike p_364774_) {
        return super.shapeless(p_364319_, p_364774_);
    }

    @Override
    public ShapelessRecipeBuilder shapeless(RecipeCategory p_362256_, ItemLike p_363786_, int p_365368_) {
        return super.shapeless(p_362256_, p_363786_, p_365368_);
    }
    
}
