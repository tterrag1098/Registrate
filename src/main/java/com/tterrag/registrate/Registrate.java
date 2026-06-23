package com.tterrag.registrate;

import lombok.extern.log4j.Log4j2;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

import java.util.Optional;

@Log4j2
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

        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modid)
                .map(ModContainer::getEventBus);
        
        modEventBus.ifPresentOrElse(ret::registerEventListeners, () -> {
            String message = "# [Registrate] Failed to register eventListeners for mod " + modid + ", This should be reported to this mod's dev #";

            
            StringBuilder hashtags = new StringBuilder().repeat("#", message.length());
            
            log.fatal(hashtags.toString());
            log.fatal(message);
            log.fatal(hashtags.toString());
        });

        return ret;
    }

    protected Registrate(String modid) {
        super(modid);
    }
}
