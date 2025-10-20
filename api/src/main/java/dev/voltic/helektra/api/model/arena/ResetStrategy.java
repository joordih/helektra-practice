package dev.voltic.helektra.api.model.arena;

public enum ResetStrategy {
    HYBRID,
    JOURNAL_ONLY,
    SECTION_REWRITE,
    CHUNK_SWAP,
    FULL_REGENERATE
}
