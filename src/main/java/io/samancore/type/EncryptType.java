package io.samancore.type;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum EncryptType {
    NONE("false", null, null, null),
    SYMMETRIC("true", 2000, "encrypt", "decrypt"),
    ASYMMETRIC("Asymmetric", 1000, "encrypt", "decrypt");

    private String description;
    private Integer maxLength;
    private String encryptMethod;
    private String decryptMethod;

    EncryptType(String description, Integer maxLength, String encryptMethod, String decryptMethod) {
        this.description = description;
        this.maxLength = maxLength;
        this.encryptMethod = encryptMethod;
        this.decryptMethod = decryptMethod;
    }

    public static EncryptType getByDescription(String value) {
        return Arrays.stream(values()).filter(encryptType -> (encryptType.getDescription().equalsIgnoreCase(value))).findAny().orElse(NONE);
    }
}
