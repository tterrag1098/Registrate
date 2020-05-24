package com.tterrag.registrate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.Message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ContainerBuilder;
import com.tterrag.registrate.builders.ContainerBuilder.ContainerFactory;
import com.tterrag.registrate.builders.ContainerBuilder.ScreenFactory;
import com.tterrag.registrate.builders.EnchantmentBuilder;
import com.tterrag.registrate.builders.EnchantmentBuilder.EnchantmentFactory;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.builders.TileEntityBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.DebugMarkers;
import com.tterrag.registrate.util.LazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.tterrag.registrate.util.nullness.NonnullType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
        Builder<R, T, ?, ?> builder;
        Supplier<? extends T> creator;        
        RegistryEntry<T> delegate;
        
        @Getter(value = AccessLevel.NONE)
        List<NonNullConsumer<? super T>> callbacks = new ArrayList<>();
        
        Registration(ResourceLocation name, Class<? super R> type, Builder<R, T, ?, ?> builder, Supplier<? extends T> creator, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
            this.name = name;
            this.type = type;
            this.builder = builder;
            this.creator =  creator;
            this.delegate = entryFactory.apply(RegistryObject.of(name, RegistryManager.ACTIVE.<R>getRegistry(type)));
        }
        
        void register(IForgeRegistry<R> registry) {
            T entry = creator.get();
            registry.register(entry.setRegistryName(name));
            delegate.updateReference(registry);
            callbacks.forEach(c -> c.accept(entry));
        }
        
        void addRegisterCallback(NonNullConsumer<? super T> callback) {
            Preconditions.checkNotNull(callback, "Callback must not be null");
            callbacks.add(callback);
        }
    }
    
    public static boolean isDevEnvironment() {
        return FMLEnvironment.naming.equals("mcp");
    }
    
    private final Table<String, Class<?>, Registration<?, ?>> registrations = HashBasedTable.create();
    /** Expected to be emptied by the time registration occurs, is emptied by {@link #accept(String, Class, Builder, NonNullSupplier, NonNullFunction)} */
    private final Multimap<Pair<String, Class<?>>, NonNullConsumer<? extends IForgeRegistryEntry<?>>> registerCallbacks = HashMultimap.create();
    private final Table<Pair<String, Class<?>>, ProviderType<?>, Consumer<? extends RegistrateProvider>> datagensByEntry = HashBasedTable.create();
    private final ListMultimap<ProviderType<?>, @NonnullType NonNullConsumer<? extends RegistrateProvider>> datagens = ArrayListMultimap.create();

    /**
     * @return The mod ID that this {@link AbstractRegistrate} is creating objects for
     */
    @Getter
    private final String modid;
    
    @Nullable
    private String currentName;
    @Nullable
    private LazyValue<? extends ItemGroup> currentGroup;
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
        bus.addListener(this::onRegister);
        bus.addListener(this::onData);
        return self();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    protected void onRegister(RegistryEvent.Register<?> event) {
        Class<?> type = event.getRegistry().getRegistrySuperType();
        if (type == null) {
            log.debug(DebugMarkers.REGISTER, "Skipping invalid registry with no supertype: " + event.getRegistry().getRegistryName());
            return;
        }
        if (!registerCallbacks.isEmpty()) {
            registerCallbacks.asMap().forEach((k, v) -> log.warn("Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?", v.size(), k.getLeft(), k.getRight().getSimpleName()));
            if (isDevEnvironment()) {
                throw new IllegalStateException("Found unused register callbacks, see logs");
            }
        }
        Map<String, Registration<?, ?>> registrationsForType = registrations.column(type);
        if (registrationsForType.size() > 0) {
            log.debug(DebugMarkers.REGISTER, "Registering {} known objects of type {}", registrationsForType.size(), type.getName());
            for (Entry<String, Registration<?, ?>> e : registrationsForType.entrySet()) {
                try {
                    e.getValue().register((IForgeRegistry) event.getRegistry());
                    log.debug(DebugMarkers.REGISTER, "Registered {} to registry {}", e.getValue().getName(), event.getRegistry().getRegistryName());
                } catch (Exception ex) {
                    String err = "Unexpected error while registering entry " + e.getValue().getName() + " to registry " + event.getRegistry().getRegistryName();
                    if (skipErrors) {
                        log.error(DebugMarkers.REGISTER, err);
                    } else {
                        throw new RuntimeException(err, ex);
                    }
                }
            }
        }
    }
    
    @Nullable
    private RegistrateDataProvider provider;
    
    protected void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(provider = new RegistrateDataProvider(this, modid, event));
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
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> get(String name, Class<? super R> type) {
        return this.<R, T>getRegistration(name, type).getDelegate();
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    private <R extends IForgeRegistryEntry<R>, T extends R> Registration<R, T> getRegistrationUnchecked(String name, Class<? super R> type) {    
        return (Registration<R, T>) registrations.get(name, type);
    }
    
    private <R extends IForgeRegistryEntry<R>, T extends R> Registration<R, T> getRegistration(String name, Class<? super R> type) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, type);
        if (reg != null) {
            return reg;
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
    
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>, T extends R> S addRegisterCallback(String name, Class<? super R> registryType, NonNullConsumer<? super T> callback) {
        Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, registryType);
        if (reg == null) {
            registerCallbacks.put(Pair.of(name, registryType), (NonNullConsumer<? extends IForgeRegistryEntry<?>>) callback);
        } else {
            reg.addRegisterCallback(callback);
        }
        return self();
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
    public <P extends RegistrateProvider, R extends IForgeRegistryEntry<R>> S setDataGenerator(Builder<R, ?, ?, ?> builder, ProviderType<P> type, NonNullConsumer<? extends P> cons) {
        return this.<P, R>setDataGenerator(builder.getName(), builder.getRegistryType(), type, cons);
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
    public <P extends RegistrateProvider, R extends IForgeRegistryEntry<R>> S setDataGenerator(String entry, Class<? super R> registryType, ProviderType<P> type, NonNullConsumer<? extends P> cons) {
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
    public <T extends RegistrateProvider> S addDataGenerator(ProviderType<T> type, NonNullConsumer<? extends T> cons) {
        datagens.put(type, cons);
        return self();
    }
    
    private final LazyValue<List<Pair<String, String>>> extraLang = new LazyValue<>(() -> {
        final List<Pair<String, String>> ret = new ArrayList<>();
        addDataGenerator(ProviderType.LANG, prov -> ret.forEach(p -> prov.add(p.getKey(), p.getValue())));
        return ret;
    });
    
    /**
     * Add a custom translation mapping, prepending this registrate's {@link #getModid() mod id} to the translation key.
     * 
     * @param key
     *            The translation key
     * @param value
     *            The (English) translation value
     * @return A {@link TranslationTextComponent} representing the translated text
     */
    @Deprecated
    public TranslationTextComponent addLang(String key, String value) {
        final String prefixedKey = getModid() + "." + key;
        addDataGenerator(ProviderType.LANG, p -> p.add(prefixedKey, value));
        return new TranslationTextComponent(prefixedKey);
    }
    
    /**
     * Add a custom translation mapping using the vanilla style of ResourceLocation -&gt; translation key conversion.
     * 
     * @param type
     *            Type of the object, this is used as a prefix (e.g. {@code ["block", "mymod:myblock"] -> "block.mymod.myblock"})
     * @param id
     *            ID of the object, which will be converted to a lang key via {@link Util#makeTranslationKey(String, ResourceLocation)}
     * @param localizedName
     *            (English) translation value
     * @return A {@link TranslationTextComponent} representing the translated text
     */
    public TranslationTextComponent addLang(String type, ResourceLocation id, String localizedName) {
        return addRawLang(Util.makeTranslationKey(type, id), localizedName);
    }
    
    /**
     * Add a custom translation mapping using the vanilla style of ResourceLocation -&gt; translation key conversion. Also appends a suffix to the key.
     * 
     * @param type
     *            Type of the object, this is used as a prefix (e.g. {@code ["block", "mymod:myblock"] -> "block.mymod.myblock"})
     * @param id
     *            ID of the object, which will be converted to a lang key via {@link Util#makeTranslationKey(String, ResourceLocation)}
     * @param suffix
     *            A suffix which will be appended to the generated key (separated by a dot)
     * @param localizedName
     *            (English) translation value
     * @return A {@link TranslationTextComponent} representing the translated text
     */
    public TranslationTextComponent addLang(String type, ResourceLocation id, String suffix, String localizedName) {
        return addRawLang(Util.makeTranslationKey(type, id) + "." + suffix, localizedName);
    }

    /**
     * Add a custom translation mapping directly to the lang provider.
     * 
     * @param key
     *            The translation key
     * @param value
     *            The (English) translation value
     * @return A {@link TranslationTextComponent} representing the translated text
     */
    public TranslationTextComponent addRawLang(String key, String value) {
        extraLang.get().add(Pair.of(key, value));
        return new TranslationTextComponent(key);
    }
    
    @SuppressWarnings("null")
    private Optional<Pair<String, Class<?>>> getEntryForGenerator(ProviderType<?> type, NonNullConsumer<? extends RegistrateProvider> generator) {
        for (Map.Entry<Pair<String, Class<?>>, Consumer<? extends RegistrateProvider>> e : datagensByEntry.column(type).entrySet()) {
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
    public <T extends RegistrateProvider> void genData(ProviderType<T> type, T gen) {
        datagens.get(type).forEach(cons -> {
            Optional<Pair<String, Class<?>>> entry = null;
            if (log.isEnabled(Level.DEBUG, DebugMarkers.DATA)) {
                entry = getEntryForGenerator(type, cons);
                if (entry.isPresent()) {
                    log.debug(DebugMarkers.DATA, "Generating data of type {} for entry {} [{}]", RegistrateDataProvider.getTypeName(type), entry.get().getLeft(), entry.get().getRight().getSimpleName());
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
                    err = log.getMessageFactory().newMessage("Unexpected error while running data generator of type {} for entry {} [{}]", RegistrateDataProvider.getTypeName(type), entry.get().getLeft(), entry.get().getRight().getSimpleName());
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
    
    protected <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name, Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
        Registration<R, T> reg = new Registration<>(new ResourceLocation(modid, name), type, builder, creator, entryFactory);
        log.debug(DebugMarkers.REGISTER, "Captured registration for entry {} of type {}", name, type.getName());
        registerCallbacks.removeAll(Pair.of(name, type)).forEach(callback -> {
            @SuppressWarnings({ "unchecked", "null" })
            @Nonnull NonNullConsumer<? super T> unsafeCallback = (NonNullConsumer<? super T>) callback; 
            reg.addRegisterCallback(unsafeCallback);
        });
        registrations.put(name, type, reg);
        return reg.getDelegate();
    }
    
    /* === Builder helpers === */
    
    // Generic
    
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> simple(Class<? super R> registryType, NonNullSupplier<T> factory) {
        return simple(currentName(), registryType, factory);
    }

    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> simple(String name, Class<? super R> registryType, NonNullSupplier<T> factory) {
        return simple(this, name, registryType, factory);
    }

    public <R extends IForgeRegistryEntry<R>, T extends R, P> RegistryEntry<T> simple(P parent, Class<? super R> registryType, NonNullSupplier<T> factory) {
        return simple(parent, currentName(), registryType, factory);
    }

    public <R extends IForgeRegistryEntry<R>, T extends R, P> RegistryEntry<T> simple(P parent, String name, Class<? super R> registryType, NonNullSupplier<T> factory) {
        return entry(name, callback -> new NoConfigBuilder<R, T, P>(this, parent, name, callback, registryType, factory)).register();
    }
    
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
        return block(parent, name, Material.ROCK, factory);
    }
    
    public <T extends Block> BlockBuilder<T, S> block(Material material, NonNullFunction<Block.Properties, T> factory) {
        return block(self(), material, factory);
    }
    
    public <T extends Block> BlockBuilder<T, S> block(String name, Material material, NonNullFunction<Block.Properties, T> factory) {
        return block(self(), name, material, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, Material material, NonNullFunction<Block.Properties, T> factory) {
        return block(parent, currentName(), material, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, Material material, NonNullFunction<Block.Properties, T> factory) {
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
    
    // Container
    
    public <T extends Container, SC extends Screen & IHasContainer<T>> ContainerBuilder<T, SC, S> container(ContainerFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
        return container(currentName(), factory, screenFactory);
    }

    public <T extends Container, SC extends Screen & IHasContainer<T>> ContainerBuilder<T, SC, S> container(String name, ContainerFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
        return container(self(), name, factory, screenFactory);
    }

    public <T extends Container, SC extends Screen & IHasContainer<T>, P> ContainerBuilder<T, SC, P> container(P parent, ContainerFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
        return container(parent, currentName(), factory, screenFactory);
    }

    public <T extends Container, SC extends Screen & IHasContainer<T>, P> ContainerBuilder<T, SC, P> container(P parent, String name, ContainerFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
        return entry(name, callback -> new ContainerBuilder<T, SC, P>(this, parent, name, callback, factory, screenFactory));
    }
    
    // Enchantment
    
    public <T extends Enchantment> EnchantmentBuilder<T, S> enchantment(EnchantmentType type, EnchantmentFactory<T> factory) {
        return enchantment(self(), type, factory);
    }
    
    public <T extends Enchantment> EnchantmentBuilder<T, S> enchantment(String name, EnchantmentType type, EnchantmentFactory<T> factory) {
        return enchantment(self(), name, type, factory);
    }
    
    public <T extends Enchantment, P> EnchantmentBuilder<T, P> enchantment(P parent, EnchantmentType type, EnchantmentFactory<T> factory) {
        return enchantment(parent, currentName(), type, factory);
    }
    
    public <T extends Enchantment, P> EnchantmentBuilder<T, P> enchantment(P parent, String name, EnchantmentType type, EnchantmentFactory<T> factory) {
        return entry(name, callback -> EnchantmentBuilder.create(this, parent, name, callback, type, factory));
    }
}
