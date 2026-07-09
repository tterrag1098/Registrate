package com.tterrag.registrate.providers.generators;

import java.util.function.BiConsumer;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

public class BlockModelBuilder extends ModelBuilder<BlockModelBuilder> {
	public BlockModelBuilder(Identifier location, BiConsumer<Identifier, ModelInstance> output) {
		super(location, output);
	}
}
