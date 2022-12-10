package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider<Item> {

    private final Function<TagKey<Block>, TagBuilder> builderLookup;

    @SuppressWarnings("null")
    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, PackOutput output, CompletableFuture<HolderLookup.Provider> registriesLookup, ExistingFileHelper existingFileHelper, RegistrateTagsProvider<Block> blockTags) {
        super(owner, type, name, output, Registries.ITEM, registriesLookup, existingFileHelper);
        this.builderLookup = blockTags::getOrCreateRawBuilder;
    }

    public void copy(TagKey<Block> p_240521_1_, TagKey<Item> p_240521_2_) {
        TagBuilder itag$builder = this.getOrCreateRawBuilder(p_240521_2_);
        TagBuilder itag$builder1 = this.builderLookup.apply(p_240521_1_);
        itag$builder1.build().forEach(itag$builder::add);
    }
}
