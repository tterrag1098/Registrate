package com.tterrag.registrate.test.gametests;

import com.tterrag.registrate.test.mod.TestMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "fluid-builder")
public class FluidBuilderTests {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensure the default source is shared by the flowing fluid and bucket")
    public static void ensureDefaultSourceReferences(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> TestMod.instance().testfluid)
                .thenExecute(entry -> helper.assertValueEqual(
                        entry.getSource(), entry.get().getSource(), "Flowing fluid source"))
                .thenExecute(entry -> helper.assertValueEqual(
                        entry.getBucket().orElseThrow(), entry.getSource().getBucket(), "Source fluid bucket"))
                .thenSucceed());
    }
}
