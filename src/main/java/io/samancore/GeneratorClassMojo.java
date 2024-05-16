package io.samancore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.samancore.component.Template;
import io.samancore.component.base.Field;
import io.samancore.util.GeneralConstant;
import io.samancore.util.GeneratorClass;
import io.samancore.util.JsonUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static io.samancore.GeneralUtil.validateLengthName;
import static io.samancore.util.GeneralConstant.MAX_LENGTH_NAME_ALLOWED;

@Mojo(name = "generate-code", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorClassMojo extends AbstractMojo {

    private static final GeneratorClass generator = new GeneratorClass();

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;


    public void execute() throws MojoExecutionException {
        String baseDirPath = project.getBasedir().getParent();
        getLog().info("baseDirPath:" + baseDirPath);

        getLog().info("project-GAST: " + project.getArtifactId());

        try {
            String absolutePath = Paths.get("").toAbsolutePath().toString();
            var templateResourcePath = absolutePath.concat("/").concat(GeneralConstant.TEMPLATE_FILE_NAME);
            getLog().info("resourcePath:" + templateResourcePath);
            File templateFile = new File(templateResourcePath);
            InputStream templateInputStream = new FileInputStream(templateFile);

            Properties templateProperties = new Properties();
            templateProperties.load(templateInputStream);

            var templateName = templateProperties.getProperty("templateName");
            var productName = templateProperties.getProperty("productName");

            validateLengthName(templateName, "template's name length should be max 20 characters");
            validateLengthName(productName, "product's name length should be max 20 characters");

            var templateBuilder = Template.newBuilder()
                    .setPackageName(templateProperties.getProperty("packageName"))
                    .setName(templateProperties.getProperty("templateName"))
                    .setProductName(templateProperties.getProperty("productName"));

            JsonMapper jsonMapper = JsonMapper.builder()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true)
                    .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                    .build();

            var jsonResourcePath = absolutePath.concat("/").concat(GeneralConstant.JSON_FILE_NAME);
            getLog().info("jsonResourcePath:" + jsonResourcePath);
            File jsonFile = new File(jsonResourcePath);
            JsonNode jsonForm = jsonMapper.readValue(jsonFile, JsonNode.class);
            getLog().info("jsonForm: " + jsonForm);

            ArrayNode components = (ArrayNode) jsonForm.get("components");
            List<Field> fieldList = JsonUtil.getFieldsOfComponent(productName, templateName, components);
            templateBuilder.setFields(fieldList);

            String module = null;
            if (project.getArtifactId().toLowerCase(Locale.ROOT).contains("model")) {
                module = "model";
            } else if (project.getArtifactId().toLowerCase(Locale.ROOT).contains("client")) {
                module = "client";
            } else if (project.getArtifactId().toLowerCase(Locale.ROOT).contains("application")) {
                module = "application";
            } else if (project.getArtifactId().toLowerCase(Locale.ROOT).contains("data")) {
                module = "data";
            }

            if (module != null) {
                List<GeneratorClass.OutputFile> outputFiles = generator.compile(templateBuilder.build(), module);

                String destinationPath = project.getBasedir().getPath().concat("/target/generated-sources/cde/java/");
                project.addCompileSourceRoot(destinationPath);
                getLog().info("destinationPath:" + destinationPath);
                for (GeneratorClass.OutputFile outputFile : outputFiles) {
                    File resourcesFileDestination = new File(destinationPath);
                    outputFile.writeToDestination(null, resourcesFileDestination);
                }
            }
        } catch (Exception error) {
            getLog().error("error", error);
            throw new MojoExecutionException(error.getMessage(), error);
        }
    }
}