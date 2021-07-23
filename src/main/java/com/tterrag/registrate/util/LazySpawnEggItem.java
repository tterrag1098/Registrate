package com.tterrag.registrate.util;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class LazySpawnEggItem<T extends Entity> extends SpawnEggItem {

    private final NonNullSupplier<EntityType<T>> typeIn;

    public LazySpawnEggItem(final NonNullSupplier<EntityType<T>> type, int primaryColor, int secondaryColor, Properties properties) {
        super(null, primaryColor, secondaryColor, properties);
        this.typeIn = type;
    }

    private static final Field _BY_ID = ObfuscationReflectionHelper.findField(SpawnEggItem.class, "field_195987_b");
    
    @SuppressWarnings("unchecked")
    public void injectType() {
        try {
            Map<EntityType<?>, SpawnEggItem> BY_ID = (Map<EntityType<?>, SpawnEggItem>) _BY_ID.get(this);
            BY_ID.put(typeIn.get(), this);
            BY_ID.remove(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityType<?> getType(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("EntityTag", 10)) {
            return super.getType(tag);
        }

        return this.typeIn.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide) {
            var itemStack = context.getItemInHand();
            var blockPos = context.getClickedPos();
            var direction = context.getClickedFace();
            var blockState = world.getBlockState(blockPos);
            if (blockState.is(Blocks.SPAWNER)) {
                var blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity instanceof SpawnerBlockEntity) {
                    var baseSpawner = ((SpawnerBlockEntity) blockEntity).getSpawner();
                    EntityType<?> entitytype1 = this.getType(itemStack.getTag());
                    baseSpawner.setEntityId(entitytype1);
                    blockEntity.setChanged();
                    world.sendBlockUpdated(blockPos, blockState, blockState, 3);
                    itemStack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }

            BlockPos blockpos1;
            if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
                blockpos1 = blockPos;
            } else {
                blockpos1 = blockPos.relative(direction);
            }

            EntityType<?> entitytype = this.getType(itemStack.getTag());
            if (entitytype.spawn((ServerLevel) world, itemStack, context.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockPos, blockpos1) && direction == Direction.UP) != null) {
                itemStack.shrink(1);
            }

        }
        return InteractionResult.SUCCESS;
    }

    public InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
        } else {
            var rayTraceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (rayTraceResult.getType() != HitResult.Type.BLOCK) {
                return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
            } else {
                var blockpos = rayTraceResult.getBlockPos();
                if (!(level.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
                    return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
                } else if (level.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos, rayTraceResult.getDirection(), itemstack)) {
                    EntityType<?> entitytype = this.getType(itemstack.getTag());
                    if (entitytype.spawn((ServerLevel) level, itemstack, player, blockpos, MobSpawnType.SPAWN_EGG, false, false) == null) {
                        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
                    } else {
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }

                        player.awardStat(Stats.ITEM_USED.get(this));
                        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
                    }
                } else {
                    return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
                }
            }
        }
    }
}
