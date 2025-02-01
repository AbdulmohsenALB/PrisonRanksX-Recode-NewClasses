package me.prisonranksx.utils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * Utility class for parsing integers, longs, floats, and doubles from strings.
 * This class provides efficient methods for checking if a string can be parsed
 * as a number and for converting strings to numbers.
 */
public class NumParser {

    public static final Set<Character> NUMBER_CHARS = QSets.newHashSet('0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9');
    private static final int ZERO_CODE = 48;
    private static final int NINE_CODE = 57;
    private static final int RADIX = 10;
    private static final char MINUS = '-';
    private static final char PLUS = '+';
    private static final char DOT = '.';
    private static final int LIMIT = Integer.MAX_VALUE / RADIX;
    private static final int LAST_DIGIT_LIMIT = Integer.MAX_VALUE % RADIX;

    // Integer parsing methods

    /**
     * Checks whether the string can be recognized as an int or not.
     *
     * @param string to check
     * @return true if string can be converted to int, false otherwise
     */
    public static boolean isInt(String string) {
        int length = string.length();
        if (length == 0) return false;

        char firstChar = string.charAt(0);
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        int result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) return false;

            int digit = current - ZERO_CODE;

            if (result > LIMIT || (result == LIMIT && digit > LAST_DIGIT_LIMIT)) return false;

            result = (result << 3) + (result << 1) + digit;
            i++;
        }
        return true;
    }

    /**
     * Converts string to int
     *
     * @param string to parse as integer
     * @return string as integer if it can be perceived as integer, int of 0 is
     * returned otherwise
     */
    public static int asInt(String string) {
        return asInt(string, 0);
    }

    /**
     * Converts string to int
     *
     * @param string to parse as integer
     * @param error  action to perform on string if parsing failed
     * @return string as integer if it can be perceived as integer, error is
     * returned otherwise
     */
    public static int asInt(String string, Consumer<String> error) {
        return asInt(string, error, 0);
    }

    /**
     * Converts string to int
     *
     * @param string      to parse as integer
     * @param error       action to perform on string if parsing failed
     * @param errorResult integer to return if string wasn't an integer
     * @return string as integer if it can be perceived as integer, errorResult is
     * returned otherwise
     */
    public static int asInt(String string, Consumer<String> error, int errorResult) {
        int length = string.length();
        if (length == 0) {
            error.accept(string);
            return errorResult;
        }

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        int result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) {
                result = errorResult;
                error.accept(string);
                break;
            }

            int digit = current - ZERO_CODE;

            if (result > LIMIT || (result == LIMIT && digit > LAST_DIGIT_LIMIT)) {
                result = errorResult;
                error.accept(string);
                break;
            }

            result = (result << 3) + (result << 1) + digit;
            i++;
        }

        return negative ? -result : result;
    }

    /**
     * Converts string to int
     *
     * @param string to parse as integer
     * @param error  value to return if string wasn't a valid int
     * @return string as integer if it can be perceived as integer, int of 0 is
     * returned otherwise
     */
    public static int asInt(String string, int error) {
        int length = string.length();
        if (length == 0) return error;

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        int result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) {
                result = error;
                break;
            }

            int digit = current - ZERO_CODE;

            if (result > LIMIT || (result == LIMIT && digit > LAST_DIGIT_LIMIT)) {
                result = error;
                break;
            }

            result = (result << 3) + (result << 1) + digit;
            i++;
        }

        return negative ? -result : result;
    }

    /**
     * Java Integer.parseInt(string) in a try catch, returning false if
     * NumberFormatException is thrown.
     *
     * @param string to parse
     * @return true if it can be parsed, false otherwise
     */
    public static boolean isParsableInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Checks whether provided string is an integer, if so, executes intAction on
     * converted int, else executes elseAction on failed string.
     *
     * @param string     to check
     * @param intAction  to perform on int. If null, doesn't get executed.
     * @param elseAction to perform on string that failed to convert. If null,
     *                   doesn't get executed.
     * @return true if converted, false if not.
     */
    public static boolean ifInt(String string, IntConsumer intAction, Consumer<String> elseAction) {
        int length = string.length();
        if (length == 0) {
            if (elseAction != null) elseAction.accept(string);
            return false;
        }

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        int result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) {
                if (elseAction != null) elseAction.accept(string);
                break;
            }

            int digit = current - ZERO_CODE;

            if (result > LIMIT || (result == LIMIT && digit > LAST_DIGIT_LIMIT)) {
                if (elseAction != null) elseAction.accept(string);
                break;
            }

            result = (result << 3) + (result << 1) + digit;
            i++;
        }
        if (intAction != null) intAction.accept(negative ? -result : result);
        return true;
    }

    /**
     * Get int in a string
     *
     * @param string to extract an int from
     * @return extracted int
     */
    public static int readInt(String string) {
        if (string == null || string.isEmpty()) return 0;

        final int length = string.length();
        int startIndex = 0;
        boolean negative = false;

        char firstChar = string.charAt(0);
        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        }

        int result = 0;
        boolean numberAdded = false;

        for (int i = startIndex; i < length; i++) {
            char character = string.charAt(i);

            if (NUMBER_CHARS.contains(character)) {
                int digit = character - '0';
                result = (result * RADIX) + digit; // Multiply by 10 instead of concatenation
                numberAdded = true;
            }
        }

        return numberAdded ? (negative ? -result : result) : 0;
    }

    // Long parsing methods

    /**
     * Java Long.parseLong(string) in a try catch, returning false if
     * NumberFormatException is thrown.
     *
     * @param string to parse
     * @return true if it can be parsed, false otherwise
     */
    public static boolean isParsableLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Checks whether the string can be recognized as a long or not.
     *
     * @param string to check
     * @return true if string can be converted to long, false otherwise
     */
    public static boolean isLong(String string) {
        int length = string.length();
        if (length == 0) return false;

        char firstChar = string.charAt(0);
        int startIndex = 0;

        if (firstChar == MINUS || firstChar == PLUS) {
            startIndex = 1;
        }

        long result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) return false;

            int digit = current - ZERO_CODE;

            if (result > Long.MAX_VALUE / RADIX || (result == Long.MAX_VALUE / RADIX && digit > Long.MAX_VALUE % RADIX))
                return false;

            result = result * RADIX + digit;
            i++;
        }
        return true;
    }

    /**
     * Converts string to long.
     *
     * @param string to parse as long
     * @return string as long if it can be perceived as long, 0L otherwise
     */
    public static long asLong(String string) {
        return asLong(string, 0L);
    }

    /**
     * Converts string to long.
     *
     * @param string to parse as long
     * @param error  action to perform on string if parsing failed
     * @return string as long if it can be perceived as long, 0L otherwise
     */
    public static long asLong(String string, Consumer<String> error) {
        return asLong(string, error, 0L);
    }

    /**
     * Converts string to long.
     *
     * @param string      to parse as long
     * @param error       action to perform on string if parsing failed
     * @param errorResult long to return if string wasn't a long
     * @return string as long if it can be perceived as long, errorResult otherwise
     */
    public static long asLong(String string, Consumer<String> error, long errorResult) {
        int length = string.length();
        if (length == 0) {
            error.accept(string);
            return errorResult;
        }

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        long result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) {
                error.accept(string);
                return errorResult;
            }

            int digit = current - ZERO_CODE;

            if (result > Long.MAX_VALUE / RADIX
                    || (result == Long.MAX_VALUE / RADIX && digit > Long.MAX_VALUE % RADIX)) {
                error.accept(string);
                return errorResult;
            }

            result = result * RADIX + digit;
            i++;
        }

        return negative ? -result : result;
    }

    /**
     * Converts string to long.
     *
     * @param string to parse as long
     * @param error  value to return if string wasn't a valid long
     * @return string as long if it can be perceived as long, error value otherwise
     */
    public static long asLong(String string, long error) {
        int length = string.length();
        if (length == 0) return error;

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        long result = 0;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current < ZERO_CODE || current > NINE_CODE) {
                return error;
            }

            int digit = current - ZERO_CODE;

            if (result > Long.MAX_VALUE / RADIX
                    || (result == Long.MAX_VALUE / RADIX && digit > Long.MAX_VALUE % RADIX)) {
                return error;
            }

            result = result * RADIX + digit;
            i++;
        }

        return negative ? -result : result;
    }

    /**
     * Extracts a long from a string.
     *
     * @param string to extract a long from
     * @return extracted long
     */
    public static long readLong(String string) {
        if (string == null || string.isEmpty()) return 0L;

        final int length = string.length();
        int startIndex = 0;
        boolean negative = false;

        char firstChar = string.charAt(0);
        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        }

        long result = 0L;
        boolean numberAdded = false;

        for (int i = startIndex; i < length; i++) {
            char character = string.charAt(i);

            if (NUMBER_CHARS.contains(character)) {
                int digit = character - '0';
                result = (result * RADIX) + digit;
                numberAdded = true;
            }
        }

        return numberAdded ? (negative ? -result : result) : 0L;
    }

    // Double parsing methods

    /**
     * Checks whether the string can be recognized as an double or not.
     *
     * @param string to check
     * @return true if string can be converted to double, false otherwise
     */
    public static boolean isDouble(String string) {
        int length = string.length();
        if (length == 0) return false;

        char firstChar = string.charAt(0);
        int startIndex = 0;

        if (firstChar == MINUS || firstChar == PLUS) {
            startIndex = 1;
        }

        boolean dotSeen = false;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) return false;
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                return false;
            }

            i++;
        }
        return true;
    }

    /**
     * Converts string to double. Only simple doubles are supported, so no scientific notation, or exponential notations.
     *
     * @param string to parse as double
     * @return string as double if it can be perceived as double, 0.0 otherwise
     */
    public static double asDouble(String string) {
        return asDouble(string, 0.0);
    }

    /**
     * Converts string to double. Only simple doubles are supported, so no scientific notation, or exponential notations.
     *
     * @param string to parse as double
     * @param error  action to perform on string if parsing failed
     * @return string as double if it can be perceived as double, 0.0 otherwise
     */
    public static double asDouble(String string, double error) {
        int length = string.length();
        if (length == 0) return error;

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        double result = 0;
        double fraction = 0;
        boolean dotSeen = false;
        double scale = 1;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) return error;
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                return error;
            } else {
                int digit = current - ZERO_CODE;
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = result * RADIX + digit;
                }
            }

            i++;
        }

        result += fraction;
        return negative ? -result : result;
    }

    /**
     * Converts string to double. Only simple doubles are supported, so no scientific notation, or exponential notations.
     *
     * @param string to parse as double
     * @param error  action to perform on string if parsing failed
     * @return string as double if it can be perceived as double, 0.0 otherwise
     */
    public static double asDouble(String string, Consumer<String> error) {
        return asDouble(string, error, 0.0);
    }

    /**
     * Converts string to double. Only simple doubles are supported, so no scientific notation, or exponential notations.
     *
     * @param string      to parse as double
     * @param error       action to perform on string if parsing failed
     * @param errorResult double to return if string wasn't a double
     * @return string as double if it can be perceived as double, errorResult
     * otherwise
     */
    public static double asDouble(String string, Consumer<String> error, double errorResult) {
        int length = string.length();
        if (length == 0) {
            error.accept(string);
            return errorResult;
        }

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        double result = 0;
        double fraction = 0;
        boolean dotSeen = false;
        double scale = 1;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) {
                    error.accept(string);
                    return errorResult;
                }
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                error.accept(string);
                return errorResult;
            } else {
                int digit = current - ZERO_CODE;
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = result * RADIX + digit;
                }
            }

            i++;
        }

        result += fraction;
        return negative ? -result : result;
    }

    /**
     * Checks whether provided string is a double, if so, executes doubleAction on
     * converted double, else executes elseAction on failed string.
     *
     * @param string       to check
     * @param doubleAction to perform on double. If null, doesn't get executed.
     * @param elseAction   to perform on string that failed to convert. If null,
     *                     doesn't get executed.
     * @return true if converted, false if not.
     */
    public static boolean ifDouble(String string, DoubleConsumer doubleAction, Consumer<String> elseAction) {
        int length = string.length();
        if (length == 0) {
            if (elseAction != null) elseAction.accept(string);
            return false;
        }

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        double result = 0;
        double fraction = 0;
        boolean dotSeen = false;
        double scale = 1;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) {
                    if (elseAction != null) elseAction.accept(string);
                    return false;
                }
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                if (elseAction != null) elseAction.accept(string);
                return false;
            } else {
                int digit = current - ZERO_CODE;
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = result * RADIX + digit;
                }
            }

            i++;
        }

        result += fraction;
        if (doubleAction != null) doubleAction.accept(negative ? -result : result);
        return true;
    }

    /**
     * Extracts a double from a string.
     *
     * @param string to extract a double from
     * @return extracted double
     */
    public static double readDouble(String string) {
        if (string == null || string.isEmpty()) return 0.0;

        final int length = string.length();
        int startIndex = 0;
        boolean negative = false;

        char firstChar = string.charAt(0);
        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        }

        double result = 0.0;
        double fraction = 0.0;
        double scale = 1.0;
        boolean numberAdded = false;
        boolean dotSeen = false;

        for (int i = startIndex; i < length; i++) {
            char character = string.charAt(i);

            if (character == DOT) {
                dotSeen = true;
            } else if (NUMBER_CHARS.contains(character)) {
                int digit = character - '0';
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = (result * RADIX) + digit;
                }
                numberAdded = true;
            }
        }

        result += fraction;
        return numberAdded ? (negative ? -result : result) : 0.0;
    }

    /**
     * Checks whether the string can be recognized as a float or not.
     *
     * @param string to check
     * @return true if string can be converted to float, false otherwise
     */
    public static boolean isFloat(String string) {
        int length = string.length();
        if (length == 0) return false;

        char firstChar = string.charAt(0);
        int startIndex = 0;

        if (firstChar == MINUS || firstChar == PLUS) {
            startIndex = 1;
        }

        boolean dotSeen = false;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) return false;
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                return false;
            }

            i++;
        }
        return true;
    }

    /**
     * Converts string to float.
     *
     * @param string to parse as float
     * @return string as float if it can be perceived as float, 0.0f otherwise
     */
    public static float asFloat(String string) {
        return asFloat(string, 0.0f);
    }

    /**
     * Converts string to float.
     *
     * @param string to parse as float
     * @param error  action to perform on string if parsing failed
     * @return string as float if it can be perceived as float, 0.0f otherwise
     */
    public static float asFloat(String string, Consumer<String> error) {
        return asFloat(string, error, 0.0f);
    }

    /**
     * Converts string to float.
     *
     * @param string      to parse as float
     * @param error       action to perform on string if parsing failed
     * @param errorResult float to return if string wasn't a float
     * @return string as float if it can be perceived as float, errorResult
     * otherwise
     */
    public static float asFloat(String string, Consumer<String> error, float errorResult) {
        int length = string.length();
        if (length == 0) {
            error.accept(string);
            return errorResult;
        }

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        float result = 0.0f;
        float fraction = 0.0f;
        boolean dotSeen = false;
        float scale = 1.0f;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) {
                    error.accept(string);
                    return errorResult;
                }
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                error.accept(string);
                return errorResult;
            } else {
                int digit = current - ZERO_CODE;
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = result * RADIX + digit;
                }
            }

            i++;
        }

        result += fraction;
        return negative ? -result : result;
    }

    /**
     * Converts string to float.
     *
     * @param string to parse as float
     * @param error  value to return if string wasn't a valid float
     * @return string as float if it can be perceived as float, error value
     * otherwise
     */
    public static float asFloat(String string, float error) {
        int length = string.length();
        if (length == 0) return error;

        char firstChar = string.charAt(0);
        boolean negative = false;
        int startIndex = 0;

        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        } else if (firstChar == PLUS) {
            startIndex = 1;
        }

        float result = 0.0f;
        float fraction = 0.0f;
        boolean dotSeen = false;
        float scale = 1.0f;
        int i = startIndex;

        while (i < length) {
            char current = string.charAt(i);

            if (current == DOT) {
                if (dotSeen) {
                    return error;
                }
                dotSeen = true;
            } else if (current < ZERO_CODE || current > NINE_CODE) {
                return error;
            } else {
                int digit = current - ZERO_CODE;
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = result * RADIX + digit;
                }
            }

            i++;
        }

        result += fraction;
        return negative ? -result : result;
    }

    /**
     * Extracts a float from a string.
     *
     * @param string to extract a float from
     * @return extracted float
     */
    public static float readFloat(String string) {
        if (string == null || string.isEmpty()) return 0.0f;

        final int length = string.length();
        int startIndex = 0;
        boolean negative = false;

        char firstChar = string.charAt(0);
        if (firstChar == MINUS) {
            startIndex = 1;
            negative = true;
        }

        float result = 0.0f;
        float fraction = 0.0f;
        float scale = 1.0f;
        boolean numberAdded = false;
        boolean dotSeen = false;

        for (int i = startIndex; i < length; i++) {
            char character = string.charAt(i);

            if (character == DOT) {
                dotSeen = true;
            } else if (NUMBER_CHARS.contains(character)) {
                int digit = character - '0';
                if (dotSeen) {
                    scale /= RADIX;
                    fraction += digit * scale;
                } else {
                    result = (result * RADIX) + digit;
                }
                numberAdded = true;
            }
        }

        result += fraction;
        return numberAdded ? (negative ? -result : result) : 0.0f;
    }

    /**
     * Utility class for creating sets.
     */
    public static class QSets {

        /**
         * Creates a new HashSet with the given elements.
         *
         * @param elements elements to add to the set
         * @param <T>      type of elements in the set
         * @return a new HashSet containing the given elements
         */
        @SafeVarargs
        public static <T> HashSet<T> newHashSet(T... elements) {
            HashSet<T> set = new HashSet<>();
            for (T t : elements) set.add(t);
            return set;
        }

        /**
         * Creates a new LinkedHashSet with the given elements.
         *
         * @param elements elements to add to the set
         * @param <T>      type of elements in the set
         * @return a new LinkedHashSet containing the given elements
         */
        @SafeVarargs
        public static <T> LinkedHashSet<T> newLinkedHashSet(T... elements) {
            LinkedHashSet<T> set = new LinkedHashSet<>();
            for (T t : elements) set.add(t);
            return set;
        }

    }

}
