package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
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
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.function.Supplier;

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

    public ModelFile.ExistingModelFile getExistingFile(Identifier location) {
        return new ModelFile.ExistingModelFile(location);
    }

    public ItemModelBuilder getBuilder(String name) {
        return new ItemModelBuilder(resolve(name), modelOutput);
    }

    public ItemModelBuilder withExistingParent(String name, Identifier parent) {
        ItemModelBuilder builder = getBuilder(name).parent(parent);
        return builder;
    }

    public ItemModelBuilder withExistingParent(String name, String parent) {
        return getBuilder(name).parent(parent);
    }

    public ItemModelBuilder generated(DataGenContext<Item, ? extends Item> ctx) {
        return generated(ctx::get);
    }

    public ItemModelBuilder generated(DataGenContext<Item, ? extends Item> ctx, Identifier texture) {
        return generated(ctx::get, texture);
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends Item> item) {
        return generated(item, modItemTexture(name(item)).sprite());
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends Item> item, Identifier texture) {
        ItemModelBuilder builder = withExistingParent(name(item), mcLoc("item/generated"))
            .texture("layer0", texture);
        itemModelOutput.accept(item.get(), ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public ItemModelBuilder blockItem(Supplier<? extends Block> block, String suffix) {
        Identifier model = ModelLocationUtils.getModelLocation(block.get(), suffix);
        Item item = block.get().asItem();
        ItemModelBuilder builder = withExistingParent(BuiltInRegistries.ITEM.getKey(item).getPath(), model);
        itemModelOutput.accept(item, ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public ItemModelBuilder blockSprite(DataGenContext<Item, ? extends Item> ctx, Identifier texture) {
        return blockSprite(ctx::get, texture);
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends Item> item, Identifier texture) {
        return generated(item, texture);
    }

    public ItemModelBuilder cubeAll(String name, Identifier texture) {
        return withExistingParent(name, mcLoc("block/cube_all")).texture("all", texture);
    }

    public ItemModelBuilder cubeColumn(String name, Identifier side, Identifier end) {
        return withExistingParent(name, mcLoc("block/cube_column"))
            .texture("side", side)
            .texture("end", end);
    }

    public ItemModelBuilder wallInventory(String name, Identifier texture) {
        return withExistingParent(name, mcLoc("block/wall_inventory"))
            .texture("wall", texture);
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

    private Identifier resolve(String name) {
        if (name.contains(":")) {
            return Identifier.parse(name);
        }
        if (name.startsWith(ModelProvider.BLOCK_FOLDER + "/") || name.startsWith(ModelProvider.ITEM_FOLDER + "/")) {
            return modLoc(name);
        }
        return modLoc(ModelProvider.ITEM_FOLDER + "/" + name);
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

    /**
     * Create a bucket model using the NeoForge built in fluid container model. Borrowed with <3 from EnderIO.
     * @param item A supplier to the bucket item, most commonly a {@link com.tterrag.registrate.providers.DataGenContext}
     * @param flipGas If true, the bucket model will be flipped upside down
     * @param applyFluidLuminosity If true, the fluid will "glow"
     */
    public void bucketItem(NonNullSupplier<? extends BucketItem> item, boolean flipGas, boolean applyFluidLuminosity) {
        Material drip = new Material(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "item/mask/bucket_fluid_drip"));
        Material bucket = new Material(Identifier.withDefaultNamespace("item/bucket"));
        DynamicFluidContainerModel.Textures textures = new DynamicFluidContainerModel.Textures(Optional.empty(), Optional.of(bucket), Optional.of(drip), Optional.empty());
        this.itemModelOutput.accept(item.get(), new DynamicFluidContainerModel.Unbaked(textures, item.get().getContent(), flipGas, false, applyFluidLuminosity));
    }
}
