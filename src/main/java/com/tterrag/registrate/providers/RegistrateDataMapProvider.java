package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class RegistrateDataMapProvider extends DataMapProvider implements RegistrateProvider {

	private final AbstractRegistrate<?> parent;

	private HolderLookup.@Nullable Provider provider;

	protected RegistrateDataMapProvider(AbstractRegistrate<?> parent, PackOutput output, CompletableFuture<HolderLookup.Provider> pvd) {
		super(output, pvd);
		this.parent = parent;
	}

	@Override
	public LogicalSide getSide() {
		return LogicalSide.SERVER;
	}

	/**
	 * Generate data map entries.
	 *
	 * @param provider
	 */
	@Override
	protected void gather(HolderLookup.Provider provider) {
		this.provider = provider;
		parent.genData(ProviderType.DATA_MAP, this);
		this.provider = null;
	}

	public HolderLookup.Provider getProvider() {
		if (provider == null) throw new IllegalStateException("Holder Lookup Provider is not available now");
		return provider;
	}

}
