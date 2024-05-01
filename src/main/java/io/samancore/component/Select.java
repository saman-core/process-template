package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Field;
import io.samancore.component.base.Multivalue;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Select extends Multivalue implements Field {

    private String dataSrc = null;

    public Select(String productName, String templateName, JsonNode jsonNodeComponent) {
        super(productName, templateName, jsonNodeComponent);
        this.dataSrc = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DATA_SRC);
        setIsEncrypted(false);
    }

    @Override
    public String getObjectTypeToModel() {
        String objectType;
        if (getIsMultiple()) {
            if (dataSrc != null && dataSrc.equalsIgnoreCase(DATA_SRC_RESOURCE)) {
                objectType = SET_LONG;
            } else {
                objectType = SET_STRING;
            }
        } else if (dataSrc != null && dataSrc.equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            objectType = DATA_TYPE_LONG;
        } else {
            objectType = DATA_TYPE_STRING;
        }
        return objectType;
    }

    @Override
    public String getObjectTypeToEntity() {
        if (getIsMultiple()) {
            if (dataSrc != null && dataSrc.equalsIgnoreCase(DATA_SRC_RESOURCE)) {
                return SET_LONG;
            } else return SET_STRING;
        } else if (dataSrc != null && dataSrc.equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            return DATA_TYPE_LONG;
        } else return DATA_TYPE_STRING;


    }

    @Override
    public List<String> getValidationToModel() {
        if (getIsRequired()) {
            if (getIsMultiple()) {
                return List.of(NOT_EMPTY);
            } else
                return List.of(NOT_BLANK, NOT_EMPTY);
        }
        return List.of();
    }
}
