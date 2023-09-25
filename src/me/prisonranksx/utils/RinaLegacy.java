/*
 * Why this?
 * Nashron library is too big for a plugin.
 * All you want is basic JavaScript conditions.
 * Insanely fast.
 * No dependencies.
 * Single class.
 * Method to retrieve failed condition part.
 */
package me.prisonranksx.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

/**
 * 
 * <b>R</b>ubbish <b>I</b>f <b>N</b>ot <b>A</b>ctual conditions are parsed by
 * this thing.
 * <p>
 * Messes up conditions silently most of the time if there is any mistake, and
 * Java / JavaScript functions cannot be used except for (equals,
 * equalsIgnoreCase, and contains).
 * </p>
 * <p>
 * Examples of valid conditions cuz I'm not familiar with JS (all of them return
 * true when evaluated):
 * </p>
 * <ul>
 * <li><b>Basic:</b>
 * {@code RinaLegacy.newRina("'exampleString'=='exampleString'");}</li>
 * <li><b>With Spaces:</b>
 * {@code RinaLegacy.newRina("'String (with) spaces'=='String (with) spaces'");}</li>
 * <li><b>Escape characters:</b>
 * {@code RinaLegacy.newRina("'String with \\'quotes\\''=='String with \\'quotes\\''");}</li>
 * <li><b>Reverse:</b>
 * {@code RinaLegacy.newRina("!'string'=='anotherstring'");}</li>
 * <li><b>Reverse (2):</b>
 * {@code RinaLegacy.newRina("'string'!='anotherstring'");}</li>
 * <li><b>Or:</b>
 * {@code RinaLegacy.newRina("'spaces spaces'=='spaces spaces'||'spaces spaces2'=='spaces spaces2'");}</li>
 * <li><b>And:</b>
 * {@code RinaLegacy.newRina("'string'=='string'&&'string2'!='string3'");}</li>
 * <li><b>Combined:</b>
 * {@code RinaLegacy.newRina("(string==string2||5==5.0)&&(5!=5||!3==2)");}</li>
 * <li><b>Math Condition:</b> {@code RinaLegacy.newRina("5>=4+1");}</li>
 * </ul>
 * <p>
 * Made specifically for Minecraft
 * </p>
 * <p>
 * Example:
 * </p>
 * Cache and store the object somewhere:
 * <br>
 * {@code RinaLegacy cachedRina = RinaLegacy.newRina("%variable%.equalsIgnoreCase('something')")}
 * <br>
 * <br>
 * As if x is a changing variable like PlaceholderAPI placeholder or something.
 * <br>
 * {@code String x = "'Something'";}
 * <br>
 * <br>
 * Then you will perform this method repeatedly.
 * <br>
 * {@code cachedRina.applyThenEvaluate(s -> s.replace("%variable%", x));}
 * <p>
 * 
 * @author TheGaming999
 */
@Deprecated
public class RinaLegacy {

	private String script;
	private RinaCondition rinaCondition;

	private static final String SCRIPT_AND = "&&";
	private static final String SCRIPT_OR = "||";
	private static final String SCRIPT_GROUP_OPENING = "(";
	private static final String SCRIPT_GROUP_CLOSING = ")";
	private static final String SCRIPT_EQUAL = "==";
	private static final String SCRIPT_NOT_EQUAL = "!=";
	private static final String SCRIPT_GREATER_THAN = ">";
	private static final String SCRIPT_GREATER_THAN_OR_EQUAL = ">=";
	private static final String SCRIPT_LESS_THAN = "<";
	private static final String SCRIPT_LESS_THAN_OR_EQUAL = "<=";
	private static final String SCRIPT_REVERSE = "!";
	private static final String SCRIPT_ESCAPING = "\\";
	private static final char STRING_QUOTATION = '\'';
	private static final char CHAR_SCRIPT_GROUP_OPENING = '(';
	private static final char CHAR_SCRIPT_GROUP_CLOSING = ')';
	private static final char CHAR_SCRIPT_ESCAPING = '\\';

	// Characters that are used to detected orands
	private static final Set<Character> SPLITTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList('|', '&')));

	// Characters that are used to detect math operations
	private static final Set<Character> MATH = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList('+', '-', '*', '/', '^')));

	// Characters that can be escaped
	private static final Set<String> ESCAPABLES = new HashSet<>(Arrays.asList("'"));

	// Holds escapables values in a map to prevent constant creation of strings when
	// escapable characters are processed
	private static final Map<String, String> REPLACABLE = Collections
			.unmodifiableMap(ESCAPABLES.stream().collect(Collectors.toMap(s -> SCRIPT_ESCAPING + s, s -> s)));

	// Holds values from 0.0 to 9.9 for double number detection
	// Negative values are detected within a method
	private static final Set<String> MATH_DOUBLE = Collections.unmodifiableSet(
			IntStream.range(0, 100).mapToObj(d -> String.valueOf(d / 10.0)).collect(Collectors.toSet()));

	// equals, equalsIgnoreCase, and contains
	private static final Map<String, MethodHandle> ONE_STRING_PARAM_STRING_METHODS = new HashMap<>();

	static {
		// No need for the set after its values are inserted into REPLACEABLE map.
		ESCAPABLES.clear();
		Method[] methods = String.class.getDeclaredMethods();
		for (Method m : methods) {
			if (!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())
					&& m.getParameterCount() == 1) {
				try {
					MethodHandle mh = MethodHandles.lookup()
							.findVirtual(String.class, m.getName(),
									MethodType.methodType(m.getReturnType(), m.getParameterTypes()));
					ONE_STRING_PARAM_STRING_METHODS.put(m.getName(), mh);
				} catch (NoSuchMethodException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Creates, parses, and caches the provided script in a new instance
	 * 
	 * @see RinaLegacy
	 * @param script to parse
	 */
	public RinaLegacy(String script) {
		this(script, true);
	}

	/**
	 * Creates a new rina instance, and parses the script if {@code parse} is set to
	 * true
	 * 
	 * @see RinaLegacy
	 * @param script to store
	 * @param parse  whether to parse the given script or not
	 */
	public RinaLegacy(String script, boolean parse) {
		this.script = script;
		if (parse) rinaCondition = parseScript(this.script);
	}

	/**
	 * Creates, parses, and caches the provided script in a new instance
	 * 
	 * @see RinaLegacy
	 * @param script to parse
	 */
	public static RinaLegacy newRinaLegacy(String script) {
		return new RinaLegacy(script);
	}

	/**
	 * Creates a new rina instance, and parses the script if {@code parse} is set to
	 * true
	 * 
	 * @see RinaLegacy
	 * @param script to parse
	 * @param parse  whether to parse the given script or not
	 */
	public static RinaLegacy newRinaLegacy(String script, boolean parse) {
		return new RinaLegacy(script, parse);
	}

	/**
	 * 
	 * @return The rina condition that holds all of the given conditions including
	 *         the groups (or, and)
	 */
	public RinaCondition getRinaCondition() {
		return rinaCondition;
	}

	/**
	 * The method {@linkplain #parseScript()} is required after modifying the script
	 * to update the actual conditions
	 * 
	 * @param script condition script
	 * @return this object
	 */
	public RinaLegacy setScript(String script) {
		this.script = script;
		return this;
	}

	/**
	 * Sets script to the given one and parses it again if parse is set to true
	 * 
	 * @param script condition script
	 * @param parse  whether to perform {@linkplain #parseScript()} or not
	 * @return this object
	 */
	public RinaLegacy setScript(String script, boolean parse) {
		this.script = script;
		if (parse) parseScript();
		return this;
	}

	/**
	 * 
	 * @return plain script that was provided within the created instance
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Parses the given script in the created instance. The script is already parsed
	 * if it was created using {@linkplain #newRinaLegacy(String)},
	 * {@linkplain #RinaLegacy(String)},
	 * {@linkplain #newRinaLegacy(String, boolean)} and the
	 * boolean was set to true, or {@linkplain #RinaLegacy(String, boolean)} and the
	 * boolean was
	 * set to true
	 * 
	 * @return RinaCondition that was parsed from the script
	 */
	public RinaCondition parseScript() {
		return rinaCondition = parseScript(script);
	}

	/**
	 * Quickly evaluates a condition for testing purposes. This is much slower than
	 * a
	 * cached RinaLegacy using {@linkplain #RinaLegacy(String)} or
	 * {@linkplain #newRinaLegacy(String)}
	 * 
	 * @param script to parse and evaluate
	 * @return true if condition is met, false otherwise
	 */
	public static boolean evaluate(String script) {
		return RinaLegacy.newRinaLegacy(script).evaluate();
	}

	/**
	 * Quickly applies a function then evaluates a condition for testing purposes.
	 * This is much slower than a cached RinaLegacy using
	 * {@linkplain #RinaLegacy(String)} or
	 * {@linkplain #newRinaLegacy(String)}
	 * 
	 * @param script   to parse and evaluate
	 * @param function to apply on the condition before evaluation
	 * @return true if condition is met, false otherwise
	 */
	public static boolean applyThenEvaluate(String script, Function<String, String> function) {
		return RinaLegacy.newRinaLegacy(script).applyThenEvaluate(function);
	}

	/**
	 * @return A string representation of this object, which includes the plain
	 *         script and the conditions
	 */
	@Override
	public String toString() {
		return "{Script=" + script + "}, \n{RinaCondition=" + rinaCondition.toString() + "}";
	}

	/**
	 * 
	 * @return true if condition is met, false otherwise
	 */
	public boolean evaluate() {
		return rinaCondition.evaluate();
	}

	/**
	 * 
	 * @return condition part that failed, or null if it succeeded
	 */
	public Entry<String, String> evaluateOrGet() {
		return rinaCondition.evaluateOrGetFailure();
	}

	/**
	 * Applies a function then evaluates the condition
	 * 
	 * @param function function to apply on the script condition before evaluation
	 * @return true if condition is met, false otherwise
	 */
	public boolean applyThenEvaluate(Function<String, String> function) {
		return rinaCondition.applyThenEvaluate(function);
	}

	/**
	 * Applies a function then evaluates the condition
	 * 
	 * @param function function to apply on the script condition before evaluation
	 * @return null if condition is met, otherwise returns failed condition part
	 */
	public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
		return rinaCondition.applyThenEvaluateOrGet(function);
	}

	/**
	 * @return whether the conditions match the given rina object conditions. The
	 *         scripts don't have to be exactly the same
	 *         for this to return true due to the fact that they get simplified
	 *         after creation. {@linkplain #cleanExtras(String)} is where things
	 *         mostly get simplified.
	 */
	@Override
	public boolean equals(Object object) {
		return object == null ? false
				: getRinaCondition().toString().equals(((RinaLegacy) object).getRinaCondition().toString());
	}

	private static RinaCondition parseScript(String script) {
		if (script == null || script.isEmpty()) {
			System.out.println("Oopsie... Provided sssss..sscript is null or empty!");
			return null;
		}
		RinaCondition rinaCondition = null;
		RinaOrGroup orGroup = new RinaOrGroup();
		RinaAndGroup andGroup = new RinaAndGroup();
		boolean usingOrGroup = false;
		boolean hasOrGroup = containsOrIgnore(script, SCRIPT_OR, c -> c == STRING_QUOTATION);
		boolean hasAndGroup = containsOrIgnore(script, SCRIPT_AND, c -> c == STRING_QUOTATION);
		if (hasOrGroup && hasAndGroup) {
			rinaCondition = parseComplexRinaCondition(script);
		} else if (hasOrGroup) {
			orGroup = RinaCondition.addConditions(orGroup, script);
			usingOrGroup = true;
		} else if (hasAndGroup) {
			andGroup = RinaCondition.addConditions(andGroup, script);
		} else {
			rinaCondition = RinaCondition.parseCondition(script);
		}
		return rinaCondition != null ? rinaCondition : usingOrGroup ? orGroup : andGroup;
	}

	private static RinaCondition parseComplexRinaCondition(String script) {
		List<String> parentGroups = parseGroups(script);
		if (parentGroups.isEmpty()) return null;
		List<String> parentOrands = parseOrands(script);
		if (parentOrands.isEmpty()) return null;
		RinaOrGroup orGroup = new RinaOrGroup();
		RinaAndGroup andGroup = new RinaAndGroup();
		boolean usingOrGroup = false;
		int groupPos = 0;
		for (String parentGroup : parentGroups) {
			String orand = parentOrands.get(groupPos == parentOrands.size() ? groupPos - 1 : groupPos);
			if (orand.equals(SCRIPT_AND)) {
				orGroup = RinaCondition.addConditions(orGroup, parentGroup);
				andGroup.addRinaCondition(new RinaOrGroup(orGroup.getConditions()));
				orGroup = new RinaOrGroup();
			} else if (orand.equals(SCRIPT_OR)) {
				andGroup = RinaCondition.addConditions(andGroup, parentGroup);
				orGroup.addRinaCondition(new RinaAndGroup(andGroup.getConditions()));
				andGroup = new RinaAndGroup();
				usingOrGroup = true;
			}
			groupPos++;
		}
		return usingOrGroup ? orGroup : andGroup;
	}

	private static boolean hasEscapingChar(char[] charArray, int index) {
		return ((index - 1 > -1 && charArray[index - 1] == CHAR_SCRIPT_ESCAPING));
	}

	/**
	 * Cleans unnecessary spaces and brackets like the following:
	 * <p>
	 * 
	 * <pre>
	 * {@code (((('equal string'        == 'equal string'))))}
	 * </pre>
	 * </p>
	 * turns into:
	 * <p>
	 * {@code ('equal string'=='equal string')}
	 * </p>
	 * 
	 * @param string string to clean
	 * @return cleaned string
	 */
	private static String cleanExtras(String string) {
		if (string == null) return null;
		char[] charArray = string.toCharArray();
		StringBuilder spaceCleaner = new StringBuilder(string);
		boolean stringQuotation = false;

		for (int index = 0; index < charArray.length; index++) {
			char c = charArray[index];
			if (c == STRING_QUOTATION && !hasEscapingChar(charArray, index)) stringQuotation = !stringQuotation;
			if (c == ' ' && !stringQuotation) {
				spaceCleaner.deleteCharAt(index);
				charArray = spaceCleaner.toString().toCharArray();
				index--;
			}
		}

		string = spaceCleaner.toString();
		int stopIndex = 0;
		int endingStopIndex = charArray.length;
		if (string.startsWith(SCRIPT_AND + SCRIPT_GROUP_OPENING)
				|| string.startsWith(SCRIPT_OR + SCRIPT_GROUP_OPENING)) {
			for (char c : charArray) {
				if (!SPLITTERS.contains(c) && c != CHAR_SCRIPT_GROUP_OPENING) break;
				stopIndex++;
			}
		} else if (string.startsWith(SCRIPT_GROUP_OPENING)) {
			for (char c : charArray) {
				if (c != CHAR_SCRIPT_GROUP_OPENING) break;
				stopIndex++;
			}
		}
		if (string.endsWith(SCRIPT_GROUP_CLOSING)) {
			for (int i = charArray.length - 1; i > string.indexOf(CHAR_SCRIPT_GROUP_CLOSING) - 1; i--) {
				endingStopIndex--;
				if (charArray[i] != CHAR_SCRIPT_GROUP_CLOSING) break;
			}
		}
		return string = endingStopIndex != charArray.length ? string.substring(stopIndex, endingStopIndex)
				: string.substring(stopIndex);
	}

	private static String[] splitOrIgnore(String string, String separator, Predicate<Character> ignoreIf) {
		string = string + separator;
		char[] charArray = string.toCharArray();
		boolean pauseCollecting = false;
		int separatorCounter = 0;
		int separatorLength = separator.length();
		StringBuilder charCollector = new StringBuilder();
		List<String> stringList = new ArrayList<>();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			charCollector.append(c);
			if (ignoreIf.test(c) && !hasEscapingChar(charArray, i)) pauseCollecting = !pauseCollecting;
			if (c == separator.charAt(separatorCounter) && !pauseCollecting) separatorCounter++;
			if (separatorCounter == separatorLength) {
				stringList.add(charCollector.substring(0, charCollector.length() - separatorLength));
				charCollector.delete(0, charCollector.length());
				separatorCounter = 0;
			}
		}
		return stringList.toArray(new String[0]);
	}

	// A little bit faster than the method above if we are pretty certain of the
	// amount of the splits
	private static String[] splitOrIgnore(String string, String separator, Predicate<Character> ignoreIf, int limit) {
		string = string + separator;
		char[] charArray = string.toCharArray();
		boolean pauseCollecting = false;
		int separatorCounter = 0;
		int separatorLength = separator.length();
		int cycles = 0;
		String[] stringArray = new String[limit + 1];
		StringBuilder charCollector = new StringBuilder();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			charCollector.append(c);
			if (ignoreIf.test(c) && !hasEscapingChar(charArray, i)) pauseCollecting = !pauseCollecting;
			if (c == separator.charAt(separatorCounter) && !pauseCollecting) separatorCounter++;
			if (separatorCounter == separatorLength) {
				stringArray[cycles] = charCollector.substring(0, charCollector.length() - separatorLength);
				charCollector.delete(0, charCollector.length());
				separatorCounter = 0;
				cycles++;
			}
		}
		return stringArray;
	}

	private static boolean containsOrIgnore(String string, String target, Predicate<Character> ignoreIf) {
		char[] charArray = string.toCharArray();
		boolean skipCollecting = false;
		int targetCounter = 0;
		int targetLength = target.length();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (ignoreIf.test(c) && !hasEscapingChar(charArray, i)) skipCollecting = !skipCollecting;
			if (c == target.charAt(targetCounter) && !skipCollecting) targetCounter++;
			if (targetCounter >= targetLength) return true;
		}
		return false;
	}

	private static boolean containsOrIgnore(String string, Set<Character> target, Predicate<Character> ignoreIf) {
		char[] charArray = string.toCharArray();
		boolean skipCollecting = false;
		int targetCounter = 0;
		int targetLength = 1;
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (ignoreIf.test(c) && !hasEscapingChar(charArray, i)) skipCollecting = !skipCollecting;
			if (target.contains(c) && !skipCollecting) targetCounter++;
			if (targetCounter >= targetLength) return true;
		}
		return false;
	}

	private static boolean containsDouble(String string) {
		char[] charArray = string.toCharArray();
		int pointIndex = string.indexOf('.');
		// point is at the beginning (0), doesn't exist (index equals -1), or at the
		// end, surely not a double.
		if (pointIndex < 1 || charArray.length - 1 == pointIndex) return false;
		for (int i = 0; i < charArray.length - pointIndex; i++) {
			char nextChar = charArray[pointIndex + i];
			int negativeIndex = pointIndex - i;
			// char won't affect anything, just a sign that it's null as primitive chars
			// can't be set to null.
			char previousChar = negativeIndex < 0 ? '?' : charArray[negativeIndex];
			if (previousChar == '?') break;
			String doubleNum = previousChar + "." + nextChar;
			if (MATH_DOUBLE.contains(doubleNum) || MATH_DOUBLE.contains("-" + doubleNum)) return true;
		}
		return false;
	}

	private static List<String> getBetween(String string, char opening, char closing,
			@Nullable Function<String, String> function) {
		List<String> collectedStrings = new ArrayList<>();
		char[] charArray = string.toCharArray();
		boolean foundOpening = false;
		boolean foundInnerOpening = false;
		boolean pauseCollecting = false;
		int processedInnerOpenings = 0;
		int innerOpenings = 0;
		StringBuilder charCollector = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (foundOpening) {
				charCollector.append(c);
				if (c == opening) {
					foundInnerOpening = true;
					innerOpenings++;
				}
				if (c == closing) {
					if (foundInnerOpening) {
						if (processedInnerOpenings++ == innerOpenings - 1) foundInnerOpening = false;
					} else {
						String finalString = charCollector.substring(0, charCollector.length() - 1);
						collectedStrings.add(function == null ? finalString : function.apply(finalString));
						charCollector.delete(0, charCollector.length());
						foundOpening = false;
						foundInnerOpening = false;
					}
				}
			}
			if (c == STRING_QUOTATION && !hasEscapingChar(charArray, i)) pauseCollecting = !pauseCollecting;
			if (c == opening && !pauseCollecting) foundOpening = true;
		}
		return collectedStrings;
	}

	private static List<String> getCentrallyLocated(String string, char opening, char closing,
			@Nullable Function<String, String> function) {
		List<String> collectedStrings = new ArrayList<>();
		char[] charArray = string.toCharArray();
		boolean foundOpening = false;
		boolean foundInnerOpening = false;
		boolean collectInBetween = false;
		boolean pauseCollecting = false;
		int processedInnerOpenings = 0;
		int innerOpenings = 0;
		StringBuilder charCollector = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (foundOpening) {
				if (c == opening) {
					foundInnerOpening = true;
					innerOpenings++;
				}
				if (collectInBetween) {
					collectInBetween = false;
					String finalString = charCollector.substring(0, charCollector.length() - 1);
					collectedStrings.add(function == null ? finalString : function.apply(finalString));
					charCollector.delete(0, charCollector.length());
				}
				if (c == closing) {
					if (foundInnerOpening) {
						if (processedInnerOpenings++ == innerOpenings - 1) foundInnerOpening = false;
					} else {
						collectInBetween = true;
						foundOpening = false;
						foundInnerOpening = false;
					}
				}
			} else {
				if (collectInBetween) charCollector.append(c);
			}
			if (c == STRING_QUOTATION && !hasEscapingChar(charArray, i)) pauseCollecting = !pauseCollecting;
			if (c == opening && !pauseCollecting) foundOpening = true;
		}
		return collectedStrings;
	}

	private static String processEscapedChars(String string) {
		for (Entry<String, String> characters : REPLACABLE.entrySet())
			string = string.replace(characters.getKey(), characters.getValue());
		return string;
	}

	private static List<String> parseGroups(String string) {
		return getBetween(string, CHAR_SCRIPT_GROUP_OPENING, CHAR_SCRIPT_GROUP_CLOSING, null);
	}

	private static List<String> parseOrands(String string) {
		return getCentrallyLocated(string, CHAR_SCRIPT_GROUP_OPENING, CHAR_SCRIPT_GROUP_CLOSING, s -> s.trim());
	}

	private static boolean isBeneficialAsDouble(double number) {
		return number % 1 != 0;
	}

	public enum ConditionType {
		EQUAL,
		NOT_EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL,
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		STRING_FUNCTION,
		OR_GROUP,
		AND_GROUP;
	}

	public static interface RinaCondition {

		public static RinaCondition parseCondition(String scriptCondition) {
			String cleanCondition = cleanExtras(scriptCondition);
			if (containsOrIgnore(cleanCondition, SCRIPT_EQUAL, c -> c == STRING_QUOTATION)) { // ==
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_EQUAL, c -> c == STRING_QUOTATION, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_NOT_EQUAL, c -> c == STRING_QUOTATION)) { // !=
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_NOT_EQUAL, c -> c == STRING_QUOTATION, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaNotEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_GREATER_THAN_OR_EQUAL, c -> c == STRING_QUOTATION)) { // >=
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_GREATER_THAN_OR_EQUAL, c -> c == STRING_QUOTATION,
						2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaGreaterThanOrEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_GREATER_THAN, c -> c == STRING_QUOTATION)) { // >
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_GREATER_THAN, c -> c == STRING_QUOTATION, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaGreaterThan(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_LESS_THAN_OR_EQUAL, c -> c == STRING_QUOTATION)) { // <=
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_LESS_THAN_OR_EQUAL, c -> c == STRING_QUOTATION,
						2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaLessThanOrEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_LESS_THAN, c -> c == STRING_QUOTATION)) { // <
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_LESS_THAN, c -> c == STRING_QUOTATION, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaLessThan(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, ".", c -> c == STRING_QUOTATION)) {
				// I rushed this crap, hence the limitations
				String[] split = splitOrIgnore(cleanCondition, ".", c -> c == STRING_QUOTATION, 2);
				String leftSection = split[0];
				leftSection = leftSection.substring(0, leftSection.length() - 1);
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				String[] splitFunction = splitOrIgnore(split[1], "(", c -> c == STRING_QUOTATION, 2);
				String methodName = splitFunction[0];
				String value = splitFunction[1];
				value = value.substring(0, value.length() - 1);
				return new RinaStringFunction(reverse ? leftSection.substring(2) : leftSection.substring(1),
						value.substring(1), methodName, reverse);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public static <T extends RinaCondition> T addConditions(T rinaCondition, String scriptConditions) {
			if (rinaCondition instanceof RinaOrGroup) {
				RinaOrGroup rinaOrGroup = (RinaOrGroup) rinaCondition;
				for (String condition : splitOrIgnore(scriptConditions, SCRIPT_OR, c -> c == STRING_QUOTATION))
					rinaOrGroup.addRinaCondition(parseCondition(condition));
				return (T) rinaOrGroup;
			} else if (rinaCondition instanceof RinaAndGroup) {
				RinaAndGroup rinaAndGroup = (RinaAndGroup) rinaCondition;
				for (String condition : splitOrIgnore(scriptConditions, SCRIPT_AND, c -> c == STRING_QUOTATION))
					rinaAndGroup.addRinaCondition(parseCondition(condition));
				return (T) rinaAndGroup;
			}
			return rinaCondition = (T) parseCondition(scriptConditions);
		}

		/**
		 * 
		 * @return true if condition is met, false otherwise
		 */
		public boolean evaluate();

		/**
		 * Applies a function then evaluates the condition(s)
		 * 
		 * @param function function to apply on the script condition(s)
		 * @return true if condition is met, false otherwise
		 */
		public boolean applyThenEvaluate(Function<String, String> function);

		/**
		 * Applies a function then evaluates the condition(s)
		 * 
		 * @param function function to apply on the script condition(s)
		 * @return true if condition is met, false otherwise
		 */
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function);

		/**
		 * 
		 * @return condition part that failed or null if it succeeded
		 */
		@Nullable
		public Entry<String, String> evaluateOrGetFailure();

		/**
		 * Script conditions that have been parsed and stored in this rina condition
		 */
		public <T> T getParsedConditions();

		/**
		 * Type of the condition(s) getting checked
		 */
		public ConditionType getConditionType();

		/**
		 * 
		 * @return String that has been transformed into this rina condition
		 */
		public String getStringCondition();
	}

	private static interface RinaGroup {

		public Set<RinaCondition> getConditions();

	}

	private static interface ExpressionEvaluator {

		default String evaluateExp(String string) {
			return string;
		}

		boolean isDouble();

		ExpressionEvaluator setDouble(boolean doubleResult);

		/**
		 * 
		 * @return whether string is a valid math expression
		 */
		boolean isEvaluatable();

		public static ExpressionEvaluator newEvaluator(String string) {
			boolean isPossiblyDouble = containsOrIgnore(string, ".", c -> c == STRING_QUOTATION);
			if (!containsOrIgnore(string, MATH, c -> c == STRING_QUOTATION))
				/*
				 * It could be a double with a pointless fractional part? In this case, we make
				 * it
				 * evaluatable so 1.0 can equal 1
				 */
				return isPossiblyDouble && containsDouble(string) ? new EvaluatableExpression(true)
						: new NonEvaluatableExpression();

			return new EvaluatableExpression(true);
		}

	}

	private static class EvaluatableExpression implements ExpressionEvaluator {

		private boolean doubleResult;

		public EvaluatableExpression(boolean doubleResult) {
			this.doubleResult = doubleResult;
		}

		@Override
		public boolean isDouble() {
			return doubleResult;
		}

		@Override
		public EvaluatableExpression setDouble(boolean doubleResult) {
			this.doubleResult = doubleResult;
			return this;
		}

		@Override
		public String evaluateExp(String string) {
			return evaluateMathExpression(string, doubleResult);
		}

		@Override
		public boolean isEvaluatable() {
			return true;
		}

	}

	private static class NonEvaluatableExpression implements ExpressionEvaluator {

		@Override
		public boolean isEvaluatable() {
			return false;
		}

		@Override
		public boolean isDouble() {
			return false;
		}

		@Override
		public NonEvaluatableExpression setDouble(boolean doubleResult) {
			return this;
		}

	}

	public static abstract class RinaConditional {

		protected Entry<String, String> condition;
		protected boolean reverse;
		protected String reverseSymbol = "";
		protected String stringCondition;
		protected ExpressionEvaluator keyEvaluator, valueEvaluator;

		public RinaConditional(String key, String value, boolean reverse) {
			key = processEscapedChars(key);
			value = processEscapedChars(value);
			this.reverse = reverse;
			condition = new SimpleEntry<String, String>(key, value);
			if (reverse) {
				reverseSymbol = SCRIPT_REVERSE;
				stringCondition = SCRIPT_REVERSE + key + "?" + value;
			} else {
				stringCondition = key + "?" + value;
			}
			keyEvaluator = ExpressionEvaluator.newEvaluator(key);
			valueEvaluator = ExpressionEvaluator.newEvaluator(value);
		}

		public boolean isReverse() {
			return reverse;
		}

		public ExpressionEvaluator getKeyEvaluator() {
			return keyEvaluator;
		}

		public ExpressionEvaluator getValueEvaluator() {
			return valueEvaluator;
		}

	}

	public static class RinaStringFunction extends RinaConditional implements RinaCondition {

		private String methodName;

		public RinaStringFunction(String key, String value, String methodName) {
			this(key, value, methodName, false);
		}

		public RinaStringFunction(String key, String value, String methodName, boolean reverse) {
			super(key, value, reverse);
			this.methodName = methodName;
		}

		@Override
		public boolean evaluate() {
			boolean result = false;
			try {
				result = (boolean) ONE_STRING_PARAM_STRING_METHODS.get(methodName)
						.invoke(condition.getKey(), condition.getValue());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return result != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaStringFunction{" + condition.getKey() + "." + methodName + "("
					+ condition.getValue() + ")}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			boolean result = false;
			try {
				result = (boolean) ONE_STRING_PARAM_STRING_METHODS.get(methodName)
						.invoke(function.apply(condition.getKey()), function.apply(condition.getValue()));
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return result != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(function.apply(condition.getKey()),
					function.apply(condition.getValue()));
			boolean result = false;
			try {
				result = (boolean) ONE_STRING_PARAM_STRING_METHODS.get(methodName)
						.invoke(evaluation.getKey(), evaluation.getValue());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return result != reverse ? null : evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.STRING_FUNCTION;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	public static class RinaEqual extends RinaConditional implements RinaCondition {

		public RinaEqual(String key, String value) {
			this(key, value, false);
		}

		public RinaEqual(String key, String value, boolean reverse) {
			super(key, value, reverse);
		}

		@Override
		public boolean evaluate() {
			return keyEvaluator.evaluateExp(condition.getKey())
					.equals(valueEvaluator.evaluateExp(condition.getValue())) != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaEqual{" + condition.getKey() + SCRIPT_EQUAL + condition.getValue() + "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return keyEvaluator.evaluateExp(function.apply(condition.getKey()))
					.equals(valueEvaluator.evaluateExp(function.apply(condition.getValue()))) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(
					keyEvaluator.evaluateExp(function.apply(condition.getKey())),
					valueEvaluator.evaluateExp(function.apply(condition.getValue())));
			return evaluation.getKey().equals(evaluation.getValue()) != reverse ? null : evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.EQUAL;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	public static class RinaNotEqual extends RinaConditional implements RinaCondition {

		public RinaNotEqual(String key, String value) {
			this(key, value, false);
		}

		public RinaNotEqual(String key, String value, boolean reverse) {
			super(key, value, reverse);
		}

		@Override
		public boolean evaluate() {
			return !keyEvaluator.evaluateExp(condition.getKey())
					.equals(valueEvaluator.evaluateExp(condition.getValue())) != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaEqual{" + condition.getKey() + SCRIPT_NOT_EQUAL + condition.getValue() + "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return !keyEvaluator.evaluateExp(function.apply(condition.getKey()))
					.equals(valueEvaluator.evaluateExp(function.apply(condition.getValue()))) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(
					keyEvaluator.evaluateExp(function.apply(condition.getKey())),
					valueEvaluator.evaluateExp(function.apply(condition.getValue())));
			return !evaluation.getKey().equals(evaluation.getValue()) != reverse ? null : evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.NOT_EQUAL;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	public static class RinaGreaterThan extends RinaConditional implements RinaCondition {

		public RinaGreaterThan(String key, String value) {
			this(key, value, false);
		}

		public RinaGreaterThan(String key, String value, boolean reverse) {
			super(key, value, reverse);
		}

		@Override
		public boolean evaluate() {
			return Double.parseDouble(keyEvaluator.evaluateExp(condition.getKey())) > Double
					.parseDouble(valueEvaluator.evaluateExp(condition.getValue())) != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaGreaterThan{" + condition.getKey() + SCRIPT_GREATER_THAN + condition.getValue()
					+ "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return Double.parseDouble(keyEvaluator.evaluateExp(function.apply(condition.getKey()))) > Double
					.parseDouble(valueEvaluator.evaluateExp(function.apply(condition.getValue()))) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(
					keyEvaluator.evaluateExp(function.apply(condition.getKey())),
					valueEvaluator.evaluateExp(function.apply(condition.getValue())));
			return Double.parseDouble(evaluation.getKey()) > Double.parseDouble(evaluation.getValue()) != reverse ? null
					: evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.GREATER_THAN;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	public static class RinaGreaterThanOrEqual extends RinaConditional implements RinaCondition {

		public RinaGreaterThanOrEqual(String key, String value) {
			this(key, value, false);
		}

		public RinaGreaterThanOrEqual(String key, String value, boolean reverse) {
			super(key, value, reverse);
		}

		@Override
		public boolean evaluate() {
			return Double.parseDouble(keyEvaluator.evaluateExp(condition.getKey())) >= Double
					.parseDouble(valueEvaluator.evaluateExp(condition.getValue())) != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaGreaterThanOrEqual{" + condition.getKey() + SCRIPT_GREATER_THAN_OR_EQUAL
					+ condition.getValue() + "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return Double.parseDouble(keyEvaluator.evaluateExp(function.apply(condition.getKey()))) >= Double
					.parseDouble(valueEvaluator.evaluateExp(function.apply(condition.getValue()))) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(
					keyEvaluator.evaluateExp(function.apply(condition.getKey())),
					valueEvaluator.evaluateExp(function.apply(condition.getValue())));
			return Double.parseDouble(evaluation.getKey()) >= Double.parseDouble(evaluation.getValue()) != reverse
					? null : evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.GREATER_THAN_OR_EQUAL;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	public static class RinaLessThan extends RinaConditional implements RinaCondition {

		public RinaLessThan(String key, String value) {
			this(key, value, false);
		}

		public RinaLessThan(String key, String value, boolean reverse) {
			super(key, value, reverse);
		}

		@Override
		public boolean evaluate() {
			return Double.parseDouble(keyEvaluator.evaluateExp(condition.getKey())) < Double
					.parseDouble(valueEvaluator.evaluateExp(condition.getValue())) != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaLessThan{" + condition.getKey() + SCRIPT_LESS_THAN + condition.getValue() + "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return Double.parseDouble(keyEvaluator.evaluateExp(function.apply(condition.getKey()))) < Double
					.parseDouble(valueEvaluator.evaluateExp(function.apply(condition.getValue()))) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(
					keyEvaluator.evaluateExp(function.apply(condition.getKey())),
					valueEvaluator.evaluateExp(function.apply(condition.getValue())));
			return Double.parseDouble(evaluation.getKey()) < Double.parseDouble(evaluation.getValue()) != reverse ? null
					: evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.LESS_THAN;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	public static class RinaLessThanOrEqual extends RinaConditional implements RinaCondition {

		public RinaLessThanOrEqual(String key, String value) {
			this(key, value, false);
		}

		public RinaLessThanOrEqual(String key, String value, boolean reverse) {
			super(key, value, reverse);
		}

		@Override
		public boolean evaluate() {
			return Double.parseDouble(keyEvaluator.evaluateExp(condition.getKey())) <= Double
					.parseDouble(valueEvaluator.evaluateExp(condition.getValue())) != reverse;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			return !evaluate() ? condition : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<String, String> getParsedConditions() {
			return condition;
		}

		@Override
		public String toString() {
			return reverseSymbol + "RinaLessThanOrEqual{" + condition.getKey() + SCRIPT_LESS_THAN_OR_EQUAL
					+ condition.getValue() + "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return Double.parseDouble(keyEvaluator.evaluateExp(function.apply(condition.getKey()))) <= Double
					.parseDouble(valueEvaluator.evaluateExp(function.apply(condition.getValue()))) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(
					keyEvaluator.evaluateExp(function.apply(condition.getKey())),
					valueEvaluator.evaluateExp(function.apply(condition.getValue())));
			return Double.parseDouble(evaluation.getKey()) <= Double.parseDouble(evaluation.getValue()) != reverse
					? null : evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.LESS_THAN_OR_EQUAL;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	private static class RinaOrGroup implements RinaCondition, RinaGroup {

		private Set<RinaCondition> rinaConditions;
		private String stringCondition;

		public RinaOrGroup() {
			this(new LinkedHashSet<>());
		}

		public RinaOrGroup(Set<RinaCondition> rinaConditions) {
			this.rinaConditions = rinaConditions;
			if (rinaConditions != null) {
				for (RinaCondition rinaCondition : rinaConditions)
					stringCondition += rinaCondition.getStringCondition();
			}
		}

		public void addRinaCondition(RinaCondition rinaCondition) {
			if (rinaCondition == null) return;
			rinaConditions.add(rinaCondition);
		}

		@Override
		public boolean evaluate() {
			for (RinaCondition rinaCondition : rinaConditions) if (rinaCondition.evaluate()) return true;
			return false;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			RinaCondition evaluatedRinaCondition = null;
			for (RinaCondition rinaCondition : rinaConditions) {
				evaluatedRinaCondition = rinaCondition;
				if (rinaCondition.evaluate()) return null;
			}
			return evaluatedRinaCondition.getParsedConditions();
		}

		@Override
		public String toString() {
			return "RinaOrGroup{" + rinaConditions.toString() + "}";
		}

		@Override
		public Set<RinaCondition> getConditions() {
			return rinaConditions;
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			for (RinaCondition rinaCondition : rinaConditions)
				if (rinaCondition.applyThenEvaluate(function)) return true;
			return false;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = null;
			for (RinaCondition rinaCondition : rinaConditions) {
				evaluation = rinaCondition.applyThenEvaluateOrGet(function);
				if (evaluation == null) return null;
			}
			return evaluation;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Set<RinaCondition> getParsedConditions() {
			return rinaConditions;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.OR_GROUP;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	private static class RinaAndGroup implements RinaCondition, RinaGroup {

		private Set<RinaCondition> rinaConditions;
		private String stringCondition;

		public RinaAndGroup() {
			this(new LinkedHashSet<>());
		}

		public RinaAndGroup(Set<RinaCondition> rinaConditions) {
			this.rinaConditions = rinaConditions;
			if (rinaConditions != null) {
				for (RinaCondition rinaCondition : rinaConditions)
					stringCondition += rinaCondition.getStringCondition();
			}
		}

		public void addRinaCondition(RinaCondition rinaCondition) {
			if (rinaCondition == null) return;
			rinaConditions.add(rinaCondition);
		}

		@Override
		public boolean evaluate() {
			for (RinaCondition rinaCondition : rinaConditions) if (!rinaCondition.evaluate()) return false;
			return true;
		}

		@Override
		public Entry<String, String> evaluateOrGetFailure() {
			for (RinaCondition rinaCondition : rinaConditions)
				if (!rinaCondition.evaluate()) return rinaCondition.getParsedConditions();
			return null;
		}

		@Override
		public String toString() {
			return "RinaAndGroup{" + rinaConditions.toString() + "}";
		}

		@Override
		public Set<RinaCondition> getConditions() {
			return rinaConditions;
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			for (RinaCondition rinaCondition : rinaConditions)
				if (!rinaCondition.applyThenEvaluate(function)) return false;
			return true;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = null;
			for (RinaCondition rinaCondition : rinaConditions) {
				evaluation = rinaCondition.applyThenEvaluateOrGet(function);
				if (evaluation != null) return evaluation;
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Set<RinaCondition> getParsedConditions() {
			return rinaConditions;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.AND_GROUP;
		}

		@Override
		public String getStringCondition() {
			return stringCondition;
		}

	}

	/**
	 * @author <This method was made by
	 *         <a href="https://stackoverflow.com/users/964243/boann">Boann</a>, all
	 *         thanks to him.
	 * @param mathExpression string expression containing math operations to
	 *                       calculate
	 * @return result of the given math expression
	 */
	public static String evaluateMathExpression(String mathExpression) {
		return evaluateMathExpression(mathExpression, true);
	}

	/**
	 * @author This method was made by
	 *         <a href="https://stackoverflow.com/users/964243/boann">Boann</a>, all
	 *         thanks to him.
	 * @param str          math expression to calculate
	 * @param doubleResult whether to give the result with decimal numbers or not
	 *                     (double or long)
	 * @return result of the given math expression
	 */
	private static String evaluateMathExpression(final String str, boolean doubleResult) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			String parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) return str;
				return doubleResult && isBeneficialAsDouble(x) ? String.valueOf(x) : String.valueOf((long) x);
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)` | number
			// | functionName `(` expression `)` | functionName factor
			// | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if (eat('+'))
						x += parseTerm(); // addition
					else if (eat('-'))
						x -= parseTerm(); // subtraction
					else
						return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if (eat('*'))
						x *= parseFactor(); // multiplication
					else if (eat('/'))
						x /= parseFactor(); // division
					else
						return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return +parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					if (!eat(')')) throw new RuntimeException("Missing ')'");
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					if (eat('(')) {
						x = parseExpression();
						if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
					} else {
						x = parseFactor();
					}
					if (func.equals("sqrt"))
						x = Math.sqrt(x);
					else if (func.equals("sin"))
						x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos"))
						x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan"))
						x = Math.tan(Math.toRadians(x));
					else
						throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char) ch);
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}

}
