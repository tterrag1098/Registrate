package com.tterrag.registrate.providers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tterrag.registrate.Registrate;

import lombok.extern.log4j.Log4j2;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;

@Log4j2
public class RegistrateAdvancementProvider implements RegistrateProvider, Consumer<Advancement> {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    private final Registrate owner;
    private final DataGenerator generator;

    public RegistrateAdvancementProvider(Registrate owner, DataGenerator generatorIn) {
        this.owner = owner;
        this.generator = generatorIn;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }
    
    public TranslationTextComponent title(String name, String title) {
        return owner.addLang("advancement." + name + ".title", title);
    }
    
    public TranslationTextComponent desc(String name, String desc) {
        return owner.addLang("advancement." + name + ".description", desc);
    }
    
    private DirectoryCache cache;
    private Set<ResourceLocation> seenAdvancements = Sets.newHashSet();

    @Override
    public void act(DirectoryCache cache) throws IOException {
        this.cache = cache;
        this.seenAdvancements.clear();
        owner.genData(ProviderType.ADVANCEMENT, this);
    }
    
    @Override
    public void accept(Advancement t) {
        Path path = this.generator.getOutputFolder();
        if (!seenAdvancements.add(t.getId())) {
            throw new IllegalStateException("Duplicate advancement " + t.getId());
        } else {
            Path path1 = getPath(path, t);

            try {
                IDataProvider.save(GSON, cache, t.copy().serialize(), path1);
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
