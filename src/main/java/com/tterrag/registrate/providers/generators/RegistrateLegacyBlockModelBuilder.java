package com.tterrag.registrate.providers.generators;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.RootTransformsBuilder;
import net.neoforged.neoforge.client.model.generators.template.TransformVecBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegistrateLegacyBlockModelBuilder {

	private final ExtendedModelTemplateBuilder template;
	private final TextureMapping texture;
	private final BiConsumer<Identifier, ModelInstance> output;

	RegistrateLegacyBlockModelBuilder(BiConsumer<Identifier, ModelInstance> output, ExtendedModelTemplateBuilder template, TextureMapping texture) {
		this.output = output;
		this.template = template;
		this.texture = texture.copy();
	}

	public RegistrateLegacyBlockModelBuilder texture(TextureSlot slot, Material texture) {
		this.template.requiredTextureSlot(slot);
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

	public Identifier build(Block block) {
		return template.build().create(block, texture, output);
	}

	public Identifier build(Identifier loc) {
		return template.build().create(loc, texture, output);
	}

	// Delegated methods from Template Builder

	/**
	 * Parent model which this template will inherit its properties from.
	 */
	public RegistrateLegacyBlockModelBuilder parent(Identifier parent) {
		template.parent(parent);
		return this;
	}

	/**
	 * Suffix appended onto the models file path.
	 */
	public RegistrateLegacyBlockModelBuilder suffix(String suffix) {
		template.suffix(suffix);
		return this;
	}

	/**
	 * Begin building a new transform for the given perspective.
	 *
	 * @param type the perspective to create or return the builder for
	 * @return the builder for the given perspective
	 * @throws NullPointerException if {@code type} is {@code null}
	 */
	public RegistrateLegacyBlockModelBuilder transform(ItemDisplayContext type, Consumer<TransformVecBuilder> action) {
		template.transform(type, action);
		return this;
	}

	/**
	 * Sets whether or not this model should apply ambient occlusion.
	 */
	public RegistrateLegacyBlockModelBuilder ambientOcclusion(boolean ambientOcclusion) {
		template.ambientOcclusion(ambientOcclusion);
		return this;
	}

	/**
	 * Sets the gui light style for this model.
	 *
	 * <ul>
	 * <li>{@link UnbakedModel.GuiLight#FRONT} for head on light, commonly used for items.</li>
	 * <li>{@link UnbakedModel.GuiLight#SIDE} for the model to be side lit, commonly used for blocks.</li>
	 * </ul>
	 */
	public RegistrateLegacyBlockModelBuilder guiLight(UnbakedModel.GuiLight light) {
		template.guiLight(light);
		return this;
	}

	/**
	 * Use a custom loader instead of the vanilla elements.
	 *
	 * @param customLoaderFactory function that returns the custom loader to set, given this
	 * @return the custom loader builder
	 */
	public <L extends CustomLoaderBuilder> RegistrateLegacyBlockModelBuilder customLoader(Supplier<L> customLoaderFactory, Consumer<L> action) {
		template.customLoader(customLoaderFactory, action);
		return this;
	}

	/**
	 * Modifies the transformation applied right before item display transformations and rotations specified in block states.
	 */
	public RegistrateLegacyBlockModelBuilder rootTransforms(Consumer<RootTransformsBuilder> action) {
		template.rootTransforms(action);
		return this;
	}

}
