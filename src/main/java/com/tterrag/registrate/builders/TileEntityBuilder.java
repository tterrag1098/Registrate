package com.tterrag.registrate.builders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

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
    public static <T extends TileEntity, P> TileEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullSupplier<? extends T> factory) {
        return new TileEntityBuilder<>(owner, parent, name, callback, factory);
    }

    private final NonNullSupplier<? extends T> factory;
    private final Set<NonNullSupplier<? extends Block>> validBlocks = new HashSet<>();

    protected TileEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullSupplier<? extends T> factory) {
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

    @Override
    protected TileEntityType<T> createEntry() {
        return TileEntityType.Builder.<T>create(factory, validBlocks.stream().map(NonNullSupplier::get).toArray(Block[]::new))
                .build(null);
    }
}
