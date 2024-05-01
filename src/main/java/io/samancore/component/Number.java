package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Number extends Component implements Field {

    Boolean requireDecimal = false;

    Boolean isArbitraryPrecision = false;
    Boolean delimiterThouzand = false;
    String displayMask = null;
    Integer decimalLimit = null;
    BigDecimal minValue = null;
    BigDecimal maxValue = null;

    public Number(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.isArbitraryPrecision = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, ARBITRARY_PRECISION);
        this.delimiterThouzand = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, DELIMITER);
        this.displayMask = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DISPLAY_MASK);
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
        if (getIsUnique() || getIsRequired() || this.requireDecimal) {
            String unique = UNIQUE_TRUE;
            String required = NULLABLE_FALSE;
            String precisionScale = String.format("precision = %d, ", DEFAULT_INTEGER_PLACES);
            String scale = String.format("scale = %d, ", decimalLimit);
            String columnDescription = COLUMN;
            if (requireDecimal) {
                columnDescription = columnDescription.concat(precisionScale).concat(scale);
            }
            if (getIsUnique()) {
                columnDescription = columnDescription.concat(unique);
            }
            if (getIsRequired()) {
                columnDescription = columnDescription.concat(required);
            }
            return List.of(columnDescription.substring(0, columnDescription.lastIndexOf(",")).concat(")"));
        }
        return List.of();
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
    public Boolean evaluateIfNeedPairToEntity() {
        return getIsEncrypted();
    }

    @Override
    public Boolean evaluateIfNeedPairToModel() {
        return getIsEncrypted() || getDisplayMask() != null;
    }

    @Override
    public List<String> getMethodTransformToEntity() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_OBJECT_TRANSFORM_S_TO_ENTITY_OBJECT_ELEMENT, getKeyFormatted()));
        if (getIsEncrypted()) {
            list.add(getMethodEncrypt());
        }
        list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getMethodTransformToModel() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_OBJECT_TRANSFORM_S_TO_MODEL_OBJECT_ELEMENT, getKeyFormatted()));
        if (getIsEncrypted()) {
            list.add(getMethodDecrypt());
        }
        if (getDisplayMask() != null) {
            list.add(ELEMENT_MASKER_APPLY_ELEMENT);
        }
        list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }
}
