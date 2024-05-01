package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import lombok.Getter;

import java.util.List;

import static io.samancore.util.GeneralConstant.DATA_TYPE_BOOLEAN;
import static io.samancore.util.GeneralConstant.DATA_TYPE_BYTEA;

@Getter
public class Checkbox extends Component implements Field {

    public Checkbox(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
    }

    @Override
    public String getObjectTypeToModel() {
        return DATA_TYPE_BOOLEAN;
    }


    @Override
    public String getObjectTypeToEntity() {
        return getIsEncrypted() ? DATA_TYPE_BYTEA : DATA_TYPE_BOOLEAN;
    }

    @Override
    public List<String> getValidationToModel() {
        return List.of();
    }
}