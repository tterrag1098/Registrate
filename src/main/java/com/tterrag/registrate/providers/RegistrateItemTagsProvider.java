package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Function;

public class RegistrateItemTagsProvider extends RegistrateTagsProvider<Item> {

    private final Function<Named<Block>, Tag.Builder> builderLookup;

    @SuppressWarnings({"deprecation", "null"})
    public RegistrateItemTagsProvider(AbstractRegistrate<?> owner, ProviderType<RegistrateItemTagsProvider> type, String name, DataGenerator generatorIn, ExistingFileHelper existingFileHelper, RegistrateProvider blockTags) {
        super(owner, type, name, generatorIn, Registry.ITEM, existingFileHelper);
        this.builderLookup = blockTags::getOrCreateRawBuilder;
    }

    public void copy(Named<Block> p_240521_1_, Named<Item> p_240521_2_) {
        Tag.Builder itag$builder = this.createBuilderIfAbsent(p_240521_2_);
        Tag.Builder itag$builder1 = this.builderLookup.apply(p_240521_1_);
        itag$builder1.getEntries().forEach(itag$builder1::add);
    }
}
