package com.tterrag.registrate.providers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;

class RegistrateDataProviderTest {

    @Test
    void waitsForEverySuccessfulProvider() {
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();
        CompletableFuture<Void> combined = RegistrateDataProvider.failFastAll(Map.of("first", first, "second", second));

        first.complete(null);
        assertFalse(combined.isDone());
        second.complete(null);

        assertDoesNotThrow(combined::join);
    }

    @Test
    void failsWithoutWaitingForAStuckDependentProvider() {
        CompletableFuture<Void> failed = new CompletableFuture<>();
        CompletableFuture<Void> stuck = new CompletableFuture<>();
        Map<String, CompletableFuture<?>> providers = new LinkedHashMap<>();
        providers.put("failed", failed);
        providers.put("stuck", stuck);
        CompletableFuture<Void> combined = RegistrateDataProvider.failFastAll(providers);
        IllegalStateException cause = new IllegalStateException("boom");

        failed.completeExceptionally(cause);

        assertTrue(combined.isCompletedExceptionally());
        CompletionException thrown = assertThrows(CompletionException.class, combined::join);
        assertSame(cause, thrown.getCause());
        assertFalse(stuck.isDone());
    }
}
