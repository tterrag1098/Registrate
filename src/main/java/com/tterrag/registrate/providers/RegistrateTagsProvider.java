package com.tterrag.registrate.providers;

import java.nio.file.Path;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.TagsProvider;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.LogicalSide;

public class RegistrateTagsProvider<T> extends TagsProvider<T> implements RegistrateProvider {

    private final AbstractRegistrate<?> owner;
    private final ProviderType<? extends RegistrateTagsProvider<T>> type;
    private final String name;

    public RegistrateTagsProvider(AbstractRegistrate<?> owner, ProviderType<? extends RegistrateTagsProvider<T>> type, String name, DataGenerator generatorIn, Registry<T> registryIn, ExistingFileHelper existingFileHelper) {
        super(generatorIn, registryIn, owner.getNamespace(), existingFileHelper);
        this.owner = owner;
        this.type = type;
        this.name = name;
    }

    protected Path makePath(ResourceLocation id) {
        return this.generator.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + name + "/" + id.getPath() + ".json");
    }

    public String getName() {
        return "Tags (" + name + ")";
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
    public Builder<T> getOrCreateBuilder(INamedTag<T> tag) { return super.getOrCreateBuilder(tag); }

    @Override
    public ITag.Builder createBuilderIfAbsent(INamedTag<T> tag) { return super.createBuilderIfAbsent(tag); }
}
