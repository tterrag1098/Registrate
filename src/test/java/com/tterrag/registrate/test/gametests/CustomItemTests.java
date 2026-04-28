package com.tterrag.registrate.test.gametests;

import com.tterrag.registrate.test.mod.TestMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.GameType;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "custom-item")
public class CustomItemTests {

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Ensure custom item is registered and has the expected properties")
    public static void ensureItemContent(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> TestMod.instance().testitem.asStack())
                .thenMap(stack -> stack.getHoverName().getString())
                .thenExecute(name -> helper.assertValueEqual(name, "Testitem", "Test Item localized name"))
                .thenSucceed()
        );
    }
}
