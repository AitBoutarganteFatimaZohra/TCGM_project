package com.tcgm.model.enums;

public enum PrioriteTache {
    BASSE(1),
    MOYENNE(2),
    HAUTE(3),
    CRITIQUE(4);

    private final int value;

    PrioriteTache(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PrioriteTache fromValue(int value) {
        for (PrioriteTache p : PrioriteTache.values()) {
            if (p.value == value) {
                return p;
            }
        }
        return MOYENNE;
    }
}