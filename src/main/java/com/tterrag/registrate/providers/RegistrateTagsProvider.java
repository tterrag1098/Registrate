package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.LogicalSide;

import java.util.concurrent.CompletableFuture;

public class RegistrateTagsProvider<T> extends TagsProvider<T> implements RegistrateProvider {

    private final AbstractRegistrate<?> owner;
    private final ProviderType<? extends RegistrateTagsProvider<T>> type;
    private final String name;

    public RegistrateTagsProvider(AbstractRegistrate<?> owner, ProviderType<? extends RegistrateTagsProvider<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup, ExistingFileHelper existingFileHelper) {
        super(packOutput, registryIn, registriesLookup, owner.getModid(), existingFileHelper);
        this.owner = owner;
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return "Tags (" + name + ")";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        owner.genData(type, this);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public TagAppender<T> tag(TagKey<T> tag) { return super.tag(tag); }

    @Override
    public TagBuilder getOrCreateRawBuilder(TagKey<T> tag) { return super.getOrCreateRawBuilder(tag); }
}
