package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.factory.MenuFactory;
import com.tterrag.registrate.builders.factory.MenuScreenFactory;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuBuilder<O extends AbstractRegistrate<O>, T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>,  P> extends AbstractBuilder<O, MenuType<?>, MenuType<T>, P, MenuBuilder<O, T, S, P>> {
    
    private final MenuFactory.WithBuffer<T> factory;
    private final NonNullSupplier<MenuScreenFactory<T, S>> screenFactory;

    public MenuBuilder(O owner, P parent, String name, BuilderCallback<O> callback, MenuFactory<T> factory, NonNullSupplier<MenuScreenFactory<T, S>> screenFactory) {
        this(owner, parent, name, callback, MenuFactory.withIgnoredBuffer(factory), screenFactory);
    }

    public MenuBuilder(O owner, P parent, String name, BuilderCallback<O> callback, MenuFactory.WithBuffer<T> factory, NonNullSupplier<MenuScreenFactory<T, S>> screenFactory) {
        super(owner, parent, name, callback, ForgeRegistries.Keys.MENU_TYPES);
        this.factory = factory;
        this.screenFactory = screenFactory;
    }

    @Override
    protected @NonnullType MenuType<T> createEntry() {
        MenuFactory.WithBuffer<T> factory = this.factory;
        NonNullSupplier<MenuType<T>> supplier = this.asSupplier();
        MenuType<T> ret = IForgeMenuType.create((windowId, inv, buf) -> factory.create(supplier.get(), windowId, inv, buf));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MenuScreenFactory<T, S> screenFactory = this.screenFactory.get();
            MenuScreens.<T, S>register(ret, (type, inv, displayName) -> screenFactory.create(type, inv, displayName));
        });
        return ret;
    }

    @Override
    protected RegistryEntry<MenuType<T>> createEntryWrapper(RegistryObject<MenuType<T>> delegate) {
        return new MenuEntry<>(getOwner(), delegate);
    }

    @Override
    public MenuEntry<T> register() {
        return (MenuEntry<T>) super.register();
    }
}