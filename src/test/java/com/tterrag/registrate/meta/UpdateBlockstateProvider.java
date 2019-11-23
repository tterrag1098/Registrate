package com.tterrag.registrate.meta;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

public class UpdateBlockstateProvider {
    
    public static void main(String[] args) throws IOException {
        new MethodGenerator(ImmutableList.of(Pair.of("T", "BlockModelBuilder")))
            .generate(Paths.get("src", "main", "java", "com", "tterrag", "registrate", "providers", "RegistrateBlockstateProvider.java"));
    }
}
