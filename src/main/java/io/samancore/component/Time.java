package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.type.CaseType;

import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

public class Time extends Component implements Field {

    public Time(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
        this.setMaxLength(MAX_LENGTH_TIME);
    }

    @Override
    public List<String> getValidationToModel() {
        var validationList = new ArrayList<String>();
        if (getIsEncrypted()) {
            validationList.add(String.format(MAX_BYTE_VALUE_S, getEncryptType().getModelMaxLength()));
        }
        if (getIsRequired()) {
            validationList.add(NOT_BLANK);
            validationList.add(NOT_EMPTY);
        }
        if (getModelMaxLength() != null) {
            validationList.add(String.format(SIZE_MAX_D, getModelMaxLength()));
        }
        return validationList;
    }
}
