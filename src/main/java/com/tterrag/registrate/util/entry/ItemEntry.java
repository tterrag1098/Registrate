package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemEntry<T extends Item> extends ItemProviderEntry<T> {

    public ItemEntry(AbstractRegistrate<?> owner, DeferredHolder<? super T,T> delegate) {
        super(owner, delegate);
    }
    
    public static <T extends Item> ItemEntry<T> cast(RegistryEntry<T> entry) {
        return RegistryEntry.cast(ItemEntry.class, entry);
    }
}
