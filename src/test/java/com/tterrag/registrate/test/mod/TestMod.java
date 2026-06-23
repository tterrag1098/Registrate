package com.tterrag.registrate.test.mod;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.attribute.*;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod(TestMod.MOD_ID)
public class TestMod {

    public static final String MOD_ID = "testmod";

    public class TestBlock extends Block implements EntityBlock {

        public TestBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(BlockStateProperties.CHEST_TYPE);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
            if (!worldIn.isClientSide()) {
                player.openMenu(new MenuProvider() {

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
                        return new ChestMenu(MenuType.GENERIC_9x3, windowId, inv, testblockbe.get(worldIn, pos).orElseThrow(IllegalStateException::new), 3);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Test");
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return testblockbe.create(pos, state);
        }
    }

    private static class TestBlockEntity extends ChestBlockEntity {

        public TestBlockEntity(BlockEntityType<? extends TestBlockEntity> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    private static class TestBlockEntityRenderer implements BlockEntityRenderer<TestBlockEntity, TestBlockEntityRenderer.RenderState> {
        private final ItemModelResolver itemModelResolver;

        public TestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
            itemModelResolver = ctx.itemModelResolver();
        }

        @Override
        public RenderState createRenderState() {
            return new RenderState();
        }

        @Override
        public void extractRenderState(TestBlockEntity blockEntity, RenderState state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
            BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
            itemModelResolver.updateForTopItem(state.item, new ItemStack(Items.DIAMOND), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
        }

        @Override
        public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
            poseStack.popPose();
        }

        private static class RenderState extends BlockEntityRenderState {
            private final ItemStackRenderState item = new ItemStackRenderState();
        }
    }

    public static class TestDummyBlockEntity extends BlockEntity {

        public TestDummyBlockEntity(BlockEntityType<? extends TestDummyBlockEntity> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    public static class TestEntity extends Pig {

        public TestEntity(EntityType<? extends Pig> p_i50250_1_, Level p_i50250_2_) {
            super(p_i50250_1_, p_i50250_2_);
        }
    }

    public static class TestCustomRegistryEntry {}

    private final Registrate registrate = Registrate.create("testmod");

    @VisibleForTesting
    public final RegistryEntry<CreativeModeTab, CreativeModeTab> testcreativetab = registrate.object("test_creative_mode_tab")
            .defaultCreativeTab(tab -> tab.withLabelColor(0xFF00AA00))
            .register();

    private final AtomicBoolean sawCallback = new AtomicBoolean();

    @VisibleForTesting
    public final ItemEntry<Item> testitem = registrate.object("testitem")
            .item(Item::new)
                .onRegister(item -> sawCallback.set(true))
                .properties(p -> p.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build()))
                .tag(ItemTags.BEDS)
            .model(() -> (ctx, prov) -> prov.createWithExistingModel(ctx.getEntry(), prov.mcLoc("block/stone")))
                .tabNew(testcreativetab.getKey(), (ctx, modifier) -> modifier.accept(ctx.get()))
                .register();

    @VisibleForTesting
    public final EntityEntry<TestEntity> testduplicatename = registrate.object("testitem")
            .entity(TestEntity::new, MobCategory.CREATURE)
            .attributes(Pig::createAttributes)
            .loot((tb, e) -> tb.add(e, LootTable.lootTable()))
            .renderer(() -> PigRenderer::new)
            .register();

    @VisibleForTesting
    public final BlockEntry<TestBlock> testblock = registrate.object("testblock")
            .block(TestBlock::new)
                .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate(() -> (ctx, prov) -> prov.create(ctx.getEntry(),
                    prov.getBuilder().transformTemplate(template -> template
                            .parent(prov.mcLoc("block/glass"))
                    ).build(ctx.getEntry())
            ))
                .transform(TestMod::applyDiamondDrop)
                .recipe((ctx, prov) -> {
                    prov.shaped(RecipeCategory.MISC, ctx.getEntry())
                            .pattern("DDD").pattern("DED").pattern("DDD")
                            .define('D', Items.DIAMOND)
                            .define('E', Items.EGG)
                            .unlockedBy("has_egg", prov.has(Items.EGG))
                            .save(prov);

                    prov.food(DataIngredient.items(ctx), RecipeCategory.MISC, CookingBookCategory.MISC, () -> Blocks.DIAMOND_BLOCK, 1f);
                })
                .tag(BlockTags.SUPPORTS_BAMBOO, BlockTags.DRAGON_IMMUNE)
                .tag(BlockTags.WITHER_IMMUNE)
                .color(() -> () -> List.of(BlockTintSources.constant(0xFFFF0000)))
                .item()
            .model(() -> Client::testBlockModel)
                    .build()
                .blockEntity(TestBlockEntity::new)
                    .renderer(() -> TestBlockEntityRenderer::new)
                    .build()
                .register();

    @VisibleForTesting
    public final BlockEntry<Block> magicItemModelTest = registrate.object("magic_item_model")
            .block(Block::new)
            .blockstate(() -> (ctx, prov) ->
                    prov.create(ctx.getEntry(), prov.getBuilder()
                            .transformTemplate(t -> t
                                    .parent(prov.mcLoc("block/gold_block"))
                            ).build(prov.modLoc("block/subfolder/" + ctx.getName()))))
            .simpleItem()
            .register();

    @VisibleForTesting
    public final ItemEntry<BlockItem> testblockitem = (ItemEntry<BlockItem>) testblock.<Item, BlockItem>getSibling(Registries.ITEM);
    @VisibleForTesting
    public final BlockEntityEntry<ChestBlockEntity> testblockbe = BlockEntityEntry.cast(testblock.getSibling(Registries.BLOCK_ENTITY_TYPE));

    @VisibleForTesting
    public final EntityEntry<TestEntity> testentity = registrate.object("testentity")
            .entity(TestEntity::new, MobCategory.CREATURE)
            .attributes(Pig::createAttributes)
            .renderer(() -> PigRenderer::new)
            .spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR)
            //TODO <1.21.4> .defaultSpawnEgg(0xFF0000, 0x00FF00)
            .loot((prov, type) -> prov.add(type, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(Items.DIAMOND)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                    .apply(EnchantedCountIncreaseFunction.lootingMultiplier(prov.getRegistries(), UniformGenerator.between(0, 2)))))))
            .tag(EntityTypeTags.RAIDERS)
            .register();

    @VisibleForTesting
    public final BlockEntityEntry<TestDummyBlockEntity> testblockentity = registrate.object("testblockentity")
            .blockEntity(TestDummyBlockEntity::new)
            .validBlock(() -> Blocks.DIRT)//TODO <1.21.4> now empty valid block is not allowed
            .register();

    @VisibleForTesting
    public final FluidEntry<BaseFlowingFluid.Flowing> testfluid = registrate.object("testfluid")
            .fluid(
                    Identifier.withDefaultNamespace("block/water_flow"),
                    Identifier.withDefaultNamespace("block/lava_still"),
					FluidType::new)
            .properties(p -> p.lightLevel(15).canConvertToSource(true))
            .source(BaseFlowingFluid.Source::new) // TODO should be unnecessary
            .bucket()
                .model(() -> (ctx, prov) -> prov.bucketItem(ctx, false, false))
                .build()
            .register();

    @VisibleForTesting
    public final MenuEntry<ChestMenu> testmenu = registrate.object("testmenu")
            .menu((type, windowId, inv) -> new ChestMenu(type, windowId, inv, new SimpleContainer(9 * 9), 9), () -> ContainerScreen::new)
            .register();
    
//    private final RegistryEntry<TestBiome> testbiome = registrate.object("testbiome")
//            .biome(TestBiome::new)
//            .properties(b -> b.category(Category.PLAINS)
//                    .surfaceBuilder(SurfaceBuilder.DEFAULT, new SurfaceBuilderConfig(Blocks.GRASS_BLOCK.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.CLAY.getDefaultState()))
//                    .precipitation(RainType.RAIN)
//                    .depth(1)
//                    .scale(1)
//                    .temperature(1)
//                    .downfall(1)
//                    .waterColor(0x3f76e4)
//                    .waterFogColor(0x050533))
//            .typeWeight(BiomeType.WARM, 1000)
//            .addDictionaryTypes(BiomeDictionary.Type.LUSH)
//            .forceAutomaticDictionaryTypes()
//            .addFeature(Decoration.SURFACE_STRUCTURES, () -> Feature.BAMBOO, new ProbabilityConfig(0), () -> Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(20))
//            .addFeature(Decoration.SURFACE_STRUCTURES, () -> Feature.ICE_SPIKE, () -> Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(100))
//            .addFeatures(DefaultBiomeFeatures::addVeryDenseGrass)
//            .addCarver(Carving.AIR, () -> WorldCarver.CAVE, new ProbabilityConfig(0.1F))
//            .addSpawn(EntityClassification.CREATURE, () -> EntityType.IRON_GOLEM, 1, 2, 3)
//            .addSpawn(EntityClassification.CREATURE, testentity, 1, 4, 8)
//            .register();
//
//    private final RegistryEntry<TestBiome> testbiome2 = registrate.object("testbiome2")
//            .biome(TestBiome::new)
//            .properties(b -> b.category(Category.DESERT)
//                    .surfaceBuilder(SurfaceBuilder.DEFAULT, new SurfaceBuilderConfig(Blocks.SAND.getDefaultState(), Blocks.RED_SANDSTONE.getDefaultState(), Blocks.GRAVEL.getDefaultState()))
//                    .precipitation(RainType.NONE)
//                    .depth(1)
//                    .scale(1)
//                    .temperature(1)
//                    .downfall(1)
//                    .waterColor(0x3f76e4)
//                    .waterFogColor(0x050533))
//            .typeWeight(BiomeType.DESERT, 1000)
//            .addDictionaryTypes(BiomeDictionary.Type.DRY)
//            .forceAutomaticDictionaryTypes()
//            .copyFeatures(() -> Biomes.DESERT)
//            .copyCarvers(() -> Biomes.DESERT)
//            .copySpawns(() -> Biomes.DESERT)
//            .register();
//
//    private @Nullable DimensionType testdimensiontype;
//    private final RegistryEntry<ModDimension> testdimension = registrate.object("testdimension")
//            .dimension(OverworldDimension::new)
//            .hasSkyLight(false)
//            .keepLoaded(false)
//            .dimensionTypeCallback(t -> testdimensiontype = t)
//            .register();

    @VisibleForTesting
    public final ResourceKey<Registry<TestCustomRegistryEntry>> CUSTOM_REGISTRY = registrate.makeRegistry("custom", RegistryBuilder::new);
    @VisibleForTesting
    public final RegistryEntry<TestCustomRegistryEntry, TestCustomRegistryEntry> testcustom = registrate.object("testcustom")
            .simple(CUSTOM_REGISTRY, TestCustomRegistryEntry::new);

//    private final BlockBuilder<Block, Registrate> INVALID_TEST = registrate.object("invalid")
//            .block(Block::new)
//            .addLayer(() -> RenderType::getTranslucent);

    private static <T extends Block, P> BlockBuilder<T, P> applyDiamondDrop(BlockBuilder<T, P> builder) {
        return builder.loot((prov, block) -> prov.dropOther(block, Items.DIAMOND));
    }

    private static @Nullable TestMod INSTANCE = null;

    public TestMod(IEventBus eventBus, ModContainer container) {
        INSTANCE = this;

        registrate.addRawLang("testmod.custom.lang", "Test");
        registrate.addRawLang("testmod.custom.lang.with_placeholders1", "Placeholder 1 %s Placeholder 2 %s");
        registrate.addRawLang("testmod.custom.lang.with_placeholders2", "Placeholder 1 %s Placeholder 2 %2$s Placeholder 3 %s");
        registrate.addRawLang("testmod.custom.lang.brackets", "(Bracket 1) [Bracket 2] {Bracket 3} <Bracket 4> ◁Bracket 5▷");
        registrate.addRawLang("testmod.custom.lang.slashes", "/commmands look good and here is a backslash \\");
        registrate.addLang("tooltip", testblock.getId(), "Egg.");
        registrate.addLang("item", testitem.getId(), "testextra", "Magic!");
        registrate.addDataGenerator(ProviderType.ADVANCEMENT, adv -> Advancement.Builder.advancement()
            .addCriterion("has_egg", InventoryChangeTrigger.TriggerInstance.hasItems(Items.EGG))
            .display(Items.EGG,
                    adv.title(registrate.getModid(), "root", "Test Advancement"), adv.desc(registrate.getModid(), "root", "Get an egg."),
                    Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"), AdvancementType.TASK, true, true, false)
            .save(adv, registrate.getModid() + ":root"));
        registrate.addDataGenerator(ProviderType.GENERIC_SERVER, provider -> provider.add(data -> {
            // generic server side provider to generate custom dimension
            // to teleport to this dimension use the following command
            // /execute as @s in testmod:test_dimension run tp @s 0 64 0
            // you can validate you are in this dimension by checking the debug screen
            // right underneath the `Chunks[C]` and `Chunk[S]` should be the dimension name
            var testDimensionTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("testmod", "test_dimension_type"));

            return new DatapackBuiltinEntriesProvider(
                    data.output(),
                    data.registries(),
                    new RegistrySetBuilder()
                            // custom dimension type, just a simple overworld-like dimension
                            .add(Registries.DIMENSION_TYPE, context -> {
                                HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);

                                context.register(
                                        testDimensionTypeKey,
                                        new DimensionType(
                                                /* hasFixedTime */ false,
                                                /* hasSky */ true,
                                                /* hasCeiling */ false,
                                                /* hasEnderDragonFight */ false,
                                                /* coordinateScale */ 1D,
                                                /* minY */ -64,
                                                /* height */ 384,
                                                /* localHeight */ 384,
                                                /* infiniBurn */ blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                                                /* ambientLight */ 0F,
                                                new DimensionType.MonsterSettings(
                                                        /* monsterSpawnLightTest */ UniformInt.of(0, 7),
                                                        /* monsterSpawnBlockLightLimit */ 0
                                                ),
                                                DimensionType.Skybox.OVERWORLD,
                                                CardinalLighting.Type.DEFAULT,
                                                EnvironmentAttributeMap.builder()
                                                        .set(EnvironmentAttributes.FOG_COLOR, 0xffc0d8ff)
                                                        .set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8F))
                                                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xff0a0a0a)
                                                        .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
                                                        .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.33f)
                                                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                                                        .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
                                                        .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                                                        .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
                                                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                                                        .build(),
                                                context.lookup(Registries.TIMELINE).getOrThrow(TimelineTags.IN_OVERWORLD),
                                                Optional.of(context.lookup(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD))
                                        )
                                );
                            })
                            // register custom dimension for the dimension type
                            // simple single biome (plains) dimension
                            .add(Registries.LEVEL_STEM, context -> {
                                var plains = context.lookup(Registries.BIOME).getOrThrow(Biomes.PLAINS);
                                var testDimensionType = context.lookup(Registries.DIMENSION_TYPE).getOrThrow(testDimensionTypeKey);
                                var overworldNoiseSettings = context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);

                                context.register(
                                        ResourceKey.create(Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath("testmod", "test_dimension")),
                                        new LevelStem(
                                                testDimensionType,
                                                new NoiseBasedChunkGenerator(
                                                        new FixedBiomeSource(plains),
                                                        overworldNoiseSettings
                                                )
                                        )
                                );
                            }),
                    Set.of("testmod")
            );
        }));

        eventBus.addListener(this::onCommonSetup);

        // Setup gametests for normal run config
        final MutableTestFramework framework = FrameworkConfiguration
                .builder(Identifier.fromNamespaceAndPath(MOD_ID, "tests"))
                .clientConfiguration(() -> ClientConfiguration.builder()
                        .toggleOverlayKey(GLFW.GLFW_KEY_O)
                        .openManagerKey(GLFW.GLFW_KEY_M)
                        .build())
                .enable(Feature.CLIENT_SYNC, Feature.TEST_STORE)
                .build()
                .create();

        framework.init(eventBus, container);

        NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
            framework.registerCommands(node);
            event.getDispatcher().register(node);
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (!sawCallback.get()) {
            throw new IllegalStateException("Register callback not fired!");
        }

        testblock.asStackTemplate();
        testitem.is(Items.SNOWBALL);
        testblockitem.is(Items.STONE);
        testblockbe.is(BlockEntityTypes.CHEST);
        // testbiome.is(Feature.BAMBOO); // should not compile
        if (testfluid.get().getBucket() == Items.AIR) throw new IllegalStateException("Expected bucket for test fluid"); // should not crash
    }

    private static class Client {
        private static void testBlockModel(DataGenContext<Item, BlockItem> ctx, RegistrateItemModelGenerator prov) {
            prov.generateTintedModel(ctx.get(), prov.mcLoc("item/egg"), new Constant(0xFFFF0000));
        }
    }

    public static TestMod instance() {
        Objects.requireNonNull(INSTANCE, "Attempting to get mod instance before mod construction");
        return INSTANCE;
    }
}
