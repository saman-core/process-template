package io.samancore.component;

import io.samancore.GeneralUtil;
import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.component.base.Input;
import io.samancore.component.base.Multivalue;
import io.samancore.util.GeneralConstant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Jacksonized
@Builder(
        setterPrefix = "set",
        builderMethodName = "newBuilder",
        toBuilder = true
)
public class Template {

    String packageName;
    String name;
    String productName;
    List<Field> fields;

    public String getFetchFromFieldIsCollection() {
        var mutinyFetch = ".onItem().call(entity -> Mutiny.fetch(entity.get%s()))";
        return getAllFieldToPersist().stream().filter(field -> field instanceof Multivalue && ((Multivalue) field).getIsMultiple())
                .map(field -> String.format(mutinyFetch, GeneralUtil.mangleTypeIdentifier(field.getKey())))
                .collect(Collectors.joining(""));
    }

    public Boolean validateIfAnyFieldHasValidation() {
        return fields.stream().anyMatch(field -> !field.getValidationToModel().isEmpty());
    }

    public Boolean evaluateIfAnyFieldIsIndex() {
        return fields.stream().anyMatch(Field::evaluateIfNeedDefineIndex);
    }

    public String getAllIndexFromField() {
        String indexDefinition = "@Index(columnList = \"%s\")";
        return getAllFieldToPersist().stream()
                .filter(Input::evaluateIfNeedDefineIndex)
                .map(field -> String.format(indexDefinition, ((Component) field).getKeyToColumn()))
                .collect(Collectors.joining(", "));
    }

    public List<Field> getAllFieldIndexed() {
        return getAllFieldToPersist().stream()
                .filter(Input::evaluateIfNeedDefineIndex)
                .toList();
    }

    public List<Field> getAllFieldToPersist() {
        return fields.stream().filter(Field::getIsPersistent).toList();
    }

    public String getPackageNameFormatted() {
        return GeneralUtil.mangle(packageName);
    }

    public String getNameFormatted() {
        return GeneralUtil.mangleTypeIdentifier(name);
    }

    public String getNameFormattedLowerCase() {
        return GeneralUtil.mangleTypeIdentifier(name).toLowerCase(Locale.ROOT);
    }

    public String getProductNameFormatted() {
        return GeneralUtil.mangleTypeIdentifier(productName);
    }

    public String getProductNameFormattedLowerCase() {
        return GeneralUtil.mangleTypeIdentifier(productName).toLowerCase(Locale.ROOT);
    }

    public Boolean evaluateIfAnyFieldIsMultiple() {
        return getAllFieldToPersist().stream().anyMatch(field -> field instanceof Multivalue);
    }

    public Boolean evaluateIfAnyFieldIsEncrypted() {
        return getAllFieldToPersist().stream().anyMatch(field -> ((Component) field).getIsEncrypted());
    }

    public Set<String> getInjectToTransform() {
        var injectTransforms = new ArrayList<String>();
        var allToPersist = getAllFieldToPersist();
        if (allToPersist.stream().anyMatch(field -> ((Component) field).getIsEncrypted())) {
            injectTransforms.add("io.samancore.common.transformer.Encrypt encrypt;");
        }
        allToPersist.forEach(field -> injectTransforms.addAll(field.getInjectToTransform()));
        var injectNotRepeated = injectTransforms.stream().collect(Collectors.groupingBy(s -> s));
        return injectNotRepeated.keySet();
    }

    public Boolean evaluateIfAnyFieldNeedPairToEntity() {
        return getAllFieldToPersist().stream().anyMatch(Input::evaluateIfNeedPairToEntity);
    }

    public Boolean evaluateIfAnyFieldNeedPairToModel() {
        return getAllFieldToPersist().stream().anyMatch(Input::evaluateIfNeedPairToModel);
    }

    public List<Field> getAllFieldWithPair() {
        return getAllFieldToPersist().stream().filter(Input::evaluateIfNeedPairToEntity).toList();
    }

    public String getTransformToEntity() {
        var transformDefinition = "return transformToEntity(model, %s);";
        return getMethodToTransform(transformDefinition);
    }

    public String getTransformToModel() {
        var transformDefinition = "return transformToModel(entity, %s);";
        return getMethodToTransform(transformDefinition);
    }

    private String getMethodToTransform(String transformDefinition) {
        var allPair = getAllFieldToPersist().stream()
                .filter(Input::evaluateIfNeedPairToEntity)
                .map(field -> String.format(GeneralConstant.PAIR_S, field.getKeyFormatted()))
                .collect(Collectors.joining(", "));
        return String.format(transformDefinition, allPair);
    }
}
