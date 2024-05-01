package io.samancore.component.base;

import java.util.List;

public interface Input {
    List<String> getAllAnnotationToEntity();

    Boolean evaluateIfNeedPairToEntity();

    Boolean evaluateIfNeedPairToModel();

    List<String> getInjectToTransform();

    String getMethodEncrypt();

    String getMethodDecrypt();

    String getPairTransformToEntity();

    String getObjectTypeToEntity();

    String getPairTransformToModel();

    String getObjectTypeToModel();

    List<String> getMethodTransformToEntity();

    List<String> getMethodTransformToModel();
}
