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
        var validation = new ArrayList<String>();
        if (getIsRequired()) {
            validation.add(NOT_BLANK);
            validation.add(NOT_EMPTY);
        }
        if (getMaxLength() != null) {
            validation.add(String.format("@Size(max = %d)", getMaxLength()));
        }
        return validation;
    }
}
