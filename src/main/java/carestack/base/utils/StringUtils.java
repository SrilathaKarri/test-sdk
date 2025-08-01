package carestack.base.utils;

/**
 * Utility class for string manipulation and validation.
 * This class provides common string operations like checking if a string is null or empty.

 */
public class StringUtils {

    /**
     * Checks whether the given string is null, empty, or contains only whitespace.
     *
     * @param string The string to check.
     * @return true if the string is null, empty, or consists only of whitespace; false otherwise.
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty() || string.equals("null");
    }

}
