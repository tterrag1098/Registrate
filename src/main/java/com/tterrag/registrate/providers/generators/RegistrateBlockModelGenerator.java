package com.tterrag.registrate.providers.generators;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistrateBlockModelGenerator extends BlockModelGenerators {

    private final AbstractRegistrate<?> parent;

    public RegistrateBlockModelGenerator(AbstractRegistrate<?> parent, Consumer<BlockStateGenerator> known, ItemModelOutput item, BiConsumer<ResourceLocation, ModelInstance> model) {
        super(known, item, model);
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }

}
