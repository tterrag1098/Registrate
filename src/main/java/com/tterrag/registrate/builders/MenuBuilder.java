package com.tterrag.registrate.builders;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class MenuBuilder<T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>,  P> extends AbstractBuilder<MenuType<?>, MenuType<T>, P, MenuBuilder<T, S, P>> {
    
    public interface MenuFactory<T extends AbstractContainerMenu> {
        
        T create(MenuType<T> type, int windowId, Inventory inv);
    }

    public interface ForgeMenuFactory<T extends AbstractContainerMenu> {

        T create(MenuType<T> type, int windowId, Inventory inv, @Nullable FriendlyByteBuf buffer);
    }
    
    public interface ScreenFactory<M extends AbstractContainerMenu, T extends Screen & MenuAccess<M>> {
        
        T create(M menu, Inventory inv, Component displayName);
    }
    
    private final ForgeMenuFactory<T> factory;
    private final NonNullSupplier<ScreenFactory<T, S>> screenFactory;

    public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        this(owner, parent, name, callback, (type, windowId, inv, $) -> factory.create(type, windowId, inv), screenFactory);
    }

    public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        super(owner, parent, name, callback, MenuType.class);
        this.factory = factory;
        this.screenFactory = screenFactory;
    }

    @Override
    protected @NonnullType MenuType<T> createEntry() {
        ForgeMenuFactory<T> factory = this.factory;
        NonNullSupplier<MenuType<T>> supplier = this.asSupplier();
        MenuType<T> ret = IForgeMenuType.create((windowId, inv, buf) -> factory.create(supplier.get(), windowId, inv, buf));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ScreenFactory<T, S> screenFactory = this.screenFactory.get();
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
