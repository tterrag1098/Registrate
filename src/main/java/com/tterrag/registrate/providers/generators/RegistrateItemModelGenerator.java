package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;

public class RegistrateItemModelGenerator extends ItemModelGenerators {

    private final AbstractRegistrate<?> parent;

    public RegistrateItemModelGenerator(AbstractRegistrate<?> parent, ItemModelOutput output, BiConsumer<ResourceLocation, ModelInstance> model) {
        super(output, model);
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.ITEM_MODEL, this);
        //TODO check if an item actually has a valid model
    }


    public void createWithExistingModel(Item item, ResourceLocation id) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(id));
    }

    public ResourceLocation mcLoc(String id) {
        return ResourceLocation.withDefaultNamespace(id);
    }

    public ResourceLocation modLoc(String id) {
        return ResourceLocation.fromNamespaceAndPath(parent.getModid(), id);
    }

    public String modid(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
    }

    public String name(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }

    public void generateTintedModel(@NonnullType Item entry, ResourceLocation model, ItemTintSource tint) {
        this.itemModelOutput.accept(entry, ItemModelUtils.tintedModel(model, tint));
    }
}
