package com.tterrag.registrate.providers;

import java.util.function.Function;

import com.tterrag.registrate.AbstractRegistrate;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider<Item> {

    private final Function<ITag.INamedTag<Block>, ITag.Builder> builderLookup;

    @SuppressWarnings({ "deprecation", "null" })
    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, DataGenerator generatorIn, ExistingFileHelper existingFileHelper, RegistrateTagsProvider<Block> blockTags) {
        super(owner, type, name, generatorIn, Registry.ITEM, existingFileHelper);
        this.builderLookup = blockTags::createBuilderIfAbsent;
    }

    public void copy(ITag.INamedTag<Block> p_240521_1_, ITag.INamedTag<Item> p_240521_2_) {
        ITag.Builder itag$builder = this.createBuilderIfAbsent(p_240521_2_);
        ITag.Builder itag$builder1 = this.builderLookup.apply(p_240521_1_);
        itag$builder1.getProxyStream().forEach(itag$builder::addProxyTag);
    }
}
