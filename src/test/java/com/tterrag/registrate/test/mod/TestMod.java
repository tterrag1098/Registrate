package com.tterrag.registrate.test.mod;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
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
    
    private static class TestEntity extends PigEntity {

        public TestEntity(EntityType<? extends PigEntity> p_i50250_1_, World p_i50250_2_) {
            super(p_i50250_1_, p_i50250_2_);
        }
    }

    public TestMod() {
        Registrate registrate = Registrate.create("testmod").itemGroup(TestItemGroup::new, "Test Mod");
        RegistryEntry<Item> testitem = registrate.object("testitem")
                .item(Item::new)
                    .properties(p -> p.food(new Food.Builder().hunger(1).saturation(0.2f).build()))
                    .tag(ItemTags.BEDS)
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("block/stone")))
                    .register();
        
        RegistryEntry<EntityType<TestEntity>> testduplicatename = registrate.object("testitem")
                .entity(TestEntity::new, EntityClassification.CREATURE)
                .loot((tb, e) -> tb.registerLootTable(e, LootTable.builder()))
                .register();
        
        RegistryEntry<Block> testblock = registrate.object("testblock")
                .block(Block::new)
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                                    prov.models().withExistingParent(ctx.getName(), new ResourceLocation("block/diamond_block"))))
                    .transform(this::applyDiamondDrop)
                    .recipe((ctx, prov) -> {
                        ShapedRecipeBuilder.shapedRecipe(ctx.getEntry())
                                .patternLine("DDD").patternLine("DED").patternLine("DDD")
                                .key('D', Items.DIAMOND)
                                .key('E', Items.EGG)
                                .addCriterion("has_egg", prov.hasItem(Items.EGG))
                                .build(prov);
                        
                        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(ctx.getEntry()), Blocks.DIAMOND_BLOCK, 1f, 200)
                                .addCriterion("has_testitem", prov.hasItem(ctx.getEntry()))
                                .build(prov, new ResourceLocation("testmod", "diamond_block_from_" + ctx.getName()));
                    })
                    .tag(BlockTags.BAMBOO_PLANTABLE_ON)
                    .item()
                        .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("item/egg")))
                        .build()
                    .tileEntity(ChestTileEntity::new)
                    .register();
        
        RegistryEntry<BlockItem> testblockitem = testblock.getSibling(Item.class);
        RegistryEntry<TileEntityType<ChestTileEntity>> testblockte = testblock.getSibling(ForgeRegistries.TILE_ENTITIES);
        
        @SuppressWarnings("deprecation")
        RegistryEntry<EntityType<TestEntity>> testentity = registrate.object("testentity")
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
        
        RegistryEntry<TileEntityType<ChestTileEntity>> testtile = registrate.object("testtile")
                .tileEntity(ChestTileEntity::new)
                .register();
        
        RegistryEntry<ForgeFlowingFluid.Flowing> testfluid = registrate.object("testfluid")
                .fluid(new ResourceLocation("block/water_flow"), new ResourceLocation("block/lava_still"))
                .attributes(a -> a.luminosity(15))
                .properties(p -> p.canMultiply())
                .register();
        
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
    
    private <T extends Block, P> @NonnullType BlockBuilder<T, P> applyDiamondDrop(BlockBuilder<T, P> builder) {
        return builder.loot((prov, block) -> prov.registerDropping(block, Items.DIAMOND));
    }
}
