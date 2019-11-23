package com.tterrag.registrate.builders;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider.LootType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class BlockBuilder<T extends Block, P> extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {

    private final Function<Block.Properties, T> factory;
    private final Block.Properties properties = Block.Properties.create(Material.ROCK);
    
    public BlockBuilder(Registrate owner, P parent, String name, BuilderCallback callback, Function<Block.Properties, T> factory) {
        super(owner, parent, name, callback, Block.class);
        this.factory = factory;
        blockstate(ctx -> ctx.getProvider().simpleBlock(ctx.getEntry()));
        loot(RegistrateBlockLootTables::registerDropSelfLootTable);
    }
    
    public <I extends BlockItem> BlockBuilder<T, P> defaultItem() {
        return item().build();
    }

    public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
        return item(BlockItem::new);
    }

    @SuppressWarnings("unchecked")
    public <I extends BlockItem> ItemBuilder<I, BlockBuilder<T, P>> item(BiFunction<? super T, Item.Properties, ? extends I> factory) {
        return getOwner().<I, BlockBuilder<T, P>>item(this, p -> factory.apply((T) getOwner().get(getName(), Block.class).get(), p))
                .model(ctx -> ctx.getProvider().blockItem(this::get));
    }

    public BlockBuilder<T, P> blockstate(Consumer<DataGenContext<RegistrateBlockstateProvider, Block, T>> cons) {
        return addData(ProviderType.BLOCKSTATE, cons);
    }
    
    public BlockBuilder<T, P> loot(BiConsumer<RegistrateBlockLootTables, T> cons) {
        return addData(ProviderType.LOOT, ctx -> ctx.getProvider()
                .addLootAction(LootType.BLOCK, prov -> cons.accept(prov, ctx.getEntry())));
    }
    
    public BlockBuilder<T, P> recipe(Consumer<DataGenContext<RegistrateRecipeProvider, Block, T>> cons) {
        return addData(ProviderType.RECIPE, cons);
    }
    
    @Override
    protected T createEntry() {
        return factory.apply(properties);
    }
}
