package com.tterrag.registrate.builders;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.LazyRegistryEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

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
public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>> implements Builder<R, T, P, S> {

    @Getter(onMethod_ = {@Override})
    private final AbstractRegistrate<?> owner;
    @Getter(onMethod_ = {@Override})
    private final P parent;
    @Getter(onMethod_ = {@Override})
    private final String name;
    @Getter(AccessLevel.PROTECTED)
    private final BuilderCallback callback;
    @Getter(onMethod_ = {@Override})
    private final ResourceKey<Registry<R>> registryKey;

    private final Multimap<ProviderType<? extends RegistrateTagsProvider<?>>, TagKey<?>> tagsByType = HashMultimap.create();

    /** A supplier for the entry that will discard the reference to this builder after it is resolved */
    private final LazyRegistryEntry<T> safeSupplier = new LazyRegistryEntry<>(this);

    /**
     * Create the built entry. This method will be lazily resolved at registration time, so it is safe to bake in values from the builder.
     *
     * @return The built entry
     */
    @SuppressWarnings("null")
    protected abstract @NonnullType T createEntry();

    @Override
    public RegistryEntry<T> register() {
        return callback.accept(name, registryKey, this, this::createEntry, this::createEntryWrapper);
    }

    protected RegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new RegistryEntry<>(getOwner(), delegate);
    }

    @Override
    public NonNullSupplier<T> asSupplier() {
        return safeSupplier;
    }

    /**
     * Tag this entry with a tag (or tags) of the correct type. Multiple calls will add additional tags.
     *
     * @param type
     *            The provider type (which must be a tag provider)
     * @param tags
     *            The tags to add
     * @return this {@link Builder}
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final S tag(ProviderType<? extends RegistrateTagsProvider<R>> type, TagKey<R>... tags) {
        if (!tagsByType.containsKey(type)) {
            setData(type, (ctx, prov) -> tagsByType.get(type).stream()
                    .map(t -> (TagKey<R>) t)
                    .map(prov::tag)
                    .forEach(b -> b.add(TagEntry.element(new ResourceLocation(getOwner().getModid(), getName())))));
        }
        tagsByType.putAll(type, Arrays.asList(tags));
        return (S) this;
    }

    /**
     * Remove a tag (or tags) from this entry of a given type. Useful to remove default tags on fluids, for example. Multiple calls will remove additional tags.
     *
     * @param type
     *            The provider type (which must be a tag provider)
     * @param tags
     *            The tags to remove
     * @return this {@link Builder}
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final S removeTag(ProviderType<RegistrateTagsProvider<R>> type, TagKey<R>... tags) {
        if (tagsByType.containsKey(type)) {
            for (TagKey<R> tag : tags) {
                tagsByType.remove(type, tag);
            }
        }
        return (S) this;
    }

    /**
     * Set the lang key for this entry to the default value (specified by {@link RegistrateLangProvider#getAutomaticName(NonNullSupplier, ResourceKey)}). Generally, specific helpers from concrete
     * builders should be used instead.
     *
     * @param langKeyProvider
     *            A function to get the translation key from the entry
     * @return this {@link Builder}
     */
    public S lang(NonNullFunction<T, String> langKeyProvider) {
        return lang(langKeyProvider, (p, t) -> p.<R>getAutomaticName(t, getRegistryKey()));
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
