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

public final class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {
    
    private final EntityType.Builder<T> builder;

    public EntityBuilder(Registrate owner, P parent, String name, BuilderCallback callback, EntityType.IFactory<T> factory, EntityClassification classification) {
        super(owner, parent, name, callback, EntityType.class);
        this.builder = EntityType.Builder.create(factory, classification);
        defaultLang();
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
        return getOwner().item(this, getName() + "_spawn_egg", p -> new LazySpawnEggItem<>(get(), primaryColor, secondaryColor, p))
                .properties(p -> p.group(ItemGroup.MISC))
                .model(ctx -> ctx.getProvider().withExistingParent(ctx.getName(), new ResourceLocation("item/template_spawn_egg")));
    }
    
    public EntityBuilder<T, P> defaultLang() {
        return lang(EntityType::getTranslationKey);
    }
    
    public EntityBuilder<T, P> lang(String name) {
        return lang(EntityType::getTranslationKey, name);
    }
    
    public EntityBuilder<T, P> loot(BiConsumer<RegistrateEntityLootTables, EntityType<T>> cons) {
        return addData(ProviderType.LOOT, ctx -> ctx.getProvider()
                .addLootAction(LootType.ENTITY, prov -> cons.accept(prov, ctx.getEntry())));
    }
    
    public EntityBuilder<T, P> tag(Tag<EntityType<?>> tag) {
        return tag(ProviderType.ENTITY_TAGS, tag);
    }
    
    @Override
    protected EntityType<T> createEntry() {
        return builder.build(getName());
    }
}
