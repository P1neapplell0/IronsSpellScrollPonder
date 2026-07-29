package com.p1nero.iss_ponder.api;

/**
 * Extends the generic preview lifecycle for spells that require gameplay after their initial cast.
 * Implementations are server-side and must leave all persistent state inside the preview session.
 */
public interface SpellPreviewAdapter {
    enum Action {
        WAIT,
        RECAST,
        COMPLETE
    }

    default boolean allowsSimulation() {
        return true;
    }

    default void onSceneReady(SpellPreviewContext context) {
    }

    default void onCastCompleted(SpellPreviewContext context) {
    }

    default Action tickAfterCast(SpellPreviewContext context) {
        return Action.COMPLETE;
    }

    default void beforeRecast(SpellPreviewContext context) {
    }

    default void onSessionClosed(SpellPreviewContext context) {
    }
}
