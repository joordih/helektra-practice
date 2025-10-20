package dev.voltic.helektra.plugin.model.arena.reset.legacy;

import dev.voltic.helektra.api.model.arena.Arena;
import dev.voltic.helektra.api.model.arena.ArenaInstance;
import java.util.concurrent.CompletableFuture;

@Deprecated
public interface ResetStrategyBase {
    CompletableFuture<Void> reset(ArenaInstance instance, Arena arena);
}
