package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Signature extends Component implements Field {

    public Signature(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        String required = NULLABLE_FALSE;
        String columnDescription = COLUMN;

        var columnDescriptionTextType = "name = \"%s\", columnDefinition=\"%s\",";

        if (getIsRequired()) {
            columnDescription = columnDescription.concat(required);
        }
        columnDescription = String.format(columnDescription.concat(columnDescriptionTextType), getKey(), DATABASE_TYPE_TEXT);

        return List.of(columnDescription.substring(0, columnDescription.lastIndexOf(",")).concat(")"));
    }

    @Override
    public List<String> getValidationToModel() {
        if (getIsRequired()) {
            return List.of(NOT_BLANK, NOT_EMPTY);
        }
        return List.of();
    }
}