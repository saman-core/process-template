package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Field;
import io.samancore.component.base.Multivalue;
import io.samancore.type.CaseType;
import io.samancore.type.EncryptType;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.NOT_EMPTY;
import static io.samancore.util.GeneralConstant.SET_STRING;

@Getter
public class Tags extends Multivalue implements Field {

    public Tags(String productName, String templateName, CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(productName, templateName, columnCaseSensitive, jsonNodeComponent);
        this.setIsMultiple(true);
        setEncryptType(EncryptType.NONE);
    }

    @Override
    public String getObjectTypeToModel() {
        return SET_STRING;
    }

    @Override
    public String getObjectTypeToEntity() {
        return SET_STRING;
    }

    @Override
    public List<String> getValidationToModel() {
        if (getIsRequired()) {
            return List.of(NOT_EMPTY);
        }
        return List.of();
    }
}
