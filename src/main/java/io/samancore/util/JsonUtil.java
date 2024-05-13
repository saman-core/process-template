package io.samancore.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.samancore.component.Number;
import io.samancore.component.*;
import io.samancore.component.base.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.samancore.GeneralUtil.RESERVED_WORDS;
import static io.samancore.util.GeneralConstant.*;

public class JsonUtil {

    private JsonUtil() {
    }

    public static List<Field> getFieldsOfComponent(String productName, String templateName, ArrayNode componentsArrayNode) {
        List<Field> fieldList = new ArrayList<>();
        for (JsonNode jsonNodeComponent : componentsArrayNode) {
            ArrayNode jsonNodeComponentColumns = (ArrayNode) jsonNodeComponent.get(JSON_COMPONENT_COLUMNS);
            ArrayNode jsonNodeComponentChildren = (ArrayNode) jsonNodeComponent.get(JSON_COMPONENT_COMPONENTS);
            ArrayNode jsonNodeComponentRows = (ArrayNode) jsonNodeComponent.get(JSON_COMPONENT_ROWS);
            if (jsonNodeComponentColumns != null) {
                for (JsonNode jsonNodeColumn : jsonNodeComponentColumns) {
                    ArrayNode componentsFromColumn = (ArrayNode) jsonNodeColumn.get(JSON_COMPONENT_COMPONENTS);
                    fieldList.addAll(getFieldsOfComponent(productName, templateName, componentsFromColumn));
                }
            } else if (jsonNodeComponentChildren != null) {
                fieldList.addAll(getFieldsOfComponent(productName, templateName, jsonNodeComponentChildren));
            } else if (jsonNodeComponentRows != null) {
                for (JsonNode jsonNodeRows : jsonNodeComponentRows) {
                    fieldList.addAll(getFieldsOfComponent(productName, templateName, (ArrayNode) jsonNodeRows));
                }
            } else {
                var field = getField(productName, templateName, jsonNodeComponent);
                if (field != null) {
                    if (RESERVED_WORDS.contains(field.getKey().toLowerCase(Locale.ROOT))) {
                        throw new RuntimeException("There is a component with name not allowed. Name=".concat(field.getKey()));
                    }
                    fieldList.add(field);
                }
            }
        }
        return fieldList;
    }

    private static Field getField(String productName, String templateName, JsonNode jsonNodeComponent) {
        return switch (jsonNodeComponent.get("type").asText().toLowerCase(Locale.ROOT)) {
            case COMPONENT_TEXTFIELD, COMPONENT_SAMANTEXTFIELD -> new Textfield(jsonNodeComponent);
            case COMPONENT_TEXTAREA, COMPONENT_SAMANTEXTAREA -> new Textarea(jsonNodeComponent);
            case COMPONENT_PASSWORD, COMPONENT_SAMANPASSWORD -> new Password(jsonNodeComponent);
            case COMPONENT_EMAIL, COMPONENT_SAMANEMAIL -> new Email(jsonNodeComponent);
            case COMPONENT_URL, COMPONENT_SAMANURL -> new Url(jsonNodeComponent);
            case COMPONENT_RADIO, COMPONENT_SAMANRADIO -> new Radio(jsonNodeComponent);
            case COMPONENT_CHECKBOX, COMPONENT_SAMANCHECKBOX -> new Checkbox(jsonNodeComponent);
            case COMPONENT_DATETIME, COMPONENT_SAMANDATETIME -> new Datetime(jsonNodeComponent);
            case COMPONENT_NUMBER, COMPONENT_SAMANNUMBER -> new Number(jsonNodeComponent);
            case COMPONENT_PHONENUMBER, COMPONENT_SAMANPHONENUMBER -> new Phonenumber(jsonNodeComponent);
            case COMPONENT_TIME, COMPONENT_SAMANTIME -> new Time(jsonNodeComponent);
            case COMPONENT_SIGNATURE, COMPONENT_SAMANSIGNATURE -> new Signature(jsonNodeComponent);
//            case COMPONENT_SELECT, COMPONENT_SAMANSELECT -> new Select(productName, templateName, jsonNodeComponent);
//            case COMPONENT_TAGS, COMPONENT_SAMANTAGS -> new Tags(productName, templateName, jsonNodeComponent);
            case COMPONENT_HIDDEN, COMPONENT_SAMANHIDDEN -> new Hidden(jsonNodeComponent);
            default -> null;

        };
    }
}
