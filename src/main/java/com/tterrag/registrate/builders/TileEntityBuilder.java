package com.tterrag.registrate.builders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.entry.TileEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * A builder for tile entities, allows for customization of the valid blocks.
 * 
 * @param <T>
 *            The type of tile entity being built
 * @param <P>
 *            Parent object type
 */
public class TileEntityBuilder<T extends TileEntity, P> extends AbstractBuilder<TileEntityType<?>, TileEntityType<T>, P, TileEntityBuilder<T, P>> {
    
    /**
     * Create a new {@link TileEntityBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The tile entity will be assigned the following data:
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
     *            Factory to create the tile entity
     * @return A new {@link TileEntityBuilder} with reasonable default data generators.
     */
    public static <T extends TileEntity, P> TileEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<TileEntityType<T>, ? extends T> factory) {
        return new TileEntityBuilder<>(owner, parent, name, callback, factory);
    }

    private final NonNullFunction<TileEntityType<T>, ? extends T> factory;
    private final Set<NonNullSupplier<? extends Block>> validBlocks = new HashSet<>();
    @Nullable
    private NonNullSupplier<Function<TileEntityRendererDispatcher, TileEntityRenderer<? super T>>> renderer;

    protected TileEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<TileEntityType<T>, ? extends T> factory) {
        super(owner, parent, name, callback, TileEntityType.class);
        this.factory = factory;
    }
    
    /**
     * Add a valid block for this tile entity.
     * 
     * @param block
     *            A supplier for the block to add at registration time
     * @return this {@link TileEntityBuilder}
     */
    public TileEntityBuilder<T, P> validBlock(NonNullSupplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }
    
    /**
     * Add valid blocks for this tile entity.
     * 
     * @param blocks
     *            An array of suppliers for the block to add at registration time
     * @return this {@link TileEntityBuilder}
     */
    @SafeVarargs
    public final TileEntityBuilder<T, P> validBlocks(NonNullSupplier<? extends Block>... blocks) {
        Arrays.stream(blocks).forEach(this::validBlock);
        return this;
    }
    
    public TileEntityBuilder<T, P> renderer(NonNullSupplier<Function<TileEntityRendererDispatcher, TileEntityRenderer<? super T>>> renderer) {
        if (this.renderer == null) { // First call only
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerRenderer));
        }
        this.renderer = renderer;
        return this;
    }
    
    protected void registerRenderer(FMLClientSetupEvent event) {
        NonNullSupplier<Function<TileEntityRendererDispatcher, TileEntityRenderer<? super T>>> renderer = this.renderer;
        if (renderer != null) {
            ClientRegistry.bindTileEntityRenderer(get(), renderer.get());
        }
    }

    @Override
    protected TileEntityType<T> createEntry() {
        return TileEntityType.Builder.<T>create(() -> factory.apply(get()), validBlocks.stream().map(NonNullSupplier::get).toArray(Block[]::new))
                .build(null);
    }
    
    @Override
    protected RegistryEntry<TileEntityType<T>> createEntryWrapper(RegistryObject<TileEntityType<T>> delegate) {
        return new TileEntityEntry<>(getOwner(), delegate);
    }
    
    @Override
    public TileEntityEntry<T> register() {
        return (TileEntityEntry<T>) super.register();
    }
}
