package com.tterrag.registrate.builders;

import java.util.function.Consumer;
import java.util.function.Function;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import net.minecraft.item.Item;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

    private final Function<Item.Properties, T> factory;
    private final Item.Properties properties = new Item.Properties();
    
    public ItemBuilder(Registrate owner, P parent, String name, BuilderCallback callback, Function<Item.Properties, T> factory) {
        super(owner, parent, name, callback, Item.class);
        this.factory = factory;
        model(ctx -> ctx.getProvider().generated(ctx::getEntry));
        lang(Item::getTranslationKey);
    }

    public ItemBuilder<T, P> properties(Consumer<Item.Properties> action) {
        action.accept(properties);
        return this;
    }

    public ItemBuilder<T, P> model(Consumer<DataGenContext<RegistrateItemModelProvider, Item, T>> model) {
        return addData(ProviderType.ITEM_MODEL, model);
    }
    
    public ItemBuilder<T, P> recipe(Consumer<DataGenContext<RegistrateRecipeProvider, Item, T>> cons) {
        return addData(ProviderType.RECIPE, cons);
    }
    
    @Override
    protected T createEntry() {
        return factory.apply(properties);
    }
}
