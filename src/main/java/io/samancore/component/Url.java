package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Url extends Component implements Field {
    private Boolean isTruncateMultipleSpaces = false;
    private String pattern = null;
    private String displayMask = null;
    private Integer minLength = null;
    private Integer maxLength = null;

    public Url(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.isTruncateMultipleSpaces = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, TRUNCATE_MULTIPLE_SPACES);
        this.pattern = JsonFormIoUtil.getPattern(jsonNodeComponent);
        this.displayMask = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DISPLAY_MASK);
        this.minLength = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MIN_LENGTH);
        this.maxLength = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MAX_LENGTH);
    }

    @Override
    public List<String> getValidationToModel() {
        return getIsRequired() ? List.of(NOT_BLANK, NOT_EMPTY) : List.of();
    }


    @Override
    public Boolean evaluateIfNeedPairToEntity() {
        return getIsTruncateMultipleSpaces() || getIsEncrypted();
    }

    @Override
    public Boolean evaluateIfNeedPairToModel() {
        return getIsTruncateMultipleSpaces() || getIsEncrypted() || getDisplayMask() != null;
    }

    @Override
    public List<String> getMethodTransformToEntity() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_OBJECT_TRANSFORM_S_TO_ENTITY_OBJECT_ELEMENT, getKeyFormatted()));
        if (getIsTruncateMultipleSpaces()) {
            list.add(ELEMENT_STRING_ELEMENT_TRIM_REPLACE_ALL_S_2_G);
        }
        if (getIsEncrypted()) {
            list.add(getMethodEncrypt());
        }
        list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getMethodTransformToModel() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_OBJECT_TRANSFORM_S_TO_MODEL_OBJECT_ELEMENT, getKeyFormatted()));
        if (getIsEncrypted()) {
            list.add(getMethodDecrypt());
        }
        if (getIsTruncateMultipleSpaces()) {
            list.add(ELEMENT_STRING_ELEMENT_TRIM_REPLACE_ALL_S_2_G);
        }
        if (getDisplayMask() != null) {
            list.add(ELEMENT_MASKER_APPLY_ELEMENT);
        }
        list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }
}
