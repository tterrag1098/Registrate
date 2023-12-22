package com.tterrag.registrate.builders;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

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
        return new ItemBuilder<>(owner, parent, name, callback, factory)
                .defaultModel().defaultLang();
    }

    private final NonNullFunction<Item.Properties, T> factory;

    private NonNullSupplier<Item.Properties> initialProperties = Item.Properties::new;
    private NonNullFunction<Item.Properties, Item.Properties> propertiesCallback = NonNullUnaryOperator.identity();

    @Nullable
    private NonNullSupplier<Supplier<ItemColor>> colorHandler;
    private Map<ResourceKey<CreativeModeTab>, NonNullBiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier>> creativeModeTabs = Maps.newLinkedHashMap();

    protected ItemBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
        super(owner, parent, name, callback, Registries.ITEM);
        this.factory = factory;

        onRegister(item -> {
            creativeModeTabs.forEach((creativeModeTab, consumer) -> owner.modifyCreativeModeTab(creativeModeTab, modifier -> consumer.accept(DataGenContext.from(this), modifier)));
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

    /**
     * Sets a tab modifier for the given tab which can be used to define custom logic for how the item stack is created and/or added to the tab.
     *
     * <p>
     * CreativeModeTab registration is delegated off until the item has been finalized and registered to the {@link net.minecraft.core.registries.BuiltInRegistries#ITEM Item registry}.<br>
     * This means you can call this method as many times as you like during the build process with no added side effects.
     * <p>
     * Calling this method with different {@link ResourceKey tab keys} will add the modifier to all the specified tabs.
     * <p>
     * Calling this method multiple times with the same {@link ResourceKey tab key} will replace any existing modifier for that tab.
     *
     * @param tab A {@link ResourceKey} representing the {@link CreativeModeTab} to use the modifier for
     * @param modifier A {@link Consumer consumer} accepting a {@link CreativeModeTabModifier} used to update the tab
     * @return This builder
     * @deprecated Use {@link #tab(ResourceKey, NonNullBiConsumer)} which provides access to the registered item.
     */
    @Deprecated
    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab, Consumer<CreativeModeTabModifier> modifier) {
        return tab(tab, ($, m) -> modifier.accept(m));
    }

    /**
     * Sets a tab modifier for the given tab which can be used to define custom logic for how the item stack is created and/or added to the tab.
     *
     * <p>
     * CreativeModeTab registration is delegated off until the item has been finalized and registered to the {@link net.minecraft.core.registries.BuiltInRegistries#ITEM Item registry}.<br>
     * This means you can call this method as many times as you like during the build process with no added side effects.
     * <p>
     * Calling this method with different {@link ResourceKey tab keys} will add the modifier to all the specified tabs.
     * <p>
     * Calling this method multiple times with the same {@link ResourceKey tab key} will replace any existing modifier for that tab.
     *
     * @param tab A {@link ResourceKey} representing the {@link CreativeModeTab} to use the modifier for
     * @param modifier A {@link NonNullBiConsumer consumer} accepting a context object and {@link CreativeModeTabModifier} used to update the tab
     * @return This builder
     */
    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab, NonNullBiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier> modifier) {
        creativeModeTabs.put(tab, modifier); // Should we get the current value in the map [if one exists] and .andThen() the 2 together? right now we replace any consumer that currently exists
        return this;
    }

    /**
     * Adds the item built from this builder into the given CreativeModeTab using the default ItemStack instance.
     * <p>
     * CreativeModeTab registration is delegated off until the item has been finalized and registered to the {@link net.minecraft.core.registries.BuiltInRegistries#ITEM Item registry}.<br>
     * This means you can call this method as many times as you like during the build process with no added side effects.
     * <p>
     * Calling this method with different {@link ResourceKey tab keys} will add the item to all the specified tabs.
     * <p>
     * Calling this method multiple times with the same {@link NonNullSupplier tab supplier} will have no effect.
     *
     * @param tab A {@link ResourceKey} representing the {@link CreativeModeTab} to add to
     * @return This builder
     * @see #tab(ResourceKey, NonNullBiConsumer)
     */
    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab) {
        return tab(tab, (item, modifier) -> modifier.accept(item));
    }

    /**
     * Removes the modifier from this builder from the given {@link CreativeModeTab}.
     *
     * @param tab A {@link ResourceKey} representing the {@link CreativeModeTab} to remove the modifier from
     * @return This builder
     */
    public ItemBuilder<T, P> removeTab(ResourceKey<CreativeModeTab> tab) {
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
            if(FMLEnvironment.dist == Dist.CLIENT){
                registerItemColor();
            }
        }
        this.colorHandler = colorHandler;
        return this;
    }

    protected void registerItemColor() {
        OneTimeEventReceiver.addModListener(getOwner(), RegisterColorHandlersEvent.Item.class, e -> {
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
    protected RegistryEntry<T> createEntryWrapper(DeferredHolder<? super T,T> delegate) {
        return new ItemEntry<>(getOwner(), delegate);
    }

    @Override
    public ItemEntry<T> register() {
        return (ItemEntry<T>) super.register();
    }
}
