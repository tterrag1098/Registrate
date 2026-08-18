package com.tterrag.registrate.providers;

import net.minecraft.data.DataProvider;
import net.neoforged.fml.LogicalSide;

public interface RegistrateProvider extends DataProvider {

    LogicalSide getSide();
}
