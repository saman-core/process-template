package io.samancore.component.base;

import com.fasterxml.jackson.databind.JsonNode;
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
    private String productName = null;
    private String templateName = null;
    private String dataSrc = null;

    public Multivalue(String productName, String templateName, JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.isMultiple = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, MULTIPLE);
        this.productName = productName;
        this.templateName = templateName;
        this.dataSrc = JsonFormIoUtil.getStringPropertyFromNode(jsonNodeComponent, DATA_SRC);
    }

    @Override
    public String getKeyToColumn() {
        var name = super.getKeyToColumn();
        if (getDataSrc() != null && getDataSrc().equalsIgnoreCase(DATA_SRC_RESOURCE)) {
            name = getKeyLowerCase().concat(COLUMN_ID);
        }
        return name;
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        var allAnnotations = new ArrayList<String>();
        if (getIsMultiple()) {
            var name = getKeyToColumn();
            allAnnotations.add("@ElementCollection(fetch = FetchType.EAGER)");
            var indexDesc = "";
            if (getHasDbIndex()) {
                indexDesc = String.format(", indexes = {@Index(columnList = \"%s\")}", name);
            }
            allAnnotations.add(String.format("@CollectionTable(name = \"%s_%s__%s\", joinColumns = @JoinColumn(name = \"%s_id\", nullable = false), uniqueConstraints = @UniqueConstraint(columnNames = {\"%s_id\", \"%s\"})".concat(indexDesc).concat(")"), productName.toLowerCase(Locale.ROOT), templateName.toLowerCase(Locale.ROOT), getKeyLowerCase(), templateName.toLowerCase(Locale.ROOT), templateName.toLowerCase(Locale.ROOT), name));
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
}
