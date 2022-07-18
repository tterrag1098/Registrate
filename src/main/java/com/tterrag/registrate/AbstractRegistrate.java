package com.tterrag.registrate;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;
import com.tterrag.registrate.builders.*;
import com.tterrag.registrate.builders.factory.*;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.DebugMarkers;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.Message;

import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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
    private class Registration<R, T extends R> {
        ResourceLocation name;
        ResourceKey<? extends Registry<R>> type;
        NonNullSupplier<? extends T> creator;        
        RegistryEntry<T> delegate;
        
        @Getter(value = AccessLevel.NONE)
        List<NonNullConsumer<? super T>> callbacks = new ArrayList<>();

        Registration(ResourceLocation name, ResourceKey<? extends Registry<R>> type, NonNullSupplier<? extends T> creator, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
            this.name = name;
            this.type = type;
            this.creator =  creator.lazy();
            this.delegate = entryFactory.apply(RegistryObject.create(name, type.location(), AbstractRegistrate.this.getModid()));
        }
        
        void register(IForgeRegistry<R> registry) {
            T entry = creator.get();
            registry.register(name, entry);
            delegate.updateReference(registry);
            callbacks.forEach(c -> c.accept(entry));
            callbacks.clear();
        }
        
        void addRegisterCallback(NonNullConsumer<? super T> callback) {
            Preconditions.checkNotNull(callback, "Callback must not be null");
            callbacks.add(callback);
        }
    }

    public static boolean isDevEnvironment() {
        return FMLEnvironment.naming.equals("mcp");
    }

    private final Table<String, ResourceKey<? extends Registry<?>>, Registration<?, ?>> registrations = HashBasedTable.create();
    /** Expected to be emptied by the time registration occurs, is emptied by {@link #accept(String, Class, Builder, NonNullSupplier, NonNullFunction)} */
    private final Multimap<Pair<String, ResourceKey<? extends Registry<?>>>, NonNullConsumer<?>> registerCallbacks = HashMultimap.create();
    /** Entry-less callbacks that are invoked after the registry type has completely finished */
    private final Multimap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks = HashMultimap.create();
    private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new HashSet<>();

    private final Table<Pair<String, ResourceKey<? extends Registry<?>>>, ProviderType<?>, Consumer<? extends RegistrateProvider>> datagensByEntry = HashBasedTable.create();
    private final ListMultimap<ProviderType<?>, @NonnullType NonNullConsumer<? extends RegistrateProvider>> datagens = ArrayListMultimap.create();
    
    private final NonNullSupplier<Boolean> doDatagen = NonNullSupplier.lazy(DatagenModLoader::isRunningDataGen);

    /**
     * @return The mod ID that this {@link AbstractRegistrate} is creating objects for
     */
    @Getter
    private final String modid;
    
    @Nullable
    private String currentName;
    @Nullable
    private Supplier<? extends @NonnullType CreativeModeTab> currentTab;
    private boolean skipErrors;
    
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

        // Register events fire multiple times, so clean them up on common setup
        try {
            Consumer<RegisterEvent> onRegister = this::onRegister;
            Consumer<RegisterEvent> onRegisterLate = this::onRegisterLate;
            bus.addListener(onRegister);
            bus.addListener(EventPriority.LOWEST, onRegisterLate);
            OneTimeEventReceiver.addListener(bus, FMLCommonSetupEvent.class, $ -> {
                OneTimeEventReceiver.unregister(bus, onRegister, RegisterEvent.class);
                OneTimeEventReceiver.unregister(bus, onRegisterLate, RegisterEvent.class);
            });
        } catch (IllegalArgumentException e) {
//            log.info("Detected new forge version, registering events reflectively.");
//            bus.register(proxy);
//            OneTimeEventReceiver.addListener(bus, FMLCommonSetupEvent.class, $ ->
//                OneTimeEventReceiver.unregister(bus, proxy, dummyEvent));
        }

        if (doDatagen.get()) {
            OneTimeEventReceiver.addListener(bus, GatherDataEvent.class, this::onData);
        }

        return self();
    }

    protected void onRegister(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        if (type == null) {
            log.debug(DebugMarkers.REGISTER, "Skipping invalid registry with no supertype: " + event.getRegistryKey());
            return;
        }
        if (!registerCallbacks.isEmpty()) {
            registerCallbacks.asMap().forEach((k, v) -> log.warn("Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?", v.size(), k.getLeft(), k.getRight().location()));
            registerCallbacks.clear();
            if (isDevEnvironment()) {
                throw new IllegalStateException("Found unused register callbacks, see logs");
            }
        }
        Map<String, Registration<?, ?>> registrationsForType = registrations.column(type);
        if (registrationsForType.size() > 0) {
            log.debug(DebugMarkers.REGISTER, "Registering {} known objects of type {}", registrationsForType.size(), type.location());
            for (Entry<String, Registration<?, ?>> e : registrationsForType.entrySet()) {
                try {
                    e.getValue().register(event.getForgeRegistry());
                    log.debug(DebugMarkers.REGISTER, "Registered {} to registry {}", e.getValue().getName(), event.getRegistryKey());
                } catch (Exception ex) {
                    String err = "Unexpected error while registering entry " + e.getValue().getName() + " to registry " + event.getRegistryKey();
                    if (skipErrors) {
                        log.error(DebugMarkers.REGISTER, err);
                    } else {
                        throw new RuntimeException(err, ex);
                    }
                }
            }
        }
    }

    protected void onRegisterLate(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        Collection<Runnable> callbacks = afterRegisterCallbacks.get(type);
        callbacks.forEach(Runnable::run);
        callbacks.clear();
        completedRegistrations.add(type);
    }

    @Nullable
    private RegistrateDataProvider provider;
    
    protected void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(true, provider = new RegistrateDataProvider(this, modid, event));
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
    public <R, T extends R> RegistryEntry<T> get(ResourceKey<? extends Registry<R>> type) {
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
    public <R, T extends R> RegistryEntry<T> get(String name, ResourceKey<? extends Registry<R>> type) {
        return this.<R, T>getRegistration(name, type).getDelegate();
    }

    @Beta
    public <R, T extends R> RegistryEntry<T> getOptional(String name, ResourceKey<? extends Registry<R>> type) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, type);
        return reg == null ? RegistryEntry.empty() : reg.getDelegate();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <R, T extends R> Registration<R, T> getRegistrationUnchecked(String name, ResourceKey<? extends Registry<R>> type) {    
        return (Registration<R, T>) registrations.get(name, type);
    }
    
    private <R, T extends R> Registration<R, T> getRegistration(String name, ResourceKey<? extends Registry<R>> type) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, type);
        if (reg != null) {
            return reg;
        }
        throw new IllegalArgumentException("Unknown registration " + name + " for type " + type);
    }

    @SuppressWarnings({ "null", "unchecked" })
    public <R> Collection<RegistryEntry<R>> getAll(ResourceKey<? extends Registry<R>> type) {
        return registrations.column(type).values().stream().map(r -> (RegistryEntry<R>) r.getDelegate()).collect(Collectors.toList());
    }

    public <R, T extends R> S addRegisterCallback(String name, ResourceKey<? extends Registry<R>> registryType, NonNullConsumer<? super T> callback) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, registryType);
        if (reg == null) {
            registerCallbacks.put(Pair.of(name, registryType), (NonNullConsumer<?>) callback);
        } else {
            reg.addRegisterCallback(callback);
        }
        return self();
    }

    public <R> S addRegisterCallback(ResourceKey<? extends Registry<R>> registryType, Runnable callback) {
        afterRegisterCallbacks.put((ResourceKey<? extends Registry<?>>) registryType, callback);
        return self();
    }

    public <R> boolean isRegistered(ResourceKey<? extends Registry<R>> registryType) {
        return completedRegistrations.contains(registryType);
    }

    /**
     * Get the data provider instance for a given {@link ProviderType}. Only works within datagen context, not during registration or init.
     * 
     * @param <P>
     *            The type of the provider
     * @param type
     *            A {@link ProviderType} representing the desired provider
     * @return An {@link Optional} holding the provider, or empty if this provider was not registered. This can happen if datagen is run only for client or server providers.
     * @throws IllegalStateException
     *             if datagen has not started yet
     */
    public <P extends RegistrateProvider> Optional<P> getDataProvider(ProviderType<P> type) {
        RegistrateDataProvider provider = this.provider;
        if (provider != null) {
            return provider.getSubProvider(type);
        }
        throw new IllegalStateException("Cannot get data provider before datagen is started");
    }

    /**
     * Mostly internal, sets the data generator for a certain entry/type combination. This will replace an existing data gen callback if it exists.
     * 
     * @param <P>
     *            The type of provider
     * @param <R>
     *            The registry type
     * @param builder
     *            The builder for the entry
     * @param type
     *            The {@link ProviderType} to generate data for
     * @param cons
     *            A callback to be invoked during data generation
     * @return this {@link AbstractRegistrate}
     */
    public <P extends RegistrateProvider, R> S setDataGenerator(Builder<S, R, ?, ?, ?> builder, ProviderType<P> type, NonNullConsumer<? extends P> cons) {
        return this.<P, R>setDataGenerator(builder.getName(), builder.getRegistryKey(), type, cons);
    }

    /**
     * Mostly internal, sets the data generator for a certain entry/type combination. This will replace an existing data gen callback if it exists.
     * 
     * @param <P>
     *            The type of provider
     * @param <R>
     *            The registry type
     * @param entry
     *            The name of the entry which the provider is for
     * @param registryType
     *            A {@link Class} representing the registry type of the entry
     * @param type
     *            The {@link ProviderType} to generate data for
     * @param cons
     *            A callback to be invoked during data generation
     * @return this {@link AbstractRegistrate}
     */
    public <P extends RegistrateProvider, R> S setDataGenerator(String entry, ResourceKey<? extends Registry<R>> registryType, ProviderType<P> type, NonNullConsumer<? extends P> cons) {
        if (!doDatagen.get()) return self();
        @SuppressWarnings("null")
        Consumer<? extends RegistrateProvider> existing = datagensByEntry.put(Pair.of(entry, registryType), type, cons);
        if (existing != null) {
            datagens.remove(type, existing);
        }
        return addDataGenerator(type, cons);
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
     * @return this {@link AbstractRegistrate}
     */
    public <T extends RegistrateProvider> S addDataGenerator(ProviderType<? extends T> type, NonNullConsumer<? extends T> cons) {
        if (doDatagen.get()) {
            datagens.put(type, cons);
        }
        return self();
    }
    
    private final NonNullSupplier<List<Pair<String, String>>> extraLang = NonNullSupplier.lazy(() -> {
        final List<Pair<String, String>> ret = new ArrayList<>();
        addDataGenerator(ProviderType.LANG, prov -> ret.forEach(p -> prov.add(p.getKey(), p.getValue())));
        return ret;
    });

    /**
     * Add a custom translation mapping using the vanilla style of ResourceLocation -&gt; translation key conversion.
     * 
     * @param type
     *            Type of the object, this is used as a prefix (e.g. {@code ["block", "mymod:myblock"] -> "block.mymod.myblock"})
     * @param id
     *            ID of the object, which will be converted to a lang key via {@link Util#makeDescriptionId(String, ResourceLocation)}
     * @param localizedName
     *            (English) translation value
     * @return A {@link MutableComponent} representing the translated text
     */
    public MutableComponent addLang(String type, ResourceLocation id, String localizedName) {
        return addRawLang(Util.makeDescriptionId(type, id), localizedName);
    }
    
    /**
     * Add a custom translation mapping using the vanilla style of ResourceLocation -&gt; translation key conversion. Also appends a suffix to the key.
     * 
     * @param type
     *            Type of the object, this is used as a prefix (e.g. {@code ["block", "mymod:myblock"] -> "block.mymod.myblock"})
     * @param id
     *            ID of the object, which will be converted to a lang key via {@link Util#makeDescriptionId(String, ResourceLocation)}
     * @param suffix
     *            A suffix which will be appended to the generated key (separated by a dot)
     * @param localizedName
     *            (English) translation value
     * @return A {@link MutableComponent} representing the translated text
     */
    public MutableComponent addLang(String type, ResourceLocation id, String suffix, String localizedName) {
        return addRawLang(Util.makeDescriptionId(type, id) + "." + suffix, localizedName);
    }

    /**
     * Add a custom translation mapping directly to the lang provider.
     * 
     * @param key
     *            The translation key
     * @param value
     *            The (English) translation value
     * @return A {@link MutableComponent} representing the translated text
     */
    public MutableComponent addRawLang(String key, String value) {
        if (doDatagen.get()) {
            extraLang.get().add(Pair.of(key, value));
        }
        return Component.translatable(key);
    }
    
    @SuppressWarnings("null")
    private Optional<Pair<String, ResourceKey<? extends Registry<?>>>> getEntryForGenerator(ProviderType<?> type, NonNullConsumer<? extends RegistrateProvider> generator) {
        for (Map.Entry<Pair<String, ResourceKey<? extends Registry<?>>>, Consumer<? extends RegistrateProvider>> e : datagensByEntry.column(type).entrySet()) {
            if (e.getValue() == generator) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
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
    public <T extends RegistrateProvider> void genData(ProviderType<? extends T> type, T gen) {
        if (!doDatagen.get()) return;
        datagens.get(type).forEach(cons -> {
            Optional<Pair<String, ResourceKey<? extends Registry<?>>>> entry = null;
            if (log.isEnabled(Level.DEBUG, DebugMarkers.DATA)) {
                entry = getEntryForGenerator(type, cons);
                if (entry.isPresent()) {
                    log.debug(DebugMarkers.DATA, "Generating data of type {} for entry {} [{}]", RegistrateDataProvider.getTypeName(type), entry.get().getLeft(), entry.get().getRight().location());
                } else {
                    log.debug(DebugMarkers.DATA, "Generating unassociated data of type {} ({})", RegistrateDataProvider.getTypeName(type), type);
                }
            }
            try {
                ((Consumer<T>) cons).accept(gen);
            } catch (Exception e) {
                if (entry == null) {
                    entry = getEntryForGenerator(type, cons);
                }
                Message err;
                if (entry.isPresent()) {
                    err = log.getMessageFactory().newMessage("Unexpected error while running data generator of type {} for entry {} [{}]", RegistrateDataProvider.getTypeName(type), entry.get().getLeft(), entry.get().getRight().location());
                } else {
                    err = log.getMessageFactory().newMessage("Unexpected error while running unassociated data generator of type {} ({})", RegistrateDataProvider.getTypeName(type), type);
                }
                if (skipErrors) {
                    log.error(err);
                } else {
                    throw new RuntimeException(err.getFormattedMessage(), e);
                }
            }
        });
    }

    /**
     * Enable skipping of registry entries and data generators that error during registration/generation.
     * <p>
     * <strong>Should only be used for debugging!</strong> {@code skipErrors(true)} will do nothing outside of a dev environment.
     * 
     * @param skipErrors
     *            {@code true} to skip errors during registration/generation
     * @return this {@link AbstractRegistrate}
     */
    public S skipErrors(boolean skipErrors) {
        if (skipErrors && !isDevEnvironment()) {
            log.error("Ignoring skipErrors(true) as this is not a development environment!");
        } else {
            this.skipErrors = skipErrors;
        }
        return self();
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
     * Set the default creative mode tab for all future items created with this Registrate, until the next time this method is called. The supplier will only be called once, and the value re-used for each
     * entry.
     * 
     * @param tab
     *            The tab to use for future items
     * @return this {@link AbstractRegistrate}
     */
    public S creativeModeTab(NonNullSupplier<? extends CreativeModeTab> tab) {
        this.currentTab = Suppliers.memoize(tab::get);
        return self();
    }

    /**
     * Set the default creative mode tab for all future items created with this Registrate, until the next time this method is called. The supplier will only be called once, and the value re-used for each
     * entry.
     * <p>
     * Additionally, add a translation for the creative mode tab.
     * 
     * @param tab
     *            The tab to use for future items
     * @param localizedName
     *            The english name to use for the creative mode tab title
     * @return this {@link AbstractRegistrate}
     */
    public S creativeModeTab(NonNullSupplier<? extends CreativeModeTab> tab, String localizedName) {
        this.addDataGenerator(ProviderType.LANG, prov -> prov.add(tab.get(), localizedName));
        return creativeModeTab(tab);
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
    public <R, T extends R, P, S2 extends Builder<S, R, T, P, S2>> S2 transform(NonNullFunction<S, S2> func) {
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
    public <R, T extends R, P, S2 extends Builder<S, R, T, P, S2>> S2 entry(NonNullBiFunction<String, BuilderCallback<S>, S2> factory) {
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
    public <R, T extends R, P, S2 extends Builder<S, R, T, P, S2>> S2 entry(String name, NonNullFunction<BuilderCallback<S>, S2> factory) {
        return factory.apply(this::accept);
    }

    protected <R, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type, Builder<S, R, T, ?, ?> builder, NonNullSupplier<? extends T> creator, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
        Registration<R, T> reg = new Registration<>(new ResourceLocation(modid, name), type, creator, entryFactory);
        log.debug(DebugMarkers.REGISTER, "Captured registration for entry {} of type {}", name, type.location());
        registerCallbacks.removeAll(Pair.of(name, type)).forEach(callback -> {
            @SuppressWarnings({ "unchecked", "null" })
            @Nonnull NonNullConsumer<? super T> unsafeCallback = (NonNullConsumer<? super T>) callback; 
            reg.addRegisterCallback(unsafeCallback);
        });
        registrations.put(name, type, reg);
        return reg.getDelegate();
    }

    @Beta
    public <R> ResourceKey<Registry<R>> makeRegistry(String name, Supplier<RegistryBuilder<R>> builder) {
        final ResourceKey<Registry<R>> registryId = ResourceKey.createRegistryKey(new ResourceLocation(getModid(), name));
        OneTimeEventReceiver.addModListener(NewRegistryEvent.class, e -> e.create(builder.get().setName(registryId.location())));
        return registryId;
    }

    /* === Builder helpers === */

    // Generic

    public <R, T extends R> RegistryEntry<T> simple(ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return simple(currentName(), registryType, factory);
    }

    public <R, T extends R> RegistryEntry<T> simple(String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return simple(this, name, registryType, factory);
    }

    public <R, T extends R, P> RegistryEntry<T> simple(P parent, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return simple(parent, currentName(), registryType, factory);
    }

    public <R, T extends R, P> RegistryEntry<T> simple(P parent, String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return entry(name, callback -> new NoConfigBuilder<S, R, T, P>(self(), parent, name, callback, registryType, factory)).register();
    }

    // Items
    
    public <T extends Item> ItemBuilder<S, T, S> item(ItemFactory<T> factory) {
        return item(self(), factory);
    }
    
    public <T extends Item> ItemBuilder<S, T, S> item(String name, ItemFactory<T> factory) {
        return item(self(), name, factory);
    }
    
    public <T extends Item, P> ItemBuilder<S, T, P> item(P parent, ItemFactory<T> factory) {
        return item(parent, currentName(), factory);
    }
    
    public <T extends Item, P> ItemBuilder<S, T, P> item(P parent, String name, ItemFactory<T> factory) {
        // TODO clean this up when NonNullLazyValue is fixed better
        Supplier<? extends @NonnullType CreativeModeTab> currentTab = this.currentTab;
        return entry(name, callback -> ItemBuilder.create(self(), parent, name, callback, factory, currentTab == null ? null : currentTab::get));
    }
    
    // Blocks
    
    public <T extends Block> BlockBuilder<S, T, S> block(BlockFactory<T> factory) {
        return block(self(), factory);
    }
    
    public <T extends Block> BlockBuilder<S, T, S> block(String name, BlockFactory<T> factory) {
        return block(self(), name, factory);
    }
    
    public <T extends Block, P> BlockBuilder<S, T, P> block(P parent, BlockFactory<T> factory) {
        return block(parent, currentName(), factory);
    }
    
    public <T extends Block, P> BlockBuilder<S, T, P> block(P parent, String name, BlockFactory<T> factory) {
        return block(parent, name, Material.STONE, factory);
    }
    
    public <T extends Block> BlockBuilder<S, T, S> block(Material material, BlockFactory<T> factory) {
        return block(self(), material, factory);
    }
    
    public <T extends Block> BlockBuilder<S, T, S> block(String name, Material material, BlockFactory<T> factory) {
        return block(self(), name, material, factory);
    }
    
    public <T extends Block, P> BlockBuilder<S, T, P> block(P parent, Material material, BlockFactory<T> factory) {
        return block(parent, currentName(), material, factory);
    }
    
    public <T extends Block, P> BlockBuilder<S, T, P> block(P parent, String name, Material material, BlockFactory<T> factory) {
        return entry(name, callback -> BlockBuilder.create(self(), parent, name, callback, factory, material));
    }
    
    // Entities
    
    public <T extends Entity> EntityBuilder<S, T, S> entity(EntityTypeFactory<T> factory, MobCategory classification) {
        return entity(self(), factory, classification);
    }
    
    public <T extends Entity> EntityBuilder<S, T, S> entity(String name, EntityTypeFactory<T> factory, MobCategory classification) {
        return entity(self(), name, factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<S, T, P> entity(P parent, EntityTypeFactory<T> factory, MobCategory classification) {
        return entity(parent, currentName(), factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<S, T, P> entity(P parent, String name, EntityTypeFactory<T> factory, MobCategory classification) {
        return entry(name, callback -> EntityBuilder.create(self(), parent, name, callback, factory, classification));
    }
    
    // Block Entities
    
    public <T extends BlockEntity> BlockEntityBuilder<S, T, S> blockEntity(BlockEntityFactory<T> factory) {
        return blockEntity(self(), factory);
    }
    
    public <T extends BlockEntity> BlockEntityBuilder<S, T, S> blockEntity(String name, BlockEntityFactory<T> factory) {
        return blockEntity(self(), name, factory);
    }
    
    public <T extends BlockEntity, P> BlockEntityBuilder<S, T, P> blockEntity(P parent, BlockEntityFactory<T> factory) {
        return blockEntity(parent, currentName(), factory);
    }
    
    public <T extends BlockEntity, P> BlockEntityBuilder<S, T, P> blockEntity(P parent, String name, BlockEntityFactory<T> factory) {
        return entry(name, callback -> BlockEntityBuilder.create(self(), parent, name, callback, factory));
    }
    
    // Fluids
    
    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid() {
        return fluid(self());
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(FluidTypeFactory typeFactory) {
        return fluid(self(), typeFactory);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(NonNullSupplier<FluidType> fluidType) {
        return fluid(self(), fluidType);
    }
    
    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(self(), stillTexture, flowingTexture);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return fluid(self(), stillTexture, flowingTexture, typeFactory);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return fluid(self(), stillTexture, flowingTexture, fluidType);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<S, T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
            FluidFactory<T> fluidFactory) {
        return fluid(self(), stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<S, T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
        FluidTypeFactory typeFactory, FluidFactory<T> fluidFactory) {
        return fluid(self(), stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<S, T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture,
        NonNullSupplier<FluidType> fluidType, FluidFactory<T> fluidFactory) {
        return fluid(self(), stillTexture, flowingTexture, fluidType, fluidFactory);
    }
    
    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(String name) {
        return fluid(self(), name);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(String name, FluidTypeFactory typeFactory) {
        return fluid(self(), name, typeFactory);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(String name, NonNullSupplier<FluidType> fluidType) {
        return fluid(self(), name, fluidType);
    }
    
    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(self(), name, stillTexture, flowingTexture);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return fluid(self(), name, stillTexture, flowingTexture, typeFactory);
    }

    public FluidBuilder<S, ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return fluid(self(), name, stillTexture, flowingTexture, fluidType);
    }
    
    public <T extends ForgeFlowingFluid> FluidBuilder<S, T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            FluidFactory<T> fluidFactory) {
        return fluid(self(), name, stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<S, T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        FluidTypeFactory typeFactory, FluidFactory<T> fluidFactory) {
        return fluid(self(), name, stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<S, T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        NonNullSupplier<FluidType> fluidType, FluidFactory<T> fluidFactory) {
        return fluid(self(), name, stillTexture, flowingTexture, fluidType, fluidFactory);
    }
        
    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent) {
        return fluid(parent, currentName());
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, FluidTypeFactory typeFactory) {
        return fluid(parent, currentName(), typeFactory);
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, NonNullSupplier<FluidType> fluidType) {
        return fluid(parent, currentName(), fluidType);
    }
    
    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(parent, currentName(), stillTexture, flowingTexture);
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, typeFactory);
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, fluidType);
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<S, T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            FluidFactory<T> fluidFactory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<S, T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        FluidTypeFactory typeFactory, FluidFactory<T> fluidFactory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<S, T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        NonNullSupplier<FluidType> fluidType, FluidFactory<T> fluidFactory) {
        return fluid(parent, currentName(), stillTexture, flowingTexture, fluidType, fluidFactory);
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, String name) {
        return fluid(parent, name, new ResourceLocation(getModid(), "block/" + currentName() + "_still"), new ResourceLocation(getModid(), "block/" + currentName() + "_flow"));
    }
    
    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, FluidTypeFactory typeFactory) {
        return fluid(parent, name, new ResourceLocation(getModid(), "block/" + currentName() + "_still"), new ResourceLocation(getModid(), "block/" + currentName() + "_flow"), typeFactory);
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, NonNullSupplier<FluidType> fluidType) {
        return fluid(parent, name, new ResourceLocation(getModid(), "block/" + currentName() + "_still"), new ResourceLocation(getModid(), "block/" + currentName() + "_flow"), fluidType);
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return entry(name, callback -> FluidBuilder.create(self(), parent, name, callback, stillTexture, flowingTexture));
    }
    
    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return entry(name, callback -> FluidBuilder.create(self(), parent, name, callback, stillTexture, flowingTexture, typeFactory));
    }

    public <P> FluidBuilder<S, ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return entry(name, callback -> FluidBuilder.create(self(), parent, name, callback, stillTexture, flowingTexture, fluidType));
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<S, T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            FluidFactory<T> fluidFactory) {
        return entry(name, callback -> FluidBuilder.create(self(), parent, name, callback, stillTexture, flowingTexture, fluidFactory));
    }
    
    public <T extends ForgeFlowingFluid, P> FluidBuilder<S, T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        FluidTypeFactory typeFactory, FluidFactory<T> fluidFactory) {
        return entry(name, callback -> FluidBuilder.create(self(), parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory));
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<S, T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        NonNullSupplier<FluidType> fluidType, FluidFactory<T> fluidFactory) {
        return entry(name, callback -> FluidBuilder.create(self(), parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory));
    }
    
    // Menu
    
    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<S, T, SC, S> menu(MenuFactory<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return menu(currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<S, T, SC, S> menu(String name, MenuFactory<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return menu(self(), name, factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<S, T, SC, P> menu(P parent, MenuFactory<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return menu(parent, currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<S, T, SC, P> menu(P parent, String name, MenuFactory<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return entry(name, callback -> new MenuBuilder<S, T, SC, P>(self(), parent, name, callback, factory, screenFactory));
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<S, T, SC, S> menu(MenuFactory.WithBuffer<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return menu(currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<S, T, SC, S> menu(String name, MenuFactory.WithBuffer<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return menu(self(), name, factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<S, T, SC, P> menu(P parent, MenuFactory.WithBuffer<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return menu(parent, currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<S, T, SC, P> menu(P parent, String name, MenuFactory.WithBuffer<T> factory, NonNullSupplier<MenuScreenFactory<T, SC>> screenFactory) {
        return entry(name, callback -> new MenuBuilder<S, T, SC, P>(self(), parent, name, callback, factory, screenFactory));
    }
    
    // Enchantment
    
    public <T extends Enchantment> EnchantmentBuilder<S, T, S> enchantment(EnchantmentCategory type, EnchantmentFactory<T> factory) {
        return enchantment(self(), type, factory);
    }
    
    public <T extends Enchantment> EnchantmentBuilder<S, T, S> enchantment(String name, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        return enchantment(self(), name, type, factory);
    }
    
    public <T extends Enchantment, P> EnchantmentBuilder<S, T, P> enchantment(P parent, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        return enchantment(parent, currentName(), type, factory);
    }
    
    public <T extends Enchantment, P> EnchantmentBuilder<S, T, P> enchantment(P parent, String name, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        return entry(name, callback -> EnchantmentBuilder.create(self(), parent, name, callback, type, factory));
    }
}