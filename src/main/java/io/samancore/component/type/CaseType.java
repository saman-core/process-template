package io.samancore.component.type;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CaseType {
    NONE(""),
    LOWERCASE("LowerCase"),
    UPPERCASE("UpperCase");

    final String description;

    CaseType(String description) {
        this.description = description;
    }

    public static CaseType getByDescription(String description) {
        return Arrays.stream(values()).filter(caseType -> (caseType.getDescription().equalsIgnoreCase(description))).findAny().orElse(NONE);
    }
}
