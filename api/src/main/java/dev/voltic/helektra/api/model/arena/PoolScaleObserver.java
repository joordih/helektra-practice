package dev.voltic.helektra.api.model.arena;

@FunctionalInterface
public interface PoolScaleObserver {
    void onProgress(String arenaId, int completed, int total);

    static PoolScaleObserver noop() {
        return (arenaId, completed, total) -> {};
    }
}
