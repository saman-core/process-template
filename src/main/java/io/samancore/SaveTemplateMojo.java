package io.samancore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.samancore.util.MongoUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

@Mojo(name = "save-template", defaultPhase = LifecyclePhase.COMPILE)
public class SaveTemplateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    MongoUtil mongoUtil = new MongoUtil();

    @Override
    public void execute() throws MojoExecutionException {
        String baseDirPath = project.getBasedir().getParent();
        getLog().info("baseDirPath:" + baseDirPath);

        try {
            String absolutePath = Paths.get("").toAbsolutePath().toString();
            var resourcePathJsonBody = absolutePath.concat("/").concat(ConstantUtil.JSON_FILE_NAME);

            File jsonFile = new File(resourcePathJsonBody);
            JsonMapper jsonMapper = JsonMapper.builder()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true)
                    .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                    .build();

            JsonNode json = jsonMapper.readValue(jsonFile, JsonNode.class);

            Properties templateProps = getTemplateProperties();
            ((ObjectNode) json).put("packageName", templateProps.getProperty("packageName"));
            ((ObjectNode) json).put("templateName", templateProps.getProperty("templateName"));
            ((ObjectNode) json).put("productName", templateProps.getProperty("productName"));

            if (project.getArtifactId().toLowerCase(Locale.ROOT).contains("application")) {
                mongoUtil.execute(json);
            }
        } catch (Exception error) {
            getLog().error("error", error);
            throw new MojoExecutionException(error.getMessage(), error);
        }
    }

    private Properties getTemplateProperties() throws IOException {
        String absolutePath = Paths.get("").toAbsolutePath().toString();
        var templateResourcePath = absolutePath.concat("/").concat(ConstantUtil.TEMPLATE_FILE_NAME);
        File templateFile = new File(templateResourcePath);
        try (InputStream templateInputStream = new FileInputStream(templateFile)) {
            Properties templateProperties = new Properties();
            templateProperties.load(templateInputStream);
            return templateProperties;
        } catch (Exception error) {
            getLog().error("error", error);
            throw error;
        }
    }
}
