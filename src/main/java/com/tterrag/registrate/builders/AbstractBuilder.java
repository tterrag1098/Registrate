package com.tterrag.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Base class which most builders should extend, instead of implementing [@link {@link Builder} directly.
 * <p>
 * Provides the most basic functionality, and some utility methods that remove the need to pass the registry class.
 *
 * @param <R>
 *            Type of the registry for the current object. This is the concrete base class that all registry entries must extend, and the type used for the forge registry itself.
 * @param <T>
 *            Actual type of the object being built.
 * @param <P>
 *            Type of the parent object, this is returned from {@link #build()} and {@link #getParent()}.
 * @param <S>
 *            Self type
 * @see Builder
 */
@RequiredArgsConstructor
public abstract class AbstractBuilder<R extends IForgeRegistryEntry<R>, T extends R, P, S extends AbstractBuilder<R, T, P, S>> implements Builder<R, T, P, S> {

    @Getter(onMethod = @__({ @Override }))
    private final AbstractRegistrate<?> owner;
    @Getter(onMethod = @__({ @Override }))
    private final P parent;
    @Getter(onMethod = @__({ @Override }))
    private final String name;
    private final BuilderCallback callback;
    @Getter(onMethod = @__({ @Override }))
    private final Class<? super R> registryType;

    /**
     * Create the built entry. This method will be lazily resolved at registration time, so it is safe to bake in values from the builder.
     * 
     * @return The built entry
     */
    @SuppressWarnings("null")
    protected abstract @NonnullType T createEntry();

    @Override
    public RegistryEntry<T> register() {
        return callback.accept(name, registryType, this::createEntry);
    }
    
    protected BuilderCallback getCallback() {
        return callback;
    }

    /**
     * Set the lang key for this entry to the default value (specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier)}). Generally, specific helpers from concrete builders should be used
     * instead.
     * 
     * @param langKeyProvider
     *            A function to get the translation key from the entry
     * @return this {@link Builder}
     */
    public S lang(NonNullFunction<T, String> langKeyProvider) {
        return lang(langKeyProvider, (p, t) -> p.getAutomaticName(t));
    }

    /**
     * Set the lang key for this entry to the specified name. Generally, specific helpers from concrete builders should be used instead.
     * 
     * @param langKeyProvider
     *            A function to get the translation key from the entry
     * @param name
     *            The name to use
     * @return this {@link Builder}
     */
    public S lang(NonNullFunction<T, String> langKeyProvider, String name) {
        return lang(langKeyProvider, (p, s) -> name);
    }

    private S lang(NonNullFunction<T, String> langKeyProvider, NonNullBiFunction<RegistrateLangProvider, NonNullSupplier<? extends T>, String> localizedNameProvider) {
        return setData(ProviderType.LANG, (ctx, prov) -> prov.add(langKeyProvider.apply(ctx.getEntry()), localizedNameProvider.apply(prov, ctx::getEntry)));
    }
}
