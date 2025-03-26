package com.tterrag.registrate.providers.generators;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistrateLegacyBlockModelBuilder {

	private final ExtendedModelTemplateBuilder template;
	private final TextureMapping texture;
	private final BiConsumer<ResourceLocation, ModelInstance> output;

	RegistrateLegacyBlockModelBuilder(BiConsumer<ResourceLocation, ModelInstance> output, ExtendedModelTemplateBuilder template, TextureMapping texture) {
		this.output = output;
		this.template = template;
		this.texture = texture.copy();
	}

	public RegistrateLegacyBlockModelBuilder texture(TextureSlot slot, ResourceLocation texture) {
		this.texture.put(slot, texture);
		return this;
	}

	public RegistrateLegacyBlockModelBuilder transformTemplate(Consumer<ExtendedModelTemplateBuilder> action) {
		action.accept(template);
		return this;
	}

	public RegistrateLegacyBlockModelBuilder transformTexture(Consumer<TextureMapping> action) {
		action.accept(texture);
		return this;
	}

	public ResourceLocation build(Block block) {
		return template.build().create(block, texture, output);
	}

	public ResourceLocation build(ResourceLocation loc) {
		return template.build().create(loc, texture, output);
	}


}
