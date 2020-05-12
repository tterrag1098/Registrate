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
            .put(IRecipeSerializer.SMELTING, SMELTING_NAME)
            .put(IRecipeSerializer.BLASTING, "blasting")
            .put(IRecipeSerializer.SMOKING, "smoking")
            .put(IRecipeSerializer.CAMPFIRE_COOKING, "campfire")
            .build();
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime, CookingRecipeSerializer<?> serializer) {
        cooking(source, result, experience, cookingTime, COOKING_TYPE_NAMES.get(serializer), serializer);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void cooking(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime, String typeName, CookingRecipeSerializer<?> serializer) {
        CookingRecipeBuilder.cookingRecipe(source, result.get(), experience, cookingTime, serializer)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()) + "_from" + (!SMELTING_NAME.equals(typeName) ? "_" + safeName(source) + "_" + typeName : ""));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(DataIngredient source, Supplier<? extends T> result, float experience) {
        smelting(source, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smelting(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMELTING);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(DataIngredient source, Supplier<? extends T> result, float experience) {
        blasting(source, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void blasting(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.BLASTING);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(DataIngredient source, Supplier<? extends T> result, float experience) {
        smoking(source, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void smoking(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.SMOKING);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(DataIngredient source, Supplier<? extends T> result, float experience) {
        campfire(source, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void campfire(DataIngredient source, Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, result, experience, cookingTime, IRecipeSerializer.CAMPFIRE_COOKING);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stonecutting(DataIngredient source, Supplier<? extends T> result) {
        stonecutting(source, result, 1);
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stonecutting(DataIngredient source, Supplier<? extends T> result, int resultAmount) {
        SingleItemRecipeBuilder.stonecuttingRecipe(source, result.get(), resultAmount)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()) + "_from_" + safeName(source) + "_stonecutting");
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
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shapedRecipe(output.get())
                .key('X', source);
        if (small) {
            builder.patternLine("XX").patternLine("XX");
        } else {
            builder.patternLine("XXX").patternLine("XXX").patternLine("XXX");
        }
        builder.addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(output.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void storage(DataIngredient source, Supplier<? extends T> output) {
        square(source, output, false);
        singleItemUnfinished(source, output, 1, 9)
            .build(this, safeId(source) + "_from_" + safeName(output.get()));;
    }

    @CheckReturnValue
    public <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder singleItemUnfinished(DataIngredient source, Supplier<? extends T> result, int required, int amount) {
        return ShapelessRecipeBuilder.shapelessRecipe(result.get(), amount)
            .addIngredient(source, required)
            .addCriterion("has_" + safeName(source), source.getCritereon(this));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void singleItem(DataIngredient source, Supplier<? extends T> result, int required, int amount) {
        singleItemUnfinished(source, result, required, amount).build(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void planks(DataIngredient source, Supplier<? extends T> result) {
        singleItemUnfinished(source, result, 1, 4)
            .setGroup("planks")
            .build(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void stairs(DataIngredient source, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 4)
            .patternLine("X  ").patternLine("XX ").patternLine("XXX")
            .key('X', source)
            .setGroup(group)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, result);
        }
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void slab(DataIngredient source, Supplier<? extends T> result, @Nullable String group, boolean stone) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 6)
            .patternLine("XXX")
            .key('X', source)
            .setGroup(group)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
        if (stone) {
            stonecutting(source, result, 2);
        }
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void fence(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 3)
            .patternLine("W#W").patternLine("W#W")
            .key('W', source)
            .key('#', Tags.Items.RODS_WOODEN)
            .setGroup(group)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void fenceGate(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shapedRecipe(result.get())
            .patternLine("#W#").patternLine("#W#")
            .key('W', source)
            .key('#', Tags.Items.RODS_WOODEN)
            .setGroup(group)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void wall(DataIngredient source, Supplier<? extends T> result) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 6)
            .patternLine("XXX").patternLine("XXX")
            .key('X', source)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
        stonecutting(source, result);
    }
    
    public <T extends IItemProvider & IForgeRegistryEntry<?>> void door(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 3)
            .patternLine("XX").patternLine("XX").patternLine("XX")
            .key('X', source)
            .setGroup(group)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
    }

    public <T extends IItemProvider & IForgeRegistryEntry<?>> void trapDoor(DataIngredient source, Supplier<? extends T> result, @Nullable String group) {
        ShapedRecipeBuilder.shapedRecipe(result.get(), 2)
            .patternLine("XXX").patternLine("XXX")
            .key('X', source)
            .setGroup(group)
            .addCriterion("has_" + safeName(source), source.getCritereon(this))
            .build(this, safeId(result.get()));
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
