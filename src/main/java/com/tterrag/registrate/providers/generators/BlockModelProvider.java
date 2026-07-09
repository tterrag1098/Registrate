package com.tterrag.registrate.providers.generators;

import java.util.function.BiConsumer;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

public class BlockModelProvider extends ModelProvider {
	private final String modid;
	private final BiConsumer<Identifier, ModelInstance> output;

	public BlockModelProvider(String modid, BiConsumer<Identifier, ModelInstance> output) {
		this.modid = modid;
		this.output = output;
	}

	public Identifier mcLoc(String path) {
		return Identifier.withDefaultNamespace(path);
	}

	public Identifier modLoc(String path) {
		return Identifier.fromNamespaceAndPath(modid, path);
	}

	public ModelFile.ExistingModelFile getExistingFile(Identifier location) {
		return new ModelFile.ExistingModelFile(location);
	}

	public BlockModelBuilder getBuilder(String name) {
		return new BlockModelBuilder(resolve(name), output);
	}

	public BlockModelBuilder withExistingParent(String name, Identifier parent) {
		return getBuilder(name).parent(parent);
	}

	public BlockModelBuilder withExistingParent(String name, String parent) {
		return getBuilder(name).parent(parent);
	}

	public BlockModelBuilder cubeAll(String name, Identifier texture) {
		return withExistingParent(name, mcLoc("block/cube_all")).texture("all", texture);
	}

	public BlockModelBuilder cubeColumn(String name, Identifier side, Identifier end) {
		return withExistingParent(name, mcLoc("block/cube_column"))
			.texture("side", side)
			.texture("end", end);
	}

	public BlockModelBuilder singleTexture(String name, Identifier parent, String textureKey, Identifier texture) {
		return withExistingParent(name, parent).texture(textureKey, texture);
	}

	private Identifier resolve(String name) {
		if (name.contains(":")) {
			return Identifier.parse(name);
		}
		if (name.startsWith(BLOCK_FOLDER + "/") || name.startsWith(ITEM_FOLDER + "/")) {
			return modLoc(name);
		}
		return modLoc(BLOCK_FOLDER + "/" + name);
	}
}
