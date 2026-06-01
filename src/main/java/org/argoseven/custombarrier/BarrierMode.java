package org.argoseven.custombarrier;

public enum BarrierMode {
    TAG("Tag",  "Tag supports delimeter [ , ] to add more tags"),
    PLAYER("Player",  "Player supports delimeter [ , ] to add more players"),
    MAINHAND("Mainhand", "Check player main hand item id"),
    PREDICATE("Predicate", "Uses vanilla predicate system as check");


    private final String name;
    private final String description;
    BarrierMode(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String asString() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }
}
