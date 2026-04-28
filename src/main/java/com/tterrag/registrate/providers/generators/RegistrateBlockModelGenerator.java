package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistrateBlockModelGenerator extends BlockModelGenerators {

    private final AbstractRegistrate<?> parent;
    public final Map<Block, BlockStateModelDispatcher> seenBlockstates = new HashMap<>();

    public RegistrateBlockModelGenerator(AbstractRegistrate<?> parent, Consumer<BlockModelDefinitionGenerator> known, ItemModelOutput item, BiConsumer<Identifier, ModelInstance> model) {
        super(known, item, model);
        ObfuscationReflectionHelper.<BlockModelGenerators, Consumer<BlockModelDefinitionGenerator>>setPrivateValue(BlockModelGenerators.class, this, g -> {
            this.seenBlockstates.put(g.block(), g.create());
            known.accept(g);
        }, "blockStateOutput");
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }


    public void create(Block block, Identifier model) {
        this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(model)));
    }

    public Identifier mcLoc(String id) {
        return Identifier.withDefaultNamespace(id);
    }

    public Identifier modLoc(String id) {
        return Identifier.fromNamespaceAndPath(parent.getModid(), id);
    }

    public Material mcBlockTexture(String path) {
        return new Material(mcLoc("block/" + path));
    }

    public Material modBlockTexture(String path) {
        return new Material(modLoc("block/" + path));
    }

    public RegistrateLegacyBlockModelBuilder withBuilder(ExtendedModelTemplateBuilder template, TextureMapping texture) {
        return new RegistrateLegacyBlockModelBuilder(modelOutput, template, texture);
    }

    public RegistrateLegacyBlockModelBuilder withBuilder(ExtendedModelTemplateBuilder template) {
        return withBuilder(template, new TextureMapping());
    }

    public RegistrateLegacyBlockModelBuilder getBuilder() {
        return withBuilder(new ExtendedModelTemplateBuilder());
    }

    public RegistrateLegacyBlockModelBuilder withParent(ModelTemplate template) {
        return withBuilder(ExtendedModelTemplateBuilder.of(template));
    }

    public RegistrateLegacyBlockModelBuilder withParent(ModelTemplate template, TextureMapping texture) {
        return withBuilder(ExtendedModelTemplateBuilder.of(template), texture);
    }

    public RegistrateLegacyBlockModelBuilder withParent(TexturedModel model) {
        return withBuilder(ExtendedModelTemplateBuilder.of(model.getTemplate()), model.getMapping());
    }

    private static TextureMapping sideBottomTopTextures(Material side, Material bottom, Material top) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
    }

    public Material blockTexture(Block block) {
        return TextureMapping.getBlockTexture(block);
    }

    public Material blockTexture(Block block, String suffix) {
        return TextureMapping.getBlockTexture(block, suffix);
    }

    public void generateWithTemplate(Block block, ModelTemplate template, TextureMapping textures) {
        create(block, template.create(block, textures, modelOutput));
    }

    public void generate(Block block, TexturedModel.Provider texture) {
        blockStateOutput.accept(createSimpleBlock(block, plainVariant(texture.create(block, modelOutput))));
    }

    public void generateAxisBlock(RotatedPillarBlock block) {
        generateAxisBlock(block, blockTexture(block, "_side"), blockTexture(block, "_end"));
    }

    public void generateLogBlock(RotatedPillarBlock block) {
        generateAxisBlock(block, blockTexture(block), blockTexture(block, "_top"));
    }

    public void generateAxisBlock(RotatedPillarBlock block, Material side, Material end) {
        generateAxisBlockInternal(block, side, end, ModelTemplates.CUBE_COLUMN, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    }

    private void generateAxisBlockInternal(RotatedPillarBlock block, Material side, Material end, ModelTemplate cubeColumn, ModelTemplate cubeColumnHorizontal) {
        generateAxisBlock(block,
                plainVariant(cubeColumn.create(block, TextureMapping.column(side, end), modelOutput)),
                plainVariant(cubeColumnHorizontal.create(block, TextureMapping.column(side, end), modelOutput))
        );
    }

    public void generateAxisBlock(RotatedPillarBlock block, MultiVariant vertical, MultiVariant horizontal) {
        blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(block, vertical, horizontal));
    }

    private static final int DEFAULT_ANGLE_OFFSET = 180;

    public void generateHorizontalBlock(Block block, Material side, Material front, Material top) {
        TextureMapping mapping = new TextureMapping().put(TextureSlot.SIDE, side).put(TextureSlot.FRONT, front).put(TextureSlot.TOP, top);
        generateHorizontalBlock(block, plainVariant(ModelTemplates.CUBE_ORIENTABLE.create(block, mapping, modelOutput)));
    }

    private static VariantMutator yRot(Direction direction, int angleOffset) {
        return switch ((direction.get2DDataValue() + angleOffset / 90) % 4) {
            case 0 -> NOP;
            case 1 -> Y_ROT_90;
            case 2 -> Y_ROT_180;
            case 3 -> Y_ROT_270;
            default -> throw new IllegalStateException();
        };
    }

    public static PropertyDispatch<VariantMutator> rotationHorizontalFacing(int angleOffset) {
        return PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .generate(direction -> yRot(direction, angleOffset));
    }

    public static PropertyDispatch<VariantMutator> rotationFacing(int angleOffsetY) {
        return PropertyDispatch.modify(BlockStateProperties.FACING).generate(direction -> {
            VariantMutator xRot = switch (direction) {
                case DOWN -> X_ROT_180;
                case UP -> NOP;
                case NORTH, EAST, SOUTH, WEST -> X_ROT_90;
            };
            VariantMutator yRot = direction.getAxis().isVertical() ? NOP : yRot(direction, angleOffsetY);
            return xRot.then(yRot);
        });
    }

    public static PropertyDispatch<VariantMutator> rotationAttachFaceAndHorizontalFacing(int angleOffsetY) {
        return PropertyDispatch.modify(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING).generate((attachFace, direction) -> {
            VariantMutator xRot = switch (attachFace) {
                case FLOOR -> NOP;
                case WALL -> X_ROT_90;
                case CEILING -> X_ROT_180;
            };
            VariantMutator yRot = yRot(direction, attachFace == AttachFace.CEILING ? angleOffsetY + 180 : angleOffsetY);
            return xRot.then(yRot);
        });
    }

    public void generateHorizontalBlock(Block block, MultiVariant model) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block, model).with(ROTATION_HORIZONTAL_FACING));
    }

    public void generateHorizontalBlock(Block block, MultiVariant multiVariant, int angleOffset) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block, multiVariant).with(rotationHorizontalFacing(angleOffset)));
    }

    public void generateHorizontalFaceBlock(Block block, MultiVariant model) {
        generateHorizontalFaceBlock(block, model, DEFAULT_ANGLE_OFFSET);
    }

    public void generateHorizontalFaceBlock(Block block, MultiVariant variant, int angleOffset) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant).with(rotationAttachFaceAndHorizontalFacing(angleOffset)));
    }

    public void generateDirectionalBlock(Block block, MultiVariant model) {
        generateDirectionalBlock(block, model, DEFAULT_ANGLE_OFFSET);
    }

    public void generateDirectionalBlock(Block block, MultiVariant model, int angleOffsetY) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block, model).with(rotationFacing(angleOffsetY)));
    }

    public void generateStairsBlock(StairBlock block, Material texture) {
        generateStairsBlock(block, texture, texture, texture);
    }

    public void generateStairsBlock(StairBlock block, String name, Material texture) {
        generateStairsBlock(block, name, texture, texture, texture);
    }

    public void generateStairsBlock(StairBlock block, Material side, Material bottom, Material top) {
        TextureMapping textures = sideBottomTopTextures(side, bottom, top);
        blockStateOutput.accept(createStairs(block,
                plainVariant(ModelTemplates.STAIRS_INNER.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.STAIRS_STRAIGHT.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.STAIRS_OUTER.create(block, textures, modelOutput))
        ));
    }

    public void generateStairsBlock(StairBlock block, String name, Material side, Material bottom, Material top) {
        generateStairsBlockInternal(block, modLoc(name + "_stairs"), side, bottom, top, ModelTemplates.STAIRS_STRAIGHT, ModelTemplates.STAIRS_INNER, ModelTemplates.STAIRS_OUTER);
    }

    private void generateStairsBlockInternal(StairBlock block, Identifier baseName, Material side, Material bottom, Material top, ModelTemplate straightTemplate, ModelTemplate innerTemplate, ModelTemplate outerTemplate) {
        Identifier modelLocation = baseName.withPrefix("block/");
        TextureMapping textures = sideBottomTopTextures(side, bottom, top);
        MultiVariant straightVariant = plainVariant(straightTemplate.create(modelLocation, textures, modelOutput));
        MultiVariant innerVariant = plainVariant(innerTemplate.create(modelLocation.withSuffix("_inner"), textures, modelOutput));
        MultiVariant outerVariant = plainVariant(outerTemplate.create(modelLocation.withSuffix("_outer"), textures, modelOutput));
        blockStateOutput.accept(createStairs(block, innerVariant, straightVariant, outerVariant));
    }

    public void generateStairsBlock(StairBlock block, MultiVariant stairs, MultiVariant stairsInner, MultiVariant stairsOuter) {
        blockStateOutput.accept(createStairs(block, stairsInner, stairs, stairsOuter));
    }

    public void generateSlabBlock(SlabBlock block, MultiVariant doubleSlab, Material texture) {
        generateSlabBlock(block, doubleSlab, texture, texture, texture);
    }

    public void generateSlabBlock(SlabBlock block, MultiVariant doubleSlab, Material side, Material bottom, Material top) {
        TextureMapping textures = sideBottomTopTextures(side, bottom, top);
        MultiVariant slabTop = plainVariant(ModelTemplates.SLAB_TOP.create(block, textures, modelOutput));
        MultiVariant slabBottom = plainVariant(ModelTemplates.SLAB_BOTTOM.create(block, textures, modelOutput));
        generateSlabBlock(block, slabBottom, slabTop, doubleSlab);
    }

    public void generateSlabBlock(SlabBlock block, MultiVariant bottom, MultiVariant top, MultiVariant doubleslab) {
        blockStateOutput.accept(createSlab(block, bottom, top, doubleslab));
    }

    public void generateButtonBlock(ButtonBlock block, Material texture) {
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        MultiVariant button = plainVariant(ModelTemplates.BUTTON.create(block, textures, modelOutput));
        MultiVariant buttonPressed = plainVariant(ModelTemplates.BUTTON_PRESSED.create(block, textures, modelOutput));
        generateButtonBlock(block, button, buttonPressed);
    }

    public void generateButtonBlock(ButtonBlock block, MultiVariant button, MultiVariant buttonPressed) {
        blockStateOutput.accept(createButton(block, button, buttonPressed));
    }

    public void generatePressurePlateBlock(PressurePlateBlock block, Material texture) {
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        MultiVariant pressurePlate = plainVariant(ModelTemplates.PRESSURE_PLATE_UP.create(block, textures, modelOutput));
        MultiVariant pressurePlateDown = plainVariant(ModelTemplates.PRESSURE_PLATE_DOWN.create(block, textures, modelOutput));
        generatePressurePlateBlock(block, pressurePlate, pressurePlateDown);
    }

    public void generatePressurePlateBlock(PressurePlateBlock block, MultiVariant pressurePlate, MultiVariant pressurePlateDown) {
        blockStateOutput.accept(createPressurePlate(block, pressurePlate, pressurePlateDown));
    }

    public void generateSignBlock(StandingSignBlock signBlock, WallSignBlock wallSignBlock, Material texture) {
        MultiVariant sign = plainVariant(ModelTemplates.PARTICLE_ONLY.create(signBlock, TextureMapping.particle(texture), modelOutput));
        generateSignBlock(signBlock, wallSignBlock, sign);
    }

    public void generateSignBlock(StandingSignBlock signBlock, WallSignBlock wallSignBlock, MultiVariant sign) {
        blockStateOutput.accept(createSimpleBlock(signBlock, sign));
        blockStateOutput.accept(createSimpleBlock(wallSignBlock, sign));
    }

    public void generateHangingSignBlock(CeilingHangingSignBlock hangingSignBlock, WallHangingSignBlock wallHangingSignBlock, Material texture) {
        MultiVariant hangingSign = plainVariant(ModelTemplates.PARTICLE_ONLY.create(hangingSignBlock, TextureMapping.particle(texture), modelOutput));
        generateHangingSignBlock(hangingSignBlock, wallHangingSignBlock, hangingSign);
    }

    public void generateHangingSignBlock(CeilingHangingSignBlock hangingSignBlock, WallHangingSignBlock wallHangingSignBlock, MultiVariant hangingSign) {
        blockStateOutput.accept(createSimpleBlock(hangingSignBlock, hangingSign));
        blockStateOutput.accept(createSimpleBlock(wallHangingSignBlock, hangingSign));
    }

    public void generateFourWayBlock(CrossCollisionBlock block, MultiVariant post, MultiVariant side) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block).with(post);
        generateFourWayMultipart(builder, side);
    }

    public void generateFourWayMultipart(MultiPartGenerator builder, MultiVariant side) {
        PipeBlock.PROPERTY_BY_DIRECTION.forEach((direction, property) -> {
            if (direction.getAxis().isHorizontal()) {
                VariantMutator yRot = switch (direction) {
                    case NORTH -> NOP;
                    case EAST -> Y_ROT_90;
                    case SOUTH -> Y_ROT_180;
                    case WEST -> Y_ROT_270;
                    default -> throw new IllegalStateException();
                };
                builder.with(condition().term(property, true), side.with(UV_LOCK.then(yRot)));
            }
        });
        blockStateOutput.accept(builder);
    }

    public void generateFenceBlock(FenceBlock block, Material texture) {
        generateFenceBlockInternal(block, texture, ModelTemplates.FENCE_POST, ModelTemplates.FENCE_SIDE);
    }

    public void generateFenceBlock(FenceBlock block, String name, Material texture) {
        generateFenceBlockInternal(block, name, texture, ModelTemplates.FENCE_POST, ModelTemplates.FENCE_SIDE);
    }

    private void generateFenceBlockInternal(FenceBlock block, Material texture, ModelTemplate fencePostTemplate, ModelTemplate fenceSideTemplate) {
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        generateFourWayBlock(block,
                plainVariant(fencePostTemplate.create(block, textures, modelOutput)),
                plainVariant(fenceSideTemplate.create(block, textures, modelOutput))
        );
    }

    private void generateFenceBlockInternal(FenceBlock block, String name, Material texture, ModelTemplate fencePostTemplate, ModelTemplate fenceSideTemplate) {
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        generateFourWayBlock(block,
                plainVariant(fencePostTemplate.create(modLoc("block/" + name + "_fence_post"), textures, modelOutput)),
                plainVariant(fenceSideTemplate.create(modLoc("block/" + name + "_fence_side"), textures, modelOutput))
        );
    }

    public void generateFenceGateBlock(FenceGateBlock block, Material texture) {
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        generateFenceGateBlock(block,
                plainVariant(ModelTemplates.FENCE_GATE_CLOSED.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.FENCE_GATE_OPEN.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, textures, modelOutput))
        );
    }

    public void generateFenceGateBlock(FenceGateBlock block, String name, Material texture) {
        generateFenceGateBlockInternal(block, modLoc(name + "_fence_gate"), texture, ModelTemplates.FENCE_GATE_CLOSED, ModelTemplates.FENCE_GATE_OPEN, ModelTemplates.FENCE_GATE_WALL_CLOSED, ModelTemplates.FENCE_GATE_WALL_OPEN);
    }

    private void generateFenceGateBlockInternal(FenceGateBlock block, Identifier baseName, Material texture, ModelTemplate closedTemplate, ModelTemplate openTemplate, ModelTemplate wallClosedTemplate, ModelTemplate wallOpenTemplate) {
        Identifier baseModel = baseName.withPrefix("block/");
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        MultiVariant gate = plainVariant(closedTemplate.create(baseModel, textures, modelOutput));
        MultiVariant gateOpen = plainVariant(openTemplate.create(baseModel.withSuffix("_open"), textures, modelOutput));
        MultiVariant gateWall = plainVariant(wallClosedTemplate.create(baseModel.withSuffix("_wall"), textures, modelOutput));
        MultiVariant gateWallOpen = plainVariant(wallOpenTemplate.create(baseModel.withSuffix("_wall_open"), textures, modelOutput));
        generateFenceGateBlock(block, gate, gateOpen, gateWall, gateWallOpen);
    }

    public void generateFenceGateBlock(FenceGateBlock block, MultiVariant gate, MultiVariant gateOpen, MultiVariant gateWall, MultiVariant gateWallOpen) {
        blockStateOutput.accept(createFenceGate(block, gateOpen, gate, gateWallOpen, gateWall, true));
    }

    public void generateWallBlock(WallBlock block, Material texture) {
        TextureMapping textures = TextureMapping.singleSlot(TextureSlot.WALL, texture);
        blockStateOutput.accept(createWall(block,
                plainVariant(ModelTemplates.WALL_POST.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.WALL_LOW_SIDE.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.WALL_TALL_SIDE.create(block, textures, modelOutput))
        ));
    }

    public void generateWallBlock(WallBlock block, String name, Material texture) {
        Identifier baseName = modLoc(name + "_wall");
        generateWallBlockInternal(block, baseName, texture, ModelTemplates.WALL_POST, ModelTemplates.WALL_LOW_SIDE, ModelTemplates.WALL_TALL_SIDE);
    }

    private void generateWallBlockInternal(WallBlock block, Identifier baseName, Material texture, ModelTemplate postTemplate, ModelTemplate sideTemplate, ModelTemplate tallSideTemplate) {
        TextureMapping textures = TextureMapping.singleSlot(TextureSlot.WALL, texture);
        Identifier baseModel = baseName.withPrefix("block/");
        generateWallBlock(block,
                plainVariant(postTemplate.create(baseModel.withSuffix("_post"), textures, modelOutput)),
                plainVariant(sideTemplate.create(baseModel.withSuffix("_side"), textures, modelOutput)),
                plainVariant(tallSideTemplate.create(baseModel.withSuffix("_side_tall"), textures, modelOutput))
        );
    }

    public void generateWallBlock(WallBlock block, MultiVariant post, MultiVariant side, MultiVariant sideTall) {
        blockStateOutput.accept(createWall(block, post, side, sideTall));
    }

    public void generatePaneBlock(IronBarsBlock block, Material pane, Material edge) {
        TextureMapping textures = new TextureMapping().put(TextureSlot.PANE, pane).put(TextureSlot.EDGE, edge);
        generatePaneBlock(block,
                plainVariant(ModelTemplates.STAINED_GLASS_PANE_POST.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.STAINED_GLASS_PANE_SIDE.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(block, textures, modelOutput))
        );
    }

    public void generatePaneBlock(IronBarsBlock block, String name, Material pane, Material edge) {
        Identifier baseName = modLoc(name + "_pane");
        generatePaneBlockInternal(block, baseName, pane, edge, ModelTemplates.STAINED_GLASS_PANE_POST, ModelTemplates.STAINED_GLASS_PANE_SIDE, ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT, ModelTemplates.STAINED_GLASS_PANE_NOSIDE, ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT);
    }

    private void generatePaneBlockInternal(IronBarsBlock block, Identifier baseName, Material pane, Material edge, ModelTemplate postTemplate, ModelTemplate sideTemplate, ModelTemplate sideAltTemplate, ModelTemplate noSideTemplate, ModelTemplate noSideAltTemplate) {
        Identifier baseModel = baseName.withPrefix("block/");
        TextureMapping textures = new TextureMapping().put(TextureSlot.PANE, pane).put(TextureSlot.EDGE, edge);
        generatePaneBlock(block,
                plainVariant(postTemplate.create(baseModel.withSuffix("_post"), textures, modelOutput)),
                plainVariant(sideTemplate.create(baseModel.withSuffix("_side"), textures, modelOutput)),
                plainVariant(sideAltTemplate.create(baseModel.withSuffix("_side_alt"), textures, modelOutput)),
                plainVariant(noSideTemplate.create(baseModel.withSuffix("_noside"), textures, modelOutput)),
                plainVariant(noSideAltTemplate.create(baseModel.withSuffix("_noside_alt"), textures, modelOutput))
        );
    }

    public void generatePaneBlock(IronBarsBlock block, MultiVariant post, MultiVariant side, MultiVariant sideAlt, MultiVariant noSide, MultiVariant noSideAlt) {
        blockStateOutput.accept(MultiPartGenerator.multiPart(block)
                .with(post)
                .with(condition().term(BlockStateProperties.NORTH, true), side)
                .with(condition().term(BlockStateProperties.EAST, true), side.with(Y_ROT_90))
                .with(condition().term(BlockStateProperties.SOUTH, true), sideAlt)
                .with(condition().term(BlockStateProperties.WEST, true), sideAlt.with(Y_ROT_90))
                .with(condition().term(BlockStateProperties.NORTH, false), noSide)
                .with(condition().term(BlockStateProperties.EAST, false), noSideAlt)
                .with(condition().term(BlockStateProperties.SOUTH, false), noSideAlt.with(Y_ROT_90))
                .with(condition().term(BlockStateProperties.WEST, false), noSide.with(Y_ROT_270))
        );
    }

    public void generateDoorBlock(DoorBlock block, Material bottom, Material top) {
        TextureMapping textures = TextureMapping.door(top, bottom);
        generateDoorBlock(block,
                plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_TOP_LEFT.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_TOP_LEFT_OPEN.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_TOP_RIGHT.create(block, textures, modelOutput)),
                plainVariant(ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(block, textures, modelOutput))
        );
    }

    public void generateDoorBlock(DoorBlock block, String name, Material bottom, Material top) {
        generateDoorBlockInternal(block, modLoc(name + "_door"), bottom, top);
    }

    private void generateDoorBlockInternal(DoorBlock block, Identifier baseName, Material bottom, Material top) {
        generateDoorBlockInternal(block, baseName, bottom, top, ModelTemplates.DOOR_BOTTOM_LEFT, ModelTemplates.DOOR_BOTTOM_LEFT_OPEN, ModelTemplates.DOOR_BOTTOM_RIGHT, ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN, ModelTemplates.DOOR_TOP_LEFT, ModelTemplates.DOOR_TOP_LEFT_OPEN, ModelTemplates.DOOR_TOP_RIGHT, ModelTemplates.DOOR_TOP_RIGHT_OPEN);
    }

    private void generateDoorBlockInternal(DoorBlock block, Identifier baseName, Material bottom, Material top, ModelTemplate bottomLeftTemplate, ModelTemplate bottomLeftOpenTemplate, ModelTemplate bottomRightTemplate, ModelTemplate bottomRightOpenTemplate, ModelTemplate topLeftTemplate, ModelTemplate topLeftOpenTemplate, ModelTemplate topRightTemplate, ModelTemplate topRightOpenTemplate) {
        Identifier baseModel = baseName.withPrefix("block/");
        TextureMapping textures = TextureMapping.door(top, bottom);
        MultiVariant bottomLeft = plainVariant(bottomLeftTemplate.create(baseModel.withSuffix("_bottom_left"), textures, modelOutput));
        MultiVariant bottomLeftOpen = plainVariant(bottomLeftOpenTemplate.create(baseModel.withSuffix("_bottom_left_open"), textures, modelOutput));
        MultiVariant bottomRight = plainVariant(bottomRightTemplate.create(baseModel.withSuffix("_bottom_right"), textures, modelOutput));
        MultiVariant bottomRightOpen = plainVariant(bottomRightOpenTemplate.create(baseModel.withSuffix("_bottom_right_open"), textures, modelOutput));
        MultiVariant topLeft = plainVariant(topLeftTemplate.create(baseModel.withSuffix("_top_left"), textures, modelOutput));
        MultiVariant topLeftOpen = plainVariant(topLeftOpenTemplate.create(baseModel.withSuffix("_top_left_open"), textures, modelOutput));
        MultiVariant topRight = plainVariant(topRightTemplate.create(baseModel.withSuffix("_top_right"), textures, modelOutput));
        MultiVariant topRightOpen = plainVariant(topRightOpenTemplate.create(baseModel.withSuffix("_top_right_open"), textures, modelOutput));
        generateDoorBlock(block, bottomLeft, bottomLeftOpen, bottomRight, bottomRightOpen, topLeft, topLeftOpen, topRight, topRightOpen);
    }

    public void generateDoorBlock(DoorBlock block, MultiVariant bottomLeft, MultiVariant bottomLeftOpen, MultiVariant bottomRight, MultiVariant bottomRightOpen, MultiVariant topLeft, MultiVariant topLeftOpen, MultiVariant topRight, MultiVariant topRightOpen) {
        blockStateOutput.accept(createDoor(block, bottomLeft, bottomLeftOpen, bottomRight, bottomRightOpen, topLeft, topLeftOpen, topRight, topRightOpen));
    }

    public void generateTrapdoorBlock(TrapDoorBlock block, Material texture, boolean orientable) {
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        generateTrapdoorBlock(block,
                plainVariant((orientable ? ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM : ModelTemplates.TRAPDOOR_BOTTOM).create(block, textures, modelOutput)),
                plainVariant((orientable ? ModelTemplates.ORIENTABLE_TRAPDOOR_TOP : ModelTemplates.TRAPDOOR_TOP).create(block, textures, modelOutput)),
                plainVariant((orientable ? ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN : ModelTemplates.TRAPDOOR_OPEN).create(block, textures, modelOutput)),
                orientable
        );
    }

    public void generateTrapdoorBlock(TrapDoorBlock block, String name, Material texture, boolean orientable) {
        generateTrapdoorBlockInternal(block, modLoc(name + "_trapdoor"), texture, orientable);
    }

    private void generateTrapdoorBlockInternal(TrapDoorBlock block, Identifier baseName, Material texture, boolean orientable) {
        generateTrapdoorBlockInternal(block, baseName, texture, orientable,
                orientable ? ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM : ModelTemplates.TRAPDOOR_BOTTOM,
                orientable ? ModelTemplates.ORIENTABLE_TRAPDOOR_TOP : ModelTemplates.TRAPDOOR_TOP,
                orientable ? ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN : ModelTemplates.TRAPDOOR_OPEN
        );
    }

    private void generateTrapdoorBlockInternal(TrapDoorBlock block, Identifier baseName, Material texture, boolean orientable, ModelTemplate bottomTemplate, ModelTemplate topTemplate, ModelTemplate openTemplate) {
        Identifier baseModel = baseName.withPrefix("block/");
        TextureMapping textures = TextureMapping.defaultTexture(texture);
        MultiVariant bottom = plainVariant(bottomTemplate.create(baseModel.withSuffix("_bottom"), textures, modelOutput));
        MultiVariant top = plainVariant(topTemplate.create(baseModel.withSuffix("_top"), textures, modelOutput));
        MultiVariant open = plainVariant(openTemplate.create(baseModel.withSuffix("_open"), textures, modelOutput));
        generateTrapdoorBlock(block, bottom, top, open, orientable);
    }

    public void generateTrapdoorBlock(TrapDoorBlock block, MultiVariant bottom, MultiVariant top, MultiVariant open, boolean orientable) {
        blockStateOutput.accept(orientable ? createOrientableTrapdoor(block, top, bottom, open) : createTrapdoor(block, top, bottom, open));
    }
}
