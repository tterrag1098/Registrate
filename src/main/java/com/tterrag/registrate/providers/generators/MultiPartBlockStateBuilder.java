package com.tterrag.registrate.providers.generators;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class MultiPartBlockStateBuilder {
	private final RegistrateBlockModelGenerator owner;
	private final Block block;
	private final List<PartBuilder> parts = new ArrayList<>();
	private final BlockModelDefinitionGenerator generator = new BlockModelDefinitionGenerator() {
		@Override
		public Block block() {
			return block;
		}

		@Override
		public BlockStateModelDispatcher create() {
			return createGenerator().create();
		}
	};
	private boolean registered;

	MultiPartBlockStateBuilder(RegistrateBlockModelGenerator owner, Block block) {
		this.owner = owner;
		this.block = block;
	}

	public PartBuilder part() {
		PartBuilder part = new PartBuilder(this);
		parts.add(part);
		return part;
	}

	private MultiPartGenerator createGenerator() {
		MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
		for (PartBuilder part : parts) {
			MultiVariant variant = part.model.toMultiVariant();
			if (part.condition == null) {
				generator.with(variant);
			} else {
				generator.with(part.condition, variant);
			}
		}
		return generator;
	}

	private void flush() {
		if (!registered) {
			registered = true;
			owner.blockStateOutput.accept(generator);
		} else {
			owner.seenBlockstates.put(block, generator.create());
		}
	}

	public static class PartBuilder {
		private final MultiPartBlockStateBuilder parent;
		private ConfiguredModel model;
		private ConditionBuilder condition;
		private ModelFile modelFile;
		private int rotationX;
		private int rotationY;
		private boolean uvLock;

		PartBuilder(MultiPartBlockStateBuilder parent) {
			this.parent = parent;
		}

		public PartBuilder modelFile(ModelFile modelFile) {
			this.modelFile = modelFile;
			return this;
		}

		public PartBuilder rotationX(int rotationX) {
			this.rotationX = rotationX;
			return this;
		}

		public PartBuilder rotationY(int rotationY) {
			this.rotationY = rotationY;
			return this;
		}

		public PartBuilder uvLock(boolean uvLock) {
			this.uvLock = uvLock;
			return this;
		}

		public PartBuilder addModel() {
			model = new ConfiguredModel(modelFile, rotationX, rotationY, uvLock);
			return this;
		}

		@SafeVarargs
		public final <T extends Comparable<T>> PartBuilder condition(Property<T> property, T value,
			T... additionalValues) {
			if (condition == null) {
				condition = new ConditionBuilder();
			}
			condition.term(property, value, additionalValues);
			return this;
		}

		public PartBuilder condition(Property<Boolean> property, boolean value) {
			if (condition == null) {
				condition = new ConditionBuilder();
			}
			condition.term(property, value);
			return this;
		}

		public MultiPartBlockStateBuilder end() {
			parent.flush();
			return parent;
		}
	}
}
