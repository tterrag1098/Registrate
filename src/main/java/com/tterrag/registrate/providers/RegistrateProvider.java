package com.tterrag.registrate.providers;

import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.LogicalSide;

public interface RegistrateProvider extends IDataProvider {

    ProviderType<?> getType();
    
    LogicalSide getSide();
}
