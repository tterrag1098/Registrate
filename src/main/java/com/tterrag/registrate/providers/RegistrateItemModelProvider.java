package com.tterrag.registrate.providers;

import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.fml.LogicalSide;

public class RegistrateItemModelProvider extends ItemModelProvider implements RegistrateProvider {
    
    private final Registrate parent;

    public RegistrateItemModelProvider(Registrate parent, DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, parent.getModid(), existingFileHelper);
        this.parent = parent;
    }
    
    @Override
    public ProviderType<RegistrateItemModelProvider> getType() {
        return ProviderType.ITEM_MODEL;
    }
    
    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
    
    @Override
    protected void registerModels() {
        parent.genData(getType(), this);
    }
    
    @Override
    public String getName() {
        return "Item models";
    }
    
    public String modid(Supplier<? extends IItemProvider> item) {
        return item.get().asItem().getRegistryName().getNamespace();
    }
    
    public String name(Supplier<? extends IItemProvider> item) {
        return item.get().asItem().getRegistryName().getPath();
    }
    
    public ResourceLocation itemTexture(Supplier<? extends IItemProvider> item) {
        return modLoc("item/" + name(item));
    }
    
    public ItemModelBuilder blockItem(Supplier<? extends Block> block) {
        return blockItem(block, "");
    }
    
    public ItemModelBuilder blockItem(Supplier<? extends Block> block, String suffix) {
        return withExistingParent(name(block), new ResourceLocation(modid(block), "block/" + name(block) + suffix));
    }

    public ItemModelBuilder blockWithInventoryModel(Supplier<? extends Block> block) {
        return withExistingParent(name(block), new ResourceLocation(modid(block), "block/" + name(block) + "_inventory"));
    }
    
    public ItemModelBuilder blockSprite(Supplier<? extends Block> block) {
        return blockSprite(block, modLoc("block/" + name(block)));
    }
    
    public ItemModelBuilder blockSprite(Supplier<? extends Block> block, ResourceLocation texture) {
        return generated(() -> block.get().asItem(), texture);
    }
    
    public ItemModelBuilder generated(Supplier<? extends IItemProvider> item) {
        return generated(item, itemTexture(item));
    }

    public ItemModelBuilder generated(Supplier<? extends IItemProvider> item, ResourceLocation... layers) {
        ItemModelBuilder ret = getBuilder(name(item)).parent(new UncheckedModelFile("item/generated"));
        for (int i = 0; i < layers.length; i++) {
            ret = ret.texture("layer" + i, layers[i]);
        }
        return ret;
    }
    
    public ItemModelBuilder handheld(Supplier<? extends IItemProvider> item) {
        return handheld(item, itemTexture(item));
    }
    
    public ItemModelBuilder handheld(Supplier<? extends IItemProvider> item, ResourceLocation texture) {
        return withExistingParent(name(item), "item/handheld").texture("layer0", texture);
    }

    // GENERATED START

    @Override
    public ItemModelBuilder getBuilder(String path) { return super.getBuilder(path); }

    @Override
    public ResourceLocation modLoc(String name) { return super.modLoc(name); }

    @Override
    public ResourceLocation mcLoc(String name) { return super.mcLoc(name); }

    @Override
    public ItemModelBuilder withExistingParent(String name, String parent) { return super.withExistingParent(name, parent); }

    @Override
    public ItemModelBuilder withExistingParent(String name, ResourceLocation parent) { return super.withExistingParent(name, parent); }

    @Override
    public ItemModelBuilder cube(String name, ResourceLocation down, ResourceLocation up, ResourceLocation north, ResourceLocation south, ResourceLocation east, ResourceLocation west) { return super.cube(name, down, up, north, south, east, west); }

    @Override
    public ItemModelBuilder singleTexture(String name, ResourceLocation parent, ResourceLocation texture) { return super.singleTexture(name, parent, texture); }

    @Override
    public ItemModelBuilder singleTexture(String name, ResourceLocation parent, String textureKey, ResourceLocation texture) { return super.singleTexture(name, parent, textureKey, texture); }

    @Override
    public ItemModelBuilder cubeAll(String name, ResourceLocation texture) { return super.cubeAll(name, texture); }

    @Override
    public ItemModelBuilder cubeTop(String name, ResourceLocation side, ResourceLocation top) { return super.cubeTop(name, side, top); }

    @Override
    public ItemModelBuilder cubeBottomTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.cubeBottomTop(name, side, bottom, top); }

    @Override
    public ItemModelBuilder cubeColumn(String name, ResourceLocation side, ResourceLocation end) { return super.cubeColumn(name, side, end); }

    @Override
    public ItemModelBuilder orientableVertical(String name, ResourceLocation side, ResourceLocation front) { return super.orientableVertical(name, side, front); }

    @Override
    public ItemModelBuilder orientableWithBottom(String name, ResourceLocation side, ResourceLocation front, ResourceLocation bottom, ResourceLocation top) { return super.orientableWithBottom(name, side, front, bottom, top); }

    @Override
    public ItemModelBuilder orientable(String name, ResourceLocation side, ResourceLocation front, ResourceLocation top) { return super.orientable(name, side, front, top); }

    @Override
    public ItemModelBuilder crop(String name, ResourceLocation crop) { return super.crop(name, crop); }

    @Override
    public ItemModelBuilder cross(String name, ResourceLocation cross) { return super.cross(name, cross); }

    @Override
    public ItemModelBuilder stairs(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.stairs(name, side, bottom, top); }

    @Override
    public ItemModelBuilder stairsOuter(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.stairsOuter(name, side, bottom, top); }

    @Override
    public ItemModelBuilder stairsInner(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.stairsInner(name, side, bottom, top); }

    @Override
    public ItemModelBuilder slab(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.slab(name, side, bottom, top); }

    @Override
    public ItemModelBuilder slabTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.slabTop(name, side, bottom, top); }

    @Override
    public ItemModelBuilder fencePost(String name, ResourceLocation texture) { return super.fencePost(name, texture); }

    @Override
    public ItemModelBuilder fenceSide(String name, ResourceLocation texture) { return super.fenceSide(name, texture); }

    @Override
    public ItemModelBuilder fenceInventory(String name, ResourceLocation texture) { return super.fenceInventory(name, texture); }

    @Override
    public ItemModelBuilder fenceGate(String name, ResourceLocation texture) { return super.fenceGate(name, texture); }

    @Override
    public ItemModelBuilder fenceGateOpen(String name, ResourceLocation texture) { return super.fenceGateOpen(name, texture); }

    @Override
    public ItemModelBuilder fenceGateWall(String name, ResourceLocation texture) { return super.fenceGateWall(name, texture); }

    @Override
    public ItemModelBuilder fenceGateWallOpen(String name, ResourceLocation texture) { return super.fenceGateWallOpen(name, texture); }

    @Override
    public ItemModelBuilder wallPost(String name, ResourceLocation wall) { return super.wallPost(name, wall); }

    @Override
    public ItemModelBuilder wallSide(String name, ResourceLocation wall) { return super.wallSide(name, wall); }

    @Override
    public ItemModelBuilder wallInventory(String name, ResourceLocation wall) { return super.wallInventory(name, wall); }

    @Override
    public ItemModelBuilder panePost(String name, ResourceLocation pane, ResourceLocation edge) { return super.panePost(name, pane, edge); }

    @Override
    public ItemModelBuilder paneSide(String name, ResourceLocation pane, ResourceLocation edge) { return super.paneSide(name, pane, edge); }

    @Override
    public ItemModelBuilder paneSideAlt(String name, ResourceLocation pane, ResourceLocation edge) { return super.paneSideAlt(name, pane, edge); }

    @Override
    public ItemModelBuilder paneNoSide(String name, ResourceLocation pane) { return super.paneNoSide(name, pane); }

    @Override
    public ItemModelBuilder paneNoSideAlt(String name, ResourceLocation pane) { return super.paneNoSideAlt(name, pane); }

    @Override
    public ItemModelBuilder doorBottomLeft(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorBottomLeft(name, bottom, top); }

    @Override
    public ItemModelBuilder doorBottomRight(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorBottomRight(name, bottom, top); }

    @Override
    public ItemModelBuilder doorTopLeft(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorTopLeft(name, bottom, top); }

    @Override
    public ItemModelBuilder doorTopRight(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorTopRight(name, bottom, top); }

    @Override
    public ItemModelBuilder trapdoorBottom(String name, ResourceLocation texture) { return super.trapdoorBottom(name, texture); }

    @Override
    public ItemModelBuilder trapdoorTop(String name, ResourceLocation texture) { return super.trapdoorTop(name, texture); }

    @Override
    public ItemModelBuilder trapdoorOpen(String name, ResourceLocation texture) { return super.trapdoorOpen(name, texture); }

    @Override
    public ItemModelBuilder trapdoorOrientableBottom(String name, ResourceLocation texture) { return super.trapdoorOrientableBottom(name, texture); }

    @Override
    public ItemModelBuilder trapdoorOrientableTop(String name, ResourceLocation texture) { return super.trapdoorOrientableTop(name, texture); }

    @Override
    public ItemModelBuilder trapdoorOrientableOpen(String name, ResourceLocation texture) { return super.trapdoorOrientableOpen(name, texture); }

    @Override
    public ItemModelBuilder torch(String name, ResourceLocation torch) { return super.torch(name, torch); }

    @Override
    public ItemModelBuilder torchWall(String name, ResourceLocation torch) { return super.torchWall(name, torch); }

    @Override
    public ItemModelBuilder carpet(String name, ResourceLocation wool) { return super.carpet(name, wool); }

    // GENERATED END
}
