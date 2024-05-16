package io.samancore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static io.samancore.util.GeneralConstant.MAX_LENGTH_NAME_ALLOWED;
import static io.samancore.util.GeneralConstant.PATTERN_LETTER_NUMBER;

public class GeneralUtil {

    private GeneralUtil() {
    }

    public static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "_", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while",
            "true", "false", "null",
            "exports", "module", "non-sealed\n", "open", "opens", "permits",
            "provides", "record", "requires", "sealed", "to", "transitive", "uses",
            "var", "when", "with", "yield"));


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

    public static void validateIfNameIsAReservedWord(String name) {
        if (RESERVED_WORDS.contains(name.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("There is a component with name not allowed. Name=".concat(name));
        }
    }

    public static void validateLengthName(String name, String message) {
        if (name != null && name.length() > MAX_LENGTH_NAME_ALLOWED) {
            throw new RuntimeException(message);
        }
    }

    public static void validateIfNameContainAnySymbol(String name) {
        if (PATTERN_LETTER_NUMBER.matcher(name).find()) {
            throw new RuntimeException("There is a component with name with a character not allowed. Name=".concat(name));
        }
    }

    public static void validateIfNameBeginWithLowerCase(String name) {
        if (!Character.isLowerCase(name.charAt(0))) {
            throw new RuntimeException("There is a component with name begin without a lower case letter. Name=".concat(name));
        }
    }
}
