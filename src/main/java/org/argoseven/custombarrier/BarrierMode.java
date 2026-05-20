package org.argoseven.custombarrier;

public enum BarrierMode {
    TAG("Tag"),
    PLAYER("Player"),
    PREDICATE("Predicate");

    private final String name;
    BarrierMode( String name ) {
        this.name = name;
    }

    public String asString() {
        return this.name;
    }
}
