package com.tterrag.registrate.builders;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.tags.Tag;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A Builder creates registry entries. A Builder instance has a constant name which will be used for the resultant object, they cannot be reused for different names. It holds a parent object that will
 * be returned from some final methods.
 * <p>
 * When a builder is completed via {@link #register()} or {@link #build()}, the object will be lazily registered (through the owning {@link AbstractRegistrate} object).
 * 
 * @param <R>
 *            Type of the registry for the current object. This is the concrete base class that all registry entries must extend, and the type used for the forge registry itself.
 * @param <T>
 *            Actual type of the object being built.
 * @param <P>
 *            Type of the parent object, this is returned from {@link #build()} and {@link #getParent()}.
 * @param <S>
 *            Self type
 */
public interface Builder<R extends IForgeRegistryEntry<R>, T extends R, P, S extends Builder<R, T, P, S>> extends NonNullSupplier<T> {

    /**
     * Complete the current entry, and return the {@link RegistryEntry} that will supply the built entry once it is available. The builder can be used afterwards, and changes made will reflect the
     * output, as long as it is before registration takes place (before forge registry events).
     * 
     * @return The {@link RegistryEntry} supplying the built entry.
     */
    RegistryEntry<T> register();

    /**
     * The owning {@link AbstractRegistrate} that created this builder.
     * 
     * @return the owner {@link AbstractRegistrate}
     */
    AbstractRegistrate<?> getOwner();

    /**
     * The parent object.
     * 
     * @return the parent object of this builder
     */
    P getParent();

    /**
     * The name of the entry being created, and combined with the mod ID of the parent {@link AbstractRegistrate}, the registry name.
     * 
     * @return the name of the current entry
     */
    String getName();
    
    Class<? super R> getRegistryType();

    /**
     * Allows retrieval of the built entry. Mostly used internally by builder classes.
     *
     * @return a {@link Supplier} to the created object, which will return null if not registered yet, and throw an exception if no such entry exists.
     * @see AbstractRegistrate#get(Class)
     */
    @Override
    default T get() {
        return getOwner().<R, T>get(getName(), getRegistryType()).get();
    }

    /**
     * Set the data provider callback for this entry for the given provider type, which will be invoked when the provider of the given type executes.
     * <p>
     * If called multiple times for the same type, the existing callback will be <em>overwritten</em>.
     * <p>
     * This is mostly unneeded, and instead helper methods for specific data types should be used when possible.
     * 
     * @param <D>
     *            The type of provider
     * @param type
     *            The {@link ProviderType} for the desired provider
     * @param cons
     *            The callback to execute when the provider is run
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    default <D extends RegistrateProvider> S setData(ProviderType<D> type, NonNullBiConsumer<DataGenContext<R, T>, D> cons) {
        getOwner().setDataGenerator(this, type, prov -> cons.accept(DataGenContext.from(this, getRegistryType()), prov));
        return (S) this;
    }

    /**
     * Add a data provider callback which will be invoked when the provider of the given type executes.
     * <p>
     * Calling this multiple times for the same type will <em>not</em> overwrite an existing callback.
     * 
     * @param <D>
     *            The type of provider
     * @param type
     *            The {@link ProviderType} for the desired provider
     * @param cons
     *            The callback to execute when the provider is run
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    default <D extends RegistrateProvider> S addMiscData(ProviderType<D> type, NonNullConsumer<? extends D> cons) {
        getOwner().addDataGenerator(type, cons);
        return (S) this;
    }

    /**
     * Tag this entry with a tag of the correct type.
     * 
     * @param type
     *            The provider type (which must be a tag provider)
     * @param tag
     *            The tag to use
     * @return this {@link Builder}
     */
    default S tag(ProviderType<RegistrateTagsProvider<R>> type, Tag<R> tag) {
        return setData(type, (ctx, prov) -> prov.getBuilder(tag).add(Objects.<@NonnullType T>requireNonNull(get(), "Object not registered")));
    }

    /**
     * Apply a transformation to this {@link Builder}. Useful to apply helper methods within a fluent chain, e.g.
     * 
     * <pre>
     * {@code
     * public static final RegistryObject<MyBlock> MY_BLOCK = REGISTRATE.object("my_block")
     *         .block(MyBlock::new)
     *         .transform(Utils::defaultBlockProperties)
     *         .register();
     * }
     * </pre>
     * 
     * @param <R2>
     *            Registry type
     * @param <T2>
     *            Entry type
     * @param <P2>
     *            Parent type
     * @param <S2>
     *            Self type
     * @param func
     *            The {@link Function function} to apply
     * @return the {@link Builder} returned by the given function
     */
    @SuppressWarnings("unchecked")
    default <R2 extends IForgeRegistryEntry<R2>, T2 extends R2, P2, S2 extends Builder<R2, T2, P2, S2>> S2 transform(NonNullFunction<S, S2> func) {
        return func.apply((S) this);
    }

    /**
     * Register the entry and return the parent object. The {@link RegistryObject} will be created but not returned. It can be retrieved later with {@link AbstractRegistrate#get(Class)} or
     * {@link AbstractRegistrate#get(String, Class)}.
     * 
     * @return the parent object
     */
    default P build() {
        register(); // Ignore return value
        return getParent();
    }

    default void postRegister(T entry) {}
}
