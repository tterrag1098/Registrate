package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.extern.log4j.Log4j2;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Log4j2
public class RegistrateAdvancementProvider implements RegistrateProvider, Consumer<Advancement> {
    private final AbstractRegistrate<?> owner;
    private final DataGenerator generator;

    public RegistrateAdvancementProvider(AbstractRegistrate<?> owner, DataGenerator generatorIn) {
        this.owner = owner;
        this.generator = generatorIn;
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
    public void run(CachedOutput cache) throws IOException {
        try {
            this.cache = cache;
            this.seenAdvancements.clear();
            owner.genData(ProviderType.ADVANCEMENT, this);
        } finally {
            this.cache = null;
        }
    }
    
    @Override
    public void accept(@Nullable Advancement t) {
        CachedOutput cache = this.cache;
        if (cache == null) {
            throw new IllegalStateException("Cannot accept advancements outside of act");
        }
        Objects.requireNonNull(t, "Cannot accept a null advancement");
        Path path = this.generator.getOutputFolder();
        if (!seenAdvancements.add(t.getId())) {
            throw new IllegalStateException("Duplicate advancement " + t.getId());
        } else {
            Path path1 = getPath(path, t);

            try {
                DataProvider.saveStable(cache, t.deconstruct().serializeToJson(), path1);
            } catch (IOException ioexception) {
                log.error("Couldn't save advancement {}", path1, ioexception);
            }
        }
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }

    public String getName() {
        return "Advancements";
    }
}
