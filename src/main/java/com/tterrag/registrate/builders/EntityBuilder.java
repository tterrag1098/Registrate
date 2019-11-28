package com.tterrag.registrate.builders;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.loot.RegistrateEntityLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider.LootType;
import com.tterrag.registrate.util.LazySpawnEggItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

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

    private final EntityType.Builder<T> builder;

    protected EntityBuilder(Registrate owner, P parent, String name, BuilderCallback callback, EntityType.IFactory<T> factory, EntityClassification classification) {
        super(owner, parent, name, callback, EntityType.class);
        this.builder = EntityType.Builder.create(factory, classification);
    }

    public EntityBuilder<T, P> properties(Consumer<EntityType.Builder<T>> cons) {
        cons.accept(builder);
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

    public EntityBuilder<T, P> defaultLang() {
        return lang(EntityType::getTranslationKey);
    }

    public EntityBuilder<T, P> lang(String name) {
        return lang(EntityType::getTranslationKey, name);
    }

    public EntityBuilder<T, P> loot(BiConsumer<RegistrateEntityLootTables, EntityType<T>> cons) {
        return addData(ProviderType.LOOT, ctx -> ctx.getProvider().addLootAction(LootType.ENTITY, prov -> cons.accept(prov, ctx.getEntry())));
    }

    public EntityBuilder<T, P> tag(Tag<EntityType<?>> tag) {
        return tag(ProviderType.ENTITY_TAGS, tag);
    }

    @Override
    protected EntityType<T> createEntry() {
        return builder.build(getName());
    }
}
