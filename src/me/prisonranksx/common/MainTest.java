package me.prisonranksx.common;

import me.prisonranksx.utils.SumMath;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MainTest {


    public static void main(String[] args) {
        //parse("(('mister'=='mister'||'car'=='car')&&('star'=='star')&&('miss'=='miss'||'mrs'=='mrs'||('1'=='1'&&'2'=='2')))");
        //Map<Integer, List<String>> mapo = InitHashMaps.<Integer, List<String>>putFirst(1, new ArrayList<>()).finish();
        //System.out.println(mapo);

        // double sum = SumMath.sum(1, 10);
        // System.out.println(sum);
        // System.out.println(SumMath.getSumEnd(1, 55));
        //double sumEnd = SumMath.getSumEnd(10, 91);
        //System.out.println(sumEnd);
        //System.out.println(SumMath.sum(10, sumEnd));

    }



    public static int getHighestRank(String expression, double money, int startLevel) {
        // Replace the placeholder with a variable for evaluation
        String evalExpression = expression.replace("{ranknumber}", "x");

        // Define a function to calculate the price for a given rank
        Function<Double, Double> priceFunction = x -> Common.eval(evalExpression.replace("x", String.valueOf(x)));

        // Calculate the cost to reach the start level
        double costToStartLevel = sumPrices(priceFunction, 1, startLevel - 1);

        // Adjust the available money
        money -= costToStartLevel;

        // Use binary search to find the highest affordable rank
        int left = startLevel;
        int right = 1_000_000;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            double sumToMid = sumPrices(priceFunction, startLevel, mid);

            if (sumToMid <= money) {
                if (mid == 1_000_000 || sumPrices(priceFunction, startLevel, mid + 1) > money) {
                    return mid;
                }
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return startLevel - 1; // In case no rank is affordable
    }

    private static double sumPrices(Function<Double, Double> priceFunction, double start, double end) {
        if (isLinearPriceFunction(priceFunction)) {
            double a = priceFunction.apply(1.0);
            double b = priceFunction.apply(2.0) - a;
            return (end - start + 1) * (priceFunction.apply(start) + priceFunction.apply(end)) / 2;
        } else {
            // For non-linear functions, use the provided sum method
            return SumMath.sum(start, end) * priceFunction.apply(1.0);
        }
    }

    private static boolean isLinearPriceFunction(Function<Double, Double> priceFunction) {
        double y1 = priceFunction.apply(1.0);
        double y2 = priceFunction.apply(2.0);
        double y3 = priceFunction.apply(3.0);

        return Math.abs((y3 - y2) - (y2 - y1)) < 1e-6;
    }


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

    public static List<String> parse(String string) {
        StringBuilder sb = new StringBuilder();
        char[] chars = string.toCharArray();
        boolean start = false;
        int startIndex = 0;
        int endIndex = 0;
        int curIndex = 0;
        int endCurIndex = 0;
        int skips = 0;
        boolean foundClosing = false;
        boolean foundOpening = false;
        for (int i = string.length() - 1; i > -1; i--) {
            if (chars[i] == ')') {
                if (start) {
                    skips++;
                    curIndex = i;
                    foundClosing = true;
                } else {
                    start = true;
                    startIndex = i;
                }

            }
            if (start) {
                if (skips == 0) {

                }
            }
            if (foundClosing && foundOpening) {
                if (curIndex > endCurIndex) {
                    System.out.println(string.substring(endCurIndex, curIndex));
                    foundClosing = false;
                    foundOpening = false;
                }
            }
            if (chars[i] == '(') {
                if (skips != 0) {
                    skips--;
                    endCurIndex = i;
                    foundOpening = true;
                } else {
                    endIndex = i;
                }
            }
        }
        System.out.println(string.substring(endIndex + 1, startIndex));
        return null;
    }

    public static char nextChar(int index, char[] chars, char error) {
        return index + 1 < chars.length - 1 ? chars[index + 1] : error;
    }

    public static char previousChar(int index, char[] chars, char error) {
        return index - 1 < -1 ? chars[index - 1] : error;
    }

}
