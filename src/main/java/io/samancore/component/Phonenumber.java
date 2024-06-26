package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.type.CaseType;
import io.samancore.type.SensitiveDataMaskType;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
public class Phonenumber extends Component implements Field {
    private CaseType caseType = CaseType.NONE;
    private Boolean isTruncateMultipleSpaces = false;
    private SensitiveDataMaskType sensitiveDataMaskType = SensitiveDataMaskType.NONE;

    public Phonenumber(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
        this.caseType = JsonFormIoUtil.getCaseType(jsonNodeComponent);
        this.isTruncateMultipleSpaces = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, TRUNCATE_MULTIPLE_SPACES);
        setMaxLength(MAX_LENGTH_PHONE_NUMBER);
        this.sensitiveDataMaskType = JsonFormIoUtil.getSensitiveDataMaskTypePropertyFromNode(jsonNodeComponent, SENSITIVE_DATA_MASK);
    }

    @Override
    public List<String> getValidationToModel() {
        var validationList = new ArrayList<String>();
        if (getIsEncrypted()) {
            validationList.add(String.format(MAX_BYTE_VALUE_S, getEncryptType().getModelMaxLength()));
        }
        if (getIsRequired()) {
            validationList.add(NOT_BLANK);
            validationList.add(NOT_EMPTY);
        }
        if (getModelMaxLength() != null) {
            validationList.add(String.format(SIZE_MAX_D, getModelMaxLength()));
        }
        return validationList;
    }

    @Override
    public Boolean evaluateIfNeedPairToEntity() {
        return getIsTruncateMultipleSpaces() || getIsEncrypted();
    }

    @Override
    public Boolean evaluateIfNeedPairToModel() {
        return getIsTruncateMultipleSpaces() || !getCaseType().equals(CaseType.NONE) || getIsEncrypted() || getHasSensitiveDataMaskType();
    }

    @Override
    public List<String> getMethodTransformToEntity() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_ENTITY_S_ELEMENT, getObjectTypeToEntity(), getKeyFormatted(), getObjectTypeToModel()));
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
        String elementName = getElementNameAndAddMethodDecrypt(list);
        if (!getCaseType().equals(CaseType.NONE)) {
            list.add(String.format(S_S_TO_S_JAVA_UTIL_LOCALE_ROOT, elementName, elementName, getCaseType().getDescription()));
        }
        if (getIsTruncateMultipleSpaces()) {
            list.add(String.format(S_S_TRIM_REPLACE_ALL_S_2_G, elementName, elementName));
        }
        if (getHasSensitiveDataMaskType()) {
            list.add(String.format(S_MASKER_S_APPLY_S, elementName, getSensitiveDataMaskType().getDescriptionCapitalize(), elementName));
        }
        list.add(String.format(RETURN_S, elementName));
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getInjectToTransform() {
        return getHasSensitiveDataMaskType() ? List.of(String.format("@Named(\"%s\")", getSensitiveDataMaskType().getDescription()), String.format(IO_SAMANCORE_COMMON_TRANSFORMER_MASKER_MASKER_S, getSensitiveDataMaskType().getDescriptionCapitalize())) : List.of();
    }

    public boolean getHasSensitiveDataMaskType() {
        return !SensitiveDataMaskType.NONE.equals(sensitiveDataMaskType);
    }
}
