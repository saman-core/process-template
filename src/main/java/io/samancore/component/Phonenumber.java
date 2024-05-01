package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.component.type.CaseType;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Phonenumber extends Component implements Field {
    private CaseType caseType = CaseType.NONE;
    private Boolean isTruncateMultipleSpaces = false;
    private String displayMask = null;

    public Phonenumber(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.caseType = JsonFormIoUtil.getCaseType(jsonNodeComponent);
        this.isTruncateMultipleSpaces = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, TRUNCATE_MULTIPLE_SPACES);
        this.displayMask = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DISPLAY_MASK);
    }

    @Override
    public List<String> getValidationToModel() {
        return getIsRequired() ? List.of(NOT_BLANK, NOT_EMPTY) : List.of();
    }


    @Override
    public Boolean evaluateIfNeedPairToEntity() {
        return getIsTruncateMultipleSpaces() || !getCaseType().equals(CaseType.NONE) || getIsEncrypted();
    }

    @Override
    public Boolean evaluateIfNeedPairToModel() {
        return getIsTruncateMultipleSpaces() || !getCaseType().equals(CaseType.NONE) || getIsEncrypted() || getDisplayMask() != null;
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
        list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }
}
