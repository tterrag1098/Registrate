package com.tterrag.registrate.providers;

import java.nio.file.Path;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.fml.LogicalSide;

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
