package io.samancore.component.base;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.GeneralUtil;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        var columnDescription = String.format(COLUMN_NAME_S, getKeyToColumn());
        if (getIsUnique() || getIsRequired()) {
            columnDescription = columnDescription.concat(", ");
            if (getIsUnique()) {
                columnDescription = columnDescription.concat(UNIQUE_TRUE);
            }
            if (getIsRequired()) {
                columnDescription = columnDescription.concat(NULLABLE_FALSE);
            }
            columnDescription = columnDescription.substring(0, columnDescription.lastIndexOf(",")).concat(")");
        } else columnDescription = columnDescription.concat(")");
        return List.of(columnDescription);
    }

    public String getKeyFormatted() {
        return GeneralUtil.mangleTypeIdentifier(getKey());
    }

    public String getKeyMangle() {
        return GeneralUtil.mangle(getKey());
    }

    public String getKeyLowerCase() {
        return getKey().toLowerCase(Locale.ROOT);
    }

    public String getKeyToColumn() {
        return getKey().toLowerCase(Locale.ROOT).concat(DEFAULT_NAME_NUMBER);
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
        return "return encrypt.encrypt(element);";
    }

    @Override
    public String getMethodDecrypt() {
        return "var newElement = encrypt.decrypt(element);";
    }


    @Override
    public String getPairTransformToEntity() {
        String pairDefinition = "var pair%s = org.apache.commons.lang3.tuple.Pair.of(\"%s\", (java.util.function.Function<%s, ?>) _%s -> _%s!=null ? transform%sToEntity(_%s) : null);";
        String keyFormatted = getKeyMangle();
        return String.format(pairDefinition, getKeyFormatted(), getKey(), getObjectTypeToModel(), keyFormatted, keyFormatted, getKeyFormatted(), keyFormatted);
    }

    @Override
    public String getObjectTypeToEntity() {
        return getIsEncrypted() ? DATA_TYPE_BYTEA : DATA_TYPE_STRING;
    }

    @Override
    public String getPairTransformToModel() {
        String pairDefinition = "var pair%s = org.apache.commons.lang3.tuple.Pair.of(\"%s\", (java.util.function.Function<%s, ?>) _%s -> _%s!=null ? transform%sToModel(_%s) : null);";
        String keyFormatted = getKeyMangle();
        return String.format(pairDefinition, getKeyFormatted(), getKey(), getObjectTypeToEntity(), keyFormatted, keyFormatted, getKeyFormatted(), keyFormatted);
    }

    @Override
    public String getObjectTypeToModel() {
        return DATA_TYPE_STRING;
    }

    @Override
    public List<String> getMethodTransformToEntity() {
        var list = new ArrayList<String>();
        list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_ENTITY_S_ELEMENT, getObjectTypeToEntity(), getKeyFormatted(), getObjectTypeToModel()));
        if (getIsEncrypted()) {
            list.add(getMethodEncrypt());
        } else list.add(RETURN_ELEMENT);
        list.add(CLOSE_KEY);
        return list;
    }

    @Override
    public List<String> getMethodTransformToModel() {
        var list = new ArrayList<String>();
        if (getIsEncrypted()) {
            list.add(String.format(PRIVATE_S_TRANSFORM_S_TO_MODEL_S_ELEMENT, getObjectTypeToModel(), getKeyFormatted(), getObjectTypeToEntity()));
            list.add(getMethodDecrypt());
            list.add("return newElement;");
            list.add(CLOSE_KEY);
        }
        return list;
    }

    @Override
    public Boolean evaluateIfNeedDefineIndex() {
        return isPersistent && !isEncrypted && hasDbIndex;
    }

    @Override
    public Boolean evaluateIfNeedDefineFilter() {
        return isPersistent && !isEncrypted && hasDbIndex;
    }

    @Override
    public String getConversionFromStringToObjectType(String value) {
        return String.format("%s.getFirst(\"%s\")", value, key);
    }
}
