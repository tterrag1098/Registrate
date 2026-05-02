package com.tterrag.registrate.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.RegistrateDistExecutor;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class FluidBuilder<T extends BaseFlowingFluid, P, S extends FluidBuilder<T, P, S>> extends AbstractBuilder<Fluid, T, P, S> {

    @FunctionalInterface
    public interface FluidTypeFactory {
        FluidType create(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture);
    }


    @Nullable
    private NonNullSupplier<Supplier<IClientFluidTypeExtensions>> clientExtension;

    /**
     * Register a client extension for this block. The {@link IClientBlockExtensions} instance can be shared across many items.
     *
     * @param clientExtension
     *            The client extension to register for this block
     * @return this {@link BlockBuilder}
     */
    public S clientExtension(NonNullSupplier<Supplier<IClientFluidTypeExtensions>> clientExtension) {
        if (this.clientExtension == null) {
            RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerClientExtension);
        }
        this.clientExtension = clientExtension;
        return self();
    }

    protected void registerClientExtension() {
        OneTimeEventReceiver.addModListener(getOwner(), RegisterClientExtensionsEvent.class, e -> {
            NonNullSupplier<Supplier<IClientFluidTypeExtensions>> clientExtension = this.clientExtension;
            if (clientExtension != null) {
                e.registerFluidType(clientExtension.get().get(), fluidType.get());
            }
        });
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default ({@link FluidType}) and fluid class ({@link BaseFlowingFluid.Flowing}).
     *
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
     * @param stillTexture
     *            The texture to use for still fluids
     * @param flowingTexture
     *            The texture to use for flowing fluids
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, NonNullFunction)
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("unchecked")
    public static <P, S extends FluidBuilder<BaseFlowingFluid.Flowing, P, S>> S create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return (S) create(owner, parent, name, callback, stillTexture, flowingTexture, (prop, still, flowing) ->
                new FluidType(prop), BaseFlowingFluid.Flowing::new)
                .clientExtension(()-> ()-> new DefaultFluidTypeExtension(stillTexture, flowingTexture));
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default fluid class ({@link BaseFlowingFluid.Flowing}).
     *
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
     * @param stillTexture
     *            The texture to use for still fluids
     * @param typeFactory
     *            A factory that creates the fluid type
     * @param flowingTexture
     *            The texture to use for flowing fluids
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, NonNullFunction)
     */
    public static <P, S extends FluidBuilder<BaseFlowingFluid.Flowing, P, S>> S create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, BaseFlowingFluid.Flowing::new);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default fluid class ({@link BaseFlowingFluid.Flowing}).
     *
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
     * @param stillTexture
     *            The texture to use for still fluids
     * @param fluidType
     *            An existing and registered fluid type.
     * @param flowingTexture
     *            The texture to use for flowing fluids
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, NonNullFunction)
     */
    public static <P, S extends FluidBuilder<BaseFlowingFluid.Flowing, P, S>> S create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, BaseFlowingFluid.Flowing::new);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default ({@link FluidType}) and fluid class ({@link BaseFlowingFluid.Flowing}).
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
     * @param stillTexture
     *            The texture to use for still fluids
     * @param flowingTexture
     *            The texture to use for flowing fluids
     * @param fluidFactory
     *            A factory that creates the flowing fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("unchecked")
    public static <T extends BaseFlowingFluid, P, S extends FluidBuilder<T, P, S>> S create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return (S) create(owner, parent, name, callback, stillTexture, flowingTexture, (prop, still, flowing) ->
                new FluidType(prop), fluidFactory).clientExtension(()-> ()-> new DefaultFluidTypeExtension(stillTexture, flowingTexture));
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>A default {@link BaseFlowingFluid.Source source fluid} (via {@link #defaultSource})</li>
     * <li>A default block for the fluid, with its own default blockstate and model that configure the particle texture (via {@link #defaultBlock()})</li>
     * <li>A default bucket item, that uses a simple generated item model with a texture of the same name as this fluid (via {@link #defaultBucket()})</li>
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
     * @param stillTexture
     *            The texture to use for still fluids
     * @param flowingTexture
     *            The texture to use for flowing fluids
     * @param typeFactory
     *            A factory that creates the fluid type
     * @param fluidFactory
     *            A factory that creates the flowing fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    public static <T extends BaseFlowingFluid, P, S extends FluidBuilder<T, P, S>> S create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return new FluidBuilder<T, P, S>(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>A default {@link BaseFlowingFluid.Source source fluid} (via {@link #defaultSource})</li>
     * <li>A default block for the fluid, with its own default blockstate and model that configure the particle texture (via {@link #defaultBlock()})</li>
     * <li>A default bucket item, that uses a simple generated item model with a texture of the same name as this fluid (via {@link #defaultBucket()})</li>
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
     * @param stillTexture
     *            The texture to use for still fluids
     * @param flowingTexture
     *            The texture to use for flowing fluids
     * @param fluidType
     *            An existing and registered fluid type
     * @param fluidFactory
     *            A factory that creates the flowing fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    public static <T extends BaseFlowingFluid, P, S extends FluidBuilder<T, P, S>> S create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return new FluidBuilder<T, P, S>(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
    }

    private final String sourceName, bucketName;

    private final ResourceLocation stillTexture, flowingTexture;
    private final NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory;

    @Nullable
    private final NonNullSupplier<FluidType> fluidType;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};

    private NonNullConsumer<BaseFlowingFluid.Properties> fluidProperties;

    private @Nullable Supplier<Supplier<RenderType>> layer = null;

    private boolean registerType;

    @Nullable
    private NonNullSupplier<? extends BaseFlowingFluid> source;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.create(makeTypeProperties(), this.stillTexture, this.flowingTexture));
        this.registerType = true;

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, Registries.ITEM).get())
            .block(() -> owner.<Block, LiquidBlock>get(name, Registries.BLOCK).get());
    }

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = fluidType;
        this.registerType = false; // Don't register if we have a fluid from outside.

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, Registries.ITEM).get())
                .block(() -> owner.<Block, LiquidBlock>get(name, Registries.BLOCK).get());
    }

    @SuppressWarnings("unchecked")
    protected final S self() {
        return (S) this;
    }

    /**
     * Modify the properties of the fluid type. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public S properties(NonNullConsumer<FluidType.Properties> cons) {
        typeProperties = typeProperties.andThen(cons);
        return self();
    }

    /**
     * Modify the properties of the flowing fluid. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public S fluidProperties(NonNullConsumer<BaseFlowingFluid.Properties> cons) {
        fluidProperties = fluidProperties.andThen(cons);
        return self();
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#toEnglishName(String)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     *
     * @return this {@link FluidBuilder}
     */
    public S defaultLang() {
        return lang(f -> f.getFluidType().getDescriptionId(), RegistrateLangProvider.toEnglishName(sourceName));
    }

    /**
     * Set the translation for this fluid.
     *
     * @param name
     *            A localized English name
     * @return this {@link FluidBuilder}
     */
    public S lang(String name) {
        return lang(f -> f.getFluidType().getDescriptionId(), name);
    }



    public S renderType(Supplier<Supplier<RenderType>> layer) {
        RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get().get()), "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return self();
    }

    protected void registerRenderType(T entry) {
        RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            OneTimeEventReceiver.addModListener(getOwner(), FMLClientSetupEvent.class, $ -> {
                if (this.layer != null) {
                    RenderType layer = this.layer.get().get();
                    ItemBlockRenderTypes.setRenderLayer(entry, layer);
                    ItemBlockRenderTypes.setRenderLayer(getSource(), layer);
                }
            });
        });
    }

    /**
     * Create a standard {@link BaseFlowingFluid.Source} for this fluid which will be built and registered along with this fluid.
     *
     * @return this {@link FluidBuilder}
     * @see #source(NonNullFunction)
     * @throws IllegalStateException
     *             If {@link #source(NonNullFunction)} has been called before this method
     */
    public S defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        }
        this.defaultSource = true;
        return self();
    }

    /**
     * Create a {@link BaseFlowingFluid} for this fluid, which is created by the given factory, and which will be built and registered along with this fluid.
     *
     * @param factory
     *            A factory for the fluid, which accepts the properties and returns a new fluid
     * @return this {@link FluidBuilder}
     */
    public S source(NonNullFunction<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(() -> factory.apply(makeProperties()));
        return self();
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #block()
     * @throws IllegalStateException
     *             If {@link #block()} or {@link #block(NonNullBiFunction)} has been called before this method
     */
    public S defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return self();
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, and return the builder for it so that further customization can be done.
     *
     * @return the {@link BlockBuilder} for the {@link LiquidBlock}
     */
    public <BB extends BlockBuilder<LiquidBlock, S, BB>> BB block() {
        return block(LiquidBlock::new);
    }

    /**
     * Create a {@link LiquidBlock} for this fluid, which is created by the given factory, and return the builder for it so that further customization can be done.
     *
     * @param <B>
     *            The type of the block
     * @param factory
     *            A factory for the block, which accepts the block object and properties and returns a new block
     * @return the {@link BlockBuilder} for the {@link LiquidBlock}
     */
    public <B extends LiquidBlock, BB extends BlockBuilder<B, S, BB>> BB block(NonNullBiFunction<T, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        final NonNullSupplier<T> supplier = asSupplier();
        final var lightLevel = Lazy.of(() -> fluidType.get().getLightLevel());
        final ToIntFunction<BlockState> lightLevelInt = $ -> lightLevel.get();
        return getOwner().<B, S, BB>block(self(), sourceName, p -> factory.apply(supplier.get(), p))
            .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
            .properties(p -> p.lightLevel(lightLevelInt))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                .texture("particle", stillTexture)));
    }

    @Beta
    public S noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return self();
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #bucket()
     * @throws IllegalStateException
     *             If {@link #bucket()} or {@link #bucket(NonNullBiFunction)} has been called before this method
     */
    public S defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return self();
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, and return the builder for it so that further customization can be done.
     *
     * @return the {@link ItemBuilder} for the {@link BucketItem}
     */
    public <IB extends ItemBuilder<BucketItem, S, IB>> IB bucket() {
        return bucket(BucketItem::new);
    }

    /**
     * Create a {@link BucketItem} for this fluid, which is created by the given factory, and return the builder for it so that further customization can be done.
     *
     * @param <I>
     *            The type of the bucket item
     * @param factory
     *            A factory for the bucket item, which accepts the fluid object supplier and properties and returns a new item
     * @return the {@link ItemBuilder} for the {@link BucketItem}
     */
    public <I extends BucketItem, IB extends ItemBuilder<I, S, IB>> IB bucket(NonNullBiFunction<BaseFlowingFluid, Item.Properties, ? extends I> factory) {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        // TODO: Can we find a way to circumvent this limitation?
        if (source == null) {
            throw new IllegalStateException("Cannot create a bucket before creating a source block");
        }
        return getOwner().<I, S, IB>item(self(), bucketName, p -> factory.apply(source.get(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .model((ctx, prov) -> prov.generated(ctx::getEntry, ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), "item/" + bucketName)));
    }

    @Beta
    public S noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return self();
    }

    /**
     * Assign {@link TagKey}{@code s} to this fluid and its source fluid. Multiple calls will add additional tags.
     *
     * @param tags
     *            The tags to assign
     * @return this {@link FluidBuilder}
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final S tag(TagKey<Fluid>... tags) {
        FluidBuilder<T, P, S> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(), ProviderType.FLUID_TAGS,
                prov -> this.tags.stream().map(prov::addTag).forEach(p -> p.add(getSource().builtInRegistryHolder().key())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return (S) ret;
    }

    /**
     * Remove {@link TagKey}{@code s} from this fluid and its source fluid. Multiple calls will remove additional tags.
     *
     * @param tags
     *            The tags to remove
     * @return this {@link FluidBuilder}
     */
    @SafeVarargs
    public final S removeTag(TagKey<Fluid>... tags) {
        this.tags.removeAll(Arrays.asList(tags));
        return this.removeTag(ProviderType.FLUID_TAGS, tags);
    }

    private BaseFlowingFluid getSource() {
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private BaseFlowingFluid.Properties makeProperties() {
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        BaseFlowingFluid.Properties ret = new BaseFlowingFluid.Properties(fluidType, source == null ? null : source::get, asSupplier());
        fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        Optional<RegistryEntry<Block, Block>> block = getOwner().getOptional(sourceName, Registries.BLOCK);
        this.typeProperties.accept(properties);

        // Force the translation key after the user callback runs
        // This is done because we need to remove the lang data generator if using the block key,
        // and if it was possible to undo this change, it might result in the user translation getting
        // silently lost, as there's no good way to check whether the translation key was changed.
        // TODO improve this?
        if (block.isPresent() && block.get().isBound()) {
            properties.descriptionId(block.get().get().getDescriptionId());
            setData(ProviderType.LANG, NonNullBiConsumer.noop());
        } else {
            properties.descriptionId(Util.makeDescriptionId("fluid", ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), sourceName)));
        }

        return properties;
    }

    @Override
    protected T createEntry() {
        return fluidFactory.apply(makeProperties());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally registers the source fluid and the fluid type (if constructed).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public FluidEntry<T> register() {
        // Check the fluid has a type.
        if (this.fluidType != null) {
            // Register the type.
            if (this.registerType) {
                getOwner().simple(this, this.sourceName, NeoForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
            }
        } else {
            throw new IllegalStateException("Fluid must have a type: " + getName());
        }

        if (defaultSource == Boolean.TRUE) {
            source(BaseFlowingFluid.Source::new);
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, Registries.FLUID, (FluidBuilder) this, source::get);
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + getName());
        }

        return (FluidEntry<T>) super.register();
    }

    @Override
    protected RegistryEntry<Fluid, T> createEntryWrapper(DeferredHolder<Fluid, T> delegate) {
        return new FluidEntry<>(getOwner(), delegate);
    }

	public static class DefaultFluidTypeExtension implements IClientFluidTypeExtensions {

		private final ResourceLocation stillTexture,flowingTexture;

		public DefaultFluidTypeExtension(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
			this.stillTexture = stillTexture;
			this.flowingTexture = flowingTexture;
		}

		@Override
		public ResourceLocation getStillTexture() {
			return stillTexture;
		}

		@Override
		public ResourceLocation getFlowingTexture() {
			return flowingTexture;
		}

	}

}
