package dev.voltic.helektra.plugin.model.arena.reset.strategies;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;

import java.util.concurrent.CompletableFuture;

public interface ResetStrategyBase {
    CompletableFuture<Void> reset(ArenaInstance instance, Arena arena);
}
