package io.samancore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.samancore.model.Field;
import io.samancore.model.Template;
import io.samancore.util.GeneratorUtil;
import io.samancore.util.JsonUtil;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

public class Runner {

    public static final String COMPONENTS = "components";
    private static GeneratorUtil generator = new GeneratorUtil();

    @SneakyThrows
    public static void main(String[] args) {
        try {
            String absolutePath = Paths.get("").toAbsolutePath().toString();
            var templateResourcePath = absolutePath.concat("/").concat(ConstantUtil.TEMPLATE_FILE_NAME);
            File templateFile = new File(templateResourcePath);
            InputStream templateInputStream = new FileInputStream(templateFile);

            Properties templateProperties = new Properties();
            templateProperties.load(templateInputStream);

            var templateBuilder = Template.newBuilder()
                    .setPackageName(templateProperties.getProperty("packageName"))
                    .setName(templateProperties.getProperty("templateName"))
                    .setProductName(templateProperties.getProperty("productName"));

            JsonMapper jsonMapper = JsonMapper.builder()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true)
                    .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                    .build();

            var jsonResourcePath = absolutePath.concat("/").concat(ConstantUtil.JSON_FILE_NAME);
            File jsonFile = new File(jsonResourcePath);
            JsonNode jsonForm = jsonMapper.readValue(jsonFile, JsonNode.class);

            ArrayNode components = (ArrayNode) jsonForm.get(COMPONENTS);

            List<Field> fieldList = JsonUtil.getFieldsOfComponent(components);
            templateBuilder.setFields(fieldList);

            String module = "model";
            List<GeneratorUtil.OutputFile> outputFiles = generator.compile(templateBuilder.build(), module);
            String destinationPath = "src/main/java/";
            for (GeneratorUtil.OutputFile outputFile : outputFiles) {
                File resourcesFileDestination = new File(destinationPath);
                outputFile.writeToDestination( null, resourcesFileDestination);
            }
        } catch (Exception error) {
            throw new MojoExecutionException(error.getMessage(), error);
        }
    }

}
