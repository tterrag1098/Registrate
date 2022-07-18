package com.tterrag.registrate.builders;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.factory.FluidFactory;
import com.tterrag.registrate.builders.factory.FluidTypeFactory;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;

import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidBuilder<O extends AbstractRegistrate<O>, T extends ForgeFlowingFluid, P> extends AbstractBuilder<O, Fluid, T, P, FluidBuilder<O, T, P>> {

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default ({@link FluidType}) and fluid class ({@link ForgeFlowingFluid.Flowing}).
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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, FluidFactory)
     */
    public static <O extends AbstractRegistrate<O>, P> FluidBuilder<O, ForgeFlowingFluid.Flowing, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, FluidBuilder::defaultFluidType, ForgeFlowingFluid.Flowing::new);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default fluid class ({@link ForgeFlowingFluid.Flowing}).
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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, FluidFactory)
     */
    public static <O extends AbstractRegistrate<O>, P> FluidBuilder<O, ForgeFlowingFluid.Flowing, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, FluidFactory.FLOWING);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default fluid class ({@link ForgeFlowingFluid.Flowing}).
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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, FluidFactory)
     */
    public static <O extends AbstractRegistrate<O>, P> FluidBuilder<O, ForgeFlowingFluid.Flowing, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, FluidFactory.FLOWING);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default ({@link FluidType}) and fluid class ({@link ForgeFlowingFluid.Flowing}).
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
    public static <O extends AbstractRegistrate<O>, T extends ForgeFlowingFluid, P> FluidBuilder<O, T, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            FluidFactory<T> fluidFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, FluidBuilder::defaultFluidType, fluidFactory);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>A default {@link ForgeFlowingFluid.Source source fluid} (via {@link #defaultSource})</li>
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
    public static <O extends AbstractRegistrate<O>, T extends ForgeFlowingFluid, P> FluidBuilder<O, T, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        FluidTypeFactory typeFactory, FluidFactory<T> fluidFactory) {
        FluidBuilder<O, T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory)
            .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>A default {@link ForgeFlowingFluid.Source source fluid} (via {@link #defaultSource})</li>
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
    public static <O extends AbstractRegistrate<O>, T extends ForgeFlowingFluid, P> FluidBuilder<O, T, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        NonNullSupplier<FluidType> fluidType, FluidFactory<T> fluidFactory) {
        FluidBuilder<O, T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    private final String sourceName, bucketName;

    private final ResourceLocation stillTexture, flowingTexture;
    private final FluidFactory<T> fluidFactory;

    @Nullable
    private final NonNullSupplier<FluidType> fluidType;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};

    private NonNullConsumer<ForgeFlowingFluid.Properties> fluidProperties;

    private @Nullable Supplier<RenderType> layer = null;

    private boolean registerType;

    @Nullable
    private NonNullSupplier<? extends ForgeFlowingFluid> source;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public FluidBuilder(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory, FluidFactory<T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, ForgeRegistries.Keys.FLUIDS);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.create(makeTypeProperties(), this.stillTexture, this.flowingTexture));
        this.registerType = true;

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, ForgeRegistries.Keys.ITEMS).get())
            .block(() -> owner.<Block, LiquidBlock>get(name, ForgeRegistries.Keys.BLOCKS).get());
    }

    public FluidBuilder(O owner, P parent, String name, BuilderCallback<O> callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, FluidFactory<T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, ForgeRegistries.Keys.FLUIDS);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = fluidType;
        this.registerType = false; // Don't register if we have a fluid from outside.

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, ForgeRegistries.Keys.ITEMS).get())
                .block(() -> owner.<Block, LiquidBlock>get(name, ForgeRegistries.Keys.BLOCKS).get());
    }

    /**
     * Modify the properties of the fluid type. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<O, T, P> properties(NonNullConsumer<FluidType.Properties> cons) {
        typeProperties = typeProperties.andThen(cons);
        return this;
    }

    /**
     * Modify the properties of the flowing fluid. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<O, T, P> fluidProperties(NonNullConsumer<ForgeFlowingFluid.Properties> cons) {
        fluidProperties = fluidProperties.andThen(cons);
        return this;
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#toEnglishName(String)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     *
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<O, T, P> defaultLang() {
        return lang(f -> f.getFluidType().getDescriptionId(), RegistrateLangProvider.toEnglishName(sourceName));
    }

    /**
     * Set the translation for this fluid.
     *
     * @param name
     *            A localized English name
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<O, T, P> lang(String name) {
        return lang(f -> f.getFluidType().getDescriptionId(), name);
    }

    @SuppressWarnings("deprecation")
    public FluidBuilder<O, T, P> renderType(Supplier<RenderType> layer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get()), "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return this;
    }

    @SuppressWarnings("deprecation")
    protected void registerRenderType(T entry) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, $ -> {
                if (this.layer != null) {
                    RenderType layer = this.layer.get();
                    ItemBlockRenderTypes.setRenderLayer(entry, layer);
                    ItemBlockRenderTypes.setRenderLayer(getSource(), layer);
                }
            });
        });
    }

    /**
     * Create a standard {@link ForgeFlowingFluid.Source} for this fluid which will be built and registered along with this fluid.
     *
     * @return this {@link FluidBuilder}
     * @see #source(NonNullFunction)
     * @throws IllegalStateException
     *             If {@link #source(NonNullFunction)} has been called before this method
     */
    public FluidBuilder<O, T, P> defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        }
        this.defaultSource = true;
        return this;
    }

    /**
     * Create a {@link ForgeFlowingFluid} for this fluid, which is created by the given factory, and which will be built and registered along with this fluid.
     *
     * @param factory
     *            A factory for the fluid, which accepts the properties and returns a new fluid
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<O, T, P> source(NonNullFunction<ForgeFlowingFluid.Properties, ? extends ForgeFlowingFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(() -> factory.apply(makeProperties()));
        return this;
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #block()
     * @throws IllegalStateException
     *             If {@link #block()} or {@link #block(NonNullBiFunction)} has been called before this method
     */
    public FluidBuilder<O, T, P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return this;
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, and return the builder for it so that further customization can be done.
     *
     * @return the {@link BlockBuilder} for the {@link LiquidBlock}
     */
    public BlockBuilder<O, LiquidBlock, FluidBuilder<O, T, P>> block() {
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
    public <B extends LiquidBlock> BlockBuilder<O, B, FluidBuilder<O, T, P>> block(NonNullBiFunction<NonNullSupplier<? extends T>, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        NonNullSupplier<T> supplier = asSupplier();
        return getOwner().<B, FluidBuilder<O, T, P>>block(this, sourceName, p -> factory.apply(supplier, p))
            .properties(p -> BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable())
            .properties(p -> p.lightLevel(blockState -> fluidType.get().getLightLevel()))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                .texture("particle", stillTexture)));
    }

    @Beta
    public FluidBuilder<O, T, P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return this;
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #bucket()
     * @throws IllegalStateException
     *             If {@link #bucket()} or {@link #bucket(NonNullBiFunction)} has been called before this method
     */
    public FluidBuilder<O, T, P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, and return the builder for it so that further customization can be done.
     *
     * @return the {@link ItemBuilder} for the {@link BucketItem}
     */
    public ItemBuilder<O, BucketItem, FluidBuilder<O, T, P>> bucket() {
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
    public <I extends BucketItem> ItemBuilder<O, I, FluidBuilder<O, T, P>> bucket(NonNullBiFunction<Supplier<? extends ForgeFlowingFluid>, Item.Properties, ? extends I> factory) {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        // TODO: Can we find a way to circumvent this limitation?
        if (source == null) {
            throw new IllegalStateException("Cannot create a bucket before creating a source block");
        }
        return getOwner().<I, FluidBuilder<O, T, P>>item(this, bucketName, p -> factory.apply(source::get, p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .model((ctx, prov) -> prov.generated(ctx::getEntry, new ResourceLocation(getOwner().getModid(), "item/" + bucketName)));
    }

    @Beta
    public FluidBuilder<O, T, P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return this;
    }

    /**
     * Assign {@link TagKey}{@code s} to this fluid and its source fluid. Multiple calls will add additional tags.
     *
     * @param tags
     *            The tags to assign
     * @return this {@link FluidBuilder}
     */
    @SafeVarargs
    public final FluidBuilder<O, T, P> tag(TagKey<Fluid>... tags) {
        FluidBuilder<O, T, P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(), ProviderType.FLUID_TAGS,
                prov -> this.tags.stream().map(prov::tag).forEach(p -> p.add(getSource())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    /**
     * Remove {@link TagKey}{@code s} from this fluid and its source fluid. Multiple calls will remove additional tags.
     *
     * @param tags
     *            The tags to remove
     * @return this {@link FluidBuilder}
     */
    @SafeVarargs
    public final FluidBuilder<O, T, P> removeTag(TagKey<Fluid>... tags) {
        this.tags.removeAll(Arrays.asList(tags));
        return this.removeTag(ProviderType.FLUID_TAGS, tags);
    }

    private ForgeFlowingFluid getSource() {
        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private ForgeFlowingFluid.Properties makeProperties() {
        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        ForgeFlowingFluid.Properties ret = new ForgeFlowingFluid.Properties(fluidType, source == null ? null : source::get, asSupplier());
        fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        RegistryEntry<Block> block = getOwner().getOptional(sourceName, ForgeRegistries.Keys.BLOCKS);
        this.typeProperties.accept(properties);

        // Force the translation key after the user callback runs
        // This is done because we need to remove the lang data generator if using the block key,
        // and if it was possible to undo this change, it might result in the user translation getting
        // silently lost, as there's no good way to check whether the translation key was changed.
        // TODO improve this?
        if (block.isPresent()) {
            properties.descriptionId(block.get().getDescriptionId());
            setData(ProviderType.LANG, NonNullBiConsumer.noop());
        } else {
            properties.descriptionId(Util.makeDescriptionId("fluid", new ResourceLocation(getOwner().getModid(), sourceName)));
        }

        return properties;
    }

    @Override
    protected T createEntry() {
        return fluidFactory.create(makeProperties());
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
                getOwner().simple(this, this.sourceName, ForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
            }
        } else {
            throw new IllegalStateException("Fluid must have a type: " + getName());
        }

        if (defaultSource == Boolean.TRUE) {
            source(ForgeFlowingFluid.Source::new);
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, ForgeRegistries.Keys.FLUIDS, (FluidBuilder) this, source::get);
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + getName());
        }

        return (FluidEntry<T>) super.register();
    }

    @Override
    protected RegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new FluidEntry<>(getOwner(), delegate);
    }

    // Basic default fluid type implementation.
    private static FluidType defaultFluidType(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return stillTexture;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowingTexture;
                    }
                });
            }
        };
    }
}