package io.samancore.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.samancore.type.CaseType;

public class Textarea extends Textfield {

    public Textarea(CaseType columnCaseSensitive, JsonNode jsonNodeComponent) {
        super(columnCaseSensitive, jsonNodeComponent);
    }
}
