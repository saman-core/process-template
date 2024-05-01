package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.util.JsonFormIoUtil;

import java.util.List;

import static io.samancore.util.GeneralConstant.*;

public class Time extends Component implements Field {

    private String inputMask;

    public Time(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.inputMask = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, INPUT_MASK);
    }

    @Override
    public List<String> getValidationToModel() {
        return getIsRequired() ? List.of(NOT_BLANK, NOT_EMPTY) : List.of();
    }
}
