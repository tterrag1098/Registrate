package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.generators.*;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
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
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents a type of data that can be generated, and specifies a factory for the provider.
 * <p>
 * Used as a key for data generator callbacks.
 * <p>
 * This file also defines the built-in provider types, but third-party types can be created with {@link #registerProvider(String, ProviderType)}.
 *
 * @param <T> The type of the provider
 */
@FunctionalInterface
@ParametersAreNonnullByDefault
public interface ProviderType<T extends RegistrateProvider> extends GeneratorType<T> {

    // SERVER DATA
    ProviderType<RegistrateDatapackProvider> DYNAMIC = registerServerData("dynamic", RegistrateDatapackProvider::new);
    ProviderType<RegistrateDataMapProvider> DATA_MAP = registerServerData("data_map", RegistrateDataMapProvider::new);
    ProviderType<RegistrateRecipeRunner> RECIPE_RUNNER = registerServerData("recipe_runner", RegistrateRecipeRunner::new);
    ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = registerServerData("advancement", RegistrateAdvancementProvider::new);
    ProviderType<RegistrateLootTableProvider> LOOT = registerServerData("loot", RegistrateLootTableProvider::new);
    ProviderType<RegistrateTagsProvider.Impl<Block>> BLOCK_TAGS = registerTag("tags/block", "blocks", Registries.BLOCK);
    ProviderType<RegistrateTagsProvider.Impl<Enchantment>> ENCHANTMENT_TAGS = registerTag("tags/enchantment", "enchantments", Registries.ENCHANTMENT);
    ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerTag("tags/item", Registries.ITEM, c -> new RegistrateItemTagsProvider(c.parent(), c.type(), "items", c.output(), c.provider(), c.get(BLOCK_TAGS).contentsGetter()));
    ProviderType<RegistrateTagsProvider.Impl<Fluid>> FLUID_TAGS = registerTag("tags/fluid", "fluids", Registries.FLUID);
    ProviderType<RegistrateTagsProvider.Impl<EntityType<?>>> ENTITY_TAGS = registerTag("tags/entity", "entity_types", Registries.ENTITY_TYPE);
    ProviderType<RegistrateGenericProvider> GENERIC_SERVER = registerProvider("registrate_generic_server_provider",  c -> new RegistrateGenericProvider(c.parent(), c.event(), c.type()));

    // CLIENT DATA
    ProviderType<RegistrateModelProvider> MODEL = registerClientProvider("model", () -> c -> new RegistrateModelProvider(c.parent(), c.output()));
    ProviderType<RegistrateLangProvider> LANG = registerClientProvider("lang", () -> c -> new RegistrateLangProvider(c.parent(), c.output()));
    ProviderType<RegistrateGenericProvider> GENERIC_CLIENT = registerClientProvider("registrate_generic_client_provider", () -> c -> new RegistrateGenericProvider(c.parent(), c.event(), c.type()));

    GeneratorType<RegistrateRecipeProvider> RECIPE = RECIPE_RUNNER.createGenerator("recipe");
    GeneratorType<RegistrateBlockModelGenerator> BLOCKSTATE = MODEL.createGenerator("blockstate");
    GeneratorType<RegistrateItemModelGenerator> ITEM_MODEL = MODEL.createGenerator("item_model");

    record Context<T extends RegistrateProvider>(ProviderType<T> type, AbstractRegistrate<?> parent,
                                                 Map<ProviderType<?>, RegistrateProvider> existing,
                                                 PackOutput output,
                                                 CompletableFuture<HolderLookup.Provider> provider) {

        @SuppressWarnings("unchecked")
        public <R extends RegistrateProvider> R get(ProviderType<R> other) {
            return (R) existing().get(other);
        }

    }

    T create(Context<T> context);

    default <R> GeneratorType<R> createGenerator(String type) {
        return new GeneratorType<>() {
            public String toString(){
                return type;
            }
        };
    }

    interface SimpleServerDataFactory<T extends RegistrateProvider> extends ProviderType<T> {

        T create(AbstractRegistrate<?> parent, PackOutput output, CompletableFuture<HolderLookup.Provider> provider);

        @Override
        default T create(Context<T> context) {
            return create(context.parent(), context.output(), context.provider());
        }

        default ProviderType<T> asProvider() {
            return this;
        }

    }

    static <T extends RegistrateProvider> ProviderType<T> registerServerData(String name, SimpleServerDataFactory<T> factory) {
        return registerProvider(name, factory.asProvider());
    }

    static <T extends RegistrateProvider> ProviderType<T> registerProvider(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    static <T extends RegistrateProvider> ProviderType<T> registerClientProvider(String name, NonNullSupplier<ProviderType<T>> supplier) {
        if (!DatagenModLoader.isRunningDataGen()) return context -> null;
        var type = supplier.get();
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    @SuppressWarnings("unchecked")
    static <T, R extends RegistrateTagsProvider<T>> ProviderType<R> registerTag(String name, ResourceKey<? extends Registry<T>> key, ProviderType<R> type) {
        if (RegistrateDataProvider.TAG_TYPES.containsKey(key)) {
            return (ProviderType<R>) RegistrateDataProvider.TAG_TYPES.get(key);
        }
        RegistrateDataProvider.TAG_TYPES.put(key, type);
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }

    static <T> ProviderType<RegistrateTagsProvider.Impl<T>> registerTag(String providerName, String typeName, ResourceKey<Registry<T>> registry) {
        return registerTag(providerName, registry, c -> new RegistrateTagsProvider.Impl<>(c.parent(), c.type(), typeName, c.output(), registry, c.provider()));
    }

    static <T extends RegistrateProvider> T create(ProviderType<T> type, AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing, CompletableFuture<HolderLookup.Provider> provider) {
        return type.create(new Context<>(type, parent, existing, event.getGenerator().getPackOutput(), provider));
    }

}
