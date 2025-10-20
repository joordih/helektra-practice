package dev.voltic.helektra.api.model.arena;

import java.util.concurrent.CompletableFuture;

public interface ISchedulerService {
    CompletableFuture<Void> runAsync(Runnable task);
    CompletableFuture<Void> runSync(Runnable task);
    void scheduleRepeating(Runnable task, long delayTicks, long periodTicks);
    void cancelAll();
    double getCurrentTps();
    boolean isBelowThreshold(double threshold);
}
