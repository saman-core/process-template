package com.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.generator.util.GeneratorUtil;
import com.generator.util.JsonUtil;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

public class Runner {

    public static final String COMPONENTS = "components";
    private static GeneratorUtil generator = new GeneratorUtil();
    private static JsonUtil jsonUtil = new JsonUtil();


    @SneakyThrows
    public static void main(String[] args) throws IOException {
        try {

            String absolutePath = Paths.get("").toAbsolutePath().toString();
            String resourcePath = absolutePath.concat("/src/main/resources/jsonBody2.tar");

            File file = new File(resourcePath);

            JsonMapper jsonMapper = JsonMapper.builder()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true)
                    .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                    .build();

            JsonNode json = jsonMapper.readValue(file, JsonNode.class);
            Template template = new Template();
            template.setPackageName(json.get("packageName").textValue());
            template.setName(json.get("name").textValue());

            ArrayNode components = (ArrayNode) json.get(COMPONENTS);
            List<Field> fieldList = jsonUtil.getFieldsOfComponent(components);
            template.setFields(fieldList);

            String module = "application";
            if (module != null) {
                List<GeneratorUtil.OutputFile> outputFiles = generator.compile(template, module);
                String destinationPath = absolutePath.concat("/src/main/java/");
                for (GeneratorUtil.OutputFile outputFile : outputFiles) {
                    File resourcesFileDestination = new File(destinationPath);
                    outputFile.writeToDestination(null, resourcesFileDestination);
                }
            }
        } catch (Exception error) {
            throw new MojoExecutionException(error.getMessage(), error);
        }
    }

}
