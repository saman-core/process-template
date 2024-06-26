package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.type.CaseType;

import java.util.List;

public class Hidden extends Component implements Field {

    public Hidden(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        return List.of();
    }

}
