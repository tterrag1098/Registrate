package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.LogicalSide;

import java.util.Optional;

public class RegistrateBlockstateProvider extends BlockStateProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> parent;

    public RegistrateBlockstateProvider(AbstractRegistrate<?> parent, DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, parent.getModid(), exFileHelper);
        this.parent = parent;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    protected void registerStatesAndModels() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }
    
    @Override
    public String getName() {
        return "Blockstates";
    }
    
    ExistingFileHelper getExistingFileHelper() {
        return this.models().existingFileHelper;
    }
    
    @SuppressWarnings("null")
    public Optional<VariantBlockStateBuilder> getExistingVariantBuilder(Block block) {
        return Optional.ofNullable(registeredBlocks.get(block))
                .filter(VariantBlockStateBuilder.class::isInstance)
                .map(VariantBlockStateBuilder.class::cast);
    }
    
    @SuppressWarnings("null")
    public Optional<MultiPartBlockStateBuilder> getExistingMultipartBuilder(Block block) {
        return Optional.ofNullable(registeredBlocks.get(block))
                .filter(MultiPartBlockStateBuilder.class::isInstance)
                .map(MultiPartBlockStateBuilder.class::cast);
    }
}
