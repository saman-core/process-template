package io.samancore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GeneralUtil {

    private GeneralUtil() {
    }

    public static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            // Keywords from Section 3.9 can't be used as identifiers.
            "_", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "create",
            "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while",
            // Literals from Section 3.10 can't be used as identifiers.
            "true", "false", "null",
            // Note that module-related restricted keywords can still be used.
            // Class names used internally by the avro code generator
            "Builder"));


    public static String mangleObjectIdentifier(String word) {
        return CaseUtils.toCamelCase(GeneralUtil.mangle(word, RESERVED_WORDS, false), false);
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


}
