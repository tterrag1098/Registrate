package com.tterrag.registrate.providers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DebugMarkers;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import org.jspecify.annotations.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j2
public class RegistrateDataProvider implements DataProvider {

    @SuppressWarnings("null")
    static final BiMap<String, ProviderType<?>> TYPES = HashBiMap.create();

    static final Map<ResourceKey<? extends Registry<?>>, ProviderType<?>> TAG_TYPES = new ConcurrentHashMap<>();

    public static @Nullable String getTypeName(GeneratorType<?> type) {
        if (type instanceof ProviderType<?> prov)
            return TYPES.inverse().get(prov);
        return type.toString();
    }

    private final String mod;
    private final Map<ProviderType<?>, RegistrateProvider> subProviders = new LinkedHashMap<>();
    private final Map<GeneratorType<?>, Object> subGenerators = new LinkedHashMap<>();
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;

    public RegistrateDataProvider(AbstractRegistrate<?> parent, String modid, GatherDataEvent event) {
        this.mod = modid;
        this.registriesLookup = event.getLookupProvider();

        // For now, generate everything together
        /*
        EnumSet<LogicalSide> sides = EnumSet.noneOf(LogicalSide.class);
        if (event.includeServer()) {
            sides.add(LogicalSide.SERVER);
        }
        if (event.includeClient()) {
            sides.add(LogicalSide.CLIENT);
        }
        */

        //log.debug(DebugMarkers.DATA, "Gathering providers for sides: {}", sides);
        log.debug(DebugMarkers.DATA, "Gathering providers");
        Map<ProviderType<?>, RegistrateProvider> known = new HashMap<>();
        for (DataProviderInitializer.Sorted sorted :parent.getDataGenInitializer().getSortedProviders()) {
            ProviderType<?> type = sorted.type();
            var lookup = registriesLookup;
            if (sorted.parent() != null) lookup = ((RegistrateLookupFillerProvider) known.get(sorted.parent())).getFilledProvider();
            RegistrateProvider prov = ProviderType.create(type, parent, event, known, lookup);
            if (prov instanceof RegistrateTagsProvider<?> tagsProvider && TAG_TYPES.get(tagsProvider.registry()) != type) {
				throw new IllegalStateException("Tag providers must be registered through ProviderType::registerTag");
            }
            known.put(type, prov);
            // if (sides.contains(prov.getSide())) {
                log.debug(DebugMarkers.DATA, "Adding provider for type: {}", sorted.id());
                subProviders.put(type, prov);
            //}
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registriesLookup.thenCompose(provider -> {
            var list = Lists.<CompletableFuture<?>>newArrayList();

            for (Map.Entry<ProviderType<?>, RegistrateProvider> e : subProviders.entrySet()) {
                log.debug(DebugMarkers.DATA, "Generating data for type: {}", getTypeName(e.getKey()));
                list.add(e.getValue().run(cache));
            };

            return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Registrate Provider for " + mod + " [" + subProviders.values().stream().map(DataProvider::getName).collect(Collectors.joining(", ")) + "]";
    }

    @SuppressWarnings("unchecked")
    public <P> Optional<P> getSubProvider(GeneratorType<P> type) {
        if (type instanceof ProviderType<?> prov)
            return Optional.ofNullable((P) subProviders.get(prov));
        return Optional.ofNullable((P) subGenerators.get(type));
    }

    public <T> void putSubProvider(GeneratorType<? extends T> type, T gen) {
        subGenerators.put(type, gen);
    }

}
