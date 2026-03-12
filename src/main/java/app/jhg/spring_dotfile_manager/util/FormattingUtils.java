package app.jhg.spring_dotfile_manager.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FormattingUtils {

    private static final String HOME = System.getProperty("user.home");

    private static final Pattern HOME_DIR_REGEX = Pattern.compile("\\{HOME\\}"); // matches "{HOME}"
    private static final Pattern TILDE_REGEX = Pattern.compile("^~"); // matches "~" at the beginning of a string
    private static final Pattern NAME_REGEX = Pattern.compile("\\{NAME\\}"); // matches "{NAME}"

    private FormattingUtils() {}


    /**
     * Replaces occurrences of "{HOME}" and "~" at the beginning of the string with the user's home directory.
     * @param original the original string containing "{HOME}" and/or "~" placeholders
     * @return the formatted string with "{HOME}" and "~" placeholders replaced by the user's home directory
     */
    public static String formatWithHomeDirectory(String original) {
        original = TILDE_REGEX.matcher(original).replaceFirst(Matcher.quoteReplacement(HOME));
        return HOME_DIR_REGEX.matcher(original).replaceAll(Matcher.quoteReplacement(HOME));
    }

    /**
     * Replaces occurrences of "{NAME}" in the original string with the provided filename.
     * @param original the original string containing "{NAME}" placeholders
     * @param filename the filename to replace the "{NAME}" placeholders with
     * @return the formatted string with "{NAME}" placeholders replaced by the provided filename
     */
    public static String formatWithName(String original, String filename) {
        return NAME_REGEX.matcher(original).replaceAll(Matcher.quoteReplacement(filename));
    }
}
