package com.tterrag.registrate.providers;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.tterrag.registrate.Registrate;

import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class RegistrateDataProvider implements IDataProvider {

    static final Map<String, ProviderType<?>> TYPES = new LinkedHashMap<>();

    private final String mod;
    private final Map<ProviderType<?>, RegistrateProvider> subProviders = new LinkedHashMap<>();

    public RegistrateDataProvider(Registrate parent, String modid, GatherDataEvent event) {
        this.mod = modid;
        EnumSet<LogicalSide> sides = EnumSet.noneOf(LogicalSide.class);
        if (event.includeServer()) {
            sides.add(LogicalSide.SERVER);
        }
        if (event.includeClient()) {
            sides.add(LogicalSide.CLIENT);
        }
        for (ProviderType<?> type : TYPES.values()) {
            RegistrateProvider prov = type.create(parent, event, subProviders);
            if (sides.contains(prov.getSide())) {
                subProviders.put(type, prov);
            }
        }
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        for (RegistrateProvider provider : subProviders.values()) {
            provider.act(cache);
        }
    }

    @Override
    public String getName() {
        return "Registrate Provider for " + mod + " [" + subProviders.values().stream().map(IDataProvider::getName).collect(Collectors.joining(", ")) + "]";
    }
}
