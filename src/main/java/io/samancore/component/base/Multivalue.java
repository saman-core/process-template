package io.samancore.component.base;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.type.CaseType;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.samancore.util.GeneralConstant.*;

@Setter
@Getter
public abstract class Multivalue extends Component {

    private Boolean isMultiple = false;
    private String moduleName = null;
    private String productName = null;
    private String templateName = null;
    private String dataSrc = null;

    public Multivalue(String moduleName, String productName, String templateName, CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
        this.isMultiple = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, MULTIPLE);
        this.moduleName = moduleName;
        this.productName = productName;
        this.templateName = templateName;
        this.dataSrc = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DATA_SRC);
    }

    @Override
    public String getKeyToColumn() {
        var nameColumn = super.getKeyToColumn();
        if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            nameColumn = getKey().concat(COLUMN_ID);
        }
        if (getDbElementCaseSensitive().equals(CaseType.LOWERCASE)) {
            return nameColumn.toLowerCase(Locale.ROOT);
        }
        return nameColumn.toUpperCase(Locale.ROOT);
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        var allAnnotations = new ArrayList<String>();
        if (getIsMultiple()) {
            var name = getKeyToColumn();
            var tableName = moduleName.concat(UNDERSCORE).concat(UNDERSCORE).concat(productName).concat(UNDERSCORE).concat(templateName).concat(UNDERSCORE).concat(UNDERSCORE).concat(getKey());
            var columnId = templateName.concat(UNDERSCORE).concat("id");
            if (getDbElementCaseSensitive().equals(CaseType.UPPERCASE)) {
                tableName = tableName.toUpperCase(Locale.ROOT);
                name = name.toUpperCase(Locale.ROOT);
                columnId = columnId.toUpperCase(Locale.ROOT);
            } else {
                tableName = tableName.toLowerCase(Locale.ROOT);
                name = name.toLowerCase(Locale.ROOT);
                columnId = columnId.toLowerCase(Locale.ROOT);
            }
            allAnnotations.add("@ElementCollection(fetch = FetchType.EAGER)");
            var indexDesc = "";
            if (getHasDbIndex()) {
                indexDesc = String.format(", indexes = {@Index(columnList = \"%s\")}", name);
            }
            allAnnotations.add(String.format("@CollectionTable(name = \"%s\", joinColumns = @JoinColumn(name = \"%s\", nullable = false), uniqueConstraints = @UniqueConstraint(columnNames = {\"%s\", \"%s\"})".concat(indexDesc).concat(")"), tableName, columnId, columnId, name));
            allAnnotations.add(String.format(COLUMN_NAME_S, name).concat(")"));
        } else {
            allAnnotations.add(String.format(COLUMN_NAME_S, getKeyToColumn()).concat(")"));
        }

        return allAnnotations;
    }

    @Override
    public Boolean evaluateIfNeedDefineIndex() {
        return !isMultiple && super.evaluateIfNeedDefineIndex();
    }

    @Override
    public String getConversionFromStringToObjectType(String value) {
        return String.format(" %s.get(\"%s\").stream().collect(java.util.stream.Collectors.toSet())", value, getKey());
    }

    @Override
    public Boolean evaluateIfFilterNeedDefineJoin() {
        return isMultiple;
    }
}
