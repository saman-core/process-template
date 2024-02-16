package io.samancore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.samancore.model.Field;
import io.samancore.model.Template;
import io.samancore.util.GeneratorUtil;
import io.samancore.util.JsonUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

@Mojo(name = "generator-class", defaultPhase = LifecyclePhase.COMPILE)
public class GeneratorClassMojo extends AbstractMojo {

    private static final GeneratorUtil generator = new GeneratorUtil();

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;


    public void execute() throws MojoExecutionException {
        String baseDirPath = project.getBasedir().getParent();
        getLog().info("baseDirPath:"+baseDirPath);

        getLog().info("project-GAST: "+ project.getArtifactId());

        try {
            String absolutePath = Paths.get("").toAbsolutePath().toString();
            var resourcePath = absolutePath.concat("/").concat(ConstantUtil.JSON_FILE_NAME);
            getLog().info("resourcePath:"+resourcePath);

            File file = new File(resourcePath);

            JsonMapper jsonMapper = JsonMapper.builder()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true)
                    .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                    .build();

            JsonNode json = jsonMapper.readValue(file, JsonNode.class);
            getLog().info("hay json> "+ json);

            var templateBuilder = Template.newBuilder()
                    .setPackageName(json.get("packageName").textValue())
                    .setName(json.get("name").textValue())
                    .setProductName(json.get("productName").textValue());

            ArrayNode components = (ArrayNode) json.get("components");
            List<Field> fieldList = JsonUtil.getFieldsOfComponent(components);
            templateBuilder.setFields(fieldList);

            String module = null;
            if(project.getArtifactId().toLowerCase(Locale.ROOT).contains("model")){
                module = "model";
            }else if(project.getArtifactId().toLowerCase(Locale.ROOT).contains("client")){
                module = "client";
            }else if(project.getArtifactId().toLowerCase(Locale.ROOT).contains("application")){
                module = "application";
            }

            if(module != null) {
                List<GeneratorUtil.OutputFile> outputFiles = generator.compile(templateBuilder.build(), module);

                String destinationPath = project.getBasedir().getPath().concat("/src/main/java/");
                getLog().info("destinationPath:"+destinationPath);
                for (GeneratorUtil.OutputFile outputFile : outputFiles) {
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