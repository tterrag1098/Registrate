package com.tterrag.registrate.builders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * A builder for block entities, allows for customization of the valid blocks.
 * 
 * @param <T>
 *            The type of block entity being built
 * @param <P>
 *            Parent object type
 */
public class BlockEntityBuilder<T extends BlockEntity, P> extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<T>, P, BlockEntityBuilder<T, P>> {

    public interface BlockEntityFactory<T extends BlockEntity> {

        public T create(BlockEntityType<T> type, BlockPos pos, BlockState state);

    }

    /**
     * Create a new {@link BlockEntityBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The block entity will be assigned the following data:
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
     *            Factory to create the block entity
     * @return A new {@link BlockEntityBuilder} with reasonable default data generators.
     */
    public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        return new BlockEntityBuilder<>(owner, parent, name, callback, factory);
    }

    private final BlockEntityFactory<T> factory;
    private final Set<NonNullSupplier<? extends Block>> validBlocks = new HashSet<>();
    @Nullable
    private NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer;

    protected BlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        super(owner, parent, name, callback, Registries.BLOCK_ENTITY_TYPE);
        this.factory = factory;
    }
    
    /**
     * Add a valid block for this block entity.
     * 
     * @param block
     *            A supplier for the block to add at registration time
     * @return this {@link BlockEntityBuilder}
     */
    public BlockEntityBuilder<T, P> validBlock(NonNullSupplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }
    
    /**
     * Add valid blocks for this block entity.
     * 
     * @param blocks
     *            An array of suppliers for the block to add at registration time
     * @return this {@link BlockEntityBuilder}
     */
    @SafeVarargs
    public final BlockEntityBuilder<T, P> validBlocks(NonNullSupplier<? extends Block>... blocks) {
        Arrays.stream(blocks).forEach(this::validBlock);
        return this;
    }
    
    /**
     * Register an {@link BlockEntityRenderer} for this block entity.
     * <p>
     * 
     * @apiNote This requires the {@link Class} of the block entity object, which can only be gotten by inspecting an instance of it. Thus, the entity will be constructed to register the renderer.
     * 
     * @param renderer
     *            A (server safe) supplier to an {@link Function} that will provide this block entity's renderer given the renderer dispatcher
     * @return this {@link BlockEntityBuilder}
     */
    public BlockEntityBuilder<T, P> renderer(NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer) {
        if (this.renderer == null) { // First call only
            if(FMLEnvironment.dist == Dist.CLIENT){
               registerRenderer();
            }
        }
        this.renderer = renderer;
        return this;
    }
    
    protected void registerRenderer() {
        OneTimeEventReceiver.addModListener(getOwner(), FMLClientSetupEvent.class, $ -> {
            var renderer = this.renderer;
            if (renderer != null) {
                BlockEntityRenderers.register(getEntry(), renderer.get()::apply);
            }
        });
    }

    @Override
    protected BlockEntityType<T> createEntry() {
        BlockEntityFactory<T> factory = this.factory;
        final var supplier = asSupplier();
        return BlockEntityType.Builder.<T>of((pos, state) -> factory.create((BlockEntityType<T>) supplier.get(), pos, state), validBlocks.stream().map(NonNullSupplier::get).toArray(Block[]::new))
                .build(null);
    }
    
    @Override
    protected RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> createEntryWrapper(DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> delegate) {
        return new BlockEntityEntry<>(getOwner(), delegate);
    }
    
    @Override
    public BlockEntityEntry<T> register() {
        return (BlockEntityEntry<T>) super.register();
    }
}
