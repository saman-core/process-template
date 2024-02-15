package io.samancore.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.samancore.model.Field;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class JsonUtil {

    public static final String COMPONENTS = "components";
    public static final int DEFUALT_DECIMAL_PLACES = 2;
    public static final int DEFUALT_CURRENCY_PLACES = 4;

    public static List<Field> getFieldsOfComponent(ArrayNode componentsArrayNode) {
        List<Field> fieldList = new ArrayList<>();
        for (JsonNode jsonNodeComponent : componentsArrayNode) {
            ArrayNode jsonNodeComponentColumns = (ArrayNode) jsonNodeComponent.get("columns");
            ArrayNode jsonNodeComponentChildren = (ArrayNode) jsonNodeComponent.get(COMPONENTS);
            if (jsonNodeComponentColumns != null) {
                for (JsonNode jsonNodeColumn : jsonNodeComponentColumns) {
                    ArrayNode componentsFromColumn = (ArrayNode) jsonNodeColumn.get(COMPONENTS);
                    fieldList.addAll(getFieldsOfComponent(componentsFromColumn));
                }
            } else if (jsonNodeComponentChildren != null) {
                fieldList.addAll(getFieldsOfComponent(jsonNodeComponentChildren));
            } else {
                fieldList.add(getField(jsonNodeComponent));
            }
        }
        return fieldList;
    }

    public static Field getField(JsonNode jsonNodeComponent) {
        JsonNode jsonNodeKey = jsonNodeComponent.get("key");
        Objects.requireNonNull(jsonNodeKey);
        JsonNode jsonNodeDataType = jsonNodeComponent.get("type");
        Objects.requireNonNull(jsonNodeDataType);
        var dataType = jsonNodeDataType.textValue();
        var fieldBuilder = Field.newBuilder();
        fieldBuilder.setKey(jsonNodeKey.textValue());
        fieldBuilder.setDataType(dataType);
        fieldBuilder.setIsPersistent(jsonNodeComponent.get("persistent") == null);
        fieldBuilder.setIsUnique(jsonNodeComponent.get("unique") != null);

        if (dataType.equalsIgnoreCase("number")) {
            var isDecimal = jsonNodeComponent.get("requireDecimal").asBoolean();
            fieldBuilder.setIsDecimal(isDecimal);
            if (isDecimal) {
                if (jsonNodeComponent.get("decimalLimit") != null) {
                    fieldBuilder.setDecimalLimit(jsonNodeComponent.get("decimalLimit").asInt());
                } else {
                    fieldBuilder.setDecimalLimit(DEFUALT_DECIMAL_PLACES);
                }
            }
        }

        if (dataType.equalsIgnoreCase("currency")) {
            fieldBuilder.setDecimalLimit(DEFUALT_CURRENCY_PLACES);
        }

        JsonNode jsonNodeValidations = jsonNodeComponent.get("validate");
        if (jsonNodeValidations != null) {
            JsonNode jsonNodeRequiredValidation = jsonNodeValidations.get("required");
            fieldBuilder.setIsRequired(jsonNodeRequiredValidation != null);
        }

        return fieldBuilder.build();
    }
}
