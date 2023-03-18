package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.FieldsAreNonnullByDefault;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.data.event.GatherDataEvent;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

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
    public static final ProviderType<RegistrateRecipeProvider> RECIPE = register("recipe", (p, e) -> new RegistrateRecipeProvider(p, e.getGenerator().getPackOutput()));
    public static final ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = register("advancement", (p, e) -> new RegistrateAdvancementProvider(p, e.getGenerator().getPackOutput(), e.getLookupProvider()));
    public static final ProviderType<RegistrateLootTableProvider> LOOT = register("loot", (p, e) -> new RegistrateLootTableProvider(p, e.getGenerator().getPackOutput()));
    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<Block>> BLOCK_TAGS = register("tags/block", type -> (p, e) -> new RegistrateTagsProvider.IntrinsicImpl<Block>(p, type, "blocks", e.getGenerator().getPackOutput(), Registries.BLOCK, e.getLookupProvider(), block -> block.builtInRegistryHolder().key(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerDelegate("tags/item", type -> (p, e, existing) -> new RegistrateItemTagsProvider(p, type, "items", e.getGenerator().getPackOutput(), e.getLookupProvider(), ((TagsProvider<Block>)existing.get(BLOCK_TAGS)).contentsGetter(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<Fluid>> FLUID_TAGS = register("tags/fluid", type -> (p, e) -> new RegistrateTagsProvider.IntrinsicImpl<Fluid>(p, type, "fluids", e.getGenerator().getPackOutput(), Registries.FLUID, e.getLookupProvider(), fluid -> fluid.builtInRegistryHolder().key(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = register("tags/entity", type -> (p, e) -> new RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>(p, type, "entity_types", e.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, e.getLookupProvider(), entityType -> entityType.builtInRegistryHolder().key(), e.getExistingFileHelper()));

    // CLIENT DATA
    public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = register("blockstate", (p, e) -> new RegistrateBlockstateProvider(p, e.getGenerator().getPackOutput(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = register("item_model", (p, e, existing) -> new RegistrateItemModelProvider(p, e.getGenerator().getPackOutput(), ((RegistrateBlockstateProvider)existing.get(BLOCKSTATE)).getExistingFileHelper()));
    public static final ProviderType<RegistrateLangProvider> LANG = register("lang", (p, e) -> new RegistrateLangProvider(p, e.getGenerator().getPackOutput()));

    T create(AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing);

    // TODO this is clunky af
    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> registerDelegate(String name, NonNullUnaryOperator<ProviderType<T>> type) {
        ProviderType<T> ret = new ProviderType<T>() {

            @Override
            public T create(@Nonnull AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
                return type.apply(this).create(parent, event, existing);
            }
        };
        return register(name, ret);
    }

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
