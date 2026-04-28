package com.tterrag.registrate.test.gametests;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.junit.jupiter.api.DynamicTest;

@ForEachTest(groups = "custom-item")
public class CustomItemTests {

    @GameTest
    @TestHolder(description = "Ensure custom item is registered and has the expected properties")
    public static void ensureItemContent(final DynamicTest test) {

    }
}
