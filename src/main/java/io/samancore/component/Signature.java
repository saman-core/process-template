package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.type.CaseType;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Signature extends Component implements Field {

    public Signature(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        var columnDescription = COLUMN_NAME_S.concat(", ");
        if (getIsRequired()) {
            columnDescription = columnDescription.concat(NULLABLE_FALSE);
        }
        var columnDescriptionTextType = " columnDefinition=\"%s\",";
        columnDescription = String.format(columnDescription.concat(columnDescriptionTextType), getKeyToColumn(), DATABASE_TYPE_TEXT);
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