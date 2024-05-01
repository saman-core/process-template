package io.samancore.component.base;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.util.JsonFormIoUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.samancore.util.GeneralConstant.MULTIPLE;

@Setter
@Getter
public abstract class Multivalue extends Component {

    private Boolean isMultiple;
    private String productName = null;
    private String templateName = null;

    public Multivalue(String productName, String templateName, JsonNode jsonNodeComponent) {
        super(jsonNodeComponent);
        this.isMultiple = JsonFormIoUtil.getBooleanPropertyFromNode(jsonNodeComponent, MULTIPLE);
        this.productName = productName;
        this.templateName = templateName;
    }

    @Override
    public List<String> getAllAnnotationToEntity() {
        var allAnnotations = new ArrayList<String>();
        if (getIsMultiple()) {
            allAnnotations.add("@ElementCollection(fetch = FetchType.LAZY)");
            allAnnotations.add(String.format("@CollectionTable(name = \"%s_%s__%s\", joinColumns = @JoinColumn(name = \"%s_id\", nullable = false), uniqueConstraints = @UniqueConstraint(columnNames = {\"%s_id\", \"%s\"}))", productName.toLowerCase(Locale.ROOT), templateName.toLowerCase(Locale.ROOT), getKey().toLowerCase(Locale.ROOT), templateName.toLowerCase(Locale.ROOT), templateName.toLowerCase(Locale.ROOT), getKey().toLowerCase(Locale.ROOT)));
        }
        return allAnnotations;
    }
}
