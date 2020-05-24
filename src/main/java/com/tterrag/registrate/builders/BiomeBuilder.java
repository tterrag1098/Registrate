package com.tterrag.registrate.builders;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;

/**
 * A builder for items, allows for customization of the {@link Biome.Builder biome properties}, and configuration of data associated with biomes (lang).
 * 
 * @param <T>
 *            The type of enchantment being built
 * @param <P>
 *            Parent object type
 */
public class BiomeBuilder<T extends Biome, P> extends AbstractBuilder<Biome, T, P, BiomeBuilder<T, P>> {

    /**
     * Create a new {@link BiomeBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The biome will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param factory
     *            Factory to create the biome
     * @return A new {@link BiomeBuilder} with reasonable default data generators.
     */
    public static <T extends Biome, P> BiomeBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Biome.Builder, T> factory) {
        return new BiomeBuilder<>(owner, parent, name, callback, factory)
                .defaultLang();
    }

    private final NonNullFunction<Biome.Builder, T> factory;
    
    private NonNullSupplier<Biome.Builder> initialProperties = Biome.Builder::new;
    private NonNullFunction<Biome.Builder, Biome.Builder> propertiesCallback = NonNullUnaryOperator.identity();
    
    @SuppressWarnings("null")
    private final EnumSet<BiomeManager.BiomeType> configuredTypes = EnumSet.noneOf(BiomeManager.BiomeType.class);

    protected BiomeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Biome.Builder, T> factory) {
        super(owner, parent, name, callback, Biome.class);
        this.factory = factory;
    }

    /**
     * Modify the properties of the biome. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     * <p>
     * If a different properties instance is returned, it will replace the existing one entirely.
     * 
     * @param func
     *            The action to perform on the properties
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> properties(NonNullUnaryOperator<Biome.Builder> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    /**
     * Replace the initial state of the biome properties, without replacing or removing any modifications done via {@link #properties(NonNullUnaryOperator)}.
     * 
     * @param properties
     *            A supplier to to create the initial properties
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> initialProperties(NonNullSupplier<Biome.Builder> properties) {
        initialProperties = properties;
        return this;
    }

    /**
     * Set the weight for this biome to generate in the overworld in regions matching the given type. This can only be called once per type.
     * 
     * @param type
     *            The type that controls which climates the biome will spawn in
     * @param weight
     *            The weight, or how common this biome should be in that climate
     * @return this {@link BiomeBuilder}
     * @see BiomeManager
     * @throws IllegalArgumentException
     *             if this type has already had its weight set
     */
    public BiomeBuilder<T, P> typeWeight(BiomeManager.BiomeType type, int weight) {
        if (!configuredTypes.add(type)) {
            throw new IllegalArgumentException("Cannot set a type weight more than once.");
        }
        this.onRegister(b -> BiomeManager.addBiome(type, new BiomeManager.BiomeEntry(b, weight)));
        return this;
    }

    /**
     * Add types to the {@link BiomeDictionary} for this biome. Can be called multiple times to add more types.
     * 
     * @param types
     *            The types to add
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> addDictionaryTypes(BiomeDictionary.Type... types) {
        this.onRegister(b -> BiomeDictionary.addTypes(b, types));
        return this;
    }

    /**
     * Manually add what would have been the "best guess" types from the {@link BiomeDictionary} for this biome. This has no effect if no types are added via
     * {@link #addDictionaryTypes(net.minecraftforge.common.BiomeDictionary.Type...)}.
     * 
     * @return this {@link BiomeBuilder}
     * @see BiomeDictionary#makeBestGuess(Biome)
     */
    public BiomeBuilder<T, P> forceAutomaticDictionaryTypes() {
        this.onRegister(BiomeDictionary::makeBestGuess);
        return this;
    }
    
    // TODO these methods need the ability to pull from another biome as a default

    /**
     * Add a callback that will be invoked after all {@link Feature Features} are registered, for the purpose of adding them to this biome.
     * <p>
     * Any {@link Feature} object can be safely referenced here and added to the biome via
     * {@link Biome#addFeature(net.minecraft.world.gen.GenerationStage.Decoration, net.minecraft.world.gen.feature.ConfiguredFeature)}
     * 
     * @param action
     *            A {@link NonNullConsumer} which will be called to add featuers to this biome.
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> addFeatures(NonNullConsumer<? super T> action) {
        this.<Feature<?>> onRegisterAfter(Feature.class, action);
        return this;
    }

    /**
     * Add a callback that will be invoked after all {@link WorldCarver WorldCarvers} are registered, for the purpose of adding them to this biome.
     * <p>
     * Any {@link WorldCarver} object can be safely referenced here and added to the biome via
     * {@link Biome#addCarver(net.minecraft.world.gen.GenerationStage.Carving, net.minecraft.world.gen.carver.ConfiguredCarver)}
     * 
     * @param action
     *            A {@link NonNullConsumer} which will be called to add carvers to this biome.
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> addCarvers(NonNullConsumer<? super T> action) {
        this.<WorldCarver<?>> onRegisterAfter(WorldCarver.class, action);
        return this;
    }

    /**
     * Add a callback that will be invoked after all {@link EntityType Entities} are registered, for the purpose of adding entity spawns to this biome.
     * <p>
     * Any {@link EntityType} object can be safely referenced here and added to the biome via {@link Biome#getSpawns(net.minecraft.entity.EntityClassification)}.
     * 
     * @param action
     *            A {@link NonNullConsumer} which will be called to add spawns to this biome.
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> addSpawns(NonNullConsumer<? super T> action) {
        // TODO this needs to be abstracted, adding multiple entity spawns is clunky.
        this.<EntityType<?>> onRegisterAfter(EntityType.class, action);
        return this;
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     * 
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> defaultLang() {
        return lang(Biome::getTranslationKey);
    }

    /**
     * Set the translation for this biome.
     * 
     * @param name
     *            A localized English name
     * @return this {@link BiomeBuilder}
     */
    public BiomeBuilder<T, P> lang(String name) {
        return lang(Biome::getTranslationKey, name);
    }

    @Override
    protected @NonnullType T createEntry() {
        @Nonnull Biome.Builder properties = this.initialProperties.get();
        properties = propertiesCallback.apply(properties);
        return factory.apply(properties);
    }
}
