package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.FieldsAreNonnullByDefault;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a type of data that can be generated, and specifies a factory for the provider.
 * <p>
 * Used as a key for data generator callbacks.
 * <p>
 * This file also defines the built-in provider types, but third-party types can be created with {@link #register(String, ProviderType)}.
 *
 * @param <T> The type of the provider
 */
@FunctionalInterface
@SuppressWarnings("deprecation")
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public interface ProviderType<T extends RegistrateProvider> {

    // SERVER DATA
    ProviderType<RegistrateDatapackProvider> DYNAMIC = registerServerData("dynamic", RegistrateDatapackProvider::new);
    ProviderType<RegistrateDataMapProvider> DATA_MAP = registerServerData("data_map", RegistrateDataMapProvider::new);
    ProviderType<RegistrateRecipeProvider> RECIPE = registerServerData("recipe", RegistrateRecipeProvider::new);
    ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = registerServerData("advancement", RegistrateAdvancementProvider::new);
    ProviderType<RegistrateLootTableProvider> LOOT = registerServerData("loot", RegistrateLootTableProvider::new);
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<Block>> BLOCK_TAGS = registerIntrinsicTag("tags/block", "blocks", Registries.BLOCK, block -> block.builtInRegistryHolder().key());
    ProviderType<RegistrateTagsProvider.Impl<Enchantment>> ENCHANTMENT_TAGS = registerDynamicTag("tags/enchantment", "enchantments", Registries.ENCHANTMENT);
    ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerTag("tags/item", Registries.ITEM, c -> new RegistrateItemTagsProvider(c.parent(), c.type(), "items", c.output(), c.provider(), c.get(BLOCK_TAGS).contentsGetter(), c.fileHelper()));
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<Fluid>> FLUID_TAGS = registerIntrinsicTag("tags/fluid", "fluids", Registries.FLUID, fluid -> fluid.builtInRegistryHolder().key());
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = registerIntrinsicTag("tags/entity", "entity_types", Registries.ENTITY_TYPE, entityType -> entityType.builtInRegistryHolder().key());
    ProviderType<RegistrateGenericProvider> GENERIC_SERVER = registerProvider("registrate_generic_server_provider",  c -> new RegistrateGenericProvider(c.parent(), c.event(), LogicalSide.SERVER, c.type()));

    // CLIENT DATA
    ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = registerProvider("blockstate", c -> new RegistrateBlockstateProvider(c.parent(), c.output(), c.fileHelper()));
    ProviderType<RegistrateItemModelProvider> ITEM_MODEL = registerProvider("item_model", c -> new RegistrateItemModelProvider(c.parent(), c.output(), c.get(BLOCKSTATE).getExistingFileHelper()));
    ProviderType<RegistrateLangProvider> LANG = registerProvider("lang", c -> new RegistrateLangProvider(c.parent(), c.output()));
    ProviderType<RegistrateGenericProvider> GENERIC_CLIENT = registerProvider("registrate_generic_client_provider", c -> new RegistrateGenericProvider(c.parent(), c.event(), LogicalSide.CLIENT, c.type()));

    record Context<T extends RegistrateProvider>(ProviderType<T> type, AbstractRegistrate<?> parent,
                                                 @Deprecated GatherDataEvent event,
                                                 Map<ProviderType<?>, RegistrateProvider> existing,
                                                 PackOutput output, ExistingFileHelper fileHelper,
                                                 CompletableFuture<HolderLookup.Provider> provider) {

        public <R extends RegistrateProvider> R get(ProviderType<R> other) {
            return (R) existing().get(other);
        }

    }

    default T create(Context<T> context) {
        return create(context.parent(), context.event(), context.existing());
    }

    @Deprecated
    T create(AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing);

    interface DependencyAwareProviderType<T extends RegistrateProvider> extends ProviderType<T> {

        @Override
        default T create(AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
            return create(new Context<>(this, parent, event, existing, event.getGenerator().getPackOutput(), event.getExistingFileHelper(), event.getLookupProvider()));
        }

        @Override
        T create(Context<T> context);

    }

    interface SimpleServerDataFactory<T extends RegistrateProvider> extends DependencyAwareProviderType<T> {

        T create(AbstractRegistrate<?> parent, PackOutput output, CompletableFuture<HolderLookup.Provider> provider);

        @Override
        default T create(Context<T> context) {
            return create(context.parent(), context.output(), context.provider());
        }

        default ProviderType<T> asProvider() {
            return this;
        }

    }

    // TODO this is clunky af
    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> registerServerData(String name, SimpleServerDataFactory<T> factory) {
        return register(name, factory.asProvider());
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> registerProvider(String name, DependencyAwareProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    @Nonnull
    static <T, R extends RegistrateTagsProvider<T>> ProviderType<R> registerTag(String name, ResourceKey<? extends Registry<T>> key, DependencyAwareProviderType<R> type) {
        if (RegistrateDataProvider.TAG_TYPES.containsKey(key)) {
            return (ProviderType<R>) RegistrateDataProvider.TAG_TYPES.get(key);
        }
        RegistrateDataProvider.TAG_TYPES.put(key, type);
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    @Nonnull
    static <T> ProviderType<RegistrateTagsProvider.IntrinsicImpl<T>> registerIntrinsicTag(String providerName, String typeName, ResourceKey<? extends Registry<T>> registry, Function<T, ResourceKey<T>> keyExtractor) {
        return registerTag(providerName, registry, c -> new RegistrateTagsProvider.IntrinsicImpl<>(c.parent(), c.type(), typeName, c.output(), registry, c.provider(), keyExtractor, c.fileHelper()));
    }

    @Nonnull
    static <T> ProviderType<RegistrateTagsProvider.Impl<T>> registerDynamicTag(String providerName, String typeName, ResourceKey<Registry<T>> registry) {
        return registerTag(providerName, registry, c -> new RegistrateTagsProvider.Impl<>(c.parent(), c.type(), typeName, c.output(), registry, c.provider(), c.fileHelper()));
    }

    static <T extends RegistrateProvider> T create(ProviderType<T> type, AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing, CompletableFuture<HolderLookup.Provider> provider) {
        return type.create(new Context<>(type, parent, event, existing, event.getGenerator().getPackOutput(), event.getExistingFileHelper(), provider));
    }

}
