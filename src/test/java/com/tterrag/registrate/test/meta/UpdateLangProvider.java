package com.tterrag.registrate.test.meta;

import java.io.IOException;
import java.nio.file.Paths;

public class UpdateLangProvider {
    
    public static void main(String[] args) throws IOException {
        new MethodGenerator()
            .exclude("addTranslations")
            .exclude("add", "String", "String")
            .generate(Paths.get("src", "main", "java", "com", "tterrag", "registrate", "providers", "RegistrateLangProvider.java"));
    }
}
