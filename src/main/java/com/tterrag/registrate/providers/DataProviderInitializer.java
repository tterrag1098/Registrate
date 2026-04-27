package com.tterrag.registrate.providers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;

import org.jspecify.annotations.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataProviderInitializer {

	private final RegistrySetBuilder datapackEntryProvider = new RegistrySetBuilder();

	private final Map<ProviderType<?>, ProviderType<? extends RegistrateLookupFillerProvider>> providerDependencies = new ConcurrentHashMap<>();

	public DataProviderInitializer() {
		addDependency(ProviderType.ITEM_TAGS, ProviderType.BLOCK_TAGS);
	}

	protected RegistrySetBuilder getDatapackRegistryProviders() {
		return datapackEntryProvider;
	}

	protected List<Sorted> getSortedProviders() {
		List<Sorted> ans = new ArrayList<>();
		Set<ProviderType<?>> added = new HashSet<>();
		List<Map.Entry<String, ProviderType<?>>> remain = new ArrayList<>(RegistrateDataProvider.TYPES.entrySet());
		while (!remain.isEmpty()) {
			if (!remain.removeIf(e -> {
				ProviderType<?> type = e.getValue();
				var parent = providerDependencies.get(type);
				if (parent == null || added.contains(parent)) {
					ans.add(new Sorted(e.getKey(), type, parent));
					added.add(type);
					return true;
				}
				return false;
			})) throw new IllegalStateException("Looping dependency detected: " + remain);
		}
		return ans;
	}

	public <T> void add(ResourceKey<Registry<T>> registry, RegistrySetBuilder.RegistryBootstrap<T> provider) {
		datapackEntryProvider.add(registry, provider);
	}

	public void addDependency(ProviderType<?> dependent, ProviderType<? extends RegistrateLookupFillerProvider> parent) {
		var old = providerDependencies.put(dependent, parent);
		if (old != null) throw new IllegalStateException("Providers can have only 1 prerequisite");
	}

	public record Sorted(
			String id, ProviderType<?> type,
			@Nullable ProviderType<? extends RegistrateLookupFillerProvider> parent
	) {
	}

}
