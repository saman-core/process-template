package io.samancore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.samancore.model.Field;
import io.samancore.model.Template;
import io.samancore.util.GeneratorUtil;
import io.samancore.util.JsonUtil;
import io.samancore.util.MongoUtil;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

public class Runner {

    public static final String COMPONENTS = "components";
    private static GeneratorUtil generator = new GeneratorUtil();
    private static MongoUtil mongoUtil = new MongoUtil();

    @SneakyThrows
    public static void main(String[] args) {
        try {

            String absolutePath = Paths.get("").toAbsolutePath().toString();
            String resourcePath = absolutePath.concat("/src/main/resources/".concat(ConstantUtil.JSON_FILE_NAME));

            File file = new File(resourcePath);

            JsonMapper jsonMapper = JsonMapper.builder()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true)
                    .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                    .build();

            JsonNode json = jsonMapper.readValue(file, JsonNode.class);
            var templateBuilder = Template.newBuilder()
                    .setPackageName(json.get("packageName").textValue())
                    .setName(json.get("name").textValue())
                    .setProductName(json.get("productName").textValue());

            ArrayNode components = (ArrayNode) json.get(COMPONENTS);
            List<Field> fieldList = JsonUtil.getFieldsOfComponent(components);
            templateBuilder.setFields(fieldList);

            String module = "application";
            List<GeneratorUtil.OutputFile> outputFiles = generator.compile(templateBuilder.build(), module);
            String destinationPath = "src/main/java/";
            for (GeneratorUtil.OutputFile outputFile : outputFiles) {
                File resourcesFileDestination = new File(destinationPath);
                outputFile.writeToDestination( null, resourcesFileDestination);
            }

            mongoUtil.execute(json);
        } catch (Exception error) {
            throw new MojoExecutionException(error.getMessage(), error);
        }
    }

}
