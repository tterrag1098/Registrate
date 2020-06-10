package com.tterrag.registrate.builders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import io.netty.buffer.Unpooled;
import lombok.Value;
import lombok.experimental.Accessors;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.world.RegisterDimensionsEvent;

/**
 * A builder for dimensions, allows for customization of the {@link Dimension dimension factory}, dimension custom data, and other properties of dimensions.
 * 
 * @param <T>
 *            The type of dimension being built
 * @param <P>
 *            Parent object type
 */
public class DimensionBuilder<T extends ModDimension, P> extends AbstractBuilder<ModDimension, T, P, DimensionBuilder<T, P>> {

    /**
     * Create a new {@link DimensionBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The dimension will be assigned the following data:
     * <ul>
     * </ul>
     * The factory for the {@link ModDimension} will default to {@link ModDimension#withFactory(BiFunction)}.
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param dimFactory
     *            Factory to create {@link Dimension} objects from a {@link World} and {@link DimensionType}
     * @return A new {@link DimensionBuilder} with reasonable default data generators.
     */
    public static <P> DimensionBuilder<ModDimension, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
            BiFunction<World, DimensionType, ? extends Dimension> dimFactory) {
        return create(owner, parent, name, callback, dimFactory, ModDimension::withFactory);
    }

    /**
     * Create a new {@link DimensionBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The dimension will be assigned the following data:
     * <ul>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link AbstractRegistrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param dimFactory
     *            Factory to create {@link Dimension} objects from a {@link World} and {@link DimensionType}
     * @param factory
     *            Factory to create the {@link ModDimension}
     * @return A new {@link DimensionBuilder} with reasonable default data generators.
     */
    public static <T extends ModDimension, P> DimensionBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
            BiFunction<World, DimensionType, ? extends Dimension> dimFactory, NonNullFunction<BiFunction<World, DimensionType, ? extends Dimension>, T> factory) {
        return new DimensionBuilder<>(owner, parent, name, callback, dimFactory, factory);
    }

    @Value
    private static class DimensionInfo {

        ModDimension dim;
        @Nullable
        NonNullConsumer<PacketBuffer> customData;
        @Accessors(fluent = true)
        boolean hasSkyLight, keepLoaded;
    }

    private static final Map<DimensionInfo, NonNullConsumer<? super DimensionType>> ALL_DIMENSIONS = new ConcurrentHashMap<>();
    private static final AtomicBoolean REGISTERED_EVENT_LISTENER = new AtomicBoolean();

    private final BiFunction<World, DimensionType, ? extends Dimension> dimFactory;
    private final NonNullFunction<BiFunction<World, DimensionType, ? extends Dimension>, T> factory;

    private NonNullConsumer<DimensionType> dimTypeCallback = NonNullConsumer.noop();

    @Nullable
    private NonNullConsumer<PacketBuffer> customData;
    private boolean hasSkyLight;
    private boolean keepLoaded;

    protected DimensionBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BiFunction<World, DimensionType, ? extends Dimension> dimFactory,
            NonNullFunction<BiFunction<World, DimensionType, ? extends Dimension>, T> factory) {
        super(owner, parent, name, callback, ModDimension.class);
        this.factory = factory;
        this.dimFactory = dimFactory;
    }

    /**
     * Set a callback to be invoked whenever the {@link DimensionType} for this dimension is registered.
     * <p>
     * Can be called whenever worlds/servers are joined or left (i.e. whenever {@link RegisterDimensionsEvent} is fired).
     * 
     * @param callback
     *            The callback to invoke
     * @return this {@link DimensionBuilder}
     */
    public DimensionBuilder<T, P> dimensionTypeCallback(NonNullConsumer<DimensionType> callback) {
        this.dimTypeCallback = callback;
        return this;
    }

    /**
     * Add custom data to the {@link PacketBuffer} held by this dimension. This data will be saved and read from disk, and sent to clients.
     * 
     * @param cons
     *            A consumer to add data to the buffer
     * @return this {@link DimensionBuilder}
     * @see DimensionType#getData()
     */
    public DimensionBuilder<T, P> customData(NonNullConsumer<PacketBuffer> cons) {
        NonNullConsumer<PacketBuffer> customData = this.customData;
        this.customData = customData == null ? cons : customData.andThen(cons);
        return this;
    }

    /**
     * Set whether this dimension has sky light.
     * 
     * @param val
     *            The value to set
     * @return this {@link DimensionBuilder}
     * @see DimensionType#hasSkyLight()
     */
    public DimensionBuilder<T, P> hasSkyLight(boolean val) {
        this.hasSkyLight = val;
        return this;
    }

    /**
     * Set whether this dimension should remain loaded when no players are in it.
     * 
     * @param val
     *            The value to set
     * @return this {@link DimensionBuilder}
     * @see DimensionManager#keepLoaded(DimensionType)
     */
    public DimensionBuilder<T, P> keepLoaded(boolean val) {
        this.keepLoaded = val;
        return this;
    }

    @Override
    protected T createEntry() {
        return factory.apply(dimFactory);
    }

    @Override
    public RegistryEntry<T> register() {
        if (REGISTERED_EVENT_LISTENER.compareAndSet(false, true)) {
            MinecraftForge.EVENT_BUS.<RegisterDimensionsEvent> addListener(DimensionBuilder::registerDimensionTypes);
        }
        this.onRegister(d -> ALL_DIMENSIONS.put(new DimensionInfo(d, customData, hasSkyLight, keepLoaded), dimTypeCallback));
        return super.register();
    }

    protected static void registerDimensionTypes(RegisterDimensionsEvent event) {
        ALL_DIMENSIONS.forEach((d, c) -> {
            NonNullConsumer<PacketBuffer> extraData = d.getCustomData();
            PacketBuffer buf = null;
            if (extraData != null) {
                buf = new PacketBuffer(Unpooled.buffer());
                extraData.accept(buf);
            }
            DimensionType type = DimensionManager.registerOrGetDimension(d.getDim().getRegistryName(), d.getDim(), buf, d.hasSkyLight());
            DimensionManager.keepLoaded(type, d.keepLoaded());
            c.accept(type);
        });
    }
}
