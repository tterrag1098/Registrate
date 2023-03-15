package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistrateItemTagsProvider extends RegistrateIntrinsicHolderTagsProvider<Item> {

    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, PackOutput output, CompletableFuture<HolderLookup.Provider> registriesLookup, CompletableFuture<TagsProvider.TagLookup<Item>> parentProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(owner, type, name, output, Registries.ITEM, registriesLookup, parentProvider, item -> item.builtInRegistryHolder().key(), existingFileHelper);
        this.blockTags = blockTags;
    }

    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, PackOutput output, CompletableFuture<HolderLookup.Provider> registriesLookup, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(owner, type, name, output, Registries.ITEM, registriesLookup, item -> item.builtInRegistryHolder().key(), existingFileHelper);
        this.blockTags = blockTags;
    }

    public void copy(TagKey<Block> p_206422_, TagKey<Item> p_206423_) {
        this.tagsToCopy.put(p_206422_, p_206423_);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider().thenCombineAsync(this.blockTags, (p_274766_, p_274767_) -> {
            this.tagsToCopy.forEach((p_274763_, p_274764_) -> {
                TagBuilder tagbuilder = this.getOrCreateRawBuilder(p_274764_);
                Optional<TagBuilder> optional = p_274767_.apply(p_274763_);
                optional.orElseThrow(() -> {
                    return new IllegalStateException("Missing block tag " + p_274764_.location());
                }).build().forEach(tagbuilder::add);
            });
            return p_274766_;
        });
    }
}
