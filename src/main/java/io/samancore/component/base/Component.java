package io.samancore.component.base;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.GeneralUtil;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static io.samancore.util.GeneralConstant.*;

@Getter
@Setter
public abstract class Component implements Input {

    private final String key;
    private final Boolean isPersistent;
    private final Boolean isRequired;
    private final Boolean isUnique;
    private final Boolean isProtected;
    private final Boolean hasDbIndex;
    private Boolean isEncrypted;

    public Component(JsonNode jsonNodeComponent) {
        this.key = JsonFormIoUtil.getKey(jsonNodeComponent);
        this.isPersistent = JsonFormIoUtil.isPersistent(jsonNodeComponent);
        this.isUnique = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, UNIQUE);
        this.hasDbIndex = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, DB_INDEX);
        this.isProtected = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, PROTECTED);
        this.isRequired = JsonFormIoUtil.isRequired(jsonNodeComponent);
        this.isEncrypted = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, ENCRYPTED);
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        if (getIsUnique() || getIsRequired()) {
            String unique = UNIQUE_TRUE;
            String required = NULLABLE_FALSE;
            String columnDescription = COLUMN;
            if (getIsUnique()) {
                columnDescription = columnDescription.concat(unique);
            }
            if (getIsRequired()) {
                columnDescription = columnDescription.concat(required);
            }
            return List.of(columnDescription.substring(0, columnDescription.lastIndexOf(",")).concat(")"));
        }
        return List.of();
    }

    public String getKeyFormatted() {
        return GeneralUtil.mangleTypeIdentifier(getKey());
    }

    public String getKeyMangle() {
        return GeneralUtil.mangle(getKey());
    }

    @Override
    public Boolean evaluateIfNeedPairToEntity() {
        return getIsEncrypted();
    }

    @Override
    public Boolean evaluateIfNeedPairToModel() {
        return getIsEncrypted();
    }

    @Override
    public List<String> getInjectToTransform() {
        return List.of();
    }

    @Override
    public String getMethodEncrypt() {
        return "element = encrypt.encrypt(element);";
    }

    @Override
    public String getMethodDecrypt() {
        return "element = encrypt.decrypt(element);";
    }


    @Override
    public String getPairTransformToEntity() {
        String pairDefinition = "var pair%s = org.apache.commons.lang3.tuple.Pair.of(\"%s\", (java.util.function.Function<%s, ?>) _%s -> _%s!=null ? transform%sToEntity(_%s) : null);";
        String keyFormatted = getKeyFormatted();
        return String.format(pairDefinition, keyFormatted, getKey(), getObjectTypeToEntity(), keyFormatted, keyFormatted, keyFormatted, keyFormatted);
    }

    @Override
    public String getObjectTypeToEntity() {
        return getIsEncrypted() ? DATA_TYPE_BYTEA : DATA_TYPE_STRING;
    }

    @Override
    public String getPairTransformToModel() {
        String pairDefinition = "var pair%s = org.apache.commons.lang3.tuple.Pair.of(\"%s\", (java.util.function.Function<%s, ?>) _%s -> _%s!=null ? transform%sToModel(_%s) : null);";
        String keyFormatted = getKeyFormatted();
        return String.format(pairDefinition, keyFormatted, getKey(), getObjectTypeToModel(), keyFormatted, keyFormatted, keyFormatted, keyFormatted);
    }

    @Override
    public String getObjectTypeToModel() {
        return DATA_TYPE_STRING;
    }

    @Override
    public List<String> getMethodTransformToEntity() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_OBJECT_TRANSFORM_S_TO_ENTITY_OBJECT_ELEMENT, getKeyFormatted()));
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
        list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }
}
