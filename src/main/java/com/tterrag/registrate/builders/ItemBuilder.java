package com.tterrag.registrate.builders;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import net.minecraft.item.Item;
import net.minecraft.tags.Tag;

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
     * The block will be assigned the following data:
     * <ul>
     * <li>A simple generated model with one texture (via {@link #defaultModel()})</li>
     * <li>The default translation (via {@link #defaultLang()}</li>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link Registrate} object
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
    public static <T extends Item, P> ItemBuilder<T, P> create(Registrate owner, P parent, String name, BuilderCallback callback, Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(owner, parent, name, callback, factory)
                .defaultModel().defaultLang();
    }

    private final Function<Item.Properties, T> factory;
    private final Item.Properties properties = new Item.Properties();
    
    protected ItemBuilder(Registrate owner, P parent, String name, BuilderCallback callback, Function<Item.Properties, T> factory) {
        super(owner, parent, name, callback, Item.class);
        this.factory = factory;
        defaultModel();
        defaultLang();
    }

    /**
     * Modify the properties of the item. Modifications are <em>not</em> done lazily, instead changing the properties object immediately, and as such this method can be called multiple times to
     * perform different operations.
     * 
     * @param cons
     *            The action to perform on the properties
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> properties(Consumer<Item.Properties> cons) {
        cons.accept(properties);
        return this;
    }
    
    /**
     * Assign the default model to this item, which is simply a generated model with a single texture of the same name.
     * 
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> defaultModel() {
        return model(ctx -> ctx.getProvider().generated(ctx::getEntry));
    }

    /**
     * Configure the model for this item.
     * 
     * @param cons
     *            The callback which will be invoked during data creation
     * @return this {@link ItemBuilder}
     * @see #addData(ProviderType, Consumer)
     */
    public ItemBuilder<T, P> model(Consumer<DataGenContext<RegistrateItemModelProvider, Item, T>> cons) {
        return addData(ProviderType.ITEM_MODEL, cons);
    }
    
    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(Supplier)}. This is the default, so it is generally not necessary to call, unless for undoing
     * previous changes.
     * 
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> defaultLang() {
        return lang(Item::getTranslationKey);
    }
    
    /**
     * Set the translation for this item.
     * 
     * @param name
     *            A localized English name
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> lang(String name) {
        return lang(Item::getTranslationKey, name);
    }

    /**
     * Configure the recipe(s) for this item.
     * 
     * @param cons
     *            The callback which will be invoked during data generation.
     * @return this {@link ItemBuilder}
     * @see #addData(ProviderType, Consumer)
     */
    public ItemBuilder<T, P> recipe(Consumer<DataGenContext<RegistrateRecipeProvider, Item, T>> cons) {
        return addData(ProviderType.RECIPE, cons);
    }
    
    /**
     * Assign a {@link Tag} to this item.
     * 
     * @param tag
     *            The tag to assign
     * @return this {@link ItemBuilder}
     */
    public ItemBuilder<T, P> tag(Tag<Item> tag) {
        return tag(ProviderType.ITEM_TAGS, tag);
    }
    
    @Override
    protected T createEntry() {
        return factory.apply(properties);
    }
}
