/*
 * Why this?
 * Nashron library is too big for a plugin.
 * All I want is basic JavaScript condition format.
 * No dependencies.
 * Single class.
 * Cached conditions.
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * <b>R</b>ubbish <b>I</b>f <b>N</b>ot <b>A</b>ctual conditions.
 * <p>
 * Create evaluatable conditions in JavaScript format.
 * </p>
 * <p>
 * Examples of valid conditions (all of them return
 * true when evaluated):
 * </p>
 * <ul>
 * <li><b>Basic:</b>
 * {@code Rina.create("'exampleString'=='exampleString'");}</li>
 * <li><b>With Spaces:</b>
 * {@code Rina.create("'String (with) spaces'=='String (with) spaces'");}</li>
 * <li><b>Escape characters:</b>
 * {@code Rina.create("'String with \\'quotes\\''=='String with \\'quotes\\''");}</li>
 * <li><b>Reverse:</b> {@code Rina.create("!'string'=='anotherstring'");}</li>
 * <li><b>Reverse (2):</b>
 * {@code Rina.create("'string'!='anotherstring'");}</li>
 * <li><b>Or:</b>
 * {@code Rina.create("'spaces spaces'=='spaces spaces'||'spaces spaces2'=='spaces spaces2'");}</li>
 * <li><b>And:</b>
 * {@code Rina.create("'string'=='string'&&'string2'!='string3'");}</li>
 * <li><b>Combined:</b>
 * {@code Rina.create("(string==string2||5==5.0)&&(5!=5||!3==2)");}</li>
 * <li><b>Math Condition:</b> {@code Rina.create("5>=4+1");}</li>
 * </ul>
 * <p>
 * Made specifically for Minecraft, most likely won't add variables support.
 * </p>
 * <p>
 * Example:
 * </p>
 * Cache and store the object somewhere:
 * <br>
 * {@code Rina myCondition = Rina.create("%variable%=='something'")}
 * <br>
 * <br>
 * Say x is a changing variable like a PlaceholderAPI placeholder or something.
 * <br>
 * {@code String x = "'something'";}
 * <br>
 * <br>
 * Then you will perform this method repeatedly.
 * <br>
 * {@code myCondition.applyThenEvaluate(s -> s.replace("%variable%", x));}
 * <p>
 * 
 * @author TheGaming999
 */
public class Rina {

	private String script;
	private RinaCondition rinaCondition;
	private Map<String, Object> assignedVariables;

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

	// Characters that are used to detect orands
	private static final Set<Character> SPLITTERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList('|', '&')));

	// Characters that are used to detect math operations
	// <!-- Shouldn't be touched !-->
	private static final Set<Character> MATH = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList('+', '-', '*', '/', '^')));

	// Characters that can be escaped
	private static final Set<String> ESCAPABLES = new HashSet<>(Arrays.asList("'"));

	// Holds escapables values in a map to prevent constant creation of strings when
	// escapable characters are processed
	// <!-- Shouldn't be touched !-->
	private static final Map<String, String> REPLACABLE = Collections
			.unmodifiableMap(ESCAPABLES.stream().collect(Collectors.toMap(s -> SCRIPT_ESCAPING + s, s -> s)));

	// Holds values from 0.0 to 9.9 for double number detection
	// Negative values are detected within a method
	// <!-- Shouldn't be touched !-->
	private static final Set<String> MATH_DOUBLE = Collections.unmodifiableSet(
			IntStream.range(0, 100).mapToObj(d -> String.valueOf(d / 10.0)).collect(Collectors.toSet()));

	public static final Set<Character> MATH_INTEGER = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')));

	// Most used predicate
	// <!-- Shouldn't be touched !-->
	public static final Predicate<Character> QUOTATION_IGNORE = q -> q == STRING_QUOTATION;

	private static final Map<Class<?>, Map<String, Map<List<Class<?>>, MethodHandle>>> exposedMethods = HashMaps
			.empty();

	private static final Map<Class<?>, Class<?>> similarClasses = HashMaps
			.<Class<?>, Class<?>>of(CharSequence.class, String.class)
			.and(Integer.class, int.class)
			.and(Double.class, double.class)
			.and(Boolean.class, boolean.class)
			.build();

	public static class HashMaps<T, V> {

		private Map<T, V> map;

		public HashMaps(T t, V v) {
			this.map = new HashMap<T, V>();
			map.put(t, v);
		}

		public HashMaps<T, V> and(T t, V v) {
			map.put(t, v);
			return this;
		}

		public Map<T, V> build() {
			return map;
		}

		public static <A, B> HashMaps<A, B> of(A t, B v) {
			return new HashMaps<A, B>(t, v);
		}

		public static <A, B> Map<A, B> empty() {
			return new HashMap<A, B>();
		}

	}

	static {
		// No need for the set after its values are inserted into REPLACEABLE map.
		ESCAPABLES.clear();
		exposeMethods(String.class);
	}

	private static List<Class<?>> fixSimilarities(List<Class<?>> classes) {
		List<Class<?>> newClasses = new ArrayList<>();
		for (Class<?> clazz : classes)
			newClasses.add(similarClasses.containsKey(clazz) ? similarClasses.get(clazz) : clazz);
		return newClasses;
	}

	/**
	 * Gives access to methods of a certain class in conditions. String methods are
	 * exposed by default.
	 * 
	 * @param <T>   class type
	 * @param clazz class to retrieve methods from
	 * @return a map consisting of keys representing method names and values
	 *         represting a map that contains keys with different method parameters
	 *         that has the same method name and
	 *         method handle values that correspond to these parameters
	 */
	public static <T> Map<String, Map<List<Class<?>>, MethodHandle>> exposeMethods(@NotNull Class<T> clazz) {
		if (clazz == null)
			throw new NullPointerException("Unable to expose null class! Make sure the given class actually exists.");
		if (exposedMethods.containsKey(clazz))
			throw new IllegalArgumentException("Class: (" + clazz + ") methods are already exposed!");
		Map<String, Map<List<Class<?>>, MethodHandle>> methods = new HashMap<>();
		exposedMethods.put(clazz, addMethods(clazz, clazz.getDeclaredMethods(), false));
		methods.putAll(exposedMethods.get(clazz));
		return methods;
	}

	@Nullable
	public static <T> Map<String, Map<List<Class<?>>, MethodHandle>> unexposeMethods(Class<T> clazz) {
		return exposedMethods.remove(clazz);
	}

	public static void setSimilar(Class<?> fromClass, @Nullable Class<?> toClass, boolean similar) {
		if (similar && toClass != null)
			similarClasses.put(fromClass, toClass);
		else
			similarClasses.remove(fromClass);
	}

	private static Map<String, Map<List<Class<?>>, MethodHandle>> addMethods(Class<?> clazz, Method[] methodArray,
			boolean staticMethods) {
		Map<String, Map<List<Class<?>>, MethodHandle>> methods = new HashMap<>();
		for (Method m : methodArray) {
			int modifiers = m.getModifiers();
			if (Modifier.isPublic(modifiers)) {
				if (Modifier.isStatic(modifiers) == staticMethods) {
					String methodName = m.getName();
					Map<List<Class<?>>, MethodHandle> methodsWithParams = methods.containsKey(methodName)
							? methods.get(methodName) : new LinkedHashMap<>();
					try {
						MethodHandle mh = staticMethods
								? MethodHandles.lookup()
										.findStatic(clazz, methodName,
												MethodType.methodType(m.getReturnType(), m.getParameterTypes()))
								: MethodHandles.lookup()
										.findVirtual(clazz, methodName,
												MethodType.methodType(m.getReturnType(), m.getParameterTypes()));
						methodsWithParams.put(fixSimilarities(mh.type().parameterList()), mh);
						methods.put(methodName, methodsWithParams);
					} catch (NoSuchMethodException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return methods;
	}

	/**
	 * Creates, parses, and caches the provided script in a new instance
	 * 
	 * @see Rina
	 * @param script to parse
	 */
	public Rina(String script) {
		this(script, true);
	}

	/**
	 * Creates a new rina instance, and parses the script if {@code parse} is set to
	 * true
	 * 
	 * @see Rina
	 * @param script to store
	 * @param parse  whether to parse the given script or not
	 */
	public Rina(String script, boolean parse) {
		this.script = script;
		assignedVariables = new HashMap<>();
		if (parse) rinaCondition = parseScript(this.script, assignedVariables);
	}

	/**
	 * Creates, parses, and caches the provided script in a new instance
	 * 
	 * @see Rina
	 * @param script to parse
	 */
	public static Rina create(String script) {
		return new Rina(script);
	}

	/**
	 * Creates a new rina instance, and parses the script if {@code parse} is set to
	 * true
	 * 
	 * @see Rina
	 * @param script to parse
	 * @param parse  whether to parse the given script or not
	 */
	public static Rina create(String script, boolean parse) {
		return new Rina(script, parse);
	}

	/**
	 * Assigns a variable to an object so it can be used in the conditions
	 * 
	 * @param variableName variable name to be replaced with the object
	 * @param variable     object to be be included in the condition
	 * @return this Rina
	 */
	public Rina assignVariable(String variableName, Object variable) {
		assignedVariables.put(variableName, variable);
		return this;
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
	public Rina setScript(String script) {
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
	public Rina setScript(String script, boolean parse) {
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
	 * if it was created using {@linkplain #create(String)},
	 * {@linkplain #Rina(String)}, {@linkplain #create(String, boolean)} and the
	 * boolean was set to true, or {@linkplain #Rina(String, boolean)} and the
	 * boolean was
	 * set to true
	 * 
	 * @return RinaCondition that was parsed from the script
	 */
	public RinaCondition parseScript() {
		return rinaCondition = parseScript(script, assignedVariables);
	}

	/**
	 * Quickly evaluates a condition for testing purposes. This is much slower than
	 * a
	 * cached Rina using {@linkplain #Rina(String)} or {@linkplain #create(String)}
	 * 
	 * @param script to parse and evaluate
	 * @return true if condition is met, false otherwise
	 */
	public static boolean evaluate(String script) {
		return Rina.create(script).evaluate();
	}

	/**
	 * Quickly applies a function then evaluates a condition for testing purposes.
	 * This is much slower than a cached Rina using {@linkplain #Rina(String)} or
	 * {@linkplain #create(String)}
	 * 
	 * @param script   to parse and evaluate
	 * @param function to apply on the condition before evaluation
	 * @return true if condition is met, false otherwise
	 */
	public static boolean applyThenEvaluate(String script, Function<String, String> function) {
		return Rina.create(script).applyThenEvaluate(function);
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
				: getRinaCondition().toString().equals(((Rina) object).getRinaCondition().toString());
	}

	private static RinaCondition parseScript(String script, Map<String, Object> assignedVariables) {
		if (script == null || script.isEmpty()) {
			System.out.println("Provided script is null or empty!");
			return null;
		}
		RinaCondition rinaCondition = null;
		RinaOrGroup orGroup = new RinaOrGroup();
		RinaAndGroup andGroup = new RinaAndGroup();
		boolean usingOrGroup = false;
		boolean hasOrGroup = containsOrIgnore(script, SCRIPT_OR, QUOTATION_IGNORE);
		boolean hasAndGroup = containsOrIgnore(script, SCRIPT_AND, QUOTATION_IGNORE);
		if (hasOrGroup && hasAndGroup) {
			rinaCondition = parseComplexRinaCondition(script, assignedVariables);
		} else if (hasOrGroup) {
			orGroup = RinaCondition.addConditions(orGroup, script, assignedVariables);
			usingOrGroup = true;
		} else if (hasAndGroup) {
			andGroup = RinaCondition.addConditions(andGroup, script, assignedVariables);
		} else {
			rinaCondition = RinaCondition.parseCondition(script, assignedVariables);
		}
		return rinaCondition != null ? rinaCondition : usingOrGroup ? orGroup : andGroup;
	}

	private static RinaCondition parseComplexRinaCondition(String script, Map<String, Object> assignedVariables) {
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
				orGroup = RinaCondition.addConditions(orGroup, parentGroup, assignedVariables);
				andGroup.addRinaCondition(new RinaOrGroup(orGroup.getConditions()));
				orGroup = new RinaOrGroup();
			} else if (orand.equals(SCRIPT_OR)) {
				andGroup = RinaCondition.addConditions(andGroup, parentGroup, assignedVariables);
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
		if (string.endsWith(SCRIPT_GROUP_CLOSING) && string.startsWith(SCRIPT_GROUP_OPENING)) {
			if (!(containsOrIgnore(string, ".", QUOTATION_IGNORE) && !containsDouble(string))) {
				for (int i = charArray.length - 1; i > string.indexOf(CHAR_SCRIPT_GROUP_CLOSING) - 1; i--) {
					endingStopIndex--;
					if (charArray[i] != CHAR_SCRIPT_GROUP_CLOSING) break;
				}
			}
		}
		return string = endingStopIndex != charArray.length ? string.substring(stopIndex, endingStopIndex)
				: string.substring(stopIndex);
	}

	public static String[] splitOrIgnore(String string, String separator, Predicate<Character> ignoreIf) {
		string = string + separator;
		char[] charArray = string.toCharArray();
		return splitOrIgnore(charArray, separator, ignoreIf);
	}

	public static String[] splitOrIgnore(char[] charArray, String separator, Predicate<Character> ignoreIf) {
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
	public static String[] splitOrIgnore(String string, String separator, Predicate<Character> ignoreIf, int limit) {
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

	public static boolean containsOrIgnore(String string, String target, Predicate<Character> ignoreIf) {
		return containsOrIgnore(string.toCharArray(), target, ignoreIf);
	}

	public static boolean containsOrIgnore(char[] charArray, String target, Predicate<Character> ignoreIf) {
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

	private static boolean containsInteger(String string) {
		if (string.startsWith("-")) string = string.substring(1);
		char[] charArray = string.toCharArray();
		boolean isInt = false;
		for (int i = 0; i < charArray.length; i++) {
			if (!MATH_INTEGER.contains(charArray[i])) {
				isInt = false;
				break;
			}
			isInt = true;
		}
		return isInt;
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
		FUNCTION,
		OR_GROUP,
		AND_GROUP;
	}

	private static Class<?> getParamType(String paramVal, Map<String, Object> assignedVariables) {
		if (assignedVariables != null && assignedVariables.get(paramVal) != null)
			return assignedVariables.get(paramVal).getClass();
		if (paramVal.startsWith(String.valueOf(STRING_QUOTATION))
				&& paramVal.endsWith(String.valueOf(STRING_QUOTATION))) {
			return String.class;
		} else if (paramVal.equals("true") || paramVal.equals("false")) {
			return boolean.class;
		} else if (containsDouble(paramVal)) {
			return double.class;
		} else if (containsInteger(paramVal)) {
			return int.class;
		} else {
			return RinaVariable.class;
		}
	}

	private static Object removeQuotes(Class<?> classType, Object object, String variableName) {
		if (classType == String.class) {
			String string = (String) object;
			if (string.startsWith(String.valueOf(STRING_QUOTATION))
					&& string.endsWith(String.valueOf(STRING_QUOTATION))) {
				return string.substring(1, string.length() - 1);
			}
		} else if (classType == boolean.class) {
			return Boolean.valueOf(String.valueOf(object));
		} else if (classType == double.class) {
			return Double.parseDouble(String.valueOf(object));
		} else if (classType == int.class) {
			return Integer.parseInt(String.valueOf(object));
		}
		return new RinaVariable(variableName);
	}

	private static Object getActualValue(String variableName, Map<String, Object> assignedVariables) {
		if (assignedVariables != null && assignedVariables.get(variableName) != null)
			return assignedVariables.get(variableName);
		return variableName;
	}

	public static class RinaVariable {

		private String varName;
		private int hashCode;
		private static final Map<Character, Integer> CODES = new HashMap<>();

		static {
			for (int i = 'a'; i < 'z' + 1; i++) {
				char upperCase = Character.toUpperCase((char) i);
				CODES.put((char) i, i);
				CODES.put(upperCase, (int) upperCase);
			}
			int i = -1;
			while (i++ < 9) CODES.put(asChar(i), i);
			CODES.put('_', (int) '_');
		}

		private static char asChar(int i) {
			String stringInt = String.valueOf(i);
			if (stringInt.length() > 1)
				throw new IllegalArgumentException("('" + stringInt + "') Too long char! The length exceeds 1.");
			return stringInt.charAt(0);
		}

		private static int asHashCode(String varName) {
			int hashCode = 0;
			for (char c : varName.toCharArray()) hashCode += CODES.get(c);
			return hashCode + varName.length();
		}

		public RinaVariable(String varName) {
			this.varName = varName;
			this.hashCode = asHashCode(varName);
		}

		public String getVarName() {
			return varName;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public String toString() {
			return "RinaVariable: " + varName;
		}

	}

	private static RinaCondition parseFunction(String scriptCondition, Map<String, Object> assignedVariables) {
		String[] methodCalls = splitOrIgnore(scriptCondition, ".", QUOTATION_IGNORE);
		String variableName = methodCalls[0];
		Class<?> variable = getParamType(variableName, assignedVariables);
		if (variable == null) throw new NullPointerException("Unknown variable: " + variableName);
		Map<String, Entry<List<Class<?>>, List<Object>>> finalMethods = new LinkedHashMap<>();
		List<Class<?>> paramTypes = new ArrayList<>();
		List<Object> values = new ArrayList<>();
		for (int i = 1; i < methodCalls.length; i++) {
			paramTypes.add(variable);
			values.add(removeQuotes(variable, getActualValue(variableName, assignedVariables), variableName));
			List<String> methodValues = getBetween(methodCalls[i], CHAR_SCRIPT_GROUP_OPENING, CHAR_SCRIPT_GROUP_CLOSING,
					null);
			String paramValues = methodValues.isEmpty() ? "" : methodValues.get(0);
			String methodName = null;
			if (!paramValues.isEmpty()) {
				String[] oneParam = splitOrIgnore(methodCalls[i], SCRIPT_GROUP_OPENING, QUOTATION_IGNORE);
				methodName = oneParam[0];
				if (containsOrIgnore(paramValues, ",", QUOTATION_IGNORE)) {
					for (String paramVal : splitOrIgnore(paramValues, ",", QUOTATION_IGNORE)) {
						Class<?> paramType = getParamType(paramVal, assignedVariables);
						paramTypes.add(paramType);
						values.add(removeQuotes(paramType, paramVal, paramVal));
					}
				} else {
					Class<?> paramType = getParamType(paramValues, assignedVariables);
					paramTypes.add(paramType);
					values.add(removeQuotes(paramType, paramValues, paramValues));
				}
			} else {
				String[] oneParam = splitOrIgnore(methodCalls[i], SCRIPT_GROUP_OPENING, QUOTATION_IGNORE);
				methodName = oneParam[0];
			}
			finalMethods.put(methodName, new SimpleEntry<List<Class<?>>, List<Object>>(new ArrayList<>(paramTypes),
					new ArrayList<>(values)));
			paramTypes.clear();
			values.clear();
		}
		RinaFunction function = new RinaFunction(String.join(".", methodCalls), "", finalMethods, assignedVariables);
		return function;
	}

	public static interface RinaCondition {

		public static RinaCondition parseCondition(String scriptCondition,
				@Nullable Map<String, Object> assignedVariables) {
			String cleanCondition = cleanExtras(scriptCondition);
			if (containsOrIgnore(cleanCondition, SCRIPT_EQUAL, QUOTATION_IGNORE)) { // ==
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_EQUAL, QUOTATION_IGNORE, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_NOT_EQUAL, QUOTATION_IGNORE)) { // !=
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_NOT_EQUAL, QUOTATION_IGNORE, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaNotEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_GREATER_THAN_OR_EQUAL, QUOTATION_IGNORE)) { // >=
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_GREATER_THAN_OR_EQUAL, QUOTATION_IGNORE, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaGreaterThanOrEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_GREATER_THAN, QUOTATION_IGNORE)) { // >
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_GREATER_THAN, QUOTATION_IGNORE, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaGreaterThan(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_LESS_THAN_OR_EQUAL, QUOTATION_IGNORE)) { // <=
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_LESS_THAN_OR_EQUAL, QUOTATION_IGNORE, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaLessThanOrEqual(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, SCRIPT_LESS_THAN, QUOTATION_IGNORE)) { // <
				String[] split = splitOrIgnore(cleanCondition, SCRIPT_LESS_THAN, QUOTATION_IGNORE, 2);
				String leftSection = split[0];
				boolean reverse = leftSection.startsWith(SCRIPT_REVERSE);
				return new RinaLessThan(reverse ? leftSection.substring(1) : leftSection, split[1], reverse);
			} else if (containsOrIgnore(cleanCondition, ".", QUOTATION_IGNORE)) {
				return parseFunction(cleanCondition, assignedVariables);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public static <T extends RinaCondition> T addConditions(T rinaCondition, String scriptConditions,
				Map<String, Object> assignedVariables) {
			if (rinaCondition instanceof RinaOrGroup) {
				RinaOrGroup rinaOrGroup = (RinaOrGroup) rinaCondition;
				for (String condition : splitOrIgnore(scriptConditions, SCRIPT_OR, QUOTATION_IGNORE))
					rinaOrGroup.addRinaCondition(parseCondition(condition, assignedVariables));
				return (T) rinaOrGroup;
			} else if (rinaCondition instanceof RinaAndGroup) {
				RinaAndGroup rinaAndGroup = (RinaAndGroup) rinaCondition;
				for (String condition : splitOrIgnore(scriptConditions, SCRIPT_AND, QUOTATION_IGNORE))
					rinaAndGroup.addRinaCondition(parseCondition(condition, assignedVariables));
				return (T) rinaAndGroup;
			}
			return rinaCondition = (T) parseCondition(scriptConditions, assignedVariables);
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
			boolean isPossiblyDouble = containsOrIgnore(string, ".", QUOTATION_IGNORE);
			if (!containsOrIgnore(string, MATH, QUOTATION_IGNORE))
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

	public static class RinaFunction extends RinaConditional implements RinaCondition {

		private Map<String, Entry<List<Class<?>>, List<Object>>> methods;
		private Map<String, Object> assignedVariables;

		public RinaFunction(String key, String value, Map<String, Entry<List<Class<?>>, List<Object>>> methods,
				Map<String, Object> assignedVariables) {
			this(key, value, methods, assignedVariables, false);
		}

		public RinaFunction(String key, String value, Map<String, Entry<List<Class<?>>, List<Object>>> methods,
				Map<String, Object> assignedVariables, boolean reverse) {
			super(key, value, reverse);
			this.methods = methods;
			this.assignedVariables = assignedVariables;
		}

		private boolean invoke() {
			Object returnedObject = null;
			for (Entry<String, Entry<List<Class<?>>, List<Object>>> entry : methods.entrySet()) {
				String sourceObject = entry.getKey();
				List<Class<?>> paramTypes = entry.getValue().getKey();
				List<Object> injectedVariables = entry.getValue().getValue();
				for (int i = 0; i < injectedVariables.size(); i++) {
					Object obj = injectedVariables.get(i);
					if (obj instanceof RinaVariable) {
						RinaVariable var = (RinaVariable) obj;
						Object actualObject = returnedObject == null ? this.assignedVariables.get(var.getVarName())
								: returnedObject;
						injectedVariables.set(i, actualObject);
						returnedObject = actualObject;
					} else {
						returnedObject = injectedVariables.get(i);
					}
				}
				Class<?> objClass = returnedObject.getClass();
				paramTypes.set(0, objClass);
				MethodHandle mh = exposedMethods.get(objClass).get(sourceObject).get(paramTypes);
				try {
					returnedObject = mh.invokeWithArguments(injectedVariables);
					paramTypes.set(0, objClass);
					if (returnedObject instanceof Boolean) return (boolean) returnedObject;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		private boolean invoke(Function<String, String> function) {
			Object returnedObject = null;
			for (Entry<String, Entry<List<Class<?>>, List<Object>>> entry : methods.entrySet()) {
				String sourceObject = function.apply(entry.getKey());
				function.apply(sourceObject);
				List<Class<?>> paramTypes = entry.getValue().getKey();
				List<Object> injectedVariables = entry.getValue().getValue();
				for (int i = 0; i < injectedVariables.size(); i++) {
					Object obj = injectedVariables.get(i);
					if (obj instanceof RinaVariable) {
						RinaVariable var = (RinaVariable) obj;
						Object actualObject = returnedObject == null ? this.assignedVariables.get(var.getVarName())
								: returnedObject;
						if (actualObject instanceof String) actualObject = function.apply((String) actualObject);
						injectedVariables.set(i, actualObject);
						returnedObject = actualObject;
					} else if (obj instanceof String) {
						injectedVariables.set(i, function.apply((String) obj));
					} else {
						returnedObject = injectedVariables.get(i);
					}
				}
				Class<?> objClass = returnedObject.getClass();
				paramTypes.set(0, objClass);
				MethodHandle mh = exposedMethods.get(objClass).get(sourceObject).get(paramTypes);
				try {
					returnedObject = mh.invokeWithArguments(injectedVariables);
					paramTypes.set(0, objClass);
					if (returnedObject instanceof Boolean) return (boolean) returnedObject;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		@Override
		public boolean evaluate() {
			return invoke() != reverse;
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
			return reverseSymbol + "RinaFunction{" + condition.getKey() + condition.getValue() + "}";
		}

		@Override
		public boolean applyThenEvaluate(Function<String, String> function) {
			return invoke(function) != reverse;
		}

		@Override
		public Entry<String, String> applyThenEvaluateOrGet(Function<String, String> function) {
			Entry<String, String> evaluation = new SimpleEntry<String, String>(function.apply(condition.getKey()),
					function.apply(condition.getValue()));
			boolean result = invoke(function);
			return result != reverse ? null : evaluation;
		}

		@Override
		public ConditionType getConditionType() {
			return ConditionType.FUNCTION;
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

	public static class RinaOrGroup implements RinaCondition, RinaGroup {

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

	public static class RinaAndGroup implements RinaCondition, RinaGroup {

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
