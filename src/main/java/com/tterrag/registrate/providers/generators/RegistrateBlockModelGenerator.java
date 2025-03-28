package com.tterrag.registrate.providers.generators;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

public class RegistrateBlockModelGenerator extends BlockModelGenerators {

    private final AbstractRegistrate<?> parent;
    public final Map<Block, BlockModelDefinition> seenBlockstates = new HashMap<>();

    public RegistrateBlockModelGenerator(AbstractRegistrate<?> parent, Consumer<BlockModelDefinitionGenerator> known, ItemModelOutput item, BiConsumer<ResourceLocation, ModelInstance> model) {
        super(known, item, model);
        ObfuscationReflectionHelper.<BlockModelGenerators, Consumer<BlockModelDefinitionGenerator>>setPrivateValue(BlockModelGenerators.class, this, g -> {
            this.seenBlockstates.put(g.block(), g.create());
            known.accept(g);
        }, "blockStateOutput");
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }


    public void create(Block block, ResourceLocation model) {
        this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(model)));
    }

    public void create(Block block, TexturedModel.Provider texture) {
        this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(texture.create(block, this.modelOutput))));
    }

    public ResourceLocation mcLoc(String id) {
        return ResourceLocation.withDefaultNamespace(id);
    }

    public ResourceLocation modLoc(String id) {
        return ResourceLocation.fromNamespaceAndPath(parent.getModid(), id);
    }

    public RegistrateLegacyBlockModelBuilder withBuilder(ExtendedModelTemplateBuilder template, TextureMapping texture) {
        return new RegistrateLegacyBlockModelBuilder(modelOutput, template, texture);
    }

    public RegistrateLegacyBlockModelBuilder withBuilder(ExtendedModelTemplateBuilder template) {
        return withBuilder(template, new TextureMapping());
    }

    public RegistrateLegacyBlockModelBuilder getBuilder() {
        return withBuilder(new ExtendedModelTemplateBuilder());
    }

    public RegistrateLegacyBlockModelBuilder withParent(ModelTemplate template) {
        return withBuilder(ExtendedModelTemplateBuilder.of(template));
    }

    public RegistrateLegacyBlockModelBuilder withParent(ModelTemplate template, TextureMapping texture) {
        return withBuilder(ExtendedModelTemplateBuilder.of(template), texture);
    }

    public RegistrateLegacyBlockModelBuilder withParent(TexturedModel model) {
        return withBuilder(ExtendedModelTemplateBuilder.of(model.getTemplate()), model.getMapping());
    }

}
