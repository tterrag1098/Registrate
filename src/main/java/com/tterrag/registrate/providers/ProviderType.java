package com.tterrag.registrate.providers;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;

import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@FunctionalInterface
public interface ProviderType<T extends RegistrateProvider> {

    // CLIENT DATA
    public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = register("item_model", (p, e) -> new RegistrateItemModelProvider(p, e.getGenerator(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = register("blockstate", (p, e) -> new RegistrateBlockstateProvider(p, e.getGenerator(), e.getExistingFileHelper()));
    public static final ProviderType<RegistrateLangProvider> LANG = register("lang", (p, e) -> new RegistrateLangProvider(p, e.getGenerator()));

    // SERVER DATA
    public static final ProviderType<RegistrateRecipeProvider> RECIPE = register("recipe", (p, e) -> new RegistrateRecipeProvider(p, e.getGenerator()));
    public static final ProviderType<RegistrateLootTableProvider> LOOT = register("loot", (p, e) -> new RegistrateLootTableProvider(p, e.getGenerator()));

    T create(Registrate parent, GatherDataEvent event);

    static <T extends RegistrateProvider> ProviderType<T> register(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }
}
