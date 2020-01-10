package com.tterrag.registrate.builders;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * A builder for fluids, allows for customization of the {@link ForgeFlowingFluid.Properties} and {@link FluidAttributes}, and creation of the source variant, fluid block, and bucket item, as well as
 * data associated with fluids (tags, etc.).
 * 
 * @param <T>
 *            The type of fluid being built
 * @param <P>
 *            Parent object type
 */
public class FluidBuilder<T extends ForgeFlowingFluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {
    
    private static class Builder extends FluidAttributes.Builder {
        
        protected Builder(ResourceLocation still, ResourceLocation flowing, BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
            super(still, flowing, attributesFactory);
        }
    }

    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use the default attributes class ({@link FluidAttributes}) and fluid class ({@link ForgeFlowingFluid.Flowing}).
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
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, NonNullBiFunction, NonNullFunction)
     */
    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, (NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes>) null);
    }
    
    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use the default fluid class ({@link ForgeFlowingFluid.Flowing}).
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
     * @param attributesFactory
     *            A factory that creates the fluid attributes instance
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, NonNullBiFunction, NonNullFunction)
     */
    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            @Nullable NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, attributesFactory, ForgeFlowingFluid.Flowing::new);
    }
    
    /**
     * Create a new {@link FluidBuilder} and configure data. The created builder will use the default attributes class ({@link FluidAttributes}).
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
     * @param factory
     *            A factory that creates the flowing fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     * @see #create(AbstractRegistrate, Object, String, BuilderCallback, ResourceLocation, ResourceLocation, NonNullBiFunction, NonNullFunction)
     */
    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, null, factory);
    }
    
    /**
     * Create a new {@link FluidBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The fluid will be assigned the following data:
     * <ul>
     * <li>A default {@link ForgeFlowingFluid.Source source fluid} (via {@link #defaultSource})</li>
     * <li>A default block for the fluid, with its own default blockstate and model that configure the particle texture (via {@link #defaultBlock()})</li>
     * <li>A default bucket item, that uses a simple generated item model with a texture of the same name as this fluid (via {@link #defaultBucket()})</li>
     * <li>Tagged with {@link FluidTags#WATER}</li>
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
     * @param attributesFactory
     *            A factory that creates the fluid attributes instance
     * @param factory
     *            A factory that creates the flowing fluid
     * @return A new {@link FluidBuilder} with reasonable default data generators.
     */
    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            @Nullable NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, attributesFactory, factory)
                .defaultSource().defaultBlock().defaultBucket()
                .tag(FluidTags.WATER);

        return ret;
    }
    
    private final ResourceLocation stillTexture;
    private final String sourceName;
    private final String bucketName;
    private final NonNullSupplier<FluidAttributes.Builder> attributes;
    private final NonNullFunction<ForgeFlowingFluid.Properties, T> factory;
    
    private NonNullConsumer<FluidAttributes.Builder> attributesCallback = $ -> {};
    private NonNullConsumer<ForgeFlowingFluid.Properties> properties;
    @Nullable
    private NonNullSupplier<? extends ForgeFlowingFluid> source;
    
    protected FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            @Nullable BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
        super(owner, parent, "flowing_" + name, callback, Fluid.class);
        this.stillTexture = stillTexture;
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.attributes = () -> attributesFactory == null ? FluidAttributes.builder(stillTexture, flowingTexture) : new Builder(stillTexture, flowingTexture, attributesFactory);
        this.factory = factory;
        this.properties = p -> p.bucket(() -> getOwner().get(bucketName, Item.class).get())
                .block(() -> getOwner().<Block, FlowingFluidBlock>get(name, Block.class).get());
    }
    
    /**
     * Modify the attributes of the fluid. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     * 
     * @param cons
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> attributes(NonNullConsumer<FluidAttributes.Builder> cons) {
        attributesCallback = attributesCallback.andThen(cons);
        return this;
    }
    
    /**
     * Modify the properties of the fluid. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the properties
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> properties(NonNullConsumer<ForgeFlowingFluid.Properties> cons) {
        properties = properties.andThen(cons);
        return this;
    }

    /**
     * Create a standard {@link ForgeFlowingFluid.Source} for this fluid which will be built and registered along with this fluid.
     * 
     * @return this {@link FluidBuilder}
     * @see #source(NonNullFunction)
     */
    public FluidBuilder<T, P> defaultSource() {
        return source(ForgeFlowingFluid.Source::new);
    }

    /**
     * Create a {@link ForgeFlowingFluid} for this fluid, which is created by the given factory, and which will be built and registered along with this fluid.
     * 
     * @param factory
     *            A factory for the fluid, which accepts the properties and returns a new fluid
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> source(NonNullFunction<ForgeFlowingFluid.Properties, ? extends ForgeFlowingFluid> factory) {
        this.source = () -> factory.apply(makeProperties());
        return this;
    }
    
    /**
     * Create a standard {@link FlowingFluidBlock} for this fluid, building it immediately, and not allowing for further configuration.
     * 
     * @return this {@link FluidBuilder}
     * @see #block()
     */
    public FluidBuilder<T, P> defaultBlock() {
        return block().build();
    }

    /**
     * Create a standard {@link FlowingFluidBlock} for this fluid, and return the builder for it so that further customization can be done.
     * 
     * @return the {@link BlockBuilder} for the {@link FlowingFluidBlock}
     */
    public BlockBuilder<FlowingFluidBlock, FluidBuilder<T, P>> block() {
        return block(FlowingFluidBlock::new);
    }

    /**
     * Create a {@link FlowingFluidBlock} for this fluid, which is created by the given factory, and return the builder for it so that further customization can be done.
     * 
     * @param <B>
     *            The type of the block
     * @param factory
     *            A factory for the block, which accepts the block object and properties and returns a new block
     * @return the {@link BlockBuilder} for the {@link FlowingFluidBlock}
     */
    public <B extends FlowingFluidBlock> BlockBuilder<B, FluidBuilder<T, P>> block(NonNullBiFunction<NonNullSupplier<? extends T>, Block.Properties, ? extends B> factory) {
        return getOwner().<B, FluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(get().asNonNull(), p))
                .properties(p -> Block.Properties.from(Blocks.WATER).noDrops())
                .properties(p -> {
                    // TODO is this ok?
                    FluidAttributes attrs = this.attributes.get().build(Fluids.WATER);
                    return p.lightValue(attrs.getLuminosity());
                })
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.getBuilder(sourceName)
                                .texture("particle", stillTexture)));
    }
    
    /**
     * Create a standard {@link BucketItem} for this fluid, building it immediately, and not allowing for further configuration.
     * 
     * @return this {@link FluidBuilder}
     * @see #bucket()
     */
    public FluidBuilder<T, P> defaultBucket() {
        return bucket().build();
    }

    /**
     * Create a standard {@link BucketItem} for this fluid, and return the builder for it so that further customization can be done.
     * 
     * @return the {@link ItemBuilder} for the {@link BucketItem}
     */
    public ItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
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
    public <I extends BucketItem> ItemBuilder<I, FluidBuilder<T, P>> bucket(NonNullBiFunction<Supplier<? extends ForgeFlowingFluid>, Item.Properties, ? extends I> factory) {
        return getOwner().<I, FluidBuilder<T, P>>item(this, bucketName, p -> factory.apply(getSource(), p))
                .model((ctx, prov) -> prov.generated(ctx::getEntry, new ResourceLocation(getOwner().getModid(), "item/" + bucketName)));
    }
    
    /**
     * Assign a {@link Tag} to this fluid and its source fluid.
     * 
     * @param tag
     *            The tag to assign
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> tag(Tag<Fluid> tag) {
        FluidBuilder<T, P> ret = this.tag(ProviderType.FLUID_TAGS, tag);
        ret.getOwner().setDataGenerator(ret, ProviderType.FLUID_TAGS, prov -> prov.getBuilder(FluidTags.WATER).add(ret.getSource().get()));
        return ret;
    }
    
    private Supplier<ForgeFlowingFluid.Source> getSource() {
        return new LazyLoadBase<>(() -> getOwner().<Fluid, ForgeFlowingFluid.Source>get(sourceName, Fluid.class).get())::getValue;
    }
    
    private ForgeFlowingFluid.Properties makeProperties() {
        FluidAttributes.Builder attributes = this.attributes.get();
        attributesCallback.accept(attributes);
        ForgeFlowingFluid.Properties ret = new ForgeFlowingFluid.Properties(getSource(), get(), attributes);
        properties.accept(ret);
        return ret;
    }

    @Override
    protected T createEntry() {
        return factory.apply(makeProperties());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally registers the source fluid.
     */
    @Override
    public RegistryEntry<T> register() {
        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, Fluid.class, source);
        }
        return super.register();
    }
}
