package io.samancore.component;

import io.samancore.component.base.Component;
import io.samancore.component.base.Field;
import io.samancore.component.base.Input;
import io.samancore.component.base.Multivalue;
import io.samancore.type.CaseType;
import io.samancore.util.GeneralConstant;
import io.samancore.validation.Validation;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static io.samancore.util.GeneralConstant.*;


@Getter
public class Template extends Validation {

    private String packageName;
    private String name;
    private String productName;
    private String moduleName;
    private CaseType dbElementCaseSensitive;
    private List<Field> fields;

    public Template(String packageName, String name, String productName, String moduleName, CaseType dbElementCaseSensitive) {
        this.name = name;
        validateIfNameIsAReservedWord(this.name);
        validateLengthName(this.name, MAX_LENGTH_NAME_ALLOWED,String.format(S_NAME_LENGTH_SHOULD_BE_MAX_S_CHARACTERS, "Template", MAX_LENGTH_NAME_ALLOWED));
        this.productName = productName;
        validateLengthName(this.productName, MAX_LENGTH_NAME_ALLOWED, String.format(S_NAME_LENGTH_SHOULD_BE_MAX_S_CHARACTERS, "Product", MAX_LENGTH_NAME_ALLOWED));
        this.moduleName = moduleName;
        validateLengthName(this.moduleName, MAX_LENGTH_MODULE_NAME_ALLOWED, String.format(S_NAME_LENGTH_SHOULD_BE_MAX_S_CHARACTERS, "Module", MAX_LENGTH_MODULE_NAME_ALLOWED));
        this.packageName = packageName;
        this.dbElementCaseSensitive = dbElementCaseSensitive;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getPackageWithProduct() {
        var packageComplete = packageName.concat(".").concat(moduleName).concat(".").concat(productName);
        return packageComplete.toLowerCase(Locale.ROOT);
    }

    public String getPackageComplete() {
        var packageComplete = packageName.concat(".").concat(moduleName).concat(".").concat(productName).concat(".").concat(name);
        return packageComplete.toLowerCase(Locale.ROOT);
    }

    public String getFetchFromFieldIsCollection() {
        var mutinyFetch = ".onItem().call(entity -> Mutiny.fetch(entity.get%s()))";
        return getAllFieldToPersist().stream().filter(field -> field instanceof Multivalue && ((Multivalue) field).getIsMultiple())
                .map(field -> String.format(mutinyFetch, field.getKeyCapitalize()))
                .collect(Collectors.joining(""));
    }

    public Boolean validateIfAnyFieldHasValidation() {
        return fields.stream().anyMatch(field -> !field.getValidationToModel().isEmpty());
    }

    public Boolean evaluateIfAnyFieldIsIndex() {
        return fields.stream().anyMatch(Field::evaluateIfNeedDefineIndex);
    }

    public Boolean evaluateIfAnyFieldNeedFilter() {
        return fields.stream().anyMatch(Field::evaluateIfNeedDefineFilter);
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

    public List<Field> getAllFieldToFilter() {
        return getAllFieldToPersist().stream()
                .filter(Input::evaluateIfNeedDefineFilter)
                .toList();
    }

    public List<Field> getAllFieldToPersist() {
        return fields.stream().filter(Field::getIsPersistent).toList();
    }

    public String getNameCapitalize() {
        return StringUtils.capitalize(name);
    }

    public String getNameLowerCase() {
        return name.toLowerCase(Locale.ROOT);
    }

    public String getProductNameLowerCase() {
        return productName.toLowerCase(Locale.ROOT);
    }

    public String getModuleNameLowerCase() {
        return moduleName.toLowerCase(Locale.ROOT);
    }

    public String getRestClientConfigKey() {
        var configKey = "cde-".concat(moduleName).concat("-").concat(productName).concat("-").concat(name).concat("-").concat("api");
        return configKey.toLowerCase(Locale.ROOT);
    }

    public Boolean evaluateIfAnyFieldIsMultiple() {
        return getAllFieldToPersist().stream().anyMatch(field -> field instanceof Multivalue);
    }

    public Boolean evaluateIfAnyFieldIsEncrypted() {
        return getAllFieldToPersist().stream().anyMatch(field -> ((Component) field).getIsEncrypted());
    }

    public List<String> getInjectToTransform() {
        var injectTransforms = new ArrayList<String>();
        var allToPersist = getAllFieldToPersist();
        if (allToPersist.stream().anyMatch(field -> ((Component) field).getIsEncrypted())) {
            injectTransforms.add("@Inject");
            injectTransforms.add("io.samancore.common.transformer.Encrypt encrypt;");
        }
        allToPersist.forEach(field -> {
            field.getInjectToTransform().stream().filter(inject -> !injectTransforms.contains(inject))
                    .forEach(inject -> {
                        if (inject.startsWith("@")) {
                            injectTransforms.add("@Inject");
                            injectTransforms.add(inject);
                        } else injectTransforms.add(inject);
                    });
        });
        return injectTransforms;
    }

    public Boolean evaluateIfAnyFieldNeedPairToEntity() {
        return getAllFieldToPersist().stream().anyMatch(Input::evaluateIfNeedPairToEntity);
    }

    public Boolean evaluateIfAnyFieldNeedPairToModel() {
        return getAllFieldToPersist().stream().anyMatch(Input::evaluateIfNeedPairToModel);
    }

    public List<Field> getAllFieldWithPairToEntity() {
        return getAllFieldToPersist().stream().filter(Input::evaluateIfNeedPairToEntity).toList();
    }

    public List<Field> getAllFieldWithPairToModel() {
        return getAllFieldToPersist().stream().filter(Input::evaluateIfNeedPairToModel).toList();
    }

    public String getTransformToEntity() {
        var transformDefinition = "return transformToEntity(model, %s);";
        return getMethodToTransformToEntity(transformDefinition);
    }

    public String getTransformToModel() {
        var transformDefinition = "return transformToModel(entity, %s);";
        return getMethodToTransformToModel(transformDefinition);
    }

    public String getIdColumn() {
        var column = "ID";
        if (dbElementCaseSensitive.equals(CaseType.UPPERCASE)) {
            return column.toUpperCase(Locale.ROOT);
        }
        return column.toLowerCase(Locale.ROOT);
    }

    public String getTableName() {
        var tableName= PREFIX_TABLENAME_CE.concat(moduleName).concat(UNDERSCORE).concat(productName).concat(UNDERSCORE).concat(name);
        if (dbElementCaseSensitive.equals(CaseType.UPPERCASE)) {
            return tableName.toUpperCase(Locale.ROOT);
        }
        return tableName.toLowerCase(Locale.ROOT);
    }

    public String getSequenceName() {
        var tableName= PREFIX_SEQUENCENAME_SQ.concat(moduleName).concat(UNDERSCORE).concat(productName).concat(UNDERSCORE).concat(name);
        if (dbElementCaseSensitive.equals(CaseType.UPPERCASE)) {
            return tableName.toUpperCase(Locale.ROOT);
        }
        return tableName.toLowerCase(Locale.ROOT);
    }

    private String getMethodToTransformToEntity(String transformDefinition) {
        var allPair = getAllFieldToPersist().stream()
                .filter(Input::evaluateIfNeedPairToEntity)
                .map(field -> String.format(GeneralConstant.PAIR_S, field.getKeyFormatted()))
                .collect(Collectors.joining(", "));
        return String.format(transformDefinition, allPair);
    }

    private String getMethodToTransformToModel(String transformDefinition) {
        var allPair = getAllFieldToPersist().stream()
                .filter(Input::evaluateIfNeedPairToModel)
                .map(field -> String.format(GeneralConstant.PAIR_S, field.getKeyFormatted()))
                .collect(Collectors.joining(", "));
        return String.format(transformDefinition, allPair);
    }
}
