package com.tterrag.registrate;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.TileEntityBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.DebugMarkers;
import com.tterrag.registrate.util.LazyValue;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

/**
 * Manages all registrations and data generators for a mod.
 * <p>
 * Generally <em>not</em> thread-safe, as it holds the current name of the object being built statefully, and uses non-concurrent collections.
 * <p>
 * Begin a new object via {@link #object(String)}. This name will be used for all future entries until the next invocation of {@link #object(String)}. Alternatively, the methods that accept a name
 * parameter (such as {@link #block(String, NonNullFunction)}) can be used. These do not affect the current name state.
 * <p>
 * A simple use may look like:
 * 
 * <pre>
 * {@code
 * public static final Registrate REGISTRATE = Registrate.create("mymod");
 * 
 * public static final RegistryObject<MyBlock> MY_BLOCK = REGISTRATE.object("my_block")
 *         .block(MyBlock::new)
 *         .defaultItem()
 *         .register();
 * }
 * </pre>
 * 
 * For specifics as to building different registry entries, read the documentation on their respective builders.
 */
@Log4j2
public abstract class AbstractRegistrate<S extends AbstractRegistrate<S>> {
    
    @Value
    private class Registration<R extends IForgeRegistryEntry<R>, T extends R> {
        ResourceLocation name;
        Class<? super R> type;
        Supplier<? extends T> creator;        
        RegistryEntry<T> delegate;
        
        Registration(ResourceLocation name, Class<? super R> type, Supplier<? extends T> creator) {
            this.name = name;
            this.type = type;
            this.creator =  creator;
            this.delegate = new RegistryEntry<>(AbstractRegistrate.this, RegistryObject.of(name, RegistryManager.ACTIVE.<R>getRegistry(type)));
        }
        
        void register(IForgeRegistry<R> registry) {
            registry.register(creator.get().setRegistryName(name));
        }
    }
    
    private final Table<String, Class<?>, Registration<?, ?>> registrations = HashBasedTable.create();
    private final Table<String, ProviderType<?>, Consumer<? extends RegistrateProvider>> datagensByEntry = HashBasedTable.create();
    private final ListMultimap<ProviderType<?>, Consumer<? extends RegistrateProvider>> datagens = ArrayListMultimap.create();

    /**
     * @return The mod ID that this {@link AbstractRegistrate} is creating objects for
     */
    @Getter
    private final String modid;
    
    @Nullable
    private String currentName;
    @Nullable
    private LazyValue<? extends ItemGroup> currentGroup;
    
    /**
     * Construct a new Registrate for the given mod ID.
     * 
     * @param modid
     *            The mod ID for which objects will be registered
     */
    protected AbstractRegistrate(String modid) {
        this.modid = modid;
    }
    
    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }
    
    protected S registerEventListeners(IEventBus bus) {
        bus.addListener(this::onRegister);
        bus.addListener(this::onData);
        return self();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    protected void onRegister(RegistryEvent.Register<?> event) {
        Class<?> type = event.getRegistry().getRegistrySuperType();
        if (type == null) {
            log.debug("Skipping unknown builder class " + event.getRegistry().getRegistrySuperType());
            return;
        }
        Map<String, Registration<?, ?>> registrationsForType = registrations.column(type);
        if (registrationsForType.size() > 0) {
            log.debug(DebugMarkers.REGISTER, "Registering {} known objects of type {}", registrationsForType.size(), type.getName());
            for (Entry<String, Registration<?, ?>> e : registrationsForType.entrySet()) {
                e.getValue().register((IForgeRegistry) event.getRegistry());
                log.debug(DebugMarkers.REGISTER, "Registered {} to registry {}", e.getValue().getName(), event.getRegistry().getRegistryName());
            }
        }
    }
    
    protected void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(new RegistrateDataProvider(this, modid, event));
    }

    /**
     * Get the current name (from the last call to {@link #object(String)}), throwing an exception if it is not set.
     * 
     * @return The current entry name
     * @throws NullPointerException
     *             if {@link #currentName} is null
     */
    protected String currentName() {
        String name = currentName;
        Objects.requireNonNull(name, "Current name not set");
        return name;
    }
    
    /**
     * Allows retrieval of a previously created entry, of the current name (from the last invocation of {@link #object(String)}. Useful to retrieve a different entry than the final state of your
     * chain may produce, e.g.
     * 
     * <pre>
     * {@code
     * public static final RegistryObject<BlockItem> MY_BLOCK_ITEM = REGISTRATE.object("my_block")
     *         .block(MyBlock::new)
     *             .defaultItem()
     *             .lang("My Special Block")
     *             .build()
     *         .get(Item.class);
     * }
     * </pre>
     * 
     * @param <R>
     *            The type of the registry for which to retrieve the entry
     * @param <T>
     *            The type of the entry to return
     * @param type
     *            A class representing the registry type
     * @return A {@link RegistryEntry} which will supply the requested entry, if it exists
     * @throws IllegalArgumentException
     *             if no such registration has been done
     * @throws NullPointerException 
     *             if current name has not been set via {@link #object(String)}
     */
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> get(Class<? super R> type) {
        return this.<R, T>get(currentName(), type);
    }

    /**
     * Allows retrieval of a previously created entry. Useful to retrieve arbitrary entries that may have been created as side-effects of earlier registrations.
     * 
     * <pre>
     * {@code
     * public static final RegistryObject<MyBlock> MY_BLOCK = REGISTRATE.object("my_block")
     *         .block(MyBlock::new)
     *             .defaultItem()
     *             .register();
     * 
     * ...
     * 
     * public static final RegistryObject<BlockItem> MY_BLOCK_ITEM = REGISTRATE.get("my_block", Item.class);
     * }
     * </pre>
     * 
     * @param <R>
     *            The type of the registry for which to retrieve the entry
     * @param <T>
     *            The type of the entry to return
     * @param name
     *            The name of the registry entry to request
     * @param type
     *            A class representing the registry type
     * @return A {@link RegistryEntry} which will supply the requested entry, if it exists
     * @throws IllegalArgumentException
     *             if no such registration has been done
     */
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> get(String name, Class<? super R> type) {
        Registration<R, T> reg = (Registration<R, T>) registrations.get(name, type);
        if (reg != null) {
            return reg.getDelegate();
        }
        throw new IllegalArgumentException("Unknown registration " + name + " for type " + type);
    }
    
    /**
     * Get all registered entries of a given registry type. Used internally for data generator scope, but can be used for post-processing of registered entries.
     * 
     * @param <R>
     *            The type of the registry for which to retrieve the entries
     * @param type
     *            A class representing the registry type
     * @return A {@link Collection} of {@link RegistryEntry RegistryEntries} representing all known registered entries of the given type.
     */
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>> Collection<RegistryEntry<R>> getAll(Class<? super R> type) {
        return registrations.column(type).values().stream().map(r -> (RegistryEntry<R>) r.getDelegate()).collect(Collectors.toList());
    }

    /**
     * Mostly internal, sets the data generator for a certain entry/type combination. This will replace an existing data gen callback if it exists.
     * 
     * @param <T>
     *            The type of provider
     * @param entry
     *            The name of the entry which the provider is for
     * @param type
     *            The {@link ProviderType} to generate data for
     * @param cons
     *            A callback to be invoked during data generation
     */
    public <T extends RegistrateProvider> void setDataGenerator(String entry, ProviderType<T> type, NonNullConsumer<? extends T> cons) {
        Consumer<? extends RegistrateProvider> existing = datagensByEntry.put(entry, type, cons);
        if (existing != null) {
            datagens.remove(type, existing);
        }
        addDataGenerator(type, cons);
    }
    
    /**
     * Add a data generator callback that is not associated with any entry, which can never replace an existing data generator.
     * <p>
     * This is useful to add data generator callbacks for miscellaneous data not strictly associated with an entry.
     * 
     * @param <T>
     *            The type of provider
     * @param type
     *            The {@link ProviderType} to generate data for
     * @param cons
     *            A callback to be invoked during data generation
     */
    public <T extends RegistrateProvider> void addDataGenerator(ProviderType<T> type, Consumer<? extends T> cons) {
        datagens.put(type, cons);
    }
    
    /**
     * Add a custom translation mapping, prepending this registrate's {@link #getModid() mod id} to the translation key.
     * 
     * @param key
     *            The translation key
     * @param value
     *            The (English) translation value
     * @return A {@link TranslationTextComponent} representing the translated text
     */
    public TranslationTextComponent addLang(String key, String value) {
        final String prefixedKey = getModid() + "." + key;
        addDataGenerator(ProviderType.LANG, p -> p.add(prefixedKey, value));
        return new TranslationTextComponent(prefixedKey);
    }
    
    /**
     * For internal use, calls upon registered data generators to actually create their data.
     * 
     * @param <T>
     *            The type of the provider
     * @param type
     *            The type of provider to run
     * @param gen
     *            The provider
     */
    @SuppressWarnings("unchecked")
    public <T extends RegistrateProvider> void genData(ProviderType<T> type, T gen) {
        datagens.get(type).forEach(cons -> {
            if (log.isEnabled(Level.DEBUG, DebugMarkers.DATA)) {
                String entry = null;
                for (Map.Entry<String, Consumer<? extends RegistrateProvider>> e : datagensByEntry.column(type).entrySet()) {
                    if (e.getValue() == cons) {
                        entry = e.getKey();
                        break;
                    }
                }
                if (entry != null) {
                    log.debug(DebugMarkers.DATA, "Generating data of type {} for entry {}", RegistrateDataProvider.getTypeName(type), entry);
                } else {
                    log.debug(DebugMarkers.DATA, "Generating unassociated data of type {} ({})", RegistrateDataProvider.getTypeName(type), type);
                }
            }
            ((Consumer<T>)cons).accept(gen);
        });
    }
    
    /**
     * Begin a new object, this is typically used at the beginning of a builder chain. The given name will be used until this method is called again. This makes it simple to create multiple entries
     * with the same name, as is often the case with blocks/items, items/entities, and blocks/TEs.
     * 
     * @param name
     *            The name to use for future entries
     * @return this {@link AbstractRegistrate}
     */
    public S object(String name) {
        this.currentName = name;
        return self();
    }

    /**
     * Set the default item group for all future items created with this Registrate, until the next time this method is called. The supplier will only be called once, and the value re-used for each
     * entry.
     * 
     * @param group
     *            The group to use for future items
     * @return this {@link AbstractRegistrate}
     */
    public S itemGroup(NonNullSupplier<? extends ItemGroup> group) {
        this.currentGroup = new LazyValue<>(group);
        return self();
    }

    /**
     * Set the default item group for all future items created with this Registrate, until the next time this method is called. The supplier will only be called once, and the value re-used for each
     * entry.
     * <p>
     * Additionally, add a translation for the item group.
     * 
     * @param group
     *            The group to use for future items
     * @param localizedName
     *            The english name to use for the item group title
     * @return this {@link AbstractRegistrate}
     */
    public S itemGroup(NonNullSupplier<? extends ItemGroup> group, String localizedName) {
        this.addDataGenerator(ProviderType.LANG, prov -> prov.add(group.get(), localizedName));
        return itemGroup(group);
    }
    
    /**
     * Apply a transformation to this {@link AbstractRegistrate}. Useful to apply helper methods within a fluent chain, e.g.
     * 
     * <pre>
     * {@code
     * public static final RegistryObject<MyBlock> MY_BLOCK = REGISTRATE.object("my_block")
     *         .transform(Utils::createMyBlock)
     *         .get(Block.class);
     * }
     * </pre>
     * 
     * @param func
     *            The {@link UnaryOperator function} to apply
     * @return this {@link AbstractRegistrate}
     */
    public S transform(NonNullUnaryOperator<S> func) {
        return func.apply(self());
    }
    
    /**
     * Apply a transformation to this {@link AbstractRegistrate}. Similar to {@link #transform(NonNullUnaryOperator)}, but for actions that return a builder in-progress. Useful to apply helper methods
     * within a fluent chain, e.g.
     * 
     * <pre>
     * {@code
     * public static final RegistryObject<MyBlock> MY_BLOCK = REGISTRATE.object("my_block")
     *         .transform(Utils::createMyBlock)
     *         .lang("My Block") // Can modify the builder afterwards
     *         .register();
     * }
     * </pre>
     * 
     * @param <R>
     *            Registry type
     * @param <T>
     *            Entry type
     * @param <P>
     *            Parent type
     * @param <S2>
     *            Self type
     * @param func
     *            The {@link Function function} to apply
     * @return the resultant {@link Builder}
     */
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 transform(NonNullFunction<S, S2> func) {
        return func.apply(self());
    }
    
    /**
     * Create a builder for a new entry. This is typically not needed, unless you are implementing a <a href="https://github.com/tterrag1098/Registrate/wiki/Custom-Builders">custom builder type</a>.
     * <p>
     * Uses the currently set name (via {@link #object(String)}) as the name for the new entry, and passes it to the factory as the first parameter.
     * 
     * @param <R>
     *            Registry type
     * @param <T>
     *            Entry type
     * @param <P>
     *            Parent type
     * @param <S2>
     *            Self type
     * @param factory
     *            The factory to create the builder
     * @return The {@link Builder} instance
     */
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(NonNullBiFunction<String, BuilderCallback, S2> factory) {
        return entry(currentName(), callback -> factory.apply(currentName(), callback));
    }

    /**
     * Create a builder for a new entry. This is typically not needed, unless you are implementing a <a href="https://github.com/tterrag1098/Registrate/wiki/Custom-Builders">custom builder type</a>.
     * 
     * @param <R>
     *            Registry type
     * @param <T>
     *            Entry type
     * @param <P>
     *            Parent type
     * @param <S2>
     *            Self type
     * @param name
     *            The name to use for the entry
     * @param factory
     *            The factory to create the builder
     * @return The {@link Builder} instance
     */
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(String name, NonNullFunction<BuilderCallback, S2> factory) {
        return factory.apply(this::accept);
    }
    
    private <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, Class<? super R> type, NonNullSupplier<? extends T> creator) {
        Registration<R, T> reg = new Registration<>(new ResourceLocation(modid, name), type, creator);
        log.debug(DebugMarkers.REGISTER, "Captured registration for entry {} of type {}", name, type.getName());
        registrations.put(name, type, reg);
        return reg.getDelegate();
    }
    
    /* === Builder helpers === */
    
    // Items
    
    public <T extends Item> ItemBuilder<T, S> item(NonNullFunction<Item.Properties, T> factory) {
        return item(self(), factory);
    }
    
    public <T extends Item> ItemBuilder<T, S> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return item(self(), name, factory);
    }
    
    public <T extends Item, P> ItemBuilder<T, P> item(P parent, NonNullFunction<Item.Properties, T> factory) {
        return item(parent, currentName(), factory);
    }
    
    public <T extends Item, P> ItemBuilder<T, P> item(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
        return entry(name, callback -> ItemBuilder.create(this, parent, name, callback, factory, this.currentGroup));
    }
    
    // Blocks
    
    public <T extends Block> BlockBuilder<T, S> block(NonNullFunction<Block.Properties, T> factory) {
        return block(self(), factory);
    }
    
    public <T extends Block> BlockBuilder<T, S> block(String name, NonNullFunction<Block.Properties, T> factory) {
        return block(self(), name, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, NonNullFunction<Block.Properties, T> factory) {
        return block(parent, currentName(), factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, NonNullFunction<Block.Properties, T> factory) {
        return block(parent, name, factory, Material.ROCK);
    }
    
    public <T extends Block> BlockBuilder<T, S> block(Material material, NonNullFunction<Block.Properties, T> factory) {
        return block(self(), factory);
    }
    
    public <T extends Block> BlockBuilder<T, S> block(String name, Material material, NonNullFunction<Block.Properties, T> factory) {
        return block(self(), name, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, Material material, NonNullFunction<Block.Properties, T> factory) {
        return block(parent, currentName(), factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, NonNullFunction<Block.Properties, T> factory, Material material) {
        return entry(name, callback -> BlockBuilder.create(this, parent, name, callback, factory, material));
    }
    
    // Entities
    
    public <T extends Entity> EntityBuilder<T, S> entity(EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(self(), factory, classification);
    }
    
    public <T extends Entity> EntityBuilder<T, S> entity(String name, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(self(), name, factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(parent, currentName(), factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, String name, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entry(name, callback -> EntityBuilder.create(this, parent, name, callback, factory, classification));
    }
    
    // Tile Entities
    
    public <T extends TileEntity> TileEntityBuilder<T, S> tileEntity(NonNullSupplier<? extends T> factory) {
        return tileEntity(self(), factory);
    }
    
    public <T extends TileEntity> TileEntityBuilder<T, S> tileEntity(String name, NonNullSupplier<? extends T> factory) {
        return tileEntity(self(), name, factory);
    }
    
    public <T extends TileEntity, P> TileEntityBuilder<T, P> tileEntity(P parent, NonNullSupplier<? extends T> factory) {
        return tileEntity(parent, currentName(), factory);
    }
    
    public <T extends TileEntity, P> TileEntityBuilder<T, P> tileEntity(P parent, String name, NonNullSupplier<? extends T> factory) {
        return entry(name, callback -> TileEntityBuilder.create(this, parent, name, callback, factory));
    }
    
    // Fluids
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid() {
        return fluid(self());
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(self(), stillTexture, flowingTexture);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return fluid(self(), stillTexture, flowingTexture, attributesFactory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(self(), stillTexture, flowingTexture, factory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(self(), stillTexture, flowingTexture, attributesFactory, factory);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name) {
        return fluid(self(), name);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(self(), name, stillTexture, flowingTexture);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return fluid(self(), name, stillTexture, flowingTexture, attributesFactory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(self(), name, stillTexture, flowingTexture, factory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(self(), name, stillTexture, flowingTexture, attributesFactory, factory);
    }
        
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent) {
        return fluid(parent, currentName());
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(parent, currentName(), stillTexture, flowingTexture);
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, attributesFactory);
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, factory);
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, attributesFactory, factory);
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name) {
        return fluid(parent, name, new ResourceLocation(getModid(), "block/" + currentName() + "_still"), new ResourceLocation(getModid(), "block/" + currentName() + "_flow"));
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture));
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, attributesFactory));
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, factory));
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, attributesFactory, factory));
    }
}
