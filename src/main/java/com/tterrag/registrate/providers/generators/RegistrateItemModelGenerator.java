package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public class RegistrateItemModelGenerator extends ItemModelGenerators {

    private final AbstractRegistrate<?> parent;

    public RegistrateItemModelGenerator(AbstractRegistrate<?> parent, ItemModelOutput output, BiConsumer<Identifier, ModelInstance> model) {
        super(output, model);
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.ITEM_MODEL, this);
        //TODO check if an item actually has a valid model
    }


    public void createWithExistingModel(Item item, Identifier id) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(id));
    }

    public void generateWithTemplate(Item item, ModelTemplate template, TextureMapping textures) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(template.create(item, textures, modelOutput)));
    }

    public void generateFlatItem(Item item, Material layer0) {
        generateFlatItem(item, ModelTemplates.FLAT_ITEM, layer0);
    }

    public void generateFlatItem(Item item, ModelTemplate template, Material layer0) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(template.create(item, TextureMapping.layer0(layer0), modelOutput)));
    }

    public void generateFlatBlockItem(BlockItem item) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock()));
    }

    public void generateFlatBlockItem(BlockItem item, String suffix) {
        generateFlatItem(item, TextureMapping.getBlockTexture(item.getBlock(), suffix));
    }

    public void generateBlockItem(BlockItem item, UnaryOperator<Identifier> modelMapper) {
        itemModelOutput.accept(item, ItemModelUtils.plainModel(modelMapper.apply(ModelLocationUtils.getModelLocation(item.getBlock()))));
    }

    public void generateBlockItem(BlockItem item, String suffix) {
        generateBlockItem(item, model -> model.withSuffix(suffix));
    }

    public Identifier mcLoc(String id) {
        return Identifier.withDefaultNamespace(id);
    }

    public Identifier modLoc(String id) {
        return Identifier.fromNamespaceAndPath(parent.getModid(), id);
    }

    public Material mcBlockTexture(String path) {
        return new Material(mcLoc("block/" + path));
    }

    public Material modBlockTexture(String path) {
        return new Material(modLoc("block/" + path));
    }

    public Material mcItemTexture(String path) {
        return new Material(mcLoc("item/" + path));
    }

    public Material modItemTexture(String path) {
        return new Material(modLoc("item/" + path));
    }

    public String modid(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
    }

    public String name(NonNullSupplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }

    public void generateTintedModel(Item entry, Identifier model, ItemTintSource tint) {
        this.itemModelOutput.accept(entry, ItemModelUtils.tintedModel(model, tint));
    }
}
