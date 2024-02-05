package me.prisonranksx.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 
 * Get all fields and methods of an object in a string.
 *
 */
public class ToString {

	private Class<?> clazz;
	private Object obj;
	private String string;

	public ToString(Class<?> clazz) {
		this(clazz, false);
	}

	public ToString(Object obj) {
		this(obj, false);
	}

	public ToString(Class<?> clazz, boolean detailed) {
		this.clazz = clazz;
		this.string = "{" + clazz.getSimpleName() + "} = (";
		if (detailed)
			initDetailed();
		else
			init();
	}

	public ToString(Object obj, boolean detailed) {
		this.obj = obj;
		this.clazz = obj.getClass();
		this.string = "{" + clazz.getSimpleName() + "} = (";
		if (detailed)
			initDetailed();
		else
			init();
	}

	public void init() {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAccessible()) field.setAccessible(true);
			try {
				string += "(" + field.getType().getSimpleName() + ")" + field.getName() + "->"
						+ (obj != null ? field.get(obj) != null ? field.get(obj).toString() : "null" : "?") + ", ";
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		string = string.substring(0, string.length() - 2);
		string += ")";
	}

	public void initDetailed() {
		Field[] fields = clazz.getDeclaredFields();
		string += "\n| FIELDS";
		for (Field field : fields) {
			if (!field.isAccessible()) field.setAccessible(true);
			try {
				string += "\n(" + field.getType().getName() + ")" + readModifiers(field) + field.getName() + "->"
						+ (obj != null ? field.get(obj) != null ? field.get(obj).toString() : "null" : "?");
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		string += "\n| METHODS";
		for (Method method : clazz.getDeclaredMethods()) {
			if (!method.isAccessible()) method.setAccessible(true);
			string += "\n" + method.getReturnType().getName() + ": " + readModifiers(method) + method.getName() + "("
					+ joinArray(method.getParameters(), ", ") + ")";
		}
		string = string.substring(0, string.length() - 2);
		string += ")";
	}

	private static String joinArray(Object[] array, String separator) {
		String joined = "";
		for (int i = 0; i < array.length; i++) {
			Object obj = array[i];
			joined += i != array.length - 1 ? obj.toString() + separator : obj.toString();
		}
		return joined;
	}

	private String readModifiers(Member member) {
		String modifiers = "";
		int mod = member.getModifiers();
		modifiers += Modifier.isPublic(mod) ? "public " : Modifier.isPrivate(mod) ? "private "
				: Modifier.isProtected(mod) ? "protected " : "";
		modifiers += Modifier.isStatic(mod) ? "static " : "";
		modifiers += Modifier.isFinal(mod) ? "final " : "";
		modifiers += Modifier.isSynchronized(mod) ? "synchronized " : "";
		return modifiers;
	}

	public String getString() {
		return string;
	}

	public static String toString(Object obj) {
		return toString(obj, false);
	}

	public static String toString(Class<?> clazz) {
		return (new ToString(clazz, false)).getString();
	}

	public static String toString(Class<?> clazz, boolean detailed) {
		return (new ToString(clazz, detailed)).getString();
	}

	public static String toString(Object obj, boolean detailed) {
		return (new ToString(obj, detailed)).getString();
	}

	public static void print(Object obj) {
		print(obj, false);
	}

	public static void print(Class<?> clazz) {
		print(clazz, false);
	}

	public static void print(Object obj, boolean detailed) {
		System.out.println(toString(obj, detailed));
	}

	public static void print(Class<?> clazz, boolean detailed) {
		System.out.println(toString(clazz, detailed));
	}

}
