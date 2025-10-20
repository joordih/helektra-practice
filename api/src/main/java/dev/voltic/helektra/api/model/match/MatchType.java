package dev.voltic.helektra.api.model.match;

public enum MatchType {
    DUEL(2, 2, "1v1 Duel"),
    QUEUE(2, 2, "Queued 1v1"),
    FFA(2, 8, "Free For All"),
    RANGE_ROVER(2, 8, "Range Rover"),
    PARTY_FFA(2, 16, "Party Free For All");

    private final int minParticipants;
    private final int maxParticipants;
    private final String displayName;

    MatchType(int minParticipants, int maxParticipants, String displayName) {
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.displayName = displayName;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public String getDisplayName() {
        return displayName;
    }
}
