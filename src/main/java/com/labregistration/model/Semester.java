package com.labregistration.model;

public enum Semester {
    FIRST_SEMESTER("First Semester"),
    SECOND_SEMESTER("Second Semester"),
    SUMMER("Summer");

    private final String displayName;

    Semester(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
