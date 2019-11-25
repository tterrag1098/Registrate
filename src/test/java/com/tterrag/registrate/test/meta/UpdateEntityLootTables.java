package com.tterrag.registrate.test.meta;

import java.io.IOException;
import java.nio.file.Paths;

public class UpdateEntityLootTables {
    
    public static void main(String[] args) throws IOException {
        new MethodGenerator()
            .exclude("isNonLiving")
            .generate(Paths.get("src", "main", "java", "com", "tterrag", "registrate", "providers", "loot", "RegistrateEntityLootTables.java"));
    }
}
