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
public class Textfield extends Component implements Field {
    private Boolean isTruncateMultipleSpaces = false;
    private CaseType caseType = CaseType.NONE;
    private String pattern = null;
    private String displayMask = null;
    private Integer minLength = null;
    private Integer maxLength = null;
    private Integer minWords = null;
    private Integer maxWords = null;

    public Textfield(JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.isTruncateMultipleSpaces = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, TRUNCATE_MULTIPLE_SPACES);
        this.displayMask = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DISPLAY_MASK);
        this.pattern = JsonFormIoUtil.getPattern(jsonNodeComponent);
        this.caseType = JsonFormIoUtil.getCaseType(jsonNodeComponent);
        this.minLength = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MIN_LENGTH);
        this.maxLength = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MAX_LENGTH);
        this.minWords = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MIN_WORDS);
        this.maxWords = JsonFormIoUtil.getIntegerPropertyFromValidate(jsonNodeComponent, MAX_WORDS);
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
        if (minWords != null && maxWords != null) {
            validation.add(String.format("@WordLimit(min = %d, max = %d)", minWords, maxWords));
        } else if (minWords != null) {
            validation.add(String.format("@WordLimit(min = %d)", minWords));
        } else if (maxWords != null) {
            validation.add(String.format("@WordLimit(max = %d)", maxWords));
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
        list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_ENTITY_S_ELEMENT, getObjectTypeToEntity(), getKeyFormatted(), getObjectTypeToModel()));
        if (!getCaseType().equals(CaseType.NONE)) {
            list.add(String.format(ELEMENT_STRING_ELEMENT_TO_S_JAVA_UTIL_LOCALE_ROOT, getCaseType().getDescription()));
        }
        if (getIsTruncateMultipleSpaces()) {
            list.add(ELEMENT_STRING_ELEMENT_TRIM_REPLACE_ALL_S_2_G);
        }
        if (getIsEncrypted()) {
            list.add(getMethodEncrypt());
        } else list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getMethodTransformToModel() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_MODEL_S_ELEMENT, getObjectTypeToModel(), getKeyFormatted(), getObjectTypeToEntity()));
        var object = "element";
        if (getIsEncrypted()) {
            list.add(getMethodDecrypt());
            object = "newElement";
        }
        if (!getCaseType().equals(CaseType.NONE)) {
            list.add(String.format(S_S_TO_S_JAVA_UTIL_LOCALE_ROOT, object, object, getCaseType().getDescription()));
        }
        if (getIsTruncateMultipleSpaces()) {
            list.add(String.format(S_S_TRIM_REPLACE_ALL_S_2_G, object, object));
        }
        if (getDisplayMask() != null) {
            list.add(String.format(S_MASKER_APPLY_S, object, object));
        }
        list.add(String.format(RETURN_S, object));
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getInjectToTransform() {
        return getDisplayMask() != null ? List.of("io.samancore.common.transformer.Masker masker;") : List.of();
    }

}
