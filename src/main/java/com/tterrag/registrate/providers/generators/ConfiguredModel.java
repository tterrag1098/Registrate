package com.tterrag.registrate.providers.generators;

import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;

public class ConfiguredModel {
	private final ModelFile modelFile;
	private final int rotationX;
	private final int rotationY;
	private final boolean uvLock;

	public ConfiguredModel(ModelFile modelFile, int rotationX, int rotationY, boolean uvLock) {
		this.modelFile = modelFile;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
		this.uvLock = uvLock;
	}

	public MultiVariant toMultiVariant() {
		Variant variant = new Variant(modelFile.getLocation());
		variant = variant.withXRot(toQuadrant(rotationX));
		variant = variant.withYRot(toQuadrant(rotationY));
		variant = variant.withUvLock(uvLock);
		return BlockModelGenerators.variant(variant);
	}

	public static Builder builder() {
		return new Builder();
	}

	private static Quadrant toQuadrant(int degrees) {
		int normalized = Math.floorMod(degrees, 360);
		return switch (normalized) {
			case 90 -> Quadrant.R90;
			case 180 -> Quadrant.R180;
			case 270 -> Quadrant.R270;
			default -> Quadrant.R0;
		};
	}

	public static class Builder {
		private final java.util.List<ConfiguredModel> models = new java.util.ArrayList<>();
		private ModelFile modelFile;
		private int rotationX;
		private int rotationY;
		private boolean uvLock;

		public Builder modelFile(ModelFile modelFile) {
			this.modelFile = modelFile;
			return this;
		}

		public Builder rotationX(int rotationX) {
			this.rotationX = rotationX;
			return this;
		}

		public Builder rotationY(int rotationY) {
			this.rotationY = rotationY;
			return this;
		}

		public Builder uvLock(boolean uvLock) {
			this.uvLock = uvLock;
			return this;
		}

		public Builder nextModel() {
			models.add(buildLast());
			modelFile = null;
			rotationX = 0;
			rotationY = 0;
			uvLock = false;
			return this;
		}

		public ConfiguredModel buildLast() {
			return new ConfiguredModel(modelFile, rotationX, rotationY, uvLock);
		}

		public ConfiguredModel[] build() {
			models.add(buildLast());
			return models.toArray(ConfiguredModel[]::new);
		}
	}
}
