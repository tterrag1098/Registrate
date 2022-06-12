package com.tterrag.registrate.test.mod;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod("testmod")
public class TestMod {
    
    private static class TestCreativeModeTab extends CreativeModeTab {

        public TestCreativeModeTab() {
            super("testmod");
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.EGG);
        }
    }
    
    private class TestBlock extends Block implements EntityBlock {

        public TestBlock(Properties properties) {
            super(properties);
        }
        
        @Override
        protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(BlockStateProperties.CHEST_TYPE);
        }

        @Override
        public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
            if (!worldIn.isClientSide) {
                player.openMenu(new MenuProvider() {
                    
                    @Override
                    @Nullable
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
                        return new ChestMenu(MenuType.GENERIC_9x3, windowId, inv, testblockbe.get(worldIn, pos).orElseThrow(IllegalStateException::new), 3);
                    }
                    
                    @Override
                    public Component getDisplayName() {
                        return new TextComponent("Test");
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        @Nullable
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return testblockbe.create(pos, state);
        }
    }

    private static class TestBlockEntity extends ChestBlockEntity {

        public TestBlockEntity(BlockEntityType<? extends TestBlockEntity> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }
    
    private static class TestBlockEntityRenderer implements BlockEntityRenderer<TestBlockEntity> {

        public TestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        }

        @Override
        public void render(TestBlockEntity blockEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Items.DIAMOND), TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, 0);
            matrixStackIn.popPose();
        }
    }
    
    private class TestDummyBlockEntity extends BlockEntity {

        public TestDummyBlockEntity(BlockEntityType<? extends TestDummyBlockEntity> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }

    private static class TestEntity extends Pig {

        public TestEntity(EntityType<? extends Pig> p_i50250_1_, Level p_i50250_2_) {
            super(p_i50250_1_, p_i50250_2_);
        }
    }
    
    private static class TestEnchantment extends Enchantment {

        public TestEnchantment(Rarity rarityIn, EnchantmentCategory typeIn, EquipmentSlot... slots) {
            super(rarityIn, typeIn, slots);
        }
        
        @Override
        public int getMaxLevel() {
            return 5;
        }
    }
    
//    private static class TestBiome extends Biome {
//
//        public TestBiome(Biome.Builder biomeBuilder) {
//            super(biomeBuilder);
//        }
//    }

    private static class TestCustomRegistryEntry extends ForgeRegistryEntry<TestCustomRegistryEntry> {}
    
    private final Registrate registrate = Registrate.create("testmod").creativeModeTab(TestCreativeModeTab::new, "Test Mod");
    
    private final AtomicBoolean sawCallback = new AtomicBoolean();
    
    private final RegistryEntry<Item> testitem = registrate.object("testitem")
            .item(Item::new)
                .onRegister(item -> sawCallback.set(true))
                .properties(p -> p.food(new FoodProperties.Builder().nutrition(1).saturationMod(0.2f).build()))
                .color(() -> () -> (stack, index) -> 0xFF0000FF)
                .tag(ItemTags.BEDS)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("block/stone")))
                .register();
    
    private final EntityEntry<TestEntity> testduplicatename = registrate.object("testitem")
            .entity(TestEntity::new, MobCategory.CREATURE)
            .attributes(Pig::createAttributes)
            .loot((tb, e) -> tb.add(e, LootTable.lootTable()))
            .renderer(() -> PigRenderer::new)
            .register();
    
    private final BlockEntry<TestBlock> testblock = registrate.object("testblock")
            .block(TestBlock::new)
                .properties(p -> p.noOcclusion())
                .addLayer(() -> RenderType::cutout)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                                prov.models().withExistingParent(ctx.getName(), new ResourceLocation("block/glass"))))
                .transform(TestMod::applyDiamondDrop)
                .recipe((ctx, prov) -> {
                    ShapedRecipeBuilder.shaped(ctx.getEntry())
                            .pattern("DDD").pattern("DED").pattern("DDD")
                            .define('D', Items.DIAMOND)
                            .define('E', Items.EGG)
                            .unlockedBy("has_egg", prov.has(Items.EGG))
                            .save(prov);
                    
                    prov.food(DataIngredient.items(ctx), Blocks.DIAMOND_BLOCK.delegate, 1f);
                })
                .tag(BlockTags.BAMBOO_PLANTABLE_ON, BlockTags.DRAGON_IMMUNE)
                .tag(BlockTags.WITHER_IMMUNE)
                .color(() -> () -> (state, world, pos, index) -> 0xFFFF0000)
                .item()
                    .color(() -> () -> (stack, index) -> 0xFFFF0000)
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("item/egg")))
                    .build()
                .blockEntity(TestBlockEntity::new)
                    .renderer(() -> TestBlockEntityRenderer::new)
                    .build()
                .register();
    
    private final BlockEntry<Block> magicItemModelTest = registrate.object("magic_item_model")
            .block(Block::new)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().withExistingParent("block/subfolder/" + ctx.getName(), prov.mcLoc("block/gold_block"))))
            .simpleItem()
            .register();
    
    private final ItemEntry<BlockItem> testblockitem = (ItemEntry<BlockItem>) testblock.<Item, BlockItem>getSibling(Registry.ITEM_REGISTRY);
    private final BlockEntityEntry<ChestBlockEntity> testblockbe = BlockEntityEntry.cast(testblock.getSibling(ForgeRegistries.BLOCK_ENTITIES));
    
    @SuppressWarnings("deprecation")
    private final RegistryEntry<EntityType<TestEntity>> testentity = registrate.object("testentity")
            .entity(TestEntity::new, MobCategory.CREATURE)
            .attributes(Pig::createAttributes)
            .renderer(() -> PigRenderer::new)
            .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules)
            .defaultSpawnEgg(0xFF0000, 0x00FF00)
            .loot((prov, type) -> prov.add(type, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(Items.DIAMOND)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 2)))))))
            .tag(EntityTypeTags.RAIDERS)
            .register();
    
    private final BlockEntityEntry<TestDummyBlockEntity> testblockentity = registrate.object("testblockentity")
            .blockEntity(TestDummyBlockEntity::new)
            .register();
    
    private final FluidEntry<ForgeFlowingFluid.Flowing> testfluid = registrate.object("testfluid")
            .fluid(new ResourceLocation("block/water_flow"), new ResourceLocation("block/lava_still"))
            .attributes(a -> a.luminosity(15))
            .properties(p -> p.canMultiply())
            .noBucket()
//            .bucket()
//                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/water_bucket")))
//                .build()
//            .removeTag(FluidTags.WATER)
            .register();
    
    private final RegistryEntry<MenuType<ChestMenu>> testmenu = registrate.object("testmenu")
            .menu((type, windowId, inv) -> new ChestMenu(type, windowId, inv, new SimpleContainer(9 * 9), 9), () -> ContainerScreen::new)
            .register();
    
    private final RegistryEntry<TestEnchantment> testenchantment = registrate.object("testenchantment")
            .enchantment(EnchantmentCategory.ARMOR, TestEnchantment::new)
            .rarity(Rarity.UNCOMMON)
            .addArmorSlots()
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

    private final Supplier<IForgeRegistry<TestCustomRegistryEntry>> customregistry = registrate.makeRegistry("custom", TestCustomRegistryEntry.class, () -> new RegistryBuilder<>());
    private final RegistryEntry<TestCustomRegistryEntry> testcustom = registrate.object("testcustom")
            .simple(TestCustomRegistryEntry.class, TestCustomRegistryEntry::new);

//    private final BlockBuilder<Block, Registrate> INVALID_TEST = registrate.object("invalid")
//            .block(Block::new)
//            .addLayer(() -> RenderType::getTranslucent);
    
    private static <T extends Block, P> @NonnullType BlockBuilder<T, P> applyDiamondDrop(BlockBuilder<T, P> builder) {
        return builder.loot((prov, block) -> prov.dropOther(block, Items.DIAMOND));
    }

    public TestMod() {
        
        registrate.addRawLang("testmod.custom.lang", "Test");
        registrate.addLang("tooltip", testblock.getId(), "Egg.");
        registrate.addLang("item", testitem.getId(), "testextra", "Magic!");
        registrate.addDataGenerator(ProviderType.ADVANCEMENT, adv -> {
            Advancement.Builder.advancement()
                .addCriterion("has_egg", InventoryChangeTrigger.TriggerInstance.hasItems(Items.EGG))
                .display(Items.EGG,
                        adv.title(registrate.getModid(), "root", "Test Advancement"), adv.desc(registrate.getModid(), "root", "Get an egg."), 
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"), FrameType.TASK, true, true, false)
                .save(adv, registrate.getModid() + ":root");
        });
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::afterServerStart);
    }
    
    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (!sawCallback.get()) {
            throw new IllegalStateException("Register callback not fired!");
        }
        
        testblock.asStack();
        testitem.is(Items.SNOWBALL);
        testblockitem.is(Items.STONE);
        testblockbe.is(BlockEntityType.CHEST);
        // testbiome.is(Feature.BAMBOO); // should not compile
    }
    
    private void afterServerStart(ServerStartedEvent event) {
    }
}
