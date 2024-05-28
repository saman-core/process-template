package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.type.CaseType;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.NOT_NULL;

@Getter
public class Radio extends Component implements Field {

    public Radio(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
    }

    @Override
    public List<String> getValidationToModel() {
        return getIsRequired() ? List.of(NOT_NULL) : List.of();
    }
}
