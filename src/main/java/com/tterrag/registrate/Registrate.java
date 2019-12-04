package com.tterrag.registrate;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
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

import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

/**
 * Manages all registrations and data generators for a mod.
 * <p>
 * Generally <em>not</em> thread-safe, as it holds the current name of the object being built statefully, and uses non-concurrent collections.
 * <p>
 * Begin a new object via {@link #object(String)}. This name will be used for all future entries until the next invocation of {@link #object(String)}. Alternatively, the methods that accept a name
 * parameter (such as {@link #block(String, Function)}) can be used. These do not affect the current name state.
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
public class Registrate {
    
    @Value
    private class Registration<R extends IForgeRegistryEntry<R>, T extends R> {
        ResourceLocation name;
        Class<? super R> type;
        Supplier<? extends T> creator;        
        RegistryObject<T> delegate;
        
        Registration(ResourceLocation name, Class<? super R> type, Supplier<? extends T> creator) {
            this.name = name;
            this.type = type;
            this.creator =  creator;
            this.delegate = RegistryObject.of(name, RegistryManager.ACTIVE.<R>getRegistry(type));
        }
        
        void register(IForgeRegistry<R> registry) {
            registry.register(creator.get().setRegistryName(name));
        }
    }
    
    /**
     * Create a new {@link Registrate} and register event listeners for registration and data generation. Used in lieu of adding side-effects to constructor, so that alternate initialization
     * strategies can be done in subclasses.
     * 
     * @param modid
     *            The mod ID for which objects will be registered
     * @return The {@link Registrate} instance
     */
    public static Registrate create(String modid) {
        return new Registrate(modid).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private final Table<String, Class<?>, Registration<?, ?>> registrations = HashBasedTable.create();
    private final Table<String, ProviderType<?>, Consumer<? extends RegistrateProvider>> datagens = HashBasedTable.create();

    /**
     * @return The mod ID that this {@link Registrate} is creating objects for
     */
    @Getter
    private final String modid;
    
    private String currentName;
    
    /**
     * Construct a new Registrate for the given mod ID.
     * 
     * @param modid
     *            The mod ID for which objects will be registered
     */
    protected Registrate(String modid) {
        this.modid = modid;
    }
    
    protected Registrate registerEventListeners(IEventBus bus) {
        bus.addListener(this::onRegister);
        bus.addListener(this::onData);
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    protected void onRegister(RegistryEvent.Register<?> event) {
        Class<?> type = event.getRegistry().getRegistrySuperType();
        if (type == null) {
            log.debug("Skipping unknown builder class " + event.getRegistry().getRegistrySuperType());
            return;
        }
        for (Entry<String, Registration<?, ?>> e : registrations.column(type).entrySet()) {
            e.getValue().register((IForgeRegistry) event.getRegistry());
        }
    }
    
    protected void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(new RegistrateDataProvider(this, modid, event));
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
     * @return A {@link RegistryObject} which will supply the requested entry, if it exists
     * @throws IllegalArgumentException
     *             if no such registration has been done
     */
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> get(Class<? super R> type) {
        return this.<R, T>get(currentName, type);
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
     * @return A {@link RegistryObject} which will supply the requested entry, if it exists
     * @throws IllegalArgumentException
     *             if no such registration has been done
     */
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> get(String name, Class<? super R> type) {
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
     * @return A {@link Collection} of {@link RegistryObject RegistryObjects} representing all known registered entries of the given type.
     */
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>> Collection<RegistryObject<R>> getAll(Class<? super R> type) {
        return registrations.column(type).values().stream().map(r -> (RegistryObject<R>) r.getDelegate()).collect(Collectors.toList());
    }
    
    public <T extends RegistrateProvider> void addDataGenerator(String name, ProviderType<T> type, Consumer<T> cons) {
        datagens.put(name, type, cons);
    }
    
    /**
     * For internal use, calls upon registered data generators to actually create their data.
     * 
     * @param <T>
     * @param type
     *            The type of provider to run
     * @param gen
     *            The provider
     */
    @SuppressWarnings("unchecked")
    public <T extends RegistrateProvider> void genData(ProviderType<T> type, T gen) {
        datagens.column(type).values().forEach(cons -> ((Consumer<T>)cons).accept(gen));
    }
    
    /**
     * Begin a new object, this is typically used at the beginning of a builder chain. The given name will be used until this method is called again. This makes it simple to create multiple entries
     * with the same name, as is often the case with blocks/items, items/entities, and blocks/TEs.
     * 
     * @param name
     *            The name to use for future entries
     * @return this {@link Registrate}
     */
    public Registrate object(String name) {
        this.currentName = name;
        return this;
    }
    
    /**
     * Apply a transformation to this {@link Registrate}. Useful to apply helper methods within a fluent chain, e.g.
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
     * @return this {@link Registrate}
     */
    public Registrate transform(UnaryOperator<Registrate> func) {
        return func.apply(this);
    }
    
    /**
     * Apply a transformation to this {@link Registrate}. Similar to {@link #transform(UnaryOperator)}, but for actions that return a builder in-progress. Useful to apply helper methods within a
     * fluent chain, e.g.
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
     * @param <T>
     * @param <P>
     * @param <S>
     * @param func
     *            The {@link Function function} to apply
     * @return the resultant {@link Builder}
     */
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> S transform(Function<Registrate, S> func) {
        return func.apply(this);
    }
    
    /**
     * Create a builder for a new entry. This is typically not needed, unless you are implementing a <a href="https://github.com/tterrag1098/Registrate/wiki/Custom-Builders">custom builder type</a>.
     * <p>
     * Uses the currently set name (via {@link #object(String)}) as the name for the new entry, and passes it to the factory as the first parameter.
     * 
     * @param <R>
     * @param <T>
     * @param <P>
     * @param <S>
     * @param factory
     *            The factory to create the builder
     * @return The {@link Builder} instance
     */
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> S entry(BiFunction<String, BuilderCallback, S> factory) {
        return entry(currentName, callback -> factory.apply(currentName, callback));
    }

    /**
     * Create a builder for a new entry. This is typically not needed, unless you are implementing a <a href="https://github.com/tterrag1098/Registrate/wiki/Custom-Builders">custom builder type</a>.
     * 
     * @param <R>
     * @param <T>
     * @param <P>
     * @param <S>
     * @param name
     *            The name to use for the entry
     * @param factory
     *            The factory to create the builder
     * @return The {@link Builder} instance
     */
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> S entry(String name, Function<BuilderCallback, S> factory) {
        return factory.apply(this::accept);
    }
    
    private <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> accept(String name, Class<? super R> type, Supplier<? extends T> creator) {
        Registration<R, T> reg = new Registration<>(new ResourceLocation(modid, name), type, creator);
        registrations.put(name, type, reg);
        return reg.getDelegate();
    }
    
    /* === Builder helpers === */
    
    // Items
    
    public <T extends Item> ItemBuilder<T, Registrate> item(Function<Item.Properties, T> factory) {
        return item(this, factory);
    }
    
    public <T extends Item> ItemBuilder<T, Registrate> item(String name, Function<Item.Properties, T> factory) {
        return item(this, name, factory);
    }
    
    public <T extends Item, P> ItemBuilder<T, P> item(P parent, Function<Item.Properties, T> factory) {
        return item(parent, currentName, factory);
    }
    
    public <T extends Item, P> ItemBuilder<T, P> item(P parent, String name, Function<Item.Properties, T> factory) {
        return entry(name, callback -> ItemBuilder.create(this, parent, name, callback, factory));
    }
    
    // Blocks
    
    public <T extends Block> BlockBuilder<T, Registrate> block(Function<Block.Properties, T> factory) {
        return block(this, factory);
    }
    
    public <T extends Block> BlockBuilder<T, Registrate> block(String name, Function<Block.Properties, T> factory) {
        return block(this, name, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, Function<Block.Properties, T> factory) {
        return block(parent, currentName, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, Function<Block.Properties, T> factory) {
        return entry(name, callback -> BlockBuilder.create(this, parent, name, callback, factory));
    }
    
    // Entities
    
    public <T extends Entity> EntityBuilder<T, Registrate> entity(EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(this, factory, classification);
    }
    
    public <T extends Entity> EntityBuilder<T, Registrate> entity(String name, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(this, name, factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(parent, currentName, factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, String name, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entry(name, callback -> EntityBuilder.create(this, parent, name, callback, factory, classification));
    }
    
    // Tile Entities
    
    public <T extends TileEntity> TileEntityBuilder<T, Registrate> tileEntity(Supplier<? extends T> factory) {
        return tileEntity(this, factory);
    }
    
    public <T extends TileEntity> TileEntityBuilder<T, Registrate> tileEntity(String name, Supplier<? extends T> factory) {
        return tileEntity(this, name, factory);
    }
    
    public <T extends TileEntity, P> TileEntityBuilder<T, P> tileEntity(P parent, Supplier<? extends T> factory) {
        return tileEntity(parent, currentName, factory);
    }
    
    public <T extends TileEntity, P> TileEntityBuilder<T, P> tileEntity(P parent, String name, Supplier<? extends T> factory) {
        return entry(name, callback -> TileEntityBuilder.create(this, parent, name, callback, factory));
    }
    
    // Fluids
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> fluid() {
        return fluid(this);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(this, stillTexture, flowingTexture);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return fluid(this, stillTexture, flowingTexture, attributesFactory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, Registrate> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            Function<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(this, stillTexture, flowingTexture, factory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, Registrate> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, Function<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(this, stillTexture, flowingTexture, attributesFactory, factory);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> fluid(String name) {
        return fluid(this, name);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(this, name, stillTexture, flowingTexture);
    }
    
    public FluidBuilder<ForgeFlowingFluid.Flowing, Registrate> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return fluid(this, name, stillTexture, flowingTexture, attributesFactory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, Registrate> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            Function<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(this, name, stillTexture, flowingTexture, factory);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<T, Registrate> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, Function<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(this, name, stillTexture, flowingTexture, attributesFactory, factory);
    }
        
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent) {
        return fluid(parent, currentName);
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(parent, currentName, stillTexture, flowingTexture);
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return fluid(parent, currentName, stillTexture, flowingTexture, attributesFactory);
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            Function<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(parent, currentName, stillTexture, flowingTexture, factory);
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, Function<ForgeFlowingFluid.Properties, T> factory) {
        return fluid(parent, currentName, stillTexture, flowingTexture, attributesFactory, factory);
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name) {
        return fluid(parent, name, new ResourceLocation(getModid(), "block/" + currentName + "_still"), new ResourceLocation(getModid(), "block/" + currentName + "_flow"));
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture));
    }
    
    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, attributesFactory));
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            Function<ForgeFlowingFluid.Properties, T> factory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, factory));
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, Function<ForgeFlowingFluid.Properties, T> factory) {
        return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, attributesFactory, factory));
    }
}
