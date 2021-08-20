package com.tterrag.registrate;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Registrate extends AbstractRegistrate<Registrate> {
    
    /**
     * Create a new {@link Registrate} and register event listeners for registration and data generation. Used in lieu of adding side-effects to constructor, so that alternate initialization
     * strategies can be done in subclasses.
     * 
     * @param modid
     *            The mod ID for which objects will be registered
     * @return The {@link Registrate} instance
     */
    public static Registrate create(String modid) {
        return new Registrate(modid).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    /**
     * Create a new {@link Registrate} and register event listeners for registration and data generation. Used in lieu of adding side-effects to constructor, so that alternate initialization
     * strategies can be done in subclasses.
     *
     * @param modid
     *            The mod ID for which objects will be registered
     * @param domain
     *            The domain in which objects will be registered
     * @return The {@link Registrate} instance
     */
    public static Registrate create(String modid, String domain) {
        return new Registrate(modid, domain).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    protected Registrate(String modid) {
        super(modid);
    }

    protected Registrate(String modid, String domain) {
        super(modid, domain);
    }
}
