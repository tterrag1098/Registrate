package com.tterrag.registrate.builders.factory;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface FluidFactory<@NonnullType FLUID extends ForgeFlowingFluid>
{
	FluidFactory<ForgeFlowingFluid.Flowing> FLOWING = ForgeFlowingFluid.Flowing::new;

	@Nonnull FLUID create(@Nonnull ForgeFlowingFluid.Properties properties);
}