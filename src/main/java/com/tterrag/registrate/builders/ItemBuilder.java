package com.tterrag.registrate.builders;

import com.google.common.collect.Maps;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.*;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A builder for items, allows for customization of the {@link Item.Properties} and configuration of data associated with items (models, recipes, etc.).
 *
 * @param <T>
 *            The type of item being built
 * @param <P>
 *            Parent object type
 */
public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

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
    public static <T extends Item, P> ItemBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.20")
    @Deprecated(forRemoval = true, since = "1.19.3")
    public static <T extends Item, P> ItemBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory, @Nullable NonNullSupplier<? extends CreativeModeTab> tab) {
        return new ItemBuilder<>(owner, parent, name, callback, factory)
                .defaultModel().defaultLang()
                .transform(ib -> tab == null ? ib : ib.tab(tab));
    }

    private final NonNullFunction<Item.Properties, T> factory;

    private NonNullSupplier<Item.Properties> initialProperties = Item.Properties::new;
    private NonNullFunction<Item.Properties, Item.Properties> propertiesCallback = NonNullUnaryOperator.identity();

    @Nullable
    private NonNullSupplier<Supplier<ItemColor>> colorHandler;
    private Map<NonNullSupplier<? extends CreativeModeTab>, Consumer<AbstractRegistrate.CreativeModeTabModifier>> creativeModeTabs = Maps.newHashMap();

    protected ItemBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
        super(owner, parent, name, callback, ForgeRegistries.Keys.ITEMS);
        this.factory = factory;

        // delegate creative mode tab modification callback registration
        // until after item has been registered
        // this allows the instance of `#tab` to be modified as many times as we want
        // during the build process
        // and only register the handler once everything is finalized
        //
        // we use a map to also keep track of the modifier used to add item to the tab
        // this allows to end user to pick and choose how & in what state to add the itemstack to the tab in
        //
        // using a map also allows item to be added to multiple tabs, rather than keep tacking of 1 tab and adding to that singular tab
        // this means, if mod registers custom tab & spawn eggs the eggs will be on the mod tab & spawn eggs tab for example, rather than only on the spawn eggs tab
        // user can modify this though, by using #removeTab() to remove item from any registered tab
        // must pass in the *EXACT* same supplier to the method though, otherwise the map keys wont match and nothing (or something else) may get removed
        onRegister(item -> {
            creativeModeTabs.forEach(owner::modifyCreativeModeTab);
            creativeModeTabs.clear(); // this registration should only fire once, to doubly ensure this, clear the map
        });
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
    public ItemBuilder<T, P> properties(NonNullUnaryOperator<Item.Properties> func) {
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
    public ItemBuilder<T, P> initialProperties(NonNullSupplier<Item.Properties> properties) {
        initialProperties = properties;
        return this;
    }

    public ItemBuilder<T, P> tab(NonNullSupplier<? extends CreativeModeTab> tab, Consumer<AbstractRegistrate.CreativeModeTabModifier> modifier) {
        creativeModeTabs.put(tab, modifier); // Should we get the current value in the map [if one exists] and .andThen() the 2 together? right now we replace any consumer that currently exists
        return this;
    }

    public ItemBuilder<T, P> tab(NonNullSupplier<? extends CreativeModeTab> tab) {
        return tab(tab, modifier -> modifier.accept(get()));
    }

    public ItemBuilder<T, P> removeTab(NonNullSupplier<? extends CreativeModeTab> tab) {
        creativeModeTabs.remove(tab);
        return this;
    }

    /**
     * Register a block color handler for this item. The {@link ItemColor} instance can be shared across many items.
     *
     * @param colorHandler
     *            The color handler to register for this item
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> color(NonNullSupplier<Supplier<ItemColor>> colorHandler) {
        if (this.colorHandler == null) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerItemColor);
        }
        this.colorHandler = colorHandler;
        return this;
    }

    protected void registerItemColor() {
        OneTimeEventReceiver.addModListener(RegisterColorHandlersEvent.Item.class, e -> {
            NonNullSupplier<Supplier<ItemColor>> colorHandler = this.colorHandler;
            if (colorHandler != null) {
                e.register(colorHandler.get().get(), getEntry());
            }
        });
    }

    /**
     * Assign the default model to this item, which is simply a generated model with a single texture of the same name.
     *
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> defaultModel() {
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
    public ItemBuilder<T, P> model(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cons) {
        return setData(ProviderType.ITEM_MODEL, cons);
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier, net.minecraft.resources.ResourceKey)}. This is the default, so it is generally
     * not necessary to call, unless for undoing previous changes.
     *
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> defaultLang() {
        return lang(Item::getDescriptionId);
    }

    /**
     * Set the translation for this item.
     *
     * @param name
     *            A localized English name
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> lang(String name) {
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
    public ItemBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons) {
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
    public final ItemBuilder<T, P> tag(TagKey<Item>... tags) {
        return tag(ProviderType.ITEM_TAGS, tags);
    }

    @Override
    protected T createEntry() {
        Item.Properties properties = this.initialProperties.get();
        properties = propertiesCallback.apply(properties);
        return factory.apply(properties);
    }

    @Override
    protected RegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new ItemEntry<>(getOwner(), delegate);
    }

    @Override
    public ItemEntry<T> register() {
        return (ItemEntry<T>) super.register();
    }
}
