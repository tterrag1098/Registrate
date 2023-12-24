package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemProviderEntry<R extends ItemLike, T extends R> extends RegistryEntry<R, T> implements ItemLike {

    public ItemProviderEntry(AbstractRegistrate<?> owner, DeferredHolder<R, T> delegate) {
        super(owner, delegate);
    }

    public ItemStack asStack() {
        return new ItemStack(this);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(this, count);
    }

    public boolean isIn(ItemStack stack) {
        return is(stack.getItem());
    }

    public boolean is(Item item) {
        return asItem() == item;
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
