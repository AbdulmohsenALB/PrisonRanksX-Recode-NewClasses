package me.prisonranksx.utils;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class Loop {

	public static <T> void forEach(T[] array, Consumer<T> action, Predicate<T> breakCondition) {
		for (T obj : array) {
			if (breakCondition.test(obj)) break;
			action.accept(obj);
		}
	}

	public static void forEach(char[] chars, Consumer<Character> action, Predicate<Character> breakCondition) {
		for (char obj : chars) {
			if (breakCondition.test(obj)) break;
			action.accept(obj);
		}
	}

	public static void forEach(int length, IntConsumer indexAction) {
		for (int i = 0; i < length; i++) {
			indexAction.accept(i);
		}
	}

	public static void forEach(int length, IntConsumer indexAction, IntPredicate breakCondition) {
		for (int i = 0; i < length; i++) {
			if (breakCondition.test(i)) break;
			indexAction.accept(i);
		}
	}

	public static <T> void forEachIndexed(T[] array, BiConsumer<Integer, T> action) {
		for (int i = 0; i < array.length; i++) action.accept(i, array[i]);
	}

	public static <T> void forEachIndexed(T[] array, BiConsumer<Integer, T> action,
			BiPredicate<Integer, T> breakCondition) {
		for (int i = 0; i < array.length; i++) {
			if (breakCondition.test(i, array[i])) break;
			action.accept(i, array[i]);
		}
	}

	public static <T> void forEachIndexed(Collection<T> collection, BiConsumer<Integer, T> action) {
		int i = 0;
		for (T obj : collection) {
			action.accept(i, obj);
			i++;
		}
	}

	public static <T> void forEachIndexed(Collection<T> collection, BiConsumer<Integer, T> action,
			BiPredicate<Integer, T> breakCondition) {
		int i = 0;
		for (T obj : collection) {
			if (breakCondition.test(i, obj)) break;
			action.accept(i, obj);
			i++;
		}
	}

	public static void forEachIndexed(char[] chars, BiConsumer<Integer, Character> action) {
		for (int i = 0; i < chars.length; i++) action.accept(i, chars[i]);
	}

	public static void forEachIndexed(char[] chars, BiConsumer<Integer, Character> action,
			BiPredicate<Integer, Character> breakCondition) {
		for (int i = 0; i < chars.length; i++) {
			if (breakCondition.test(i, chars[i])) break;
			action.accept(i, chars[i]);
		}
	}

}
