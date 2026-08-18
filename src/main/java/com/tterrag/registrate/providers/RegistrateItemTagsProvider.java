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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider.Impl<Item> {

    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new LinkedHashMap<>();

    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, PackOutput output, CompletableFuture<HolderLookup.Provider> registriesLookup, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(owner, type, name, output, Registries.ITEM, registriesLookup);
        this.blockTags = blockTags;
    }

    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        this.tagsToCopy.put(blockTag, itemTag);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider().thenCombineAsync(this.blockTags, (provider, blockTagLookup) -> {
            this.tagsToCopy.forEach((blockTag, itemTag) -> {
                TagBuilder itemTagBuilder = this.getOrCreateRawBuilder(itemTag);
                Optional<TagBuilder> blockTagBuilder = blockTagLookup.apply(blockTag);
                blockTagBuilder.orElseThrow(() -> new IllegalStateException("Missing block tag " + blockTag.location())).build().forEach(itemTagBuilder::add);
            });
            return provider;
        });
    }
}
