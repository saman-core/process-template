package io.samancore.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import io.samancore.component.type.CaseType;

import java.math.BigDecimal;
import java.util.Locale;

import static io.samancore.util.GeneralConstant.*;

public class JsonFormIoUtil {

    private JsonFormIoUtil() {
    }

    public static String getKey(JsonNode jsonNodeComponent) {
        return jsonNodeComponent.get(KEY).textValue();
    }

    public static CaseType getCaseType(JsonNode jsonNodeComponent) {
        return jsonNodeComponent.get(CASE) != null ? CaseType.getByDescription(jsonNodeComponent.get(CASE).asText().toLowerCase(Locale.ROOT)) : CaseType.NONE;
    }

    public static boolean isRequired(JsonNode jsonNodeComponent) {
        JsonNode jsonNodeValidations = jsonNodeComponent.get(VALIDATE);
        return jsonNodeValidations != null && jsonNodeValidations.get(REQUIRED) != null && jsonNodeValidations.get(REQUIRED).asBoolean();
    }

    public static boolean getBooleanPropertyFromNode(JsonNode jsonNodeComponent, String propertyName) {
        return jsonNodeComponent.get(propertyName) != null && jsonNodeComponent.get(propertyName).asBoolean();
    }

    public static boolean isTruncateMultipleSpaces(JsonNode jsonNodeComponent) {
        return jsonNodeComponent.get(TRUNCATE_MULTIPLE_SPACES) != null && jsonNodeComponent.get(TRUNCATE_MULTIPLE_SPACES).asBoolean();
    }

    public static boolean isPersistent(JsonNode jsonNodeComponent) {
        var persistent = jsonNodeComponent.get(PERSISTENT);
        return persistent == null || !persistent.asText().equalsIgnoreCase("false");
    }

    public static String getStringPropertyFromNode(JsonNode jsonNodeComponent, String propertyName) {
        return jsonNodeComponent.get(propertyName) != null ? jsonNodeComponent.get(propertyName).asText() : null;
    }

    public static String getPattern(JsonNode jsonNodeComponent) {
        JsonNode jsonNodeValidations = jsonNodeComponent.get(VALIDATE);
        return jsonNodeValidations != null && jsonNodeValidations.get(PATTERN) != null ? jsonNodeValidations.get(PATTERN).asText() : null;
    }

    public static Integer getIntegerPropertyFromValidate(JsonNode jsonNodeComponent, String property) {
        JsonNode jsonNodeValidations = jsonNodeComponent.get(VALIDATE);
        return jsonNodeValidations != null && jsonNodeValidations.get(property) != null ? jsonNodeValidations.get(property).asInt() : null;
    }

    public static Integer getDecimalLimit(JsonNode jsonNodeComponent) {
        return jsonNodeComponent.get(DECIMAL_LIMIT) != null ? jsonNodeComponent.get(DECIMAL_LIMIT).asInt() : null;
    }

    public static BigDecimal getBigDecimalPropertyFromValidate(JsonNode jsonNodeComponent, String property) {
        JsonNode jsonNodeValidations = jsonNodeComponent.get(VALIDATE);
        return jsonNodeValidations != null && jsonNodeValidations.get(property) != null ? new BigDecimal(jsonNodeValidations.get(property).asText()) : null;
    }

    public static String getStringPropertyFromWidget(JsonNode jsonNodeComponent, String property) {
        JsonNode jsonNodeValidations = jsonNodeComponent.get(WIDGET);
        return jsonNodeValidations != null && jsonNodeValidations.get(property) != null && !(jsonNodeValidations.get(property) instanceof NullNode) ? jsonNodeValidations.get(property).asText() : null;
    }

    public static Boolean getBooleanPropertyFromWidget(JsonNode jsonNodeComponent, String property) {
        JsonNode jsonNodeValidations = jsonNodeComponent.get(WIDGET);
        return jsonNodeValidations != null && jsonNodeValidations.get(property) != null ? jsonNodeValidations.get(property).asBoolean() : null;
    }
}
