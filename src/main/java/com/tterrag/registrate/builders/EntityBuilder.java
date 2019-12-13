package com.tterrag.registrate.builders;

import java.util.function.Supplier;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.loot.RegistrateEntityLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider.LootType;
import com.tterrag.registrate.util.LazySpawnEggItem;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

/**
 * A builder for entities, allows for customization of the {@link EntityType.Builder}, easy creation of spawn egg items, and configuration of data associated with entities (loot tables, etc.).
 * 
 * @param <T>
 *            The type of entity being built
 * @param <P>
 *            Parent object type
 */
public class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {

    /**
     * Create a new {@link BlockBuilder} and configure data. Used in lieu of adding side-effects to constructor, so that alternate initialization strategies can be done in subclasses.
     * <p>
     * The block will be assigned the following data:
     * <ul>
     * <li>The default translation (via {@link #defaultLang()}</li>
     * </ul>
     * 
     * @param <T>
     *            The type of the builder
     * @param <P>
     *            Parent object type
     * @param owner
     *            The owning {@link Registrate} object
     * @param parent
     *            The parent object
     * @param name
     *            Name of the entry being built
     * @param callback
     *            A callback used to actually register the built entry
     * @param factory
     *            Factory to create the entity
     * @param classification
     *            The {@link EntityClassification} of the entity
     * @return A new {@link EntityBuilder} with reasonable default data generators.
     */
    public static <T extends Entity, P> EntityBuilder<T, P> create(Registrate owner, P parent, String name, BuilderCallback callback, EntityType.IFactory<T> factory,
            EntityClassification classification) {
        return new EntityBuilder<>(owner, parent, name, callback, factory, classification)
                .defaultLang();
    }

    private final NonNullSupplier<EntityType.Builder<T>> builder;
    
    private NonNullConsumer<EntityType.Builder<T>> builderCallback = $ -> {};

    protected EntityBuilder(Registrate owner, P parent, String name, BuilderCallback callback, EntityType.IFactory<T> factory, EntityClassification classification) {
        super(owner, parent, name, callback, EntityType.class);
        this.builder = () -> EntityType.Builder.create(factory, classification);
    }

    /**
     * Modify the properties of the entity. Modifications are done lazily, but the passed function is composed with the current one, and as such this method can be called multiple times to perform
     * different operations.
     *
     * @param cons
     *            The action to perform on the properties
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> properties(NonNullConsumer<EntityType.Builder<T>> cons) {
        builderCallback = builderCallback.andThen(cons);
        return this;
    }

    /**
     * @deprecated This does not work properly, see <a href="https://github.com/MinecraftForge/MinecraftForge/pull/6299">this issue</a>.
     *             <p>
     *             As a temporary measure, uses a custom egg class that imperfectly emulates the functionality
     */
    @Deprecated
    public EntityBuilder<T, P> defaultSpawnEgg(int primaryColor, int secondaryColor) {
        return spawnEgg(primaryColor, secondaryColor).build();
    }

    /**
     * @deprecated This does not work properly, see <a href="https://github.com/MinecraftForge/MinecraftForge/pull/6299">this issue</a>.
     *             <p>
     *             As a temporary measure, uses a custom egg class that imperfectly emulates the functionality
     */
    @Deprecated
    public ItemBuilder<? extends SpawnEggItem, EntityBuilder<T, P>> spawnEgg(int primaryColor, int secondaryColor) {
        return getOwner().item(this, getName() + "_spawn_egg", p -> new LazySpawnEggItem<>(get(), primaryColor, secondaryColor, p)).properties(p -> p.group(ItemGroup.MISC))
                .model(ctx -> ctx.getProvider().withExistingParent(ctx.getName(), new ResourceLocation("item/template_spawn_egg")));
    }

    /**
     * Assign the default translation, as specified by {@link RegistrateLangProvider#getAutomaticName(Supplier)}. This is the default, so it is generally not necessary to call, unless for undoing
     * previous changes.
     * 
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> defaultLang() {
        return lang(EntityType::getTranslationKey);
    }

    /**
     * Set the translation for this entity.
     * 
     * @param name
     *            A localized English name
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> lang(String name) {
        return lang(EntityType::getTranslationKey, name);
    }

    /**
     * Configure the loot table for this entity. This is different than most data gen callbacks as the callback does not accept a {@link DataGenContext}, but instead a
     * {@link RegistrateEntityLootTables}, for creating specifically entity loot tables.
     * 
     * @param cons
     *            The callback which will be invoked during entity loot table creation.
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> loot(NonNullBiConsumer<RegistrateEntityLootTables, EntityType<T>> cons) {
        return setData(ProviderType.LOOT, ctx -> ctx.getProvider().addLootAction(LootType.ENTITY, prov -> cons.accept(prov, ctx.getEntry())));
    }

    /**
     * Assign a {@link Tag} to this entity.
     * 
     * @param tag
     *            The tag to assign
     * @return this {@link EntityBuilder}
     */
    public EntityBuilder<T, P> tag(Tag<EntityType<?>> tag) {
        return tag(ProviderType.ENTITY_TAGS, tag);
    }

    @Override
    protected EntityType<T> createEntry() {
        EntityType.Builder<T> builder = this.builder.get();
        builderCallback.accept(builder);
        return builder.build(getName());
    }
}
