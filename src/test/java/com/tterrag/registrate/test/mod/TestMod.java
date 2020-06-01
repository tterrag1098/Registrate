package com.tterrag.registrate.test.mod;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("testmod")
public class TestMod {
    
    private static class TestItemGroup extends ItemGroup {

        public TestItemGroup() {
            super("testmod");
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.EGG);
        }
    }
    
    private class TestBlock extends Block {

        public TestBlock(Properties properties) {
            super(properties);
        }
        
        @Override
        protected void fillStateContainer(Builder<Block, BlockState> builder) {
            super.fillStateContainer(builder);
            builder.add(BlockStateProperties.CHEST_TYPE);
        }

        @Override
        public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            if (!worldIn.isRemote) {
                player.openContainer(new INamedContainerProvider() {
                    
                    @Override
                    @Nullable
                    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
                        return new ChestContainer(ContainerType.GENERIC_9X3, windowId, inv, (TestTileEntity) worldIn.getTileEntity(pos), 3);
                    }
                    
                    @Override
                    public ITextComponent getDisplayName() {
                        return new StringTextComponent("Test");
                    }
                });
            }
            return ActionResultType.SUCCESS;
        }

        @Override
        public boolean hasTileEntity(BlockState state) {
            return true;
        }

        @Override
        @Nullable
        public TileEntity createTileEntity(BlockState state, IBlockReader world) {
            return testblockte.get().create();
        }
    }

    private static class TestTileEntity extends ChestTileEntity {

        public TestTileEntity(TileEntityType<? extends TestTileEntity> type) {
            super(type);
        }
    }
    
    private static class TestTileEntityRenderer extends TileEntityRenderer<TestTileEntity> {

        public TestTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
            super(rendererDispatcherIn);
        }

        @Override
        public void render(TestTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(Items.DIAMOND), TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();
        }
    }
    
    private class TestDummyTileEntity extends TileEntity {

        public TestDummyTileEntity() {
            super(testtile.get());
        }
    }

    private static class TestEntity extends PigEntity {

        public TestEntity(EntityType<? extends PigEntity> p_i50250_1_, World p_i50250_2_) {
            super(p_i50250_1_, p_i50250_2_);
        }
    }
    
    private static class TestEnchantment extends Enchantment {

        public TestEnchantment(Rarity rarityIn, EnchantmentType typeIn, EquipmentSlotType... slots) {
            super(rarityIn, typeIn, slots);
        }
        
        @Override
        public int getMaxLevel() {
            return 5;
        }
    }
    
    private static class TestBiome extends Biome {

        public TestBiome(Biome.Builder biomeBuilder) {
            super(biomeBuilder);
        }
    }
    
    private final Registrate registrate = Registrate.create("testmod").itemGroup(TestItemGroup::new, "Test Mod");
    
    private final AtomicBoolean sawCallback = new AtomicBoolean();
    
    private final RegistryEntry<Item> testitem = registrate.object("testitem")
            .item(Item::new)
                .onRegister(item -> sawCallback.set(true))
                .properties(p -> p.food(new Food.Builder().hunger(1).saturation(0.2f).build()))
                .tag(ItemTags.BEDS)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("block/stone")))
                .register();
    
    private final RegistryEntry<EntityType<TestEntity>> testduplicatename = registrate.object("testitem")
            .entity(TestEntity::new, EntityClassification.CREATURE)
            .loot((tb, e) -> tb.registerLootTable(e, LootTable.builder()))
            .register();
    
    private final BlockEntry<TestBlock> testblock = registrate.object("testblock")
            .block(TestBlock::new)
                .properties(p -> p.notSolid())
                .addLayer(() -> RenderType::getCutout)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                                prov.models().withExistingParent(ctx.getName(), new ResourceLocation("block/glass"))))
                .transform(TestMod::applyDiamondDrop)
                .recipe((ctx, prov) -> {
                    ShapedRecipeBuilder.shapedRecipe(ctx.getEntry())
                            .patternLine("DDD").patternLine("DED").patternLine("DDD")
                            .key('D', Items.DIAMOND)
                            .key('E', Items.EGG)
                            .addCriterion("has_egg", prov.hasItem(Items.EGG))
                            .build(prov);
                    
                    prov.food(DataIngredient.items(ctx), Blocks.DIAMOND_BLOCK.delegate, 1f);
                })
                .tag(BlockTags.BAMBOO_PLANTABLE_ON)
                .item()
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("item/egg")))
                    .build()
                .tileEntity(TestTileEntity::new)
                    .renderer(() -> TestTileEntityRenderer::new)
                    .build()
                .register();
    
    private final BlockEntry<Block> magicItemModelTest = registrate.object("magic_item_model")
            .block(Block::new)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().withExistingParent("block/subfolder/" + ctx.getName(), prov.mcLoc("block/gold_block"))))
            .simpleItem()
            .register();
    
    private final ItemEntry<BlockItem> testblockitem = (ItemEntry<BlockItem>) testblock.<Item, BlockItem>getSibling(Item.class);
    private final RegistryEntry<TileEntityType<ChestTileEntity>> testblockte = testblock.getSibling(ForgeRegistries.TILE_ENTITIES);
    
    @SuppressWarnings("deprecation")
    private final RegistryEntry<EntityType<TestEntity>> testentity = registrate.object("testentity")
            .entity(TestEntity::new, EntityClassification.CREATURE)
            .defaultSpawnEgg(0xFF0000, 0x00FF00)
            .loot((prov, type) -> prov.registerLootTable(type, LootTable.builder()
                    .addLootPool(LootPool.builder()
                            .rolls(ConstantRange.of(1))
                            .addEntry(ItemLootEntry.builder(Items.DIAMOND)
                                    .acceptFunction(SetCount.builder(RandomValueRange.of(1, 3)))
                                    .acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0, 2)))))))
            .tag(EntityTypeTags.RAIDERS)
            .register();
    
    private final RegistryEntry<TileEntityType<TestDummyTileEntity>> testtile = registrate.object("testtile")
            .tileEntity(TestDummyTileEntity::new)
            .register();
    
    private final RegistryEntry<ForgeFlowingFluid.Flowing> testfluid = registrate.object("testfluid")
            .fluid(new ResourceLocation("block/water_flow"), new ResourceLocation("block/lava_still"))
            .attributes(a -> a.luminosity(15))
            .properties(p -> p.canMultiply())
            .bucket()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/water_bucket")))
                .build()
            .register();
    
    private final RegistryEntry<ContainerType<ChestContainer>> GENERIC_9x9 = registrate.object("testcontainer")
            .container((type, windowId, inv) -> new ChestContainer(type, windowId, inv, new Inventory(9 * 9), 9), () -> ChestScreen::new)
            .register();
    
    private final RegistryEntry<TestEnchantment> testenchantment = registrate.object("testenchantment")
            .enchantment(EnchantmentType.ARMOR, TestEnchantment::new)
            .rarity(Rarity.UNCOMMON)
            .addArmorSlots()
            .register();
    
    private final RegistryEntry<TestBiome> testbiome = registrate.object("testbiome")
            .biome(TestBiome::new)
            .properties(b -> b.category(Category.PLAINS)
                    .surfaceBuilder(SurfaceBuilder.DEFAULT, new SurfaceBuilderConfig(Blocks.COARSE_DIRT.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.CLAY.getDefaultState()))
                    .precipitation(RainType.RAIN)
                    .depth(1)
                    .scale(1)
                    .temperature(1)
                    .downfall(1)
                    .waterColor(0x3f76e4)
                    .waterFogColor(0x050533))
            .typeWeight(BiomeType.WARM, 1000)
            .addDictionaryTypes(BiomeDictionary.Type.DRY)
            .forceAutomaticDictionaryTypes()
            .addFeatures(DefaultBiomeFeatures::addVeryDenseGrass)
            .addSpawns(b -> b.getSpawns(EntityClassification.CREATURE)
                    .add(new SpawnListEntry(EntityType.CAT, 1, 5, 10)))
            .register();
    
//    private final BlockBuilder<Block, Registrate> INVALID_TEST = registrate.object("invalid")
//            .block(Block::new)
//            .addLayer(() -> RenderType::getTranslucent);
    
    private static <T extends Block, P> @NonnullType BlockBuilder<T, P> applyDiamondDrop(BlockBuilder<T, P> builder) {
        return builder.loot((prov, block) -> prov.registerDropping(block, Items.DIAMOND));
    }

    public TestMod() {
        
        registrate.addRawLang("testmod.custom.lang", "Test");
        registrate.addLang("tooltip", testblock.getId(), "Egg.");
        registrate.addLang("item", testitem.getId(), "testextra", "Magic!");
        registrate.addDataGenerator(ProviderType.ADVANCEMENT, adv -> {
            Advancement.Builder.builder()
                .withCriterion("has_egg", InventoryChangeTrigger.Instance.forItems(Items.EGG))
                .withDisplay(Items.EGG,
                        adv.title(registrate.getModid(), "root", "Test Advancement"), adv.desc(registrate.getModid(), "root", "Get an egg."), 
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"), FrameType.TASK, true, true, false)
                .register(adv, registrate.getModid() + ":root");
        });
    }
    
    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (!sawCallback.get()) {
            throw new IllegalStateException("Register callback not fired!");
        }
        
        testblock.asStack();
        testitem.is(Items.SNOWBALL);
        testblockitem.is(Items.STONE);
        testblockte.is(TileEntityType.CHEST);
        // testbiome.is(Feature.BAMBOO); // should not compile
    }
}
