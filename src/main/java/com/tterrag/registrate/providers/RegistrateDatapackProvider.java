package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistrateDatapackProvider extends DatapackBuiltinEntriesProvider implements RegistrateLookupFillerProvider {

	public RegistrateDatapackProvider(AbstractRegistrate<?> parent, PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, RegistryPatchGenerator.createLookup(provider, parent.getDataGenInitializer().getDatapackRegistryProviders()), Set.of(parent.getModid()));
	}

	@Override
	public CompletableFuture<HolderLookup.Provider> getFilledProvider() {
		return getRegistryProvider();
	}

	@Override
	public LogicalSide getSide() {
		return LogicalSide.SERVER;
	}

}
