package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ContainerBuilder<T extends Container, S extends Screen & IHasContainer<T>,  P> extends AbstractBuilder<ContainerType<?>, ContainerType<T>, P, ContainerBuilder<T, S, P>> {
    
    public interface ContainerFactory<T extends Container> {
        
        T create(ContainerType<T> type, int windowId, PlayerInventory inv);
    }
    
    public interface ScreenFactory<C extends Container, T extends Screen & IHasContainer<C>> {
        
        T create(C container, PlayerInventory inv, ITextComponent displayName);
    }
    
    private final ContainerFactory<T> factory;
    private final NonNullSupplier<ScreenFactory<T, S>> screenFactory;

    public ContainerBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ContainerFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        super(owner, parent, name, callback, ContainerType.class);
        this.factory = factory;
        this.screenFactory = screenFactory;
    }

    @Override
    protected @NonnullType ContainerType<T> createEntry() {
        ContainerFactory<T> factory = this.factory;
        NonNullSupplier<ContainerType<T>> supplier = this.asSupplier();
        ContainerType<T> ret = new ContainerType<>((windowId, inv) -> factory.create(supplier.get(), windowId, inv));
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ScreenFactory<T, S> screenFactory = this.screenFactory.get();
            ScreenManager.<T, S>registerFactory(ret, (type, inv, displayName) -> screenFactory.create(type, inv, displayName));
        });
        return ret;
    }
}
