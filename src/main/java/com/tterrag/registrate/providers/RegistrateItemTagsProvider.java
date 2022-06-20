package com.tterrag.registrate.providers;

import java.util.function.Function;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider<Item> {

    private final Function<TagKey<Block>, TagBuilder> builderLookup;

    @SuppressWarnings({ "deprecation", "null" })
    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, DataGenerator generatorIn, ExistingFileHelper existingFileHelper, RegistrateTagsProvider<Block> blockTags) {
        super(owner, type, name, generatorIn, Registry.ITEM, existingFileHelper);
        this.builderLookup = blockTags::getOrCreateRawBuilder;
    }

    public void copy(TagKey<Block> p_240521_1_, TagKey<Item> p_240521_2_) {
        TagBuilder itag$builder = this.getOrCreateRawBuilder(p_240521_2_);
        TagBuilder itag$builder1 = this.builderLookup.apply(p_240521_1_);
        itag$builder1.build().forEach(itag$builder::add);
    }
}
