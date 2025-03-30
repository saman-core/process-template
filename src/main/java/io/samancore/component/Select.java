package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Field;
import io.samancore.component.base.Multivalue;
import io.samancore.type.CaseType;
import io.samancore.type.EncryptType;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Select extends Multivalue implements Field {


    public Select(String moduleName, String productName, String templateName, CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(moduleName, productName, templateName, columnCaseSensitive, jsonNodeComponent);
        setEncryptType(EncryptType.NONE);
    }

    @Override
    public String getObjectTypeToModel() {
        String objectType;
        if (getIsMultiple()) {
            if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
                objectType = SET_LONG;
            } else {
                objectType = SET_STRING;
            }
        } else if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            objectType = DATA_TYPE_LONG;
        } else {
            objectType = DATA_TYPE_STRING;
        }
        return objectType;
    }

    @Override
    public String getObjectTypeToEntity() {
        if (getIsMultiple()) {
            if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
                return SET_LONG;
            } else return SET_STRING;
        } else if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            return DATA_TYPE_LONG;
        } else return DATA_TYPE_STRING;


    }

    @Override
    public List<String> getValidationToModel() {
        if (getIsRequired()) {
            if (getIsMultiple()) {
                return List.of(NOT_EMPTY);
            } else if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
                return List.of(NOT_NULL);
            }else return List.of(NOT_BLANK, NOT_EMPTY);
        }
        return List.of();
    }


    @Override
    public String getConversionFromStringToObjectType(String value) {
        var result = "";
        if (getIsMultiple()) {
            if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
                result = String.format(" %s.get(\"%s\").stream().map(s -> Long.valueOf(s)).collect(java.util.stream.Collectors.toSet())", value, getKey());
            } else {
                result = String.format(" %s.get(\"%s\").stream().collect(java.util.stream.Collectors.toSet())", value, getKey());
            }
        } else if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            result = String.format(" Long.valueOf(%s.getFirst(\"%s\"))", value, getKey());
        } else {
            result = String.format(" %s.getFirst(\"%s\")", value, getKey());
        }
        return result;
    }
}
