package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;

public class RegistrateModelProvider extends ModelProvider implements RegistrateProvider {

	private final AbstractRegistrate<?> parent;

	public RegistrateModelProvider(AbstractRegistrate<?> parent, PackOutput p_388260_) {
		super(p_388260_, parent.getModid());
		this.parent = parent;
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		new RegistrateBlockModelGenerator(parent, blockModels.blockStateOutput, blockModels.itemModelOutput, blockModels.modelOutput).run();
		new RegistrateItemModelGenerator(parent, itemModels.itemModelOutput, itemModels.modelOutput).run();
	}

	@Override
	public LogicalSide getSide() {
		return LogicalSide.CLIENT;
	}

}
