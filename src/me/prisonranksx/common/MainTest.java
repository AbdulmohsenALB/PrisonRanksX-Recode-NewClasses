package me.prisonranksx.common;

import java.util.ArrayList;
import java.util.List;

public class MainTest {

	public static void main(String[] args) {}

	public static List<String> parseString(String input) {
		List<String> result = new ArrayList<>();
		StringBuilder currentString = new StringBuilder();
		boolean skipMode = false;

		for (char c : input.toCharArray()) {
			if (c == '%') {
				skipMode = !skipMode;
				if (!skipMode && currentString.length() > 0) {
					result.add(currentString.toString());
					currentString = new StringBuilder();
				}
			} else if (!skipMode) {
				currentString.append(c);
			}
		}

		if (currentString.length() > 0) {
			result.add(currentString.toString());
		}

		return result;
	}

	public static char nextChar(int index, char[] chars, char error) {
		return index + 1 < chars.length - 1 ? chars[index + 1] : error;
	}

	public static char previousChar(int index, char[] chars, char error) {
		return index - 1 < -1 ? chars[index - 1] : error;
	}

}
