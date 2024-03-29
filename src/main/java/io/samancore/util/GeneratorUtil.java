package io.samancore.util;

import io.samancore.model.Field;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GeneratorUtil {
    public static final List<String> DATA_TEMPLATES_LIST = List.of("Entity.vm");
    public static final List<String> MODEL_TEMPLATES_LIST = List.of("Model.vm");
    public static final List<String> CLIENT_TEMPLATES_LIST = List.of("Client.vm", "RestClient.vm", "RestClientWrapper.vm");
    public static final List<String> APPLICATION_TEMPLATES_LIST = List.of("Api.vm", "Repository.vm", "RepositoryReactivePanache.vm", "Service.vm", "ServiceImpl.vm", "Transformer.vm");
    public static final Map<String, List<String>> FILES_MODULE_GENERATOR_MAP = Map.of("model", MODEL_TEMPLATES_LIST, "application", APPLICATION_TEMPLATES_LIST, "client", CLIENT_TEMPLATES_LIST, "data", DATA_TEMPLATES_LIST);
    public static final Map<String, String> ROUTE_PACKAGE_OUTPUT_MAP = Map.ofEntries(
            Map.entry("Api", "api"),
            Map.entry("Model", "model"),
            Map.entry("Entity", "entity"),
            Map.entry("Repository", "repository"),
            Map.entry("Service", "service"),
            Map.entry("ServiceImpl", "service/impl"),
            Map.entry("RepositoryReactivePanache", "repository/reactive/panache"),
            Map.entry("Transformer", "transformer"),
            Map.entry("Client", "client"),
            Map.entry("RestClient", "client/rest/microprofile"),
            Map.entry("RestClientWrapper", "client/rest"));
    public static final String BIG_DECIMAL = "BigDecimal";
    public static final Map<String, String> DATA_IMPORT_TYPE_MAP = Map.of(BIG_DECIMAL, "java.math.BigDecimal", "Date", "java.util.Date", "List", "java.util.List", "List<String>", "java.util.List", "Set<String>", "java.util.Set");
    public static final String STRING = "String";
    public static final Map<String, String> DATA_TYPE_MAP = getDataTypeMap();
    public static final List<String> COMPONENTS_ALLOWED = List.of("textfield", "textarea", "password", "email", "url", "radio", "currency", "checkbox", "select", "datetime", "number", "phonenumber", "time");
    public static final Map<String, String> DATA_TYPE_MULTIPLE_MAP_TO_ENTITY = Map.of( "select", "Set<String>");
    public static final Map<String, String> DATA_TYPE_MULTIPLE_MAP_TO_MODEL = Map.of( "select", "Set<String>");
    public static final Map<String, String> DATA_TYPE_WITH_SPECIAL_PAIR = Map.of( );
    public static final Map<String, String> DATA_TYPE_DEFINITION_TYPE_MAP = Map.of( "String[]", "CustomStringArrayType.class");

    public static HashMap getDataTypeMap(){
        var dataTypeMap = new HashMap<>();
        dataTypeMap.put("textfield", STRING);
        dataTypeMap.put("textarea", STRING);
        dataTypeMap.put("password", STRING);
        dataTypeMap.put("email", STRING);
        dataTypeMap.put("url", STRING);
        dataTypeMap.put("radio", STRING);
        dataTypeMap.put("currency", BIG_DECIMAL);
        dataTypeMap.put("checkbox", "Boolean");
        dataTypeMap.put("select", STRING);
        dataTypeMap.put("datetime", "Date");
        dataTypeMap.put("phoneNumber", STRING);
        dataTypeMap.put("time", STRING);
        return dataTypeMap;
    }

    public static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            // Keywords from Section 3.9 can't be used as identifiers.
            "_", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while",
            // Literals from Section 3.10 can't be used as identifiers.
            "true", "false", "null",
            // Note that module-related restricted keywords can still be used.
            // Class names used internally by the avro code generator
            "Builder"));
    private VelocityEngine velocityEngine;
    private static final String javaSuffix = ".java";
    private static final String velocityFileSuffix = ".vm";

    public GeneratorUtil() {
        initialize();
    }

    public static String mangleObjectIdentifier(String word) {
        String key = mangle(word, RESERVED_WORDS, false);
        return CaseUtils.toCamelCase(key, false);
    }

    public String getJavaFileNameDestination(String fileName, String templateName) {
        return templateName.concat(fileName).concat(javaSuffix);
    }

    public String makePathDestination(String templateName, String templatePackageName, String fileName) {
        fileName = fileName.replace(velocityFileSuffix, "");
        var javaFileName = getJavaFileNameDestination(fileName, templateName);
        if (templatePackageName == null || templatePackageName.isEmpty()) {
            return javaFileName;
        } else {
            var packageDestination = ROUTE_PACKAGE_OUTPUT_MAP.get(fileName);
            return templatePackageName.toLowerCase(Locale.ROOT).replace('.', File.separatorChar) + File.separatorChar + templateName.toLowerCase(Locale.ROOT) + File.separatorChar + packageDestination + File.separatorChar + javaFileName;
        }
    }

    public static String mangleTypeIdentifier(String word) {
        return StringUtils.capitalize(mangle(word, RESERVED_WORDS, false));
    }

    public static String mangle(String word) {
        return mangle(word, RESERVED_WORDS, false);
    }

    public static String mangle(String word, Set<String> reservedWords, boolean isMethod) {
        if (StringUtils.isBlank(word)) {
            return word;
        }
        if (word.contains(".")) {
            // If the 'word' is really a full path of a class we must mangle just the
            String[] packageWords = word.split("\\.");
            String[] newPackageWords = new String[packageWords.length];

            for (int i = 0; i < packageWords.length; i++) {
                String oldName = packageWords[i];
                newPackageWords[i] = mangle(oldName, reservedWords, false);
            }

            return String.join(".", newPackageWords);
        }
        if (reservedWords.contains(word) || (isMethod && reservedWords
                .contains(Character.toLowerCase(word.charAt(0)) + ((word.length() > 1) ? word.substring(1) : "")))) {
            return word + "$";
        }
        return word;
    }

    void initialize() {
        initializeVelocity();
    }

    private void initializeVelocity() {
        this.velocityEngine = new VelocityEngine();

        // These properties tell Velocity to use its own classpath-based
        // loader, then drop down to check the root and the current folder
        velocityEngine.addProperty("resource.loaders", "class, file");
        velocityEngine.addProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.addProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        velocityEngine.addProperty("resource.loader.file.path", "/, ., ");
        velocityEngine.setProperty("runtime.strict_mode.enable", true);

        // Set whitespace gobbling to Backward Compatible (BC)
        // https://velocity.apache.org/engine/2.0/developer-guide.html#space-gobbling
        velocityEngine.setProperty("parser.space_gobbling", "bc");
    }

    public static String getObjectTypeToEntity(Field field) {
        if(field.isMultiple()){
            return DATA_TYPE_MULTIPLE_MAP_TO_ENTITY.get(field.getDataType());
        }else
        if (DATA_TYPE_MAP.containsKey(field.getDataType())) {
            return DATA_TYPE_MAP.get(field.getDataType());
        }
        if (field.getDataType().equalsIgnoreCase("number")) {
            if (field.isDecimal()) {
                return BIG_DECIMAL;
            } else {
                return "Integer";
            }
        }
        throw new RuntimeException("Not valid objectType for field: " + field.getKey());
    }

    public static String getObjectTypeToModel(Field field) {
        if(field.isMultiple()){
            return DATA_TYPE_MULTIPLE_MAP_TO_MODEL.get(field.getDataType());
        }else
        if (DATA_TYPE_MAP.containsKey(field.getDataType())) {
            return DATA_TYPE_MAP.get(field.getDataType());
        }
        if (field.getDataType().equalsIgnoreCase("number")) {
            if (field.isDecimal()) {
                return BIG_DECIMAL;
            } else {
                return "Integer";
            }
        }
        throw new RuntimeException("Not valid objectType for field: " + field.getKey());
    }


    public static String getImport(String dataType) {
        return DATA_IMPORT_TYPE_MAP.get(dataType);
    }

    public static List<String> getImports(List<Field> fields) {
        var imports = new ArrayList<String>();
        for(var field : fields){
            if(DATA_IMPORT_TYPE_MAP.containsKey(getObjectTypeToEntity(field)))
                imports.add(DATA_IMPORT_TYPE_MAP.get(getObjectTypeToEntity(field)));
        }
        return imports;
    }

    public static Boolean validateIfHasElementCollection(Field field) {
        return (field.isMultiple());
    }

    public static String getEntityElementCollection(String templateName, Field field) {
        return String.format("@CollectionTable(name = \"%s__%s\", joinColumns = @JoinColumn(name = \"%s_id\", nullable = false), uniqueConstraints = @UniqueConstraint(columnNames = {\"%s_id\", \"%s\"}))",templateName, field.getKey(), templateName, templateName, field.getKey());
    }

    public static String getEntityColumnDefinition(Field field) {
        String unique = "unique = true, ";
        String required = "nullable = false, ";
        String precisionScale = "precision = 30, ";
        String scale = String.format("scale = %d, ", field.getDecimalLimit());
        String columnDescription = "@Column(";

        if (field.isDecimal()) {
            columnDescription = columnDescription.concat(precisionScale).concat(scale);
        }
        if (field.isUnique()) {
            columnDescription = columnDescription.concat(unique);
        }
        if (field.isRequired()) {
            columnDescription = columnDescription.concat(required);
        }
        return columnDescription.substring(0, columnDescription.lastIndexOf(",")).concat(")");
    }

    public static Boolean validateIfHasColumnDefinition(Field field) {
        return (field.isUnique() || field.isRequired() || field.isDecimal());
    }

    public static String getTranformerPairToEntityDefinition(Field field) {
        String pairDescription = null;
        if(DATA_TYPE_WITH_SPECIAL_PAIR.containsKey(field.getDataType())){
            pairDescription = String.format("var pair%s = Pair.of(\"%s\", (Function<List<String>, ?>) list -> list.toArray(String[]::new));", mangleTypeIdentifier(field.getKey()),  mangle(field.getKey()).toLowerCase(Locale.ROOT));
        }
        return pairDescription;
    }

    public static String getTranformerPairToModelDefinition(Field field) {
        String pairDescription = null;
        if(DATA_TYPE_WITH_SPECIAL_PAIR.containsKey(field.getDataType())){
            pairDescription = String.format("var pair%s = Pair.of(\"%s\", (Function<String[], ?>) array -> Arrays.stream(array).collect(Collectors.toList()));", mangleTypeIdentifier(field.getKey()),  mangle(field.getKey()).toLowerCase(Locale.ROOT));
        }
        return pairDescription;
    }

    public static String getTranformerToEntityDefinition(List<Field> fields) {
        String tranformerToEntityDescription = "return transformToEntity(model, ";
        for (var field: fields){
            if(DATA_TYPE_WITH_SPECIAL_PAIR.containsKey(field.getDataType())){
                tranformerToEntityDescription = tranformerToEntityDescription.concat(String.format(" pair%s, ", mangleTypeIdentifier(field.getKey())));
            }
        }
        return tranformerToEntityDescription.substring(0, tranformerToEntityDescription.lastIndexOf(",")).concat(")");
    }

    public static String getTranformerToModelDefinition(List<Field> fields) {
        String tranformerToEntityDescription = "return transformToModel(entity, ";
        for (var field: fields){
            if(DATA_TYPE_WITH_SPECIAL_PAIR.containsKey(field.getDataType())){
                tranformerToEntityDescription = tranformerToEntityDescription.concat(String.format(" pair%s, ", mangleTypeIdentifier(field.getKey())));
            }
        }
        return tranformerToEntityDescription.substring(0, tranformerToEntityDescription.lastIndexOf(",")).concat(")");
    }

    public static Boolean getIfAnyFieldNeedPair(List<Field> fields) {
        for (var field: fields){
            if(field.isMultiple()){
                return true;
            }
        }
        return false;
    }

    public static String getAnotation(String validation, String dataType) {
        if (validation.equalsIgnoreCase("required")) {
            if (dataType.equalsIgnoreCase(STRING)) {
                return "@NotBlank\r\n\t".concat("@NotEmpty");
            } else {
                return "@".concat("NotNull");
            }
        }
        return "@".concat(validation);
    }


    private String renderTemplate(String templateName, VelocityContext context) {
        Template template;
        try {
            template = this.velocityEngine.getTemplate(templateName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    public List<OutputFile> compile(io.samancore.model.Template template, String module) {
        var templateWithFieldsAllowed = getTemplateWithFieldsAllowed(template);
        List<OutputFile> outputFiles = new ArrayList<>();
        VelocityContext context = new VelocityContext();
        context.put("this", this);
        context.put("template", templateWithFieldsAllowed);

        String templateDir = System.getProperty("velocity.templates.".concat(module),
                "/velocity/templates/".concat(module).concat(File.separator));

        var templatesToGenerateList = FILES_MODULE_GENERATOR_MAP.get(module);
        for (String templateFileName : templatesToGenerateList) {
            var output = renderTemplate(templateDir.concat(templateFileName), context);

            String templateName = mangleTypeIdentifier(templateWithFieldsAllowed.getName());
            String productName = mangleTypeIdentifier(templateWithFieldsAllowed.getProductName());
            var templatePackageName = mangle(templateWithFieldsAllowed.getPackageName()).concat(".").concat(productName.toLowerCase(Locale.ROOT));
            var outputFilePathDestination = makePathDestination(templateName, templatePackageName, templateFileName);

            OutputFile outputFile = new OutputFile();
            outputFile.path = outputFilePathDestination;
            outputFile.contents = output;
            outputFiles.add(outputFile);
        }
        return outputFiles;
    }

    private static io.samancore.model.Template getTemplateWithFieldsAllowed(io.samancore.model.Template template) {
        var fieldFiltered = template.getFields().stream().filter(field -> COMPONENTS_ALLOWED.contains(field.getDataType().toLowerCase(Locale.ROOT))).toList();
        return template.toBuilder().setFields(fieldFiltered).build();
    }

    public String toLowerCase(String str) {
        return str.toLowerCase(Locale.ROOT);
    }

    public static class OutputFile {
        String path;
        String contents;
        String outputCharacterEncoding;

        /**
         * Writes output to path destination directory when it is newer than src,
         * creating directories as necessary. Returns the created file.
         */
        public File writeToDestination(File src, File destDir) throws IOException {
            File f = new File(destDir, path);
            if (src != null && f.exists() && f.lastModified() >= src.lastModified())
                return f; // already up to date: ignore
            f.getParentFile().mkdirs();
            Writer fw = null;
            FileOutputStream fos = null;
            try {
                if (outputCharacterEncoding != null) {
                    fos = new FileOutputStream(f);
                    fw = new OutputStreamWriter(fos, outputCharacterEncoding);
                } else {
                    fw = Files.newBufferedWriter(f.toPath(), UTF_8);
                }
                fw.write(contents);
            } finally {
                if (fw != null)
                    fw.close();
                if (fos != null)
                    fos.close();
            }
            return f;
        }
    }
}
