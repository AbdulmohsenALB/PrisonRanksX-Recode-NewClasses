package me.prisonranksx.common;

import me.prisonranksx.utils.Rina.RinaAndGroup;
import me.prisonranksx.utils.Rina.RinaCondition;

public class MainTest {

	public static final char OPEN_BRACKET = '(';
	public static final char CLOSE_BRACKET = ')';

	// (('sniper'=='sniper'||1+1==2)&&('ump'=='ump'||('ak'=='ak'&&'test'=='test')))
	static String ca = "(('sniper'=='sniper'||1+1==2)&&('ump'=='ump'||('ak'=='ak'&&'test'=='test')))";

	public static void main(String[] args) {
		parseCondition(ca);
	}

	public static RinaCondition parseCondition(String string) {
		char[] chars = string.toCharArray();
		// initial state
		RinaAndGroup rc = new RinaAndGroup();
		int amountOfOpenBrackets = 0;
		String spitter = "";
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			char n = nextChar(i, chars, '?');
			if (amountOfOpenBrackets != 0) {
				spitter += c;
			}
			if (c == OPEN_BRACKET) {
				// spitter += c;
				amountOfOpenBrackets++;
			} else if (c == CLOSE_BRACKET) {
				amountOfOpenBrackets--;
				s.print(spitter);
				spitter = "";
			}

		}
		return null;
	}

	public static RinaCondition parseInner(String string) {
		return null;
	}

	public static char nextChar(int index, char[] chars, char error) {
		return index + 1 < chars.length - 1 ? chars[index + 1] : error;
	}

	public static char previousChar(int index, char[] chars, char error) {
		return index - 1 < -1 ? chars[index - 1] : error;
	}

}
