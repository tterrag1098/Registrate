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
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

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

    public void generateWithTemplate(Item item, ModelTemplate template, TextureMapping textures) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(template.create(item, textures, modelOutput)));
    }

    public void generateFlatItem(Item item, ResourceLocation layer0) {
        generateFlatItem(item, ModelTemplates.FLAT_ITEM, layer0);
    }

    public void generateFlatItem(Item item, ModelTemplate template, ResourceLocation layer0) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(template.create(item, TextureMapping.layer0(layer0), modelOutput)));
    }

    public void generateFlatBlockItem(BlockItem item) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock()));
    }

    public void generateFlatBlockItem(BlockItem item, String suffix) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock(), suffix));
    }

    public void generateBlockItem(BlockItem item, UnaryOperator<ResourceLocation> modelMapper) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(modelMapper.apply(ModelLocationUtils.getModelLocation(item.getBlock()))));
    }

    public void generateBlockItem(BlockItem item, String suffix) {
        generateBlockItem(item, model -> model.withSuffix(suffix));
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
