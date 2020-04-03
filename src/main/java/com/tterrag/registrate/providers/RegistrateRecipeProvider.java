package com.tterrag.registrate.providers;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
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
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
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
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        this.callback = consumer;
        owner.genData(ProviderType.RECIPE, this);
        this.callback = null;
    }
    
    private ResourceLocation safeId(ResourceLocation id) {
        return new ResourceLocation(owner.getModid(), safeName(id));
    }
    
    private ResourceLocation safeId(IForgeRegistryEntry<?> registryEntry) {
        return safeId(registryEntry.getRegistryName());
    }
    
    private String safeName(ResourceLocation nameSource) {
        return nameSource.getPath().replace('/', '_');
    }
    
    private String safeName(IForgeRegistryEntry<?> registryEntry) {
        return safeName(registryEntry.getRegistryName());
    }
    
    public static final int DEFAULT_SMELT_TIME = 200;
    public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
    public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
    public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;
    
    private static final String SMELTING_NAME = "smelting";
    @SuppressWarnings("null")
    private static final ImmutableMap<CookingRecipeSerializer<?>, String> COOKING_TYPE_NAMES = ImmutableMap.<CookingRecipeSerializer<?>, String>builder()
            .put(IRecipeSerializer.SMELTING, SMELTING_NAME)
            .put(IRecipeSerializer.BLASTING, "blasting")
            .put(IRecipeSerializer.SMOKING, "smoking")
            .put(IRecipeSerializer.CAMPFIRE_COOKING, "campfire")
            .build();
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(Supplier<? extends T> source, Supplier<? extends T> result, float experience, int cookingTime, CookingRecipeSerializer<?> serializer, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(Tag<Item> source, Supplier<? extends T> result, float experience, int cookingTime, CookingRecipeSerializer<?> serializer, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(Supplier<? extends T> source, Supplier<? extends T> result, float experience, int cookingTime, String typeName, CookingRecipeSerializer<?> serializer, Consumer<IFinishedRecipe> consumer) {
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromItems(source.get()), result.get(), experience, cookingTime, serializer)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()) + "_from" + (SMELTING_NAME.equals(typeName) ? "_" + safeName(source.get()) + "_" + typeName : ""));
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(Tag<Item> source, Supplier<? extends T> result, float experience, int cookingTime, String typeName, CookingRecipeSerializer<?> serializer, Consumer<IFinishedRecipe> consumer) {
        CookingRecipeBuilder.cookingRecipe(Ingredient.fromTag(source), result.get(), experience, cookingTime, serializer)
            .addCriterion("has_" + safeName(source.getId()), this.hasItem(source))
            .build(consumer, safeId(result.get()) + "_from" + (SMELTING_NAME.equals(typeName) ? "_" + safeName(source.getId()) + "_" + typeName : ""));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(Supplier<? extends T> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        smelting(source, result, experience, DEFAULT_SMELT_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(Supplier<? extends T> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMELTING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(Tag<Item> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        smelting(source, result, experience, DEFAULT_SMELT_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(Tag<Item> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMELTING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(Supplier<? extends T> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        blasting(source, result, experience, DEFAULT_BLAST_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(Supplier<? extends T> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.BLASTING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(Tag<Item> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        blasting(source, result, experience, DEFAULT_BLAST_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(Tag<Item> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.BLASTING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(Supplier<? extends T> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        smoking(source, result, experience, DEFAULT_SMOKE_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(Supplier<? extends T> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMOKING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(Tag<Item> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        smoking(source, result, experience, DEFAULT_SMOKE_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(Tag<Item> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMOKING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(Supplier<? extends T> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        campfire(source, result, experience, DEFAULT_CAMPFIRE_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(Supplier<? extends T> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.CAMPFIRE_COOKING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(Tag<Item> source, Supplier<? extends T> result, float experience, Consumer<IFinishedRecipe> consumer) {
        campfire(source, result, experience, DEFAULT_CAMPFIRE_TIME, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(Tag<Item> source, Supplier<? extends T> result, float experience, int cookingTime, Consumer<IFinishedRecipe> consumer) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.CAMPFIRE_COOKING, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stonecutting(Supplier<? extends T> source, Supplier<? extends T> result, Consumer<IFinishedRecipe> consumer) {
        stonecutting(source, result, 1, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stonecutting(Supplier<? extends T> source, Supplier<? extends T> result, int resultAmount, Consumer<IFinishedRecipe> consumer) {
        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(source.get()), result.get(), resultAmount)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()) + "_from_" + safeName(source.get()) + "_stonecutting");
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smeltingAndBlasting(Supplier<? extends T> source, Supplier<? extends T> result, float xp, Consumer<IFinishedRecipe> consumer) {
        smelting(source, result, xp, consumer);
        blasting(source, result, xp, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smeltingAndBlasting(Tag<Item> source, Supplier<? extends T> result, float xp, Consumer<IFinishedRecipe> consumer) {
        smelting(source, result, xp, consumer);
        blasting(source, result, xp, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void food(Supplier<? extends T> source, Supplier<? extends T> result, float xp, Consumer<IFinishedRecipe> consumer) {
        smelting(source, result, xp, consumer);
        smoking(source, result, xp, consumer);
        campfire(source, result, xp, consumer);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void food(Tag<Item> source, Supplier<? extends T> result, float xp, Consumer<IFinishedRecipe> consumer) {
        smelting(source, result, xp, consumer);
        smoking(source, result, xp, consumer);
        campfire(source, result, xp, consumer);
    }
        
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void storage(Supplier<? extends T> input, Supplier<? extends T> output, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(output.get())
            .patternLine("XXX").patternLine("XXX").patternLine("XXX")
            .key('X', input.get())
            .addCriterion("has_" + safeName(input.get()), this.hasItem(input.get()))
            .build(consumer, safeId(output.get()));
        
        singleItemUnfinished(input, output, 1, 9)
            .build(consumer, safeId(input.get()) + "_from_" + safeName(output.get()));;
    }

    @CheckReturnValue
    public <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder singleItemUnfinished(Supplier<? extends T> source, Supplier<? extends T> result, int required, int amount) {
        return ShapelessRecipeBuilder.shapelessRecipe(result.get(), amount)
            .addIngredient(source.get(), required)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void singleItem(Supplier<? extends T> source, Supplier<? extends T> result, int required, int amount, Consumer<IFinishedRecipe> consumer) {
        singleItemUnfinished(source, result, required, amount).build(consumer, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void planks(Supplier<? extends T> source, Supplier<? extends T> result, Consumer<IFinishedRecipe> consumer) {
        singleItemUnfinished(source, result, 1, 4)
            .setGroup("planks")
            .build(consumer, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stairs(Supplier<? extends T> source, Supplier<? extends T> result, @Nullable String group, boolean stone, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 4)
            .patternLine("X  ").patternLine("XX ").patternLine("XXX")
            .key('X', source.get())
            .setGroup(group)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
        if (stone) {
            stonecutting(source, result, consumer);
        }
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void slab(Supplier<? extends T> source, Supplier<? extends T> result, @Nullable String group, boolean stone, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 6)
            .patternLine("XXX")
            .key('X', source.get())
            .setGroup(group)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
        if (stone) {
            stonecutting(source, result, 2, consumer);
        }
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void fence(Supplier<? extends T> source, Supplier<? extends T> result, @Nullable String group, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 3)
            .patternLine("W#W").patternLine("W#W")
            .key('W', source.get())
            .key('#', Tags.Items.RODS_WOODEN)
            .setGroup(group)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void fenceGate(Supplier<? extends T> source, Supplier<? extends T> result, @Nullable String group, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get())
            .patternLine("#W#").patternLine("#W#")
            .key('W', source.get())
            .key('#', Tags.Items.RODS_WOODEN)
            .setGroup(group)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void wall(Supplier<? extends T> source, Supplier<? extends T> result, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 6)
            .patternLine("XXX").patternLine("XXX")
            .key('X', source.get())
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
        stonecutting(source, result, consumer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void door(Supplier<? extends T> source, Supplier<? extends T> result, @Nullable String group, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 3)
            .patternLine("XX").patternLine("XX").patternLine("XX")
            .key('X', source.get())
            .setGroup(group)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void trapDoor(Supplier<? extends T> source, Supplier<? extends T> result, @Nullable String group, Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 2)
            .patternLine("XXX").patternLine("XXX")
            .key('X', source.get())
            .setGroup(group)
            .addCriterion("has_" + safeName(source.get()), this.hasItem(source.get()))
            .build(consumer, safeId(result.get()));
    }
    
    // @formatter:off
    // GENERATED START

    @Override
    public void saveRecipeAdvancement(DirectoryCache cache, JsonObject advancementJson, Path pathIn) { super.saveRecipeAdvancement(cache, advancementJson, pathIn); }

    @Override
    public EnterBlockTrigger.Instance enteredBlock(Block blockIn) { return super.enteredBlock(blockIn); }

    @Override
    public InventoryChangeTrigger.Instance hasItem(IItemProvider itemIn) { return super.hasItem(itemIn); }

    @Override
    public InventoryChangeTrigger.Instance hasItem(Tag<Item> tagIn) { return super.hasItem(tagIn); }

    @Override
    public InventoryChangeTrigger.Instance hasItem(ItemPredicate... predicates) { return super.hasItem(predicates); }

    // GENERATED END
}
