package com.tterrag.registrate.util;

import com.google.common.collect.ObjectArrays;
import com.tterrag.registrate.providers.generators.RegistrateRecipeProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.Getter;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A helper for data generation when using ingredients as input(s) to recipes.<br>
 * It remembers the name of the primary ingredient for use in creating recipe names/criteria.
 * <p>
 * Create an instance of this class with the various factory methods such as {@link #items(ItemLike, ItemLike...)} and {@link #tag(HolderSet.Named)} )}.
 * <p>
 * <strong>This class should not be used for any purpose other than data generation</strong>, it will throw an exception if it is serialized to a packet buffer.
 */
public final class DataIngredient {

    //TODO <1.21.4> removed delegate. Is there a need to add it back?
    private final Ingredient parent;
    @Getter
    private final Identifier id;
    private final Function<RegistrateRecipeProvider, Criterion<InventoryChangeTrigger.TriggerInstance>> criteriaFactory;

    private DataIngredient(Ingredient parent, ItemLike item) {
        this.parent = parent;
        this.id = BuiltInRegistries.ITEM.getKey(item.asItem());
        this.criteriaFactory = prov -> prov.has(item);
    }
    
    private DataIngredient(Ingredient parent, TagKey<Item> tag) {
        this.parent = parent;
        this.id = tag.location();
        this.criteriaFactory = prov -> prov.has(tag);
    }
    
    private DataIngredient(Ingredient parent, Identifier id, ItemPredicate... predicates) {
        this.parent = parent;
        this.id = id;
        this.criteriaFactory = prov -> RegistrateRecipeProvider.inventoryTrigger(predicates);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> getCriterion(RegistrateRecipeProvider prov) {
        return criteriaFactory.apply(prov);
    }
    
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(NonNullSupplier<? extends T> first, NonNullSupplier<? extends T>... others) {
        return items(first.get(), (T[]) Arrays.stream(others).map(Supplier::get).toArray(ItemLike[]::new));
    }

    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(T first, T... others) {
        return ingredient(Ingredient.of(ObjectArrays.concat(first, others)), first);
    }

    public static DataIngredient tag(HolderSet.Named<Item> tag) {
        return ingredient(Ingredient.of(tag), tag.key());
    }
    
    public static DataIngredient ingredient(Ingredient parent, ItemLike required) {
        return new DataIngredient(parent, required);
    }
    
    public static DataIngredient ingredient(Ingredient parent, TagKey<Item> required) {
        return new DataIngredient(parent, required);
    }
    
    public static DataIngredient ingredient(Ingredient parent, Identifier id, ItemPredicate... criteria) {
        return new DataIngredient(parent, id, criteria);
    }

    public Ingredient toVanilla() {
        return parent;
    }
}
