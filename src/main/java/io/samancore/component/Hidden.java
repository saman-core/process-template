package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;

import java.util.List;

public class Hidden extends Component implements Field {

    public Hidden(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
    }

    @Override
    public List<String> getValidationToModel() {
        return List.of();
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        return List.of();
    }

}
