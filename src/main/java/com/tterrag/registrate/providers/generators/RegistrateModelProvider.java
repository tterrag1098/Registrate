package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
	protected Stream<? extends Holder<Block>> getKnownBlocks() {
		return super.getKnownBlocks().filter(holder -> parent.hasDataGenerator(
				holder.getKey().identifier().getPath(), Registries.BLOCK, ProviderType.BLOCKSTATE));
	}

	@Override
	protected Stream<? extends Holder<Item>> getKnownItems() {
		return super.getKnownItems().filter(holder -> parent.hasDataGenerator(
				holder.getKey().identifier().getPath(), Registries.ITEM, ProviderType.ITEM_MODEL));
	}

	@Override
	public LogicalSide getSide() {
		return LogicalSide.CLIENT;
	}

}
