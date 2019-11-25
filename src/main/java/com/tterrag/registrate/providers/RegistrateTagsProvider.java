package com.tterrag.registrate.providers;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.tterrag.registrate.Registrate;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.TagsProvider;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.Tag.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.LogicalSide;

public class RegistrateTagsProvider<T> extends TagsProvider<T> implements RegistrateProvider {

    private final Registrate owner;
    private final ProviderType<RegistrateTagsProvider<T>> type;
    private final String name;
    private final Consumer<TagCollection<T>> callback;

    public RegistrateTagsProvider(Registrate owner, ProviderType<RegistrateTagsProvider<T>> type, String name, Consumer<TagCollection<T>> callback, DataGenerator generatorIn, Registry<T> registryIn) {
        super(generatorIn, registryIn);
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.callback = callback;
    }

    protected Path makePath(ResourceLocation id) {
        return this.generator.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + name + "/" + id.getPath() + ".json");
    }

    public String getName() {
        return "Tags (" + name + ")";
    }

    protected void setCollection(TagCollection<T> collection) {
        callback.accept(collection);
    }

    @Override
    protected void registerTags() {
        owner.genData(type, this);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }
    
    @Override
    public Builder<T> getBuilder(Tag<T> tagIn) { return super.getBuilder(tagIn); }
}
