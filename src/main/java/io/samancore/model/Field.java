package io.samancore.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(
        setterPrefix = "set",
        builderMethodName = "newBuilder",
        toBuilder = true
)
public class Field {
    String key;
    String dataType;
    boolean isPersistent;
    boolean isRequired;
    boolean isUnique;
    boolean isDecimal;
    int decimalLimit;
}
