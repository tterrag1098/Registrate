package com.tterrag.registrate.providers;

import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface RegistrateProviderDelegate<R extends IForgeRegistryEntry<R>, T extends R> extends IDataProvider {
    
    String getName();
    
    ResourceLocation getId();
    
    T getEntry();
}