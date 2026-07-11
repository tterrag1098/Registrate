package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class RegistrateGenericProvider implements RegistrateProvider
{
    private final AbstractRegistrate<?> registrate;
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final ProviderType<RegistrateGenericProvider> providerType;
    private final List<Generator> generators = Lists.newArrayList();

    @ApiStatus.Internal
    RegistrateGenericProvider(AbstractRegistrate<?> registrate, GatherDataEvent event, ProviderType<RegistrateGenericProvider> providerType)
    {
        this.registrate = registrate;
        this.providerType = providerType;

        output = event.getGenerator().getPackOutput();
        registries = event.getLookupProvider();
    }

    public RegistrateGenericProvider add(Generator generator)
    {
        generators.add(generator);
        return this;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache)
    {
        generators.clear();
        var data = new GeneratorData(output, registries);
        registrate.genData(providerType, this);
        return CompletableFuture.allOf(generators
                .stream()
                .map(generator -> generator.generate(data))
                .map(provider -> provider.run(cache))
                .toArray(CompletableFuture[]::new)
        );
    }

    @Override
    public String getName()
    {
        return "generic_provider";
    }

    public record GeneratorData(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    }

    @FunctionalInterface
    public interface Generator
    {
        DataProvider generate(GeneratorData data);
    }
}
