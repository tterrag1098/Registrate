package com.tterrag.registrate.builders;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;

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
import net.minecraftforge.fml.RegistryObject;

public class FluidBuilder<T extends ForgeFlowingFluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {
    
    private static class Builder extends FluidAttributes.Builder {
        
        protected Builder(ResourceLocation still, ResourceLocation flowing, BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
            super(still, flowing, attributesFactory);
        }
    }
    
    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(Registrate owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, (BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes>) null);
    }
    
    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(Registrate owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, attributesFactory, ForgeFlowingFluid.Flowing::new);
    }
    
    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(Registrate owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            Function<ForgeFlowingFluid.Properties, T> factory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, null, factory);
    }
    
    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(Registrate owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, Function<ForgeFlowingFluid.Properties, T> factory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, attributesFactory, factory)
                .defaultSource().defaultBlock().defaultBucket()
                .tag(FluidTags.WATER);

        return ret;
    }
    
    private final ResourceLocation stillTexture;
    private final String sourceName;
    private final String bucketName;
    private final Supplier<FluidAttributes.Builder> attributes;
    private final Function<ForgeFlowingFluid.Properties, T> factory;
    
    private Consumer<FluidAttributes.Builder> attributesCallback = $ -> {};
    private Consumer<ForgeFlowingFluid.Properties> properties;
    private Supplier<? extends ForgeFlowingFluid> source;
    
    protected FluidBuilder(Registrate owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
            BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory, Function<ForgeFlowingFluid.Properties, T> factory) {
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
     * <p>
     * If a different properties instance is returned, it will replace the existing one entirely.
     * 
     * @param func
     *            The action to perform on the attributes
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> attributes(Consumer<FluidAttributes.Builder> cons) {
        attributesCallback = attributesCallback.andThen(cons);
        return this;
    }
    
    /**
     * Modify the properties of the fluid. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param func
     *            The action to perform on the properties
     * @return this {@link FluidBuilder}
     */
    public FluidBuilder<T, P> properties(Consumer<ForgeFlowingFluid.Properties> cons) {
        properties = properties.andThen(cons);
        return this;
    }
    
    public FluidBuilder<T, P> defaultBlock() {
        return block().build();
    }

    public BlockBuilder<FlowingFluidBlock, FluidBuilder<T, P>> block() {
        return block(FlowingFluidBlock::new);
    }

    public <I extends FlowingFluidBlock> BlockBuilder<I, FluidBuilder<T, P>> block(BiFunction<Supplier<? extends T>, Block.Properties, ? extends I> factory) {
        return getOwner().<I, FluidBuilder<T, P>>block(this, p -> factory.apply(get(), p))
                .properties(p -> Block.Properties.from(Blocks.WATER).noDrops())
                .properties(p -> {
                    // TODO is this ok?
                    FluidAttributes attrs = this.attributes.get().build(Fluids.WATER);
                    return p.lightValue(attrs.getLuminosity());
                })
                .blockstate(ctx -> ctx.getProvider()
                        .simpleBlock(ctx.getEntry(), ctx.getProvider().getBuilder(sourceName)
                                .texture("particle", stillTexture)));
    }
    
    public FluidBuilder<T, P> defaultBucket() {
        return bucket().build();
    }

    public ItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
        return bucket(BucketItem::new);
    }

    public <I extends BucketItem> ItemBuilder<I, FluidBuilder<T, P>> bucket(BiFunction<Supplier<? extends ForgeFlowingFluid>, Item.Properties, ? extends I> factory) {
        return getOwner().<I, FluidBuilder<T, P>>item(this, bucketName, p -> factory.apply(getSource(), p))
                .model(ctx -> ctx.getProvider().generated(ctx::getEntry, new ResourceLocation(getOwner().getModid(), "item/" + bucketName)));
    }

    public FluidBuilder<T, P> defaultSource() {
        return source(ForgeFlowingFluid.Source::new);
    }

    public FluidBuilder<T, P> source(Function<ForgeFlowingFluid.Properties, ? extends ForgeFlowingFluid> factory) {
        this.source = () -> factory.apply(makeProperties());
        return this;
    }
    
    public FluidBuilder<T, P> tag(Tag<Fluid> tag) {
        FluidBuilder<T, P> ret = this.tag(ProviderType.FLUID_TAGS, tag);
        ret.getOwner().addDataGenerator(ret.sourceName, ProviderType.FLUID_TAGS, prov -> prov.getBuilder(FluidTags.WATER).add(ret.getSource().get()));
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
    
    @Override
    public RegistryObject<T> register() {
        getCallback().accept(sourceName, Fluid.class, source);
        return super.register();
    }
}
