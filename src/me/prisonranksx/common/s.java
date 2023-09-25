package me.prisonranksx.common;

public class s {

	public static void print(String print) {
		System.out.println(print);
	}

	public static void print(Object print) {
		System.out.println(print);
	}

	public static void print(Object... print) {
		for (Object pr : print) print(pr);
	}

	public static void print(String... print) {
		for (String pr : print) print(pr);
	}

	public static void sprint(String print) {
		System.out.print(print);
	}

}
