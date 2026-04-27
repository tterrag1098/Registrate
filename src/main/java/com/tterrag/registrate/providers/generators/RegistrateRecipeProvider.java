package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.experimental.Delegate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.CheckReturnValue;
import javax.annotation.Generated;
import org.jspecify.annotations.Nullable;
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

    public Identifier safeId(Identifier id) {
        return Identifier.fromNamespaceAndPath(runner.owner.getModid(), safeName(id));
    }

    public Identifier safeId(DataIngredient source) {
        return safeId(source.getId());
    }

    public Identifier safeId(ItemLike registryEntry) {
        return safeId(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public ResourceKey<Recipe<?>> safeKey(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(runner.owner.getModid(), safeName(id)));
    }

    public ResourceKey<Recipe<?>> safeKey(DataIngredient source) {
        return safeKey(source.getId());
    }

    public ResourceKey<Recipe<?>> safeKey(ItemLike registryEntry) {
        return safeKey(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public String safeName(Identifier id) {
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

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(DataIngredient source, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience, int cookingTime, String typeName, AbstractCookingRecipe.Factory<S> factory) {
        SimpleCookingRecipeBuilder.generic(source.toVanilla(), craftingCategory, cookingCategory, result.get(), experience, cookingTime, factory)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience) {
        smelting(source, category, cookingCategory, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, cookingCategory, result, experience, cookingTime, "smelting", SmeltingRecipe::new);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience) {
        blasting(source, category, cookingCategory, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, cookingCategory, result, experience, cookingTime, "blasting", BlastingRecipe::new);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience) {
        smoking(source, category, cookingCategory, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, cookingCategory, result, experience, cookingTime, "smoking", SmokingRecipe::new);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience) {
        campfire(source, category, cookingCategory, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, cookingCategory, result, experience, cookingTime, "campfire", CampfireCookingRecipe::new);
    }

    public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {
        stonecutting(source, category, result, 1);
    }

    public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int resultAmount) {
        SingleItemRecipeBuilder.stonecutting(source.toVanilla(), category, result.get(), resultAmount)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_stonecutting");
    }

    public <T extends ItemLike> void smeltingAndBlasting(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float xp) {
        smelting(source, category, cookingCategory, result, xp);
        blasting(source, category, cookingCategory, result, xp);
    }

    public <T extends ItemLike> void food(DataIngredient source, RecipeCategory category, CookingBookCategory cookingCategory, Supplier<? extends T> result, float xp) {
        smelting(source, category, cookingCategory, result, xp);
        smoking(source, category, cookingCategory, result, xp);
        campfire(source, category, cookingCategory, result, xp);
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
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void generateForEnabledBlockFamilies(FeatureFlagSet flagSet) { super.generateForEnabledBlockFamilies(flagSet); }

    /** Generated override to expose protected method: {@link RecipeProvider#netheriteSmithing} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void netheriteSmithing(Item base, RecipeCategory category, Item result) { super.netheriteSmithing(base, category, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#trimSmithing} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void trimSmithing(Item trimTemplate, ResourceKey<TrimPattern> patternId, ResourceKey<Recipe<?>> id) { super.trimSmithing(trimTemplate, patternId, id); }

    /** Generated override to expose protected method: {@link RecipeProvider#twoByTwoPacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void twoByTwoPacker(RecipeCategory category, ItemLike result, ItemLike ingredient) { super.twoByTwoPacker(category, result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient, String unlockedBy) { super.threeByThreePacker(category, result, ingredient, unlockedBy); }

    /** Generated override to expose protected method: {@link RecipeProvider#threeByThreePacker} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient) { super.threeByThreePacker(category, result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLog} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void planksFromLog(ItemLike result, TagKey<Item> logs, int count) { super.planksFromLog(result, logs, count); }

    /** Generated override to expose protected method: {@link RecipeProvider#planksFromLogs} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void planksFromLogs(ItemLike result, TagKey<Item> logs, int count) { super.planksFromLogs(result, logs, count); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodFromLogs} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void woodFromLogs(ItemLike result, ItemLike log) { super.woodFromLogs(result, log); }

    /** Generated override to expose protected method: {@link RecipeProvider#woodenBoat} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void woodenBoat(ItemLike result, ItemLike planks) { super.woodenBoat(result, planks); }

    /** Generated override to expose protected method: {@link RecipeProvider#chestBoat} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void chestBoat(ItemLike chestBoat, ItemLike boat) { super.chestBoat(chestBoat, boat); }

    /** Generated override to expose protected method: {@link RecipeProvider#buttonBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder buttonBuilder(ItemLike result, Ingredient base) { return super.buttonBuilder(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#doorBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder doorBuilder(ItemLike result, Ingredient base) { return super.doorBuilder(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder fenceBuilder(ItemLike result, Ingredient base) { return super.fenceBuilder(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#fenceGateBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder fenceGateBuilder(ItemLike result, Ingredient planks) { return super.fenceGateBuilder(result, planks); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void pressurePlate(ItemLike result, ItemLike base) { super.pressurePlate(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#pressurePlateBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike result, Ingredient base) { return super.pressurePlateBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#slab} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void slab(RecipeCategory category, ItemLike result, ItemLike base) { super.slab(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#shelf} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void shelf(ItemLike result, ItemLike strippedLogs) { super.shelf(result, strippedLogs); }

    /** Generated override to expose protected method: {@link RecipeProvider#slabBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder slabBuilder(RecipeCategory category, ItemLike result, Ingredient base) { return super.slabBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#stairBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder stairBuilder(ItemLike result, Ingredient base) { return super.stairBuilder(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#trapdoorBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder trapdoorBuilder(ItemLike result, Ingredient base) { return super.trapdoorBuilder(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#signBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder signBuilder(ItemLike result, Ingredient planks) { return super.signBuilder(result, planks); }

    /** Generated override to expose protected method: {@link RecipeProvider#hangingSign} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void hangingSign(ItemLike result, ItemLike ingredient) { super.hangingSign(result, ingredient); }

    /** Generated override to expose protected method: {@link RecipeProvider#colorItemWithDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void colorItemWithDye(List<Item> dyes, List<Item> items, String groupName, RecipeCategory category) { super.colorItemWithDye(dyes, items, groupName, category); }

    /** Generated override to expose protected method: {@link RecipeProvider#carpet} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void carpet(ItemLike result, ItemLike sourceItem) { super.carpet(result, sourceItem); }

    /** Generated override to expose protected method: {@link RecipeProvider#bedFromPlanksAndWool} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void bedFromPlanksAndWool(ItemLike result, ItemLike wool) { super.bedFromPlanksAndWool(result, wool); }

    /** Generated override to expose protected method: {@link RecipeProvider#banner} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void banner(ItemLike result, ItemLike wool) { super.banner(result, wool); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassFromGlassAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void stainedGlassFromGlassAndDye(ItemLike result, ItemLike dye) { super.stainedGlassFromGlassAndDye(result, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#dryGhast} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void dryGhast(ItemLike result) { super.dryGhast(result); }

    /** Generated override to expose protected method: {@link RecipeProvider#harness} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void harness(ItemLike result, ItemLike wool) { super.harness(result, wool); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromStainedGlass} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void stainedGlassPaneFromStainedGlass(ItemLike result, ItemLike stainedGlass) { super.stainedGlassPaneFromStainedGlass(result, stainedGlass); }

    /** Generated override to expose protected method: {@link RecipeProvider#stainedGlassPaneFromGlassPaneAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void stainedGlassPaneFromGlassPaneAndDye(ItemLike result, ItemLike dye) { super.stainedGlassPaneFromGlassPaneAndDye(result, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#coloredTerracottaFromTerracottaAndDye} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void coloredTerracottaFromTerracottaAndDye(ItemLike result, ItemLike dye) { super.coloredTerracottaFromTerracottaAndDye(result, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#concretePowder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void concretePowder(ItemLike result, ItemLike dye) { super.concretePowder(result, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#candle} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void candle(ItemLike result, ItemLike dye) { super.candle(result, dye); }

    /** Generated override to expose protected method: {@link RecipeProvider#wall} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void wall(RecipeCategory category, ItemLike result, ItemLike base) { super.wall(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#wallBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder wallBuilder(RecipeCategory category, ItemLike result, Ingredient base) { return super.wallBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#polished} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void polished(RecipeCategory category, ItemLike result, ItemLike base) { super.polished(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#polishedBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient base) { return super.polishedBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#cut} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void cut(RecipeCategory category, ItemLike result, ItemLike base) { super.cut(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#cutBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike result, Ingredient base) { return super.cutBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseled} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void chiseled(RecipeCategory category, ItemLike result, ItemLike base) { super.chiseled(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#mosaicBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike base) { super.mosaicBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#chiseledBuilder} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike result, Ingredient base) { return super.chiseledBuilder(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base) { super.stonecutterResultFromBase(category, result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#stonecutterResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base, int count) { super.stonecutterResultFromBase(category, result, base, count); }

    /** Generated override to expose protected method: {@link RecipeProvider#smeltingResultFromBase} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void smeltingResultFromBase(ItemLike result, ItemLike base) { super.smeltingResultFromBase(result, base); }

    /** Generated override to expose protected method: {@link RecipeProvider#nineBlockStorageRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm) { super.nineBlockStorageRecipes(unpackedFormCategory, unpackedForm, packedFormCategory, packedForm); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void copySmithingTemplate(ItemLike smithingTemplate, ItemLike baseMaterial) { super.copySmithingTemplate(smithingTemplate, baseMaterial); }

    /** Generated override to expose protected method: {@link RecipeProvider#copySmithingTemplate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void copySmithingTemplate(ItemLike smithingTemplate, Ingredient baseMaterials) { super.copySmithingTemplate(smithingTemplate, baseMaterials); }

    /** Generated override to expose protected method: {@link RecipeProvider#cookRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public <T extends AbstractCookingRecipe> void cookRecipes(String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime) { super.cookRecipes(source, factory, cookingTime); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void waxRecipes(FeatureFlagSet flagSet) { super.waxRecipes(flagSet); }

    /** Generated override to expose protected method: {@link RecipeProvider#grate} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void grate(Block grateBlock, Block material) { super.grate(grateBlock, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#copperBulb} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void copperBulb(Block copperBulb, Block copperMaterial) { super.copperBulb(copperBulb, copperMaterial); }

    /** Generated override to expose protected method: {@link RecipeProvider#waxedChiseled} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void waxedChiseled(Block result, Block material) { super.waxedChiseled(result, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#suspiciousStew} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void suspiciousStew(Item item, SuspiciousEffectHolder effectHolder) { super.suspiciousStew(item, effectHolder); }

    /** Generated override to expose protected method: {@link RecipeProvider#dyedItem} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void dyedItem(Item target, String group) { super.dyedItem(target, group); }

    /** Generated override to expose protected method: {@link RecipeProvider#dyedShulkerBoxRecipe} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void dyedShulkerBoxRecipe(Item dye, Item dyedResult) { super.dyedShulkerBoxRecipe(dye, dyedResult); }

    /** Generated override to expose protected method: {@link RecipeProvider#dyedBundleRecipe} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void dyedBundleRecipe(Item dye, Item dyedResult) { super.dyedBundleRecipe(dye, dyedResult); }

    /** Generated override to expose protected method: {@link RecipeProvider#generateRecipes} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public void generateRecipes(BlockFamily family, FeatureFlagSet flagSet) { super.generateRecipes(family, flagSet); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBaseBlockForCrafting} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public Block getBaseBlockForCrafting(BlockFamily family, BlockFamily.Variant variant) { return super.getBaseBlockForCrafting(family, variant); }

    /** Generated override to expose protected method: {@link RecipeProvider#insideOf} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) { return RecipeProvider.insideOf(block); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) { return super.has(count, item); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) { return super.has(item); }

    /** Generated override to expose protected method: {@link RecipeProvider#has} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) { return super.has(tag); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... predicates) { return RecipeProvider.inventoryTrigger(predicates); }

    /** Generated override to expose protected method: {@link RecipeProvider#inventoryTrigger} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) { return RecipeProvider.inventoryTrigger(predicates); }

    /** Generated override to expose protected method: {@link RecipeProvider#getHasName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static String getHasName(ItemLike baseBlock) { return RecipeProvider.getHasName(baseBlock); }

    /** Generated override to expose protected method: {@link RecipeProvider#getItemName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static String getItemName(ItemLike itemLike) { return RecipeProvider.getItemName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSimpleRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static String getSimpleRecipeName(ItemLike itemLike) { return RecipeProvider.getSimpleRecipeName(itemLike); }

    /** Generated override to expose protected method: {@link RecipeProvider#getConversionRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static String getConversionRecipeName(ItemLike product, ItemLike material) { return RecipeProvider.getConversionRecipeName(product, material); }

    /** Generated override to expose protected method: {@link RecipeProvider#getSmeltingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static String getSmeltingRecipeName(ItemLike product) { return RecipeProvider.getSmeltingRecipeName(product); }

    /** Generated override to expose protected method: {@link RecipeProvider#getBlastingRecipeName} */
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public static String getBlastingRecipeName(ItemLike product) { return RecipeProvider.getBlastingRecipeName(product); }

    /** Generated override to expose protected method: {@link RecipeProvider#tag} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public Ingredient tag(TagKey<Item> id) { return super.tag(id); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemStackTemplate stack) { return super.shaped(category, stack); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item) { return super.shaped(category, item); }

    /** Generated override to expose protected method: {@link RecipeProvider#shaped} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item, int count) { return super.shaped(category, item, count); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result) { return super.shapeless(category, result); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item) { return super.shapeless(category, item); }

    /** Generated override to expose protected method: {@link RecipeProvider#shapeless} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateRecipeProvider", date = "Sun, 19 Apr 2026 10:13:58 GMT")
    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item, int count) { return super.shapeless(category, item, count); }

    // GENERATED END

}
