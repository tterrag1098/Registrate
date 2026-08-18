package com.tterrag.registrate.providers;

import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;

/**
 * @deprecated Retained for binary compatibility with Registrate 1.6.0.
 */
@Deprecated(forRemoval = false)
public interface RegistrateProviderDelegate<R, T extends R> extends DataProvider {
    String getName();

    Identifier getId();

    T getEntry();
}
