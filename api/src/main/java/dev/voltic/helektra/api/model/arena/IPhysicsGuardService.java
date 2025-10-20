package dev.voltic.helektra.api.model.arena;

public interface IPhysicsGuardService {
    void suspend(Region region);
    void resume(Region region);
    boolean isSuspended(Region region);
    void suspendAll();
    void resumeAll();
}
