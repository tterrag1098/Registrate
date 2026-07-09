package com.tterrag.registrate.providers.generators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.PropertyValueList;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class VariantBlockStateBuilder {
	private final RegistrateBlockModelGenerator owner;
	private final Block block;

	VariantBlockStateBuilder(RegistrateBlockModelGenerator owner, Block block) {
		this.owner = owner;
		this.block = block;
	}

	public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> mapper) {
		Map<String, BlockStateModel.Unbaked> entries = new LinkedHashMap<>();
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			entries.put(key(state), combine(mapper.apply(state)).toUnbaked());
		}
		owner.blockStateOutput.accept(new SimpleGenerator(block, entries));
		return this;
	}

	@SafeVarargs
	public final VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> mapper,
		Property<?>... ignored) {
		return forAllStates(mapper);
	}

	public PartialBlockstate partialState() {
		return new PartialBlockstate();
	}

	private static MultiVariant combine(ConfiguredModel[] models) {
		if (models == null || models.length == 0) {
			throw new IllegalStateException("No models supplied");
		}
		return models[0].toMultiVariant();
	}

	public class PartialBlockstate {
		public VariantBlockStateBuilder setModels(ConfiguredModel... models) {
			owner.blockStateOutput.accept(new SimpleGenerator(block, Map.of("", combine(models).toUnbaked())));
			return VariantBlockStateBuilder.this;
		}
	}

	private static String key(BlockState state) {
		return PropertyValueList.of(state.getValues()
			.toArray(Property.Value[]::new))
			.getKey();
	}

	private record SimpleGenerator(Block block, Map<String, BlockStateModel.Unbaked> entries)
		implements net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator {
		@Override
		public BlockStateModelDispatcher create() {
			return new BlockStateModelDispatcher(
				java.util.Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(entries)), java.util.Optional.empty());
		}
	}
}
