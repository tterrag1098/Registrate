package com.tterrag.registrate.providers;

import com.tterrag.registrate.Registrate;

import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.fml.LogicalSide;

public class RegistrateBlockstateProvider extends BlockStateProvider implements RegistrateProvider {

    private final Registrate parent;

    public RegistrateBlockstateProvider(Registrate parent, DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, parent.getModid(), exFileHelper);
        this.parent = parent;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    protected void registerStatesAndModels() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }
    
    @Override
    public String getName() {
        return "Blockstates";
    }

    // @formatter:off
    // GENERATED START

    @Override
    public VariantBlockStateBuilder getVariantBuilder(Block b) { return super.getVariantBuilder(b); }

    @Override
    public MultiPartBlockStateBuilder getMultipartBuilder(Block b) { return super.getMultipartBuilder(b); }

    @Override
    public ResourceLocation blockTexture(Block block) { return super.blockTexture(block); }

    @Override
    public ModelFile cubeAll(Block block) { return super.cubeAll(block); }

    @Override
    public void simpleBlock(Block block) { super.simpleBlock(block); }

    @Override
    public void simpleBlock(Block block, ModelFile model) { super.simpleBlock(block, model); }

    @Override
    public void simpleBlock(Block block, ConfiguredModel... models) { super.simpleBlock(block, models); }

    @Override
    public void axisBlock(RotatedPillarBlock block) { super.axisBlock(block); }

    @Override
    public void logBlock(LogBlock block) { super.logBlock(block); }

    @Override
    public void axisBlock(RotatedPillarBlock block, ResourceLocation baseName) { super.axisBlock(block, baseName); }

    @Override
    public void axisBlock(RotatedPillarBlock block, ResourceLocation side, ResourceLocation end) { super.axisBlock(block, side, end); }

    @Override
    public void axisBlock(RotatedPillarBlock block, ModelFile model) { super.axisBlock(block, model); }

    @Override
    public void horizontalBlock(Block block, ResourceLocation side, ResourceLocation front, ResourceLocation top) { super.horizontalBlock(block, side, front, top); }

    @Override
    public void horizontalBlock(Block block, ModelFile model) { super.horizontalBlock(block, model); }

    @Override
    public void horizontalBlock(Block block, ModelFile model, int angleOffset) { super.horizontalBlock(block, model, angleOffset); }

    @Override
    public void horizontalFaceBlock(Block block, ModelFile model) { super.horizontalFaceBlock(block, model); }

    @Override
    public void horizontalFaceBlock(Block block, ModelFile model, int angleOffset) { super.horizontalFaceBlock(block, model, angleOffset); }

    @Override
    public void directionalBlock(Block block, ModelFile model) { super.directionalBlock(block, model); }

    @Override
    public void directionalBlock(Block block, ModelFile model, int angleOffset) { super.directionalBlock(block, model, angleOffset); }

    @Override
    public void stairsBlock(StairsBlock block, ResourceLocation texture) { super.stairsBlock(block, texture); }

    @Override
    public void stairsBlock(StairsBlock block, String name, ResourceLocation texture) { super.stairsBlock(block, name, texture); }

    @Override
    public void stairsBlock(StairsBlock block, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { super.stairsBlock(block, side, bottom, top); }

    @Override
    public void stairsBlock(StairsBlock block, String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { super.stairsBlock(block, name, side, bottom, top); }

    @Override
    public void stairsBlock(StairsBlock block, ModelFile stairs, ModelFile stairsInner, ModelFile stairsOuter) { super.stairsBlock(block, stairs, stairsInner, stairsOuter); }

    @Override
    public void slabBlock(SlabBlock block, ResourceLocation doubleslab, ResourceLocation texture) { super.slabBlock(block, doubleslab, texture); }

    @Override
    public void slabBlock(SlabBlock block, ResourceLocation doubleslab, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { super.slabBlock(block, doubleslab, side, bottom, top); }

    @Override
    public void slabBlock(SlabBlock block, ModelFile bottom, ModelFile top, ModelFile doubleslab) { super.slabBlock(block, bottom, top, doubleslab); }

    @Override
    public void fourWayBlock(FourWayBlock block, ModelFile post, ModelFile side) { super.fourWayBlock(block, post, side); }

    @Override
    public void fourWayMultipart(MultiPartBlockStateBuilder builder, ModelFile side) { super.fourWayMultipart(builder, side); }

    @Override
    public void fenceBlock(FenceBlock block, ResourceLocation texture) { super.fenceBlock(block, texture); }

    @Override
    public void fenceBlock(FenceBlock block, String name, ResourceLocation texture) { super.fenceBlock(block, name, texture); }

    @Override
    public void fenceGateBlock(FenceGateBlock block, ResourceLocation texture) { super.fenceGateBlock(block, texture); }

    @Override
    public void fenceGateBlock(FenceGateBlock block, String name, ResourceLocation texture) { super.fenceGateBlock(block, name, texture); }

    @Override
    public void fenceGateBlock(FenceGateBlock block, ModelFile gate, ModelFile gateOpen, ModelFile gateWall, ModelFile gateWallOpen) { super.fenceGateBlock(block, gate, gateOpen, gateWall, gateWallOpen); }

    @Override
    public void wallBlock(WallBlock block, ResourceLocation texture) { super.wallBlock(block, texture); }

    @Override
    public void wallBlock(WallBlock block, String name, ResourceLocation texture) { super.wallBlock(block, name, texture); }

    @Override
    public void wallBlock(WallBlock block, ModelFile post, ModelFile side) { super.wallBlock(block, post, side); }

    @Override
    public void paneBlock(PaneBlock block, ResourceLocation pane, ResourceLocation edge) { super.paneBlock(block, pane, edge); }

    @Override
    public void paneBlock(PaneBlock block, String name, ResourceLocation pane, ResourceLocation edge) { super.paneBlock(block, name, pane, edge); }

    @Override
    public void paneBlock(PaneBlock block, ModelFile post, ModelFile side, ModelFile sideAlt, ModelFile noSide, ModelFile noSideAlt) { super.paneBlock(block, post, side, sideAlt, noSide, noSideAlt); }

    @Override
    public void doorBlock(DoorBlock block, ResourceLocation bottom, ResourceLocation top) { super.doorBlock(block, bottom, top); }

    @Override
    public void doorBlock(DoorBlock block, String name, ResourceLocation bottom, ResourceLocation top) { super.doorBlock(block, name, bottom, top); }

    @Override
    public void doorBlock(DoorBlock block, ModelFile bottomLeft, ModelFile bottomRight, ModelFile topLeft, ModelFile topRight) { super.doorBlock(block, bottomLeft, bottomRight, topLeft, topRight); }

    @Override
    public void trapdoorBlock(TrapDoorBlock block, ResourceLocation texture, boolean orientable) { super.trapdoorBlock(block, texture, orientable); }

    @Override
    public void trapdoorBlock(TrapDoorBlock block, String name, ResourceLocation texture, boolean orientable) { super.trapdoorBlock(block, name, texture, orientable); }

    @Override
    public void trapdoorBlock(TrapDoorBlock block, ModelFile bottom, ModelFile top, ModelFile open, boolean orientable) { super.trapdoorBlock(block, bottom, top, open, orientable); }

    @Override
    public BlockModelBuilder getBuilder(String path) { return super.getBuilder(path); }

    @Override
    public ResourceLocation modLoc(String name) { return super.modLoc(name); }

    @Override
    public ResourceLocation mcLoc(String name) { return super.mcLoc(name); }

    @Override
    public BlockModelBuilder withExistingParent(String name, String parent) { return super.withExistingParent(name, parent); }

    @Override
    public BlockModelBuilder withExistingParent(String name, ResourceLocation parent) { return super.withExistingParent(name, parent); }

    @Override
    public BlockModelBuilder cube(String name, ResourceLocation down, ResourceLocation up, ResourceLocation north, ResourceLocation south, ResourceLocation east, ResourceLocation west) { return super.cube(name, down, up, north, south, east, west); }

    @Override
    public BlockModelBuilder singleTexture(String name, ResourceLocation parent, ResourceLocation texture) { return super.singleTexture(name, parent, texture); }

    @Override
    public BlockModelBuilder singleTexture(String name, ResourceLocation parent, String textureKey, ResourceLocation texture) { return super.singleTexture(name, parent, textureKey, texture); }

    @Override
    public BlockModelBuilder cubeAll(String name, ResourceLocation texture) { return super.cubeAll(name, texture); }

    @Override
    public BlockModelBuilder cubeTop(String name, ResourceLocation side, ResourceLocation top) { return super.cubeTop(name, side, top); }

    @Override
    public BlockModelBuilder cubeBottomTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.cubeBottomTop(name, side, bottom, top); }

    @Override
    public BlockModelBuilder cubeColumn(String name, ResourceLocation side, ResourceLocation end) { return super.cubeColumn(name, side, end); }

    @Override
    public BlockModelBuilder orientableVertical(String name, ResourceLocation side, ResourceLocation front) { return super.orientableVertical(name, side, front); }

    @Override
    public BlockModelBuilder orientableWithBottom(String name, ResourceLocation side, ResourceLocation front, ResourceLocation bottom, ResourceLocation top) { return super.orientableWithBottom(name, side, front, bottom, top); }

    @Override
    public BlockModelBuilder orientable(String name, ResourceLocation side, ResourceLocation front, ResourceLocation top) { return super.orientable(name, side, front, top); }

    @Override
    public BlockModelBuilder crop(String name, ResourceLocation crop) { return super.crop(name, crop); }

    @Override
    public BlockModelBuilder cross(String name, ResourceLocation cross) { return super.cross(name, cross); }

    @Override
    public BlockModelBuilder stairs(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.stairs(name, side, bottom, top); }

    @Override
    public BlockModelBuilder stairsOuter(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.stairsOuter(name, side, bottom, top); }

    @Override
    public BlockModelBuilder stairsInner(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.stairsInner(name, side, bottom, top); }

    @Override
    public BlockModelBuilder slab(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.slab(name, side, bottom, top); }

    @Override
    public BlockModelBuilder slabTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) { return super.slabTop(name, side, bottom, top); }

    @Override
    public BlockModelBuilder fencePost(String name, ResourceLocation texture) { return super.fencePost(name, texture); }

    @Override
    public BlockModelBuilder fenceSide(String name, ResourceLocation texture) { return super.fenceSide(name, texture); }

    @Override
    public BlockModelBuilder fenceInventory(String name, ResourceLocation texture) { return super.fenceInventory(name, texture); }

    @Override
    public BlockModelBuilder fenceGate(String name, ResourceLocation texture) { return super.fenceGate(name, texture); }

    @Override
    public BlockModelBuilder fenceGateOpen(String name, ResourceLocation texture) { return super.fenceGateOpen(name, texture); }

    @Override
    public BlockModelBuilder fenceGateWall(String name, ResourceLocation texture) { return super.fenceGateWall(name, texture); }

    @Override
    public BlockModelBuilder fenceGateWallOpen(String name, ResourceLocation texture) { return super.fenceGateWallOpen(name, texture); }

    @Override
    public BlockModelBuilder wallPost(String name, ResourceLocation wall) { return super.wallPost(name, wall); }

    @Override
    public BlockModelBuilder wallSide(String name, ResourceLocation wall) { return super.wallSide(name, wall); }

    @Override
    public BlockModelBuilder wallInventory(String name, ResourceLocation wall) { return super.wallInventory(name, wall); }

    @Override
    public BlockModelBuilder panePost(String name, ResourceLocation pane, ResourceLocation edge) { return super.panePost(name, pane, edge); }

    @Override
    public BlockModelBuilder paneSide(String name, ResourceLocation pane, ResourceLocation edge) { return super.paneSide(name, pane, edge); }

    @Override
    public BlockModelBuilder paneSideAlt(String name, ResourceLocation pane, ResourceLocation edge) { return super.paneSideAlt(name, pane, edge); }

    @Override
    public BlockModelBuilder paneNoSide(String name, ResourceLocation pane) { return super.paneNoSide(name, pane); }

    @Override
    public BlockModelBuilder paneNoSideAlt(String name, ResourceLocation pane) { return super.paneNoSideAlt(name, pane); }

    @Override
    public BlockModelBuilder doorBottomLeft(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorBottomLeft(name, bottom, top); }

    @Override
    public BlockModelBuilder doorBottomRight(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorBottomRight(name, bottom, top); }

    @Override
    public BlockModelBuilder doorTopLeft(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorTopLeft(name, bottom, top); }

    @Override
    public BlockModelBuilder doorTopRight(String name, ResourceLocation bottom, ResourceLocation top) { return super.doorTopRight(name, bottom, top); }

    @Override
    public BlockModelBuilder trapdoorBottom(String name, ResourceLocation texture) { return super.trapdoorBottom(name, texture); }

    @Override
    public BlockModelBuilder trapdoorTop(String name, ResourceLocation texture) { return super.trapdoorTop(name, texture); }

    @Override
    public BlockModelBuilder trapdoorOpen(String name, ResourceLocation texture) { return super.trapdoorOpen(name, texture); }

    @Override
    public BlockModelBuilder trapdoorOrientableBottom(String name, ResourceLocation texture) { return super.trapdoorOrientableBottom(name, texture); }

    @Override
    public BlockModelBuilder trapdoorOrientableTop(String name, ResourceLocation texture) { return super.trapdoorOrientableTop(name, texture); }

    @Override
    public BlockModelBuilder trapdoorOrientableOpen(String name, ResourceLocation texture) { return super.trapdoorOrientableOpen(name, texture); }

    @Override
    public BlockModelBuilder torch(String name, ResourceLocation torch) { return super.torch(name, torch); }

    @Override
    public BlockModelBuilder torchWall(String name, ResourceLocation torch) { return super.torchWall(name, torch); }

    @Override
    public BlockModelBuilder carpet(String name, ResourceLocation wool) { return super.carpet(name, wool); }

    // GENERATED END
}
