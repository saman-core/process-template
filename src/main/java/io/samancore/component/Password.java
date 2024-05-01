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
public class Password extends Component implements Field {

    private Boolean isTruncateMultipleSpaces = false;
    private CaseType caseType = CaseType.NONE;
    private String pattern = null;
    private String displayMask = null;
    private Integer minLength = null;
    private Integer maxLength = null;

    public Password(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.isTruncateMultipleSpaces = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, TRUNCATE_MULTIPLE_SPACES);
        this.displayMask = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DISPLAY_MASK);
        this.caseType = JsonFormIoUtil.getCaseType(jsonNodeComponent);
        this.pattern = JsonFormIoUtil.getPattern(jsonNodeComponent);
        this.minLength = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MIN_LENGTH);
        this.maxLength = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MAX_LENGTH);
        setIsEncrypted(true);
    }

    @Override
    public List<String> getValidationToModel() {
        var validation = new ArrayList<String>();
        if (getIsRequired()) {
            validation.add(NOT_BLANK);
            validation.add(NOT_EMPTY);
        }
        if (pattern != null) {
            validation.add(String.format("@Pattern(regexp = \"%s\")", pattern));
        }
        if (minLength != null && maxLength != null) {
            validation.add(String.format("@Size(min = %d, max = %d)", minLength, maxLength));
        } else if (minLength != null) {
            validation.add(String.format("@Size(min = %d)", minLength));
        } else if (maxLength != null) {
            validation.add(String.format("@Size(max = %d)", maxLength));
        }
        return validation;
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
        if (!getCaseType().equals(CaseType.NONE)) {
            list.add(String.format(ELEMENT_STRING_ELEMENT_TO_S_JAVA_UTIL_LOCALE_ROOT, getCaseType().getDescription()));
        }
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
        if (!getCaseType().equals(CaseType.NONE)) {
            list.add(String.format(ELEMENT_STRING_ELEMENT_TO_S_JAVA_UTIL_LOCALE_ROOT, getCaseType().getDescription()));
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
