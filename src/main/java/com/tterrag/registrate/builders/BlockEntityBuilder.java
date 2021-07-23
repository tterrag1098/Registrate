package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmllegacy.RegistryObject;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * A builder for tile entities, allows for customization of the valid blocks.
 *
 * @param <T> The type of tile entity being built
 * @param <P> Parent object type
 */
public class BlockEntityBuilder<T extends BlockEntity, P> extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<T>, P, BlockEntityBuilder<T, P>> {

    private final BlockEntitySupplier<? extends T> factory;
    @Nullable
    private BlockEntityRendererProvider<T> renderer;
    private final Set<NonNullSupplier<? extends Block>> validBlocks = new HashSet<>();

    protected BlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntitySupplier<? extends T> factory) {
        super(owner, parent, name, callback, BlockEntityType.class);
        this.factory = factory;
    }

    /**
     * Create a new {@link BlockEntityBuilder} and configure data. Used in lieu of adding side-effects to constructor,
     * so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The tile entity will be assigned the following data:
     *
     * @param <T>      The type of the builder
     * @param <P>      Parent object type
     * @param owner    The owning {@link AbstractRegistrate} object
     * @param parent   The parent object
     * @param name     Name of the entry being built
     * @param callback A callback used to actually register the built entry
     * @param factory  Factory to create the tile entity
     * @return A new {@link BlockEntityBuilder} with reasonable default data generators.
     */
    public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntitySupplier<? extends T> factory) {
        return new BlockEntityBuilder<>(owner, parent, name, callback, factory);
    }

    /**
     * Add a valid block for this tile entity.
     *
     * @param block A supplier for the block to add at registration time
     * @return this {@link BlockEntityBuilder}
     */
    public BlockEntityBuilder<T, P> validBlock(NonNullSupplier<? extends Block> block) {
        validBlocks.add(block);
        return this;
    }

    /**
     * Add valid blocks for this tile entity.
     *
     * @param blocks An array of suppliers for the block to add at registration time
     * @return this {@link BlockEntityBuilder}
     */
    @SafeVarargs
    public final BlockEntityBuilder<T, P> validBlocks(NonNullSupplier<? extends Block>... blocks) {
        Arrays.stream(blocks).forEach(this::validBlock);
        return this;
    }

    /**
     * Register an {@link BlockEntityRenderer} for this tile entity.
     * <p>
     *
     * @param renderer A (server safe) supplier to an {@link Function} that will provide this tile entity's renderer
     *                 given the renderer dispatcher
     * @return this {@link BlockEntityBuilder}
     * @apiNote This requires the {@link Class} of the tile entity object, which can only be gotten by inspecting an
     * instance of it. Thus, the entity will be constructed to register the renderer.
     */
    public BlockEntityBuilder<T, P> renderer(BlockEntityRendererProvider<T> renderer) {
        if (this.renderer == null) { // First call only
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> this::registerRenderer);
        }
        this.renderer = renderer;
        return this;
    }

    protected void registerRenderer() {
        OneTimeEventReceiver.addModListener(FMLClientSetupEvent.class, $ -> {
            var renderer = this.renderer;
            if (renderer != null) {
                BlockEntityRenderers.register(getEntry(), renderer);
            }
        });
    }

    @Override
    protected BlockEntityType<T> createEntry() {
        var factory = this.factory;
        return BlockEntityType.Builder.<T> of(factory::create, validBlocks.stream()
                .map(NonNullSupplier::get)
                .toArray(Block[]::new)
        ).build(null);
    }

    @Override
    protected RegistryEntry<BlockEntityType<T>> createEntryWrapper(RegistryObject<BlockEntityType<T>> delegate) {
        return new BlockEntityEntry<>(getOwner(), delegate);
    }

    @Override
    public BlockEntityEntry<T> register() {
        return (BlockEntityEntry<T>) super.register();
    }
}
