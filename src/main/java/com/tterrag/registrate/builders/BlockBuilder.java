package com.tterrag.registrate.builders;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider.LootType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;

/**
 * A builder for blocks, allows for customization of the {@link Block.Properties}, creation of block items, and configuration of data associated with blocks (loot tables, recipes, etc.).
 * <p>
 * By default, the block will be assigned the following data:
 * <ul>
 * <li>A default blockstate file mapping all states to one model</li>
 * <li>A simple cube_all model (used in the blockstate) with one texture</li>
 * <li>A self-dropping loot table</li>
 * <li>The default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(Supplier)}.</li>
 * </ul>
 * 
 * @param <T>
 *            The type of block being built
 * @param <P>
 *            Parent object type
 */
public final class BlockBuilder<T extends Block, P> extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {

    private final Function<Block.Properties, T> factory;
    private final Block.Properties properties = Block.Properties.create(Material.ROCK);

    public BlockBuilder(Registrate owner, P parent, String name, BuilderCallback callback, Function<Block.Properties, T> factory) {
        super(owner, parent, name, callback, Block.class);
        this.factory = factory;
        defaultBlockstate();
        defaultLoot();
        defaultLang();
    }

    /**
     * Modify the properties of the block. Modifications are <em>not</em> done lazily, instead changing the properteis object immediately, and as such this method can be called multiple times to
     * perform different operations.
     * 
     * @param cons
     *            The action to perform on the properties
     * @return this {@link BlockBuilder}
     */
    public BlockBuilder<T, P> properties(Consumer<Block.Properties> cons) {
        cons.accept(properties);
        return this;
    }

    /**
     * Create a standard {@link BlockItem} for this block, building it immediately, and not allowing for further configuration.
     * 
     * @return this {@link BlockBuilder}
     * @see #item()
     */
    public BlockBuilder<T, P> simpleItem() {
        return item().build();
    }

    /**
     * Create a standard {@link BlockItem} for this block, and return the builder for it so that further customization can be done.
     * 
     * @return the {@link ItemBuilder} for the {@link BlockItem}
     */
    public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
        return item(BlockItem::new);
    }

    /**
     * Create a {@link BlockItem} for this block, which is created by the given factory, and return the builder for it so that further customization can be done.
     * 
     * @param <I>
     *            The type of the item
     * @param factory
     *            A factory for the item, which accepts the block object and properties and returns a new item
     * @return the {@link ItemBuilder} for the {@link BlockItem}
     */
    public <I extends BlockItem> ItemBuilder<I, BlockBuilder<T, P>> item(BiFunction<? super T, Item.Properties, ? extends I> factory) {
        return getOwner().<I, BlockBuilder<T, P>> item(this, p -> factory.apply(get().get(), p)).model(ctx -> ctx.getProvider().blockItem(get()));
    }

    /**
     * Create a {@link TileEntity} for this block, which is created by the given factory, and assigned this block as its one and only valid block.
     * 
     * @param <TE>
     *            The type of the tile entity
     * @param factory
     *            A factory for the tile entity
     * @return this {@link BlockBuilder}
     */
    public <TE extends TileEntity> BlockBuilder<T, P> tileEntity(Supplier<? extends TE> factory) {
        return getOwner().<TE, BlockBuilder<T, P>> tileEntity(this, factory).validBlock(get()).build();
    }

    /**
     * Assign the default blockstate, which maps all states to a single model file (via {@link RegistrateBlockstateProvider#simpleBlock(Block)}). This is the default, so it is generally not necessary
     * to call, unless for undoing previous changes.
     * 
     * @return this {@link BlockBuilder}
     */
    public BlockBuilder<T, P> defaultBlockstate() {
        return blockstate(ctx -> ctx.getProvider().simpleBlock(ctx.getEntry()));
    }

    /**
     * Configure the blockstate/models for this block.
     * 
     * @param cons
     *            The callback which will be invoked during data generation.
     * @return this {@link BlockBuilder}
     * @see #addData(ProviderType, Consumer)
     */
    public BlockBuilder<T, P> blockstate(Consumer<DataGenContext<RegistrateBlockstateProvider, Block, T>> cons) {
        return addData(ProviderType.BLOCKSTATE, cons);
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(Supplier)}. This is the default, so it is generally not necessary to call, unless for undoing
     * previous changes.
     * 
     * @return this {@link BlockBuilder}
     */
    public BlockBuilder<T, P> defaultLang() {
        return lang(Block::getTranslationKey);
    }

    /**
     * Set the translation for this block.
     * 
     * @param name
     *            A localized english name
     * @return this {@link BlockBuilder}
     * @see #addData(ProviderType, Consumer)
     */
    public BlockBuilder<T, P> lang(String name) {
        return lang(Block::getTranslationKey, name);
    }

    /**
     * Assign the default loot table, as specified by {@link RegistrateBlockLootTables#registerDropSelfLootTable(Block)}. This is the default, so it is generally not necessary to call, unless for
     * undoing previous changes.
     * 
     * @return this {@link BlockBuilder}
     */
    public BlockBuilder<T, P> defaultLoot() {
        return loot(RegistrateBlockLootTables::registerDropSelfLootTable);
    }

    /**
     * Configure the loot table for this block. This is different than most data gen callbacks as the callback does not accept a {@link DataGenContext}, but instead a
     * {@link RegistrateBlockLootTables}, for creating specifically block loot tables.
     * 
     * @param cons
     *            The callback which will be invoked during block loot table creation.
     * @return this {@link BlockBuilder}
     */
    public BlockBuilder<T, P> loot(BiConsumer<RegistrateBlockLootTables, T> cons) {
        return addData(ProviderType.LOOT, ctx -> ctx.getProvider().addLootAction(LootType.BLOCK, prov -> cons.accept(prov, ctx.getEntry())));
    }

    /**
     * Configure the recipe(s) for this block.
     * 
     * @param cons
     *            The callback which will be invoked during block data generation.
     * @return this {@link BlockBuilder}
     * @see #addData(ProviderType, Consumer)
     */
    public BlockBuilder<T, P> recipe(Consumer<DataGenContext<RegistrateRecipeProvider, Block, T>> cons) {
        return addData(ProviderType.RECIPE, cons);
    }

    /**
     * Assign a {@link Tag} to this block.
     * 
     * @param tag
     *            The tag to assign
     * @return this {@link BlockBuilder}
     */
    public BlockBuilder<T, P> tag(Tag<Block> tag) {
        return tag(ProviderType.BLOCK_TAGS, tag);
    }

    @Override
    protected T createEntry() {
        return factory.apply(properties);
    }
}
