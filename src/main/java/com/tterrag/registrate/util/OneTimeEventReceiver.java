package com.tterrag.registrate.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@RequiredArgsConstructor
@Log4j2
public class OneTimeEventReceiver<T extends Event> implements Consumer<T> {
    
    public static <T extends Event> void addModListener(Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addModListener(EventPriority.NORMAL, evtClass, listener);
    }
    
    public static <T extends Event> void addModListener(EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addListener(FMLJavaModLoadingContext.get().getModEventBus(), priority, evtClass, listener);
    }
    
    public static <T extends Event> void addForgeListener(Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addForgeListener(EventPriority.NORMAL, evtClass, listener);
    }
    
    public static <T extends Event> void addForgeListener(EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addListener(MinecraftForge.EVENT_BUS, priority, evtClass, listener);
    }
    
    public static <T extends Event> void addListener(IEventBus bus, Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addListener(bus, EventPriority.NORMAL, evtClass, listener);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Event> void addListener(IEventBus bus, EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
        bus.addListener(priority, false, (Class<T>) evtClass, new OneTimeEventReceiver<>(bus, listener));
    }

    private static final @Nullable MethodHandle getBusId;
    static {
        MethodHandle ret;
        try {
            ret = MethodHandles.lookup().unreflectGetter(ObfuscationReflectionHelper.findField(EventBus.class, "busID"));
        } catch (IllegalAccessException e) {
            log.warn("Failed to set up EventBus reflection to release one-time event listeners, leaks will occur. This is not a big deal.");
            ret = null;
        }
        getBusId = ret;
    }

    private final IEventBus bus;
    private final Consumer<? super T> listener;

    @Override
    public void accept(T event) {
        listener.accept(event);
        bus.unregister(this);
        try {
            final MethodHandle mh = getBusId;
            if (mh != null) {
                event.getListenerList().getListeners((int) mh.invokeExact((EventBus) bus));
            }
        } catch (Throwable t) {
            log.warn("Failed to clear listener list of one-time event receiver, so the receiver has leaked. This is not a big deal.", t);
        }
    }
}
