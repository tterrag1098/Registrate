package com.tterrag.registrate.builders;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
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

public class FluidBuilder<TSource extends ForgeFlowingFluid, TFlowing extends ForgeFlowingFluid, P> extends AbstractBuilder<Fluid, TSource, P, FluidBuilder<TSource, TFlowing, P>> {

    @FunctionalInterface
    public interface FluidTypeFactory {
        FluidType create(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture);
    }

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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, NonNullFunction, NonNullFunction)
     */
    public static <P> FluidBuilder<ForgeFlowingFluid.Source, ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, FluidBuilder::defaultFluidType, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new);
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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, NonNullFunction, NonNullFunction)
     */
    public static <P> FluidBuilder<ForgeFlowingFluid.Source, ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new);
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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, FluidTypeFactory, NonNullFunction, NonNullFunction)
     */
    public static <P> FluidBuilder<ForgeFlowingFluid.Source, ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use a default ({@link FluidType}) and fluid class ({@link ForgeFlowingFluid.Flowing}).
     *
     * @param <TSource>>
     *            The source fluid type
     * @param <TFlowing>
     *            The flowing fluid type
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
     * @param sourceFactory
     *            A factory that creates the source fluid
     * @param flowingFactory
     *            A factory that creates the source fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    public static <TSource extends ForgeFlowingFluid, TFlowing extends ForgeFlowingFluid, P> FluidBuilder<TSource, TFlowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        NonNullFunction<ForgeFlowingFluid.Properties, TSource> sourceFactory, NonNullFunction<ForgeFlowingFluid.Properties, TFlowing> flowingFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, FluidBuilder::defaultFluidType, sourceFactory, flowingFactory);
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>A default block for the fluid, with its own default blockstate and model that configure the particle texture (via {@link #defaultBlock()})</li>
     * <li>A default bucket item, that uses a simple generated item model with a texture of the same name as this fluid (via {@link #defaultBucket()})</li>
     * </ul>
     *
     * @param <TSource>>
     *            The source fluid type
     * @param <TFlowing>
     *            The flowing fluid type
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
     * @param sourceFactory
     *            A factory that creates the source fluid
     * @param flowingFactory
     *            A factory that creates the source fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    public static <TSource extends ForgeFlowingFluid, TFlowing extends ForgeFlowingFluid, P> FluidBuilder<TSource, TFlowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
        FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, TSource> sourceFactory, NonNullFunction<ForgeFlowingFluid.Properties, TFlowing> flowingFactory) {
        FluidBuilder<TSource, TFlowing, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, sourceFactory, flowingFactory)
            .defaultLang().defaultBlock().defaultBucket();
        return ret;
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>A default block for the fluid, with its own default blockstate and model that configure the particle texture (via {@link #defaultBlock()})</li>
     * <li>A default bucket item, that uses a simple generated item model with a texture of the same name as this fluid (via {@link #defaultBucket()})</li>
     * </ul>
     *
     * @param <TSource>>
     *            The source fluid type
     * @param <TFlowing>
     *            The flowing fluid type
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
     * @param sourceFactory
     *            A factory that creates the source fluid
     * @param flowingFactory
     *            A factory that creates the source fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    public static <TSource extends ForgeFlowingFluid, TFlowing extends ForgeFlowingFluid, P> FluidBuilder<TSource, TFlowing, P> create(
            AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType,
            NonNullFunction<ForgeFlowingFluid.Properties, TSource> sourceFactory, NonNullFunction<ForgeFlowingFluid.Properties, TFlowing> flowingFactory) {
        FluidBuilder<TSource, TFlowing, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, sourceFactory, flowingFactory)
                .defaultLang().defaultBlock().defaultBucket();
        return ret;
    }

    private final String flowingName, bucketName;

    private final ResourceLocation stillTexture, flowingTexture;
    private final NonNullFunction<ForgeFlowingFluid.Properties, ? extends TSource> sourceFactory;
    private final NonNullFunction<ForgeFlowingFluid.Properties, ? extends TFlowing> flowingFactory;

    @Nullable
    private final NonNullSupplier<FluidType> fluidType;

    @Nullable
    private Boolean defaultBlock, defaultBucket;

    private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};

    private NonNullConsumer<ForgeFlowingFluid.Properties> fluidProperties;

    private @Nullable Supplier<Supplier<RenderType>> layer = null;

    private boolean registerType;

    private NonNullSupplier<TFlowing> flowingFluid;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory,
                        NonNullFunction<ForgeFlowingFluid.Properties, ? extends TSource> sourceFactory, NonNullFunction<ForgeFlowingFluid.Properties, ? extends TFlowing> flowingFactory) {
        super(owner, parent, name, callback, ForgeRegistries.Keys.FLUIDS);
        this.flowingName = "flowing_" + name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.sourceFactory = sourceFactory;
        this.flowingFactory = flowingFactory;
        this.flowingFluid = NonNullSupplier.lazy(() -> this.flowingFactory.apply(makeProperties()));
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.create(makeTypeProperties(), this.stillTexture, this.flowingTexture));
        this.registerType = true;

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, ForgeRegistries.Keys.ITEMS).get())
            .block(() -> owner.<Block, LiquidBlock>get(name, ForgeRegistries.Keys.BLOCKS).get());
    }

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType,
                        NonNullFunction<ForgeFlowingFluid.Properties, TSource> sourceFactory, NonNullFunction<ForgeFlowingFluid.Properties, TFlowing> flowingFactory) {
        super(owner, parent, name, callback, ForgeRegistries.Keys.FLUIDS);
        this.flowingName = "flowing_" + name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.sourceFactory = sourceFactory;
        this.flowingFactory = flowingFactory;
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
    public FluidBuilder<TSource, TFlowing, P> properties(NonNullConsumer<FluidType.Properties> cons) {
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
    public FluidBuilder<TSource, TFlowing, P> fluidProperties(NonNullConsumer<ForgeFlowingFluid.Properties> cons) {
        fluidProperties = fluidProperties.andThen(cons);
        return this;
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#toEnglishName(String)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     *
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<TSource, TFlowing, P> defaultLang() {
        return lang(f -> f.getFluidType().getDescriptionId(), RegistrateLangProvider.toEnglishName(getName()));
    }

    /**
     * Set the translation for this fluid.
     *
     * @param name
     *            A localized English name
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<TSource, TFlowing, P> lang(String name) {
        return lang(f -> f.getFluidType().getDescriptionId(), name);
    }

    @SuppressWarnings("deprecation")
    public FluidBuilder<TSource, TFlowing, P> renderType(Supplier<Supplier<RenderType>> layer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get().get()), "Invalid render type: " + layer);
        });

        if (this.layer == null) {
            onRegister(this::registerRenderType);
        }
        this.layer = layer;
        return this;
    }

    @SuppressWarnings("deprecation")
    protected void registerRenderType(TSource entry) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            OneTimeEventReceiver.addModListener(getOwner(), FMLClientSetupEvent.class, $ -> {
                if (this.layer != null) {
                    RenderType layer = this.layer.get().get();
                    ItemBlockRenderTypes.setRenderLayer(entry, layer);
                    ItemBlockRenderTypes.setRenderLayer(flowingFluid.get(), layer);
                }
            });
        });
    }

    /**
     * Create a standard {@link LiquidBlock} for this fluid, building it immediately, and not allowing for further configuration.
     *
     * @return this {@link FluidBuilder}
     * @see #block()
     * @throws IllegalStateException
     *             If {@link #block()} or {@link #block(NonNullBiFunction)} has been called before this method
     */
    public FluidBuilder<TSource, TFlowing, P> defaultBlock() {
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
    public BlockBuilder<LiquidBlock, FluidBuilder<TSource, TFlowing, P>> block() {
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
    public <B extends LiquidBlock> BlockBuilder<B, FluidBuilder<TSource, TFlowing, P>> block(NonNullBiFunction<NonNullSupplier<? extends TSource>, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        NonNullSupplier<TSource> supplier = asSupplier();
        return getOwner().<B, FluidBuilder<TSource, TFlowing, P>>block(this, getName(), p -> factory.apply(supplier, p))
            .properties(p -> BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable())
            .properties(p -> p.lightLevel(blockState -> fluidType.get().getLightLevel()))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(getName())
                .texture("particle", stillTexture)));
    }

    @Beta
    public FluidBuilder<TSource, TFlowing, P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }

        // Remove block lookup
        this.fluidProperties = p -> p.block(null);
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
    public FluidBuilder<TSource, TFlowing, P> defaultBucket() {
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
    public ItemBuilder<BucketItem, FluidBuilder<TSource, TFlowing, P>> bucket() {
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
    public <I extends BucketItem> ItemBuilder<I, FluidBuilder<TSource, TFlowing, P>> bucket(NonNullBiFunction<Supplier<? extends ForgeFlowingFluid>, Item.Properties, ? extends I> factory) {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;

        return getOwner().<I, FluidBuilder<TSource, TFlowing, P>>item(this, bucketName, p -> factory.apply(() -> get().get(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .model((ctx, prov) -> prov.generated(ctx::getEntry, new ResourceLocation(getOwner().getModid(), "item/" + bucketName)));
    }

    @Beta
    public FluidBuilder<TSource, TFlowing, P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }

        // Disable bucket lookup
        fluidProperties = p -> p.bucket(null);
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
    public final FluidBuilder<TSource, TFlowing, P> tag(TagKey<Fluid>... tags) {
        FluidBuilder<TSource, TFlowing, P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.flowingName, getRegistryKey(), ProviderType.FLUID_TAGS,
                prov -> this.tags.stream().map(prov::addTag).forEach(p -> p.add(ret.flowingFluid.get().builtInRegistryHolder().key())));
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
    public final FluidBuilder<TSource, TFlowing, P> removeTag(TagKey<Fluid>... tags) {
        this.tags.removeAll(Arrays.asList(tags));
        return this.removeTag(ProviderType.FLUID_TAGS, tags);
    }

    private ForgeFlowingFluid.Properties makeProperties() {
        ForgeFlowingFluid.Properties ret = new ForgeFlowingFluid.Properties(fluidType, asSupplier(), flowingFluid);
        fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        RegistryEntry<Block> block = getOwner().getOptional(getName(), ForgeRegistries.Keys.BLOCKS);
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
            properties.descriptionId(Util.makeDescriptionId("fluid", new ResourceLocation(getOwner().getModid(), getName())));
        }

        return properties;
    }

    @Override
    protected TSource createEntry() {
        return sourceFactory.apply(makeProperties());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally registers the source fluid and the fluid type (if constructed).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public FluidEntry<TSource> register() {
        // Check the fluid has a type.
        if (this.fluidType != null) {
            // Register the type.
            if (this.registerType) {
                getOwner().simple(this, getName(), ForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
            }
        } else {
            throw new IllegalStateException("Fluid must have a type: " + getName());
        }

        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        getCallback().accept(flowingName, ForgeRegistries.Keys.FLUIDS, (FluidBuilder) this, flowingFluid);

        return (FluidEntry<TSource>) super.register();
    }

    @Override
    protected RegistryEntry<TSource> createEntryWrapper(RegistryObject<TSource> delegate) {
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
