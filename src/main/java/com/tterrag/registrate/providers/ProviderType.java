package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.FieldsAreNonnullByDefault;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
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
    public static final ProviderType<RegistrateRecipeProvider> RECIPE = register("recipe", (p, e) -> new RegistrateRecipeProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = register("advancement", (p, e) -> new RegistrateAdvancementProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateLootTableProvider> LOOT = register("loot", (p, e) -> new RegistrateLootTableProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateTagsProvider<Block>> BLOCK_TAGS = register("tags/block", type -> (p, e) -> new RegistrateTagsProvider<Block>(p, type, "blocks", e.getGenerator(), Registry.BLOCK, e.getExistingFileHelper()));
    public static final ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerDelegate("tags/item", type -> (p, e, existing) -> new RegistrateItemTagsProvider(p, type, "items", e.getGenerator(), e.getExistingFileHelper(), (RegistrateTagsProvider<Block>)existing.get(BLOCK_TAGS)));
    public static final ProviderType<RegistrateTagsProvider<Fluid>> FLUID_TAGS = register("tags/fluid", type -> (p, e) -> new RegistrateTagsProvider<Fluid>(p, type, "fluids", e.getGenerator(), Registry.FLUID, e.getExistingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider<EntityType<?>>> ENTITY_TAGS = register("tags/entity", type -> (p, e) -> new RegistrateTagsProvider<EntityType<?>>(p, type, "entity_types", e.getGenerator(), Registry.ENTITY_TYPE, e.getExistingFileHelper()));

    ProviderType<RegistrateTagsProvider<BannerPattern>> BANNER_PATTERN_TAGS = register("tags/banner_pattern", type -> (p, e) -> new RegistrateTagsProvider<BannerPattern>(p, type, "banner_pattern", e.getGenerator(), Registry.BANNER_PATTERN, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<Biome>> BIOME_TAGS = register("tags/worldgen/biome", type -> (p, e) -> new RegistrateTagsProvider<Biome>(p, type, "worldgen/biome", e.getGenerator(), BuiltinRegistries.BIOME, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<CatVariant>> CAT_VARIANT_TAGS = register("tags/cat_variant", type -> (p, e) -> new RegistrateTagsProvider<CatVariant>(p, type, "cat_variant", e.getGenerator(), Registry.CAT_VARIANT, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<FlatLevelGeneratorPreset>> FLAT_LEVEL_GENERATOR_PRESET_TAGS = register("tags/worldgen/flat_level_generator_preset", type -> (p, e) -> new RegistrateTagsProvider<FlatLevelGeneratorPreset>(p, type, "worldgen/flat_level_generator_preset", e.getGenerator(), BuiltinRegistries.FLAT_LEVEL_GENERATOR_PRESET, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<GameEvent>> GAME_EVENT_TAGS = register("tags/game_events", type -> (p, e) -> new RegistrateTagsProvider<GameEvent>(p, type, "game_events", e.getGenerator(), Registry.GAME_EVENT, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<Instrument>> INSTRUMENT_TAGS = register("tags/instrument", type -> (p, e) -> new RegistrateTagsProvider<Instrument>(p, type, "instrument", e.getGenerator(), Registry.INSTRUMENT, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<PaintingVariant>> PAINTING_VARIANT_TAGS = register("tags/painting_variant", type -> (p, e) -> new RegistrateTagsProvider<PaintingVariant>(p, type, "painting_variant", e.getGenerator(), Registry.PAINTING_VARIANT, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<PoiType>> POI_TYPE_TAGS = register("tags/point_of_interest_type", type -> (p, e) -> new RegistrateTagsProvider<PoiType>(p, type, "point_of_interest_type", e.getGenerator(), Registry.POINT_OF_INTEREST_TYPE, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<Structure>> STRUCTURE_TAGS = register("tags/worldgen/structure", type -> (p, e) -> new RegistrateTagsProvider<Structure>(p, type, "worldgen/structure", e.getGenerator(), BuiltinRegistries.STRUCTURES, e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider<WorldPreset>> WORLD_PRESET_TAGS = register("tags/worldgen/world_preset", type -> (p, e) -> new RegistrateTagsProvider<WorldPreset>(p, type, "worldgen/world_preset", e.getGenerator(), BuiltinRegistries.WORLD_PRESET, e.getExistingFileHelper()));
    // CLIENT DATA
    public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = register("blockstate", (p, e) -> new RegistrateBlockstateProvider(p, e.getGenerator(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = register("item_model", (p, e, existing) -> new RegistrateItemModelProvider(p, e.getGenerator(), ((RegistrateBlockstateProvider)existing.get(BLOCKSTATE)).getExistingFileHelper()));
    public static final ProviderType<RegistrateLangProvider> LANG = register("lang", (p, e) -> new RegistrateLangProvider(p, e.getGenerator()));

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