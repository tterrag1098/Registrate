package com.tterrag.registrate;

import com.google.common.base.Preconditions;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Registrate extends AbstractRegistrate<Registrate> {
    
    /**
     * Create a new {@link Registrate} and register event listeners for registration and data generation. Used in lieu of adding side-effects to constructor, so that alternate initialization
     * strategies can be done in subclasses.
     * <p>
     * <strong>NOTE: This must be called at a point where mod loading context exists.</strong> This means that this method cannot be called from static init of the {@code @Mod} class!
     * 
     * @param modid
     *            The mod ID for which objects will be registered
     * @return The {@link Registrate} instance
     * @throws NullPointerException
     *             If mod loading context is not available
     */
    public static Registrate create(String modid) {
        Preconditions.checkNotNull(FMLJavaModLoadingContext.get(), "Registrate initialized too early!");
        return new Registrate(modid).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    protected Registrate(String modid) {
        super(modid);
    }
}
