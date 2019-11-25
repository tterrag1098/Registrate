package com.tterrag.registrate;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.TileEntityBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.providers.RegistrateProvider;

import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

@Log4j2
public class Registrate {
    
    @Value
    private class Registration<R extends IForgeRegistryEntry<R>, T extends R> {
        ResourceLocation name;
        Class<? super R> type;
        Supplier<? extends T> creator;        
        RegistryObject<T> delegate;
        
        Registration(ResourceLocation name, Class<? super R> type, Supplier<? extends T> creator) {
            this.name = name;
            this.type = type;
            this.creator =  creator;
            this.delegate = RegistryObject.of(name, RegistryManager.ACTIVE.<R>getRegistry(type));
        }
        
        void register(IForgeRegistry<R> registry) {
            registry.register(creator.get().setRegistryName(name));
        }
    }
    
    private final Table<String, Class<?>, Registration<?, ?>> registrations = HashBasedTable.create();
    private final Table<String, ProviderType<?>, Consumer<? extends RegistrateProvider>> datagens = HashBasedTable.create();

    @Getter
    private final String modid;
    
    private String currentName;
    
    public Registrate(String modid) {
        this.modid = modid;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegister);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onData);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<?> event) {
        Class<?> type = event.getRegistry().getRegistrySuperType();
        if (type == null) {
            log.debug("Skipping unknown builder class " + event.getRegistry().getRegistrySuperType());
            return;
        }
        for (Entry<String, Registration<?, ?>> e : registrations.column(type).entrySet()) {
            e.getValue().register((IForgeRegistry) event.getRegistry());
        }
    }
    
    public void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(new RegistrateDataProvider(this, modid, event));
    }
    
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> get(Class<? super R> type) {
        return this.<R, T>get(currentName, type);
    }
    
    @SuppressWarnings("unchecked")
    public <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> get(String name, Class<? super R> type) {
        Registration<R, T> reg = (Registration<R, T>) registrations.get(name, type);
        if (reg != null) {
            return reg.getDelegate();
        }
        throw new IllegalArgumentException("Unknown registration " + name + " for type " + type);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>> Collection<RegistryObject<T>> getAll(Class<? super T> clazz) {
        return registrations.column(clazz).values().stream().map(r -> (RegistryObject<T>) r.getDelegate()).collect(Collectors.toList());
    }
    
    public <T extends RegistrateProvider> void addDataGenerator(String name, ProviderType<T> type, Consumer<T> cons) {
        datagens.put(name, type, cons);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends RegistrateProvider> void genData(ProviderType<T> type, T gen) {
        datagens.column(type).values().forEach(cons -> ((Consumer<T>)cons).accept(gen));
    }
    
    public Registrate object(String name) {
        this.currentName = name;
        return this;
    }
    
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> S entry(BiFunction<String, BuilderCallback, S> factory) {
        return entry(currentName, callback -> factory.apply(currentName, callback));
    }
    
    public <R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> S entry(String name, Function<BuilderCallback, S> factory) {
        return factory.apply(this::accept);
    }
    
    public <T extends Item> ItemBuilder<T, Registrate> item(Function<Item.Properties, T> factory) {
        return item(this, factory);
    }
    
    public <T extends Item, P> ItemBuilder<T, P> item(P parent, Function<Item.Properties, T> factory) {
        return item(parent, currentName, factory);
    }
    
    public <T extends Item, P> ItemBuilder<T, P> item(P parent, String name, Function<Item.Properties, T> factory) {
        return entry(name, callback -> new ItemBuilder<>(this, parent, name, callback, factory));
    }
    
    public <T extends Block> BlockBuilder<T, Registrate> block(Function<Block.Properties, T> factory) {
        return block(this, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, Function<Block.Properties, T> factory) {
        return block(parent, currentName, factory);
    }
    
    public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, Function<Block.Properties, T> factory) {
        return entry(name, callback -> new BlockBuilder<>(this, parent, name, callback, factory));
    }
    
    public <T extends Entity> EntityBuilder<T, Registrate> entity(EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(this, factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entity(parent, currentName, factory, classification);
    }
    
    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, String name, EntityType.IFactory<T> factory, EntityClassification classification) {
        return entry(name, callback -> new EntityBuilder<>(this, parent, name, callback, factory, classification));
    }
    
    public <T extends TileEntity> TileEntityBuilder<T, Registrate> tileEntity(Supplier<? extends T> factory) {
        return tileEntity(this, factory);
    }
    
    public <T extends TileEntity, P> TileEntityBuilder<T, P> tileEntity(P parent, Supplier<? extends T> factory) {
        return tileEntity(parent, currentName, factory);
    }
    
    public <T extends TileEntity, P> TileEntityBuilder<T, P> tileEntity(P parent, String name, Supplier<? extends T> factory) {
        return entry(name, callback -> new TileEntityBuilder<>(this, parent, name, callback, factory));
    }

    private <R extends IForgeRegistryEntry<R>, T extends R> RegistryObject<T> accept(String name, Class<? super R> type, Supplier<? extends T> creator) {
        Registration<R, T> reg = new Registration<>(new ResourceLocation(modid, name), type, creator);
        registrations.put(name, type, reg);
        return reg.getDelegate();
    }
}
