package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.type.CaseType;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Number extends Component implements Field {
    private Boolean requireDecimal = false;
    private Boolean isArbitraryPrecision = false;
    private Boolean delimiterThouzand = false;
    private Integer decimalLimit = null;
    private BigDecimal minValue = null;
    private BigDecimal maxValue = null;

    public Number(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
        this.isArbitraryPrecision = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, ARBITRARY_PRECISION);
        this.delimiterThouzand = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, DELIMITER);
        this.decimalLimit = JsonFormIoUtil.getDecimalLimit(jsonNodeComponent);
        this.requireDecimal = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, REQUIRE_DECIMAL);
        this.minValue = JsonFormIoUtil.getBigDecimalPropertyFromValidate(jsonNodeComponent, MIN);
        this.maxValue = JsonFormIoUtil.getBigDecimalPropertyFromValidate(jsonNodeComponent, MAX);
        if (decimalLimit == null && requireDecimal) {
            decimalLimit = DEFAULT_DECIMAL_PLACES;
        }
    }

    @Override
    public String getObjectTypeToModel() {
        if (requireDecimal || decimalLimit != null) {
            if (isArbitraryPrecision) {
                return DATA_TYPE_BIG_DECIMAL;
            } else return DATA_TYPE_DOUBLE;
        } else if (isArbitraryPrecision) {
            return DATA_TYPE_LONG;
        } else return DATA_TYPE_INTEGER;

    }

    @Override
    public String getObjectTypeToEntity() {
        if (getIsEncrypted()) {
            return DATA_TYPE_BYTEA;
        } else if (requireDecimal) {
            if (isArbitraryPrecision) {
                return DATA_TYPE_BIG_DECIMAL;
            } else return DATA_TYPE_DOUBLE;
        } else if (isArbitraryPrecision) {
            return DATA_TYPE_LONG;
        } else return DATA_TYPE_INTEGER;

    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        String columnDescription = String.format(COLUMN_NAME_S, getKeyToColumn());
        if (getIsUnique() || getIsRequired() || this.requireDecimal) {
            String unique = UNIQUE_TRUE;
            String required = NULLABLE_FALSE;
            String precisionScale = String.format("precision = %d, ", DEFAULT_INTEGER_PLACES);
            String scale = String.format("scale = %d, ", decimalLimit);
            columnDescription = columnDescription.concat(", ");
            if (requireDecimal) {
                columnDescription = columnDescription.concat(precisionScale).concat(scale);
            }
            if (getIsUnique()) {
                columnDescription = columnDescription.concat(unique);
            }
            if (getIsRequired()) {
                columnDescription = columnDescription.concat(required);
            }
            columnDescription = columnDescription.substring(0, columnDescription.lastIndexOf(",")).concat(")");
        } else columnDescription = columnDescription.concat(")");
        return List.of(columnDescription);
    }

    @Override
    public List<String> getValidationToModel() {
        var validation = new ArrayList<String>();
        if (getIsRequired()) {
            validation.add(NOT_NULL);
        }
        if (minValue != null || maxValue != null) {
            if (isArbitraryPrecision) {
                if (minValue != null) {
                    validation.add(String.format("@MinDecimal(value = \"%s\" )", minValue));
                }
                if (maxValue != null) {
                    validation.add(String.format("@MaxDecimal(value = \"%s\" )", maxValue));
                }
            } else {
                if (minValue != null) {
                    validation.add(String.format("@Min(value = %s )", minValue));
                }
                if (maxValue != null) {
                    validation.add(String.format("@Max(value = %s )", maxValue));
                }
            }
        }
        return validation;
    }

    @Override
    public String getMethodEncrypt() {
        return String.format("return encrypt.%s(element.toString());", getEncryptType().getEncryptMethod());
    }

    @Override
    public List<String> getMethodTransformToEntity() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_ENTITY_S_ELEMENT, getObjectTypeToEntity(), getKeyFormatted(), getObjectTypeToModel()));
        if (getIsEncrypted()) {
            list.add(getMethodEncrypt());
        } else list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getMethodTransformToModel() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_MODEL_S_ELEMENT, getObjectTypeToModel(), getKeyFormatted(), getObjectTypeToEntity()));
        var object = "element";
        if (getIsEncrypted()) {
            list.add(getMethodDecrypt());
            object = "newElement";
        }
        var objectType = getObjectTypeToModel();
        String descriptionReturn = String.format("%s.valueOf", objectType);
        if (objectType.equals(DATA_TYPE_BIG_DECIMAL)) {
            descriptionReturn = String.format("new %s", objectType);
        }
        list.add(String.format("return %s(%s);", descriptionReturn, object));
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public String getConversionFromStringToObjectType(String value) {
        var objectType = getObjectTypeToModel();
        String descriptionReturn = String.format("%s.valueOf", objectType);
        if (objectType.equals(DATA_TYPE_BIG_DECIMAL)) {
            descriptionReturn = String.format("new %s", objectType);
        }
        return String.format(" %s(%s.getFirst(\"%s\"))", descriptionReturn, value, getKey());
    }
}
