package com.generator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.generator.Field;

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
        Iterator<JsonNode> iteratorComponents = componentsArrayNode.iterator();
        while (iteratorComponents.hasNext()) {
            JsonNode jsonNodeComponent = iteratorComponents.next();
            ArrayNode jsonNodeComponentColumns = (ArrayNode) jsonNodeComponent.get("columns");
            ArrayNode jsonNodeComponentChildren = (ArrayNode) jsonNodeComponent.get(COMPONENTS);
            if (jsonNodeComponentColumns != null) {
                Iterator<JsonNode> iteratorComponentsOfColumns = jsonNodeComponentColumns.iterator();
                while (iteratorComponentsOfColumns.hasNext()) {
                    JsonNode jsonNodeColumn = iteratorComponentsOfColumns.next();
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
        Field field = new Field();
        field.setKey(jsonNodeKey.textValue());
        field.setDataType(jsonNodeDataType.textValue());
        field.setPersistent(jsonNodeComponent.get("persistent") == null);
        field.setUnique(jsonNodeComponent.get("unique") != null);

        if (field.getDataType().equalsIgnoreCase("number")) {
            field.setIsDecimal(jsonNodeComponent.get("requireDecimal").asBoolean());
            if (field.isDecimal()) {
                if (jsonNodeComponent.get("decimalLimit") != null) {
                    field.setDecimalLimit(jsonNodeComponent.get("decimalLimit").asInt());
                } else {
                    field.setDecimalLimit(DEFUALT_DECIMAL_PLACES);
                }
            }
        }

        if (field.getDataType().equalsIgnoreCase("currency")) {
            field.setDecimalLimit(DEFUALT_CURRENCY_PLACES);
        }

        JsonNode jsonNodeValidations = jsonNodeComponent.get("validate");
        if (jsonNodeValidations != null) {
            JsonNode jsonNodeRequiredValidation = jsonNodeValidations.get("required");
            field.setRequired(jsonNodeRequiredValidation != null);
        }

        return field;
    }
}
