package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class TileEntityEntry<T extends TileEntity> extends RegistryEntry<TileEntityType<T>> {

    public TileEntityEntry(AbstractRegistrate<?> owner, RegistryObject<TileEntityType<T>> delegate) {
        super(owner, delegate);
    }

    public T create() {
        return get().create();
    }
    
    public boolean is(TileEntity t) {
        return t != null && t.getType() == get();
    }

    public static <T extends TileEntity> TileEntityEntry<T> cast(RegistryEntry<TileEntityType<T>> entry) {
        return RegistryEntry.cast(TileEntityEntry.class, entry);
    }
}
