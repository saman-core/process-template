package io.samancore.component.base;

import java.util.List;

public interface Field extends Input {

    String getKey();

    Boolean getIsPersistent();

    Boolean getHasDbIndex();

    List<String> getValidationToModel();

    String getPairTransformToEntity();

    String getKeyFormatted();

    String getPairTransformToModel();
}
