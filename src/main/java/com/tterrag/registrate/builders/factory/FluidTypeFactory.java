package com.tterrag.registrate.builders.factory;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface FluidTypeFactory
{
	@Nonnull FluidType create(@Nonnull FluidType.Properties properties, @Nonnull ResourceLocation stillTexture, @Nonnull ResourceLocation flowingTexture);
}