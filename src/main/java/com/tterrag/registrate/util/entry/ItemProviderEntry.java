package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public class ItemProviderEntry<T extends ItemLike> extends RegistryEntry<T> {

    public ItemProviderEntry(AbstractRegistrate<?> owner, RegistryObject<T> delegate) {
        super(owner, delegate);
    }

    public ItemStack asStack() {
        return new ItemStack(get());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(get(), count);
    }

    public boolean isIn(ItemStack stack) {
        return is(stack.getItem());
    }

    public boolean is(Item item) {
        return get().asItem() == item;
    }
}
