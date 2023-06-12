package com.tterrag.registrate;

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
        var ret = new Registrate(modid);
        ret.registerEventListeners(ret.getModEventBus());
        return ret;
    }

    protected Registrate(String modid) {
        super(modid);
    }
}
