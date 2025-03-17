package com.winseslas.microservices.bookstore.UserManager.model.enums;

public enum Gender {
    MALE("Masculin"),
    FEMALE("Féminin"),
    OTHER("Autre");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
