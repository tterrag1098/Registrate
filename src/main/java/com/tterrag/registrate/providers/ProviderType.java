package com.tterrag.registrate.providers;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.FieldsAreNonnullByDefault;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

/**
 * Represents a type of data that can be generated, and specifies a factory for the provider.
 * <p>
 * Used as a key for data generator callbacks.
 * <p>
 * This file also defines the built-in provider types, but third-party types can be created with {@link #register(String, ProviderType)}.
 *
 * @param <T>
 *            The type of the provider
 */
@FunctionalInterface
@SuppressWarnings("deprecation")
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public interface ProviderType<T extends RegistrateProvider> {

    // SERVER DATA
    public static final ProviderType<RegistrateRecipeProvider> RECIPE = register("recipe", (p, e) -> new RegistrateRecipeProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = register("advancement", (p, e) -> new RegistrateAdvancementProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateLootTableProvider> LOOT = register("loot", (p, e) -> new RegistrateLootTableProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateTagsProvider<Block>> BLOCK_TAGS = register("tags/block", type -> (p, e) -> new RegistrateTagsProvider<Block>(p, type, "blocks", e.getGenerator(), Registry.BLOCK, e.getExistingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider<Item>> ITEM_TAGS = register("tags/item", type -> (p, e) -> new RegistrateTagsProvider<Item>(p, type, "items", e.getGenerator(), Registry.ITEM, e.getExistingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider<Fluid>> FLUID_TAGS = register("tags/fluid", type -> (p, e) -> new RegistrateTagsProvider<Fluid>(p, type, "fluids", e.getGenerator(), Registry.FLUID, e.getExistingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider<EntityType<?>>> ENTITY_TAGS = register("tags/entity", type -> (p, e) -> new RegistrateTagsProvider<EntityType<?>>(p, type, "entity_types", e.getGenerator(), Registry.ENTITY_TYPE, e.getExistingFileHelper()));

    // CLIENT DATA
    public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = register("blockstate", (p, e) -> new RegistrateBlockstateProvider(p, e.getGenerator(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = register("item_model", (p, e, existing) -> new RegistrateItemModelProvider(p, e.getGenerator(), ((RegistrateBlockstateProvider)existing.get(BLOCKSTATE)).getExistingFileHelper()));
    public static final ProviderType<RegistrateLangProvider> LANG = register("lang", (p, e) -> new RegistrateLangProvider(p, e.getGenerator()));

    T create(AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing);
    
    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, NonNullFunction<ProviderType<T>, NonNullBiFunction<AbstractRegistrate<?>, GatherDataEvent, T>> type) {
        ProviderType<T> ret = new ProviderType<T>() {
            
            @Override
            public T create(@Nonnull AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
                return type.apply(this).apply(parent, event);
            }
        };
        return register(name, ret);
    }
    
    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, NonNullBiFunction<AbstractRegistrate<?>, GatherDataEvent, T> type) {
        ProviderType<T> ret = new ProviderType<T>() {
            
            @Override
            public T create(AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
                return type.apply(parent, event);
            }
        };
        return register(name, ret);
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }
}
