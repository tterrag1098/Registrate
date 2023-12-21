package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class RegistrateItemModelProvider extends ItemModelProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> parent;

    public RegistrateItemModelProvider(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, parent.getModid(), existingFileHelper);
        this.parent = parent;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    protected void registerModels() {
        parent.genData(ProviderType.ITEM_MODEL, this);
    }

    @Override
    public String getName() {
        return "Item models";
    }

    public String modid(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
    }

    public String name(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }

    public ResourceLocation itemTexture(NonNullSupplier<? extends ItemLike> item) {
        return modLoc("item/" + name(item));
    }

    public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block) {
        return blockItem(block, "");
    }

    public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block, String suffix) {
        return withExistingParent(name(block), new ResourceLocation(modid(block), "block/" + name(block) + suffix));
    }

    public ItemModelBuilder blockWithInventoryModel(NonNullSupplier<? extends ItemLike> block) {
        return withExistingParent(name(block), new ResourceLocation(modid(block), "block/" + name(block) + "_inventory"));
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block) {
        return blockSprite(block, modLoc("block/" + name(block)));
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block, ResourceLocation texture) {
        return generated(() -> block.get().asItem(), texture);
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item) {
        return generated(item, itemTexture(item));
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item, ResourceLocation... layers) {
        ItemModelBuilder ret = getBuilder(name(item)).parent(new ModelFile.UncheckedModelFile("item/generated"));
        for (int i = 0; i < layers.length; i++) {
            ret = ret.texture("layer" + i, layers[i]);
        }
        return ret;
    }

    public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item) {
        return handheld(item, itemTexture(item));
    }

    public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item, ResourceLocation texture) {
        return withExistingParent(name(item), "item/handheld").texture("layer0", texture);
    }
}
