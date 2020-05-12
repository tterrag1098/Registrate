package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public class ItemEntry<T extends Item> extends ItemProviderEntry<T> {

    public ItemEntry(AbstractRegistrate<?> owner, RegistryObject<T> delegate) {
        super(owner, delegate);
    }
}
