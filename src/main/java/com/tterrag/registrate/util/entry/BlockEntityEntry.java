package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.RegistryObject;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockEntityEntry<T extends BlockEntity> extends RegistryEntry<BlockEntityType<T>> {

    public BlockEntityEntry(AbstractRegistrate<?> owner, RegistryObject<BlockEntityType<T>> delegate) {
        super(owner, delegate);
    }

    public static <T extends BlockEntity> BlockEntityEntry<T> cast(RegistryEntry<BlockEntityType<T>> entry) {
        return RegistryEntry.cast(BlockEntityEntry.class, entry);
    }

    /**
     * Create a "default" instance of this {@link BlockEntity} via the {@link BlockEntityType}.
     *
     * @return The instance
     */
    @SuppressWarnings("null")
    public T create(BlockPos pos, BlockState state) {
        return get().create(pos, state);
    }

    /**
     * Check that the given {@link BlockEntity} is an instance of this type.
     *
     * @param t The {@link BlockEntity} instance
     * @return {@code true} if the type matches, {@code false} otherwise.
     */
    public boolean is(@Nullable BlockEntity t) {
        return t != null && t.getType() == get();
    }

    /**
     * Get an instance of this {@link BlockEntity} from the world.
     *
     * @param world
     *            The world to look for the instance in
     * @param pos
     *            The position of the instance
     * @return An {@link Optional} containing the instance, if it exists and matches this type. Otherwise, {@link Optional#empty()}.
     */
    @SuppressWarnings("null")
    public Optional<T> get(Level world, BlockPos pos) {
        return Optional.ofNullable(getNullable(world, pos));
    }

    /**
     * Get an instance of this {@link BlockEntity} from the world.
     *
     * @param world
     *            The world to look for the instance in
     * @param pos
     *            The position of the instance
     * @return The instance, if it exists and matches this type. Otherwise, {@code null}.
     */
    @SuppressWarnings("unchecked")
    public @Nullable
    T getNullable(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return is(te) ? (T) te : null;
    }
}
