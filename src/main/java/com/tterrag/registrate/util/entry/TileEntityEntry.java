package com.tterrag.registrate.util.entry;

import java.util.Optional;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;

public class TileEntityEntry<T extends TileEntity> extends RegistryEntry<TileEntityType<T>> {

    public TileEntityEntry(AbstractRegistrate<?> owner, RegistryObject<TileEntityType<T>> delegate) {
        super(owner, delegate);
    }

    /**
     * Create a "default" instance of this {@link TileEntity} via the {@link TileEntityType}.
     * 
     * @return The instance
     */
    @SuppressWarnings("null")
    public T create() {
        return get().create();
    }

    /**
     * Check that the given {@link TileEntity} is an instance of this type.
     * 
     * @param t
     *            The {@link TileEntity} instance
     * @return {@code true} if the type matches, {@code false} otherwise.
     */
    public boolean is(@Nullable TileEntity t) {
        return t != null && t.getType() == get();
    }

    /**
     * Get an instance of this {@link TileEntity} from the world.
     * 
     * @param world
     *            The world to look for the instance in
     * @param pos
     *            The position of the instance
     * @return An {@link Optional} containing the instance, if it exists and matches this type. Otherwise, {@link Optional#empty()}.
     */
    @SuppressWarnings("null")
    public Optional<T> get(IBlockReader world, BlockPos pos) {
        return Optional.ofNullable(getNullable(world, pos));
    }

    /**
     * Get an instance of this {@link TileEntity} from the world.
     * 
     * @param world
     *            The world to look for the instance in
     * @param pos
     *            The position of the instance
     * @return The instance, if it exists and matches this type. Otherwise, {@code null}.
     */
    @SuppressWarnings("unchecked")
    public @Nullable T getNullable(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return is(te) ? (T) te : null;
    }

    public static <T extends TileEntity> TileEntityEntry<T> cast(RegistryEntry<TileEntityType<T>> entry) {
        return RegistryEntry.cast(TileEntityEntry.class, entry);
    }
}
