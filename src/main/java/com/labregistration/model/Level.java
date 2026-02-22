package com.labregistration.model;

public enum Level {
    LEVEL_100("100 Level"),
    LEVEL_200("200 Level"),
    LEVEL_300("300 Level"),
    LEVEL_400("400 Level"),
    LEVEL_500("500 Level");

    private final String displayName;

    Level(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
