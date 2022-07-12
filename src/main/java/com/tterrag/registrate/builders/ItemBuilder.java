package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.factory.ItemFactory;
import com.tterrag.registrate.providers.*;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A builder for items, allows for customization of the {@link Item.Properties} and configuration of data associated with items (models, recipes, etc.).
 * 
 * @param <T>
 *            The type of item being built
 * @param <P>
 *            Parent object type
 */
public class ItemBuilder<O extends AbstractRegistrate<O>, T extends Item, P> extends AbstractBuilder<O, Item, T, P, ItemBuilder<O, T, P>> {

    /**
     * Create a new {@link ItemBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The item will be assigned the following data:
     * <ul>
     * <li>A simple generated model with one texture (via {@link #defaultModel()})</li>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param factory
     *            Factory to create the item
     * @return A new {@link ItemBuilder} with reasonable default data generators.
     */
    public static <O extends AbstractRegistrate<O>, T extends Item, P> ItemBuilder<O, T, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ItemFactory<T> factory) {
        return create(owner, parent, name, callback, factory, null);
    }
    
    /**
     * Create a new {@link ItemBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The item will be assigned the following data:
     * <ul>
     * <li>A simple generated model with one texture (via {@link #defaultModel()})</li>
     * <li>The default translation (via {@link #defaultLang()})</li>
     * <li>An {@link CreativeModeTab} set in the properties from the tab supplier parameter, if non-null</li>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param factory
     *            Factory to create the item
     * @param tab
     *            The {@link CreativeModeTab} for the object, can be null for none
     * @return A new {@link ItemBuilder} with reasonable default data generators.
     */
    public static <O extends AbstractRegistrate<O>, T extends Item, P> ItemBuilder<O, T, P> create(O owner, P parent, String name, BuilderCallback<O> callback, ItemFactory<T> factory, @Nullable NonNullSupplier<? extends CreativeModeTab> tab) {
        return new ItemBuilder<>(owner, parent, name, callback, factory)
                .defaultModel().defaultLang()
                .transform(ib -> tab == null ? ib : ib.tab(tab));
    }

    private final ItemFactory<T> factory;
    
    private NonNullSupplier<Item.Properties> initialProperties = Item.Properties::new;
    private NonNullFunction<Item.Properties, Item.Properties> propertiesCallback = NonNullUnaryOperator.identity();
    
    @Nullable
    private NonNullSupplier<Supplier<ItemColor>> colorHandler;
    
    protected ItemBuilder(O owner, P parent, String name, BuilderCallback<O> callback, ItemFactory<T> factory) {
        super(owner, parent, name, callback, Registry.ITEM_REGISTRY);
        this.factory = factory;
    }

    /**
     * Modify the properties of the item. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     * <p>
     * If a different properties instance is returned, it will replace the existing one entirely.
     * 
     * @param func
     *            The action to perform on the properties
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<O, T, P> properties(NonNullUnaryOperator<Item.Properties> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    /**
     * Replace the initial state of the item properties, without replacing or removing any modifications done via {@link #properties(NonNullUnaryOperator)}.
     * 
     * @param properties
     *            A supplier to to create the initial properties
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<O, T, P> initialProperties(NonNullSupplier<Item.Properties> properties) {
        initialProperties = properties;
        return this;
    }

    public ItemBuilder<O, T, P> tab(NonNullSupplier<? extends CreativeModeTab> tab) {
        return properties(p -> p.tab(tab.get()));
    }
    
    /**
     * Register a block color handler for this item. The {@link ItemColor} instance can be shared across many items.
     * 
     * @param colorHandler
     *            The color handler to register for this item
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<O, T, P> color(NonNullSupplier<Supplier<ItemColor>> colorHandler) {
        if (this.colorHandler == null) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerItemColor);
        }
        this.colorHandler = colorHandler;
        return this;
    }
    
    protected void registerItemColor() {
        OneTimeEventReceiver.addModListener(ColorHandlerEvent.Item.class, e -> {
            NonNullSupplier<Supplier<ItemColor>> colorHandler = this.colorHandler;
            if (colorHandler != null) {
                e.getItemColors().register(colorHandler.get().get(), getEntry());
            }
        });
    }
    
    /**
     * Assign the default model to this item, which is simply a generated model with a single texture of the same name.
     * 
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<O, T, P> defaultModel() {
        return model((ctx, prov) -> prov.generated(ctx::getEntry));
    }

    /**
     * Configure the model for this item.
     * 
     * @param cons
     *            The callback which will be invoked during data creation
     * @return this {@link ItemBuilder}
     * @see #setData(ProviderType, NonNullBiConsumer)
     */
    public ItemBuilder<O, T, P> model(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cons) {
        return setData(ProviderType.ITEM_MODEL, cons);
    }
    
    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier)}. This is the default, so it is generally not necessary to call, unless for undoing
     * previous changes.
     * 
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<O, T, P> defaultLang() {
        return lang(Item::getDescriptionId);
    }
    
    /**
     * Set the translation for this item.
     * 
     * @param name
     *            A localized English name
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<O, T, P> lang(String name) {
        return lang(Item::getDescriptionId, name);
    }

    /**
     * Configure the recipe(s) for this item.
     * 
     * @param cons
     *            The callback which will be invoked during data generation.
     * @return this {@link ItemBuilder}
     * @see #setData(ProviderType, NonNullBiConsumer)
     */
    public ItemBuilder<O, T, P> recipe(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons) {
        return setData(ProviderType.RECIPE, cons);
    }
    
    /**
     * Assign {@link TagKey}{@code s} to this item. Multiple calls will add additional tags.
     * 
     * @param tags
     *            The tag to assign
     * @return this {@link ItemBuilder}
     */
    @SafeVarargs
    public final ItemBuilder<O, T, P> tag(TagKey<Item>... tags) {
        return tag(ProviderType.ITEM_TAGS, tags);
    }
    
    @Override
    protected T createEntry() {
        Item.Properties properties = this.initialProperties.get();
        properties = propertiesCallback.apply(properties);
        return factory.create(properties);
    }
    
    @Override
    protected RegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new ItemEntry<>(getOwner(), delegate);
    }
    
    @Override
    public ItemEntry<T> register() {
        return (ItemEntry<T>) super.register();
    }

    /*
        The following methods exist as shortcuts into Item Properties
            Stops you needing to have long chains inside `properties()`
            or having multiple `properties()` calls

            ```
            builder.properties(props -> props
                .stacksTo(1)
                .tab(CreativeModeTab.TAB_MISC)
            )
            ```

            becomes

            ```
            builder.stacksTo(1).tab(CreativeModeTab.TAB_MISC)
            ```
     */
    // region: Item Properties Wrappers
    public final ItemBuilder<O, T, P> food(FoodProperties food)
    {
        return properties(properties -> properties.food(food));
    }

    public final ItemBuilder<O, T, P> stacksTo(int maxStackSize)
    {
        return properties(properties -> properties.stacksTo(maxStackSize));
    }

    public final ItemBuilder<O, T, P> defaultDurability(int durability)
    {
        return properties(properties -> properties.defaultDurability(durability));
    }

    public final ItemBuilder<O, T, P> durability(int durability)
    {
        return properties(properties -> properties.durability(durability));
    }

    /**
     * @deprecated Exists purely for legacy & vanilla item reasons, should never be used with custom modded items. Modded should use {@link #craftRemainder(Supplier)}
     */
    @Deprecated
    public final ItemBuilder<O, T, P> craftRemainder(Item item)
    {
        return properties(properties -> properties.craftRemainder(item));
    }

    /**
     * @deprecated Exists purely for legacy & vanilla item reasons, should never be used with custom modded items. Modded should use {@link #craftRemainder(Supplier)}
     */
    @Deprecated
    public final ItemBuilder<O, T, P> craftRemainder(ItemLike item)
    {
        return properties(properties -> properties.craftRemainder(item.asItem()));
    }

    public final ItemBuilder<O, T, P> craftRemainder(Supplier<? extends ItemLike> item)
    {
        return properties(properties -> properties.craftRemainder(item.get().asItem()));
    }

    /**
     * @deprecated Exists purely for legacy & vanilla creative mode tab reasons, should never be used with custom modded creative mode tabs. Modded should use {@link #tab(NonNullSupplier)}
     */
    @Deprecated
    public final ItemBuilder<O, T, P> tab(CreativeModeTab tab)
    {
        return properties(properties -> properties.tab(tab));
    }

    public final ItemBuilder<O, T, P> rarity(Rarity rarity)
    {
        return properties(properties -> properties.rarity(rarity));
    }

    public final ItemBuilder<O, T, P> fireResistant()
    {
        return properties(Item.Properties::fireResistant);
    }

    public final ItemBuilder<O, T, P> setNoRepair()
    {
        return properties(Item.Properties::setNoRepair);
    }
    // endregion
}