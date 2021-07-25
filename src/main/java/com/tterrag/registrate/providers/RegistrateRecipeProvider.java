package com.tterrag.registrate.providers;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistrateRecipeProvider extends RecipeProvider implements RegistrateProvider, Consumer<IFinishedRecipe> {
    
    private final AbstractRegistrate<?> owner;

    public RegistrateRecipeProvider(AbstractRegistrate<?> owner, DataGenerator generatorIn) {
        super(generatorIn);
        this.owner = owner;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }
    
    @Nullable
    private Consumer<IFinishedRecipe> callback;
    
    @Override
    public void accept(@Nullable IFinishedRecipe t) {
        if (callback == null) {
            throw new IllegalStateException("Cannot accept recipes outside of a call to registerRecipes");
        }
        callback.accept(t);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
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

    public ResourceLocation safeId(IForgeRegistryEntry<?> registryEntry) {
        return safeId(registryEntry.getRegistryName());
    }

    public String safeName(ResourceLocation id) {
        return id.getPath().replace('/', '_');
    }

    public String safeName(DataIngredient source) {
        return safeName(source.getId());
    }

    public String safeName(IForgeRegistryEntry<?> registryEntry) {
        return safeName(registryEntry.getRegistryName());
    }

    public static final int DEFAULT_SMELT_TIME = 200;
    public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
    public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
    public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;
    
    private static final String SMELTING_NAME = "smelting";
    @SuppressWarnings("null")
    private static final ImmutableMap<CookingRecipeSerializer<?>, String> COOKING_TYPE_NAMES = ImmutableMap.<CookingRecipeSerializer<?>, String>builder()
            .put(IRecipeSerializer.SMELTING_RECIPE, SMELTING_NAME)
            .put(IRecipeSerializer.BLASTING_RECIPE, "blasting")
            .put(IRecipeSerializer.SMOKING_RECIPE, "smoking")
            .put(IRecipeSerializer.CAMPFIRE_COOKING_RECIPE, "campfire")
            .build();
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime, CookingRecipeSerializer<?> serializer) {
        cooking(source, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime, String typeName, CookingRecipeSerializer<?> serializer) {
        CookingRecipeBuilder.cooking(source, result.get(), experience, cookingTime, serializer)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(DataIngredient source, Supplier<? extends T> result, float experience) {
        smelting(source, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMELTING_RECIPE);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(DataIngredient source, Supplier<? extends T> result, float experience) {
        blasting(source, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.BLASTING_RECIPE);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(DataIngredient source, Supplier<? extends T> result, float experience) {
        smoking(source, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMOKING_RECIPE);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(DataIngredient source, Supplier<? extends T> result, float experience) {
        campfire(source, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.CAMPFIRE_COOKING_RECIPE);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stonecutting(DataIngredient source, Supplier<? extends T> result) {
        stonecutting(source, result, 1);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stonecutting(DataIngredient source, Supplier<? extends T> result, int resultAmount) {
        SingleItemRecipeBuilder.stonecutting(source, result.get(), resultAmount)
            .unlocks("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_stonecutting");
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smeltingAndBlasting(DataIngredient source, Supplier<? extends T> result, float xp) {
        smelting(source, result, xp);
        blasting(source, result, xp);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void food(DataIngredient source, Supplier<? extends T> result, float xp) {
        smelting(source, result, xp);
        smoking(source, result, xp);
        campfire(source, result, xp);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void square(DataIngredient source, Supplier<? extends T> output, boolean small) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(output.get())
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
     * @deprecated Broken, use {@link #storage(NonNullSupplier, NonNullSupplier)} or {@link #storage(DataIngredient, NonNullSupplier, DataIngredient, NonNullSupplier)}.
     */
    @Deprecated
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void storage(DataIngredient source, NonNullSupplier<? extends T> output) {
        square(source, output, false);
        // This is backwards, but leaving in for binary compat
        singleItemUnfinished(source, output, 1, 9)
            .save(this, safeId(source) + "_from_" + safeName(output.get()));
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void storage(NonNullSupplier<? extends T> source, NonNullSupplier<? extends T> output) {
        storage(DataIngredient.items(source), source, DataIngredient.items(output), output);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void storage(DataIngredient sourceIngredient, NonNullSupplier<? extends T> source, DataIngredient outputIngredient, NonNullSupplier<? extends T> output) {
        square(sourceIngredient, output, false);
        singleItemUnfinished(outputIngredient, source, 1, 9)
            .save(this, safeId(sourceIngredient) + "_from_" + safeName(output.get()));
    }

    @CheckReturnValue
    public <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder singleItemUnfinished(DataIngredient source, Supplier<? extends T> result, int required, int amount) {
        return ShapelessRecipeBuilder.shapeless(result.get(), amount)
            .requires(source, required)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void singleItem(DataIngredient source, Supplier<? extends T> result, int required, int amount) {
        singleItemUnfinished(source, result, required, amount).save(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void planks(DataIngredient source, Supplier<? extends T> result) {
        singleItemUnfinished(source, result, 1, 4)
            .group("planks")
            .save(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stairs(DataIngredient source, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shaped(result.get(), 4)
            .pattern("X  ").pattern("XX ").pattern("XXX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, result);
        }
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void slab(DataIngredient source, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shaped(result.get(), 6)
            .pattern("XXX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, result, 2);
        }
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void fence(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(result.get(), 3)
            .pattern("W#W").pattern("W#W")
            .define('W', source)
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void fenceGate(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(result.get())
            .pattern("#W#").pattern("#W#")
            .define('W', source)
            .define('#', Tags.Items.RODS_WOODEN)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void wall(DataIngredient source, Supplier<? extends T> result) {
        ShapedRecipeBuilder.shaped(result.get(), 6)
            .pattern("XXX").pattern("XXX")
            .define('X', source)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
        stonecutting(source, result);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void door(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(result.get(), 3)
            .pattern("XX").pattern("XX").pattern("XX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void trapDoor(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shaped(result.get(), 2)
            .pattern("XXX").pattern("XXX")
            .define('X', source)
            .group(group)
            .unlockedBy("has_" + safeName(source), source.getCritereon(this))
            .save(this, safeId(result.get()));
    }

    // @formatter:off
    // GENERATED START

    @Override
    public void saveAdvancement(DirectoryCache cache, JsonObject cache2, Path advancementJson) { super.saveAdvancement(cache, cache2, advancementJson); }

    public static EnterBlockTrigger.Instance enteredBlock(Block block) { return RecipeProvider.insideOf(block); }

    public static InventoryChangeTrigger.Instance hasItem(IItemProvider item) { return RecipeProvider.has(item); }

    public static InventoryChangeTrigger.Instance hasItem(ITag<Item> tag) { return RecipeProvider.has(tag); }

    public static InventoryChangeTrigger.Instance hasItem(ItemPredicate... predicate) { return RecipeProvider.inventoryTrigger(predicate); }

    // GENERATED END
}
