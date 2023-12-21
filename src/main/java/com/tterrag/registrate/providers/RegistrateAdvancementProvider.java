package com.tterrag.registrate.providers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import lombok.extern.log4j.Log4j2;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Log4j2
public class RegistrateAdvancementProvider implements RegistrateProvider, Consumer<Advancement> {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    private final AbstractRegistrate<?> owner;
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    private final List<CompletableFuture<?>> advancementsToSave = Lists.newArrayList();

    public RegistrateAdvancementProvider(AbstractRegistrate<?> owner, PackOutput packOutputIn, CompletableFuture<HolderLookup.Provider> registriesLookupIn) {
        this.owner = owner;
        this.packOutput = packOutputIn;
        this.registriesLookup = registriesLookupIn;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public MutableComponent title(String category, String name, String title) {
        return owner.addLang("advancements", new ResourceLocation(category, name), "title", title);
    }

    public MutableComponent desc(String category, String name, String desc) {
        return owner.addLang("advancements", new ResourceLocation(category, name), "description", desc);
    }

    private @Nullable CachedOutput cache;
    private Set<ResourceLocation> seenAdvancements = new HashSet<>();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registriesLookup.thenCompose(lookup -> {
            advancementsToSave.clear();

            try {
                this.cache = cache;
                this.seenAdvancements.clear();
                owner.genData(ProviderType.ADVANCEMENT, this);
            } finally {
                this.cache = null;
            }

            return CompletableFuture.allOf(advancementsToSave.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public void accept(@Nullable Advancement t) {
        CachedOutput cache = this.cache;
        if (cache == null) {
            throw new IllegalStateException("Cannot accept advancements outside of act");
        }
        Objects.requireNonNull(t, "Cannot accept a null advancement");
        Path path = this.packOutput.getOutputFolder();
        if (!seenAdvancements.add(t.getId())) {
            throw new IllegalStateException("Duplicate advancement " + t.getId());
        } else {
            Path path1 = getPath(path, t);
            advancementsToSave.add(DataProvider.saveStable(cache, t.deconstruct().serializeToJson(), path1));
        }
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }

    public String getName() {
        return "Advancements";
    }
}
