package com.tterrag.registrate.test.mod;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

@Mod("testmod")
public class TestMod {
    
    private static class TestEntity extends PigEntity {

        public TestEntity(EntityType<? extends PigEntity> p_i50250_1_, World p_i50250_2_) {
            super(p_i50250_1_, p_i50250_2_);
        }
    }

    public TestMod() {
        Registrate registrate = Registrate.create("testmod");
        RegistryObject<Item> testitem = registrate.object("testitem")
                .item(Item::new)
                    .properties(p -> p.group(ItemGroup.MISC).food(new Food.Builder().hunger(1).saturation(0.2f).build()))
                    .tag(ItemTags.BEDS)
                    .model(ctx -> ctx.getProvider()
                            .withExistingParent(ctx.getName(), new ResourceLocation("block/stone")))
                    .register();
        
        RegistryObject<Block> testblock = registrate.object("testblock")
                .block(Block::new)
                    .blockstate(ctx -> ctx.getProvider()
                            .simpleBlock(ctx.getEntry(),
                                    ctx.getProvider().withExistingParent(ctx.getName(), new ResourceLocation("block/diamond_block"))))
                    .transform(this::applyDiamondDrop)
                    .recipe(ctx -> {
                        ShapedRecipeBuilder.shapedRecipe(ctx.getEntry())
                                .patternLine("DDD").patternLine("DED").patternLine("DDD")
                                .key('D', Items.DIAMOND)
                                .key('E', Items.EGG)
                                .addCriterion("has_egg", ctx.getProvider().hasItem(Items.EGG))
                                .build(ctx.getProvider());
                        
                        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(ctx.getEntry()), Blocks.DIAMOND_BLOCK, 1f, 200)
                                .addCriterion("has_testitem", ctx.getProvider().hasItem(ctx.getEntry()))
                                .build(ctx.getProvider(), new ResourceLocation("testmod", "diamond_block_from_" + ctx.getName()));
                    })
                    .tag(BlockTags.BAMBOO_PLANTABLE_ON)
                    .item()
                        .properties(p -> p.group(ItemGroup.MISC))
                        .model(ctx -> ctx.getProvider()
                                .withExistingParent(ctx.getName(), new ResourceLocation("item/egg")))
                        .build()
                    .tileEntity(ChestTileEntity::new)
                    .register();
        
        RegistryObject<TileEntityType<ChestTileEntity>> testblockte = registrate.get(TileEntityType.class);
        
        @SuppressWarnings("deprecation")
        RegistryObject<EntityType<TestEntity>> testentity = registrate.object("testentity")
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
        
        RegistryObject<TileEntityType<ChestTileEntity>> testtile = registrate.object("testtile")
                .tileEntity(ChestTileEntity::new)
                .register();
    }
    
    private <T extends Block, P> BlockBuilder<T, P> applyDiamondDrop(BlockBuilder<T, P> builder) {
        return builder.loot((prov, block) -> prov.registerDropping(block, Items.DIAMOND));
    }
}
