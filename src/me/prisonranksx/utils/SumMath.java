package me.prisonranksx.utils;

import java.util.function.DoubleSupplier;

public class SumMath {

    public static double sum(double num) {
        return (num * (num + 1)) / 2;
    }

    /**
     * Sums numbers from {@code begin} to {@code end}.
     *
     * @param begin to start from
     * @param end   to end at
     * @return the sum
     */
    public static double sum(double begin, double end) {
        return ((end - begin + 1) * (end + begin)) / 2;
    }

    /**
     * Gets end of {@link #sum(double, double)}.
     *
     * @param begin to start from
     * @param sum   to get the end of
     * @return the end
     */
    public static double getSumEnd(double begin, double sum) {
        // Sum = ((end - begin + 1) * (end + begin)) / 2
        // 2Sum = (end - begin + 1) * (end + begin)
        // 2Sum = end^2 + end - begin^2 + begin
        // end^2 + end - (2Sum + begin^2 - begin) = 0
        // Using quadratic formula: ax^2 + bx + c = 0
        // a = 1, b = 1, c = -(2Sum + begin^2 - begin)
        double a = 1;
        double b = 1;
        double c = -(2 * sum + begin * begin - begin);
        // Quadratic formula: (-b + sqrt(b^2 - 4ac)) / (2a)
        // We use the positive root as we're looking for the upper bound
        double end = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
        return Math.floor(end);
    }

    public static double getSumEnd(DoubleSupplier beginSupplier, double sum) {
        double begin = beginSupplier.getAsDouble();
        // Sum = ((end - begin + 1) * (end + begin)) / 2
        // 2Sum = (end - begin + 1) * (end + begin)
        // 2Sum = end^2 + end - begin^2 + begin
        // end^2 + end - (2Sum + begin^2 - begin) = 0
        // Using quadratic formula: ax^2 + bx + c = 0
        // a = 1, b = 1, c = -(2Sum + begin^2 - begin)
        double a = 1;
        double b = 1;
        double c = -(2 * sum + begin * begin - begin);
        // Quadratic formula: (-b + sqrt(b^2 - 4ac)) / (2a)
        // We use the positive root as we're looking for the upper bound
        double end = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
        return Math.floor(end);

    }

    public static double getSumBegin(double end, double sum) {
        double begin = Math.sqrt(end * end + end - 2 * sum + 0.25) + 0.5;
        return Math.round(begin) + 1;
    }

    public static double arithmeticSequenceSum(double firstTerm, double lastTerm, int numberOfTerms) {
        return (numberOfTerms * (firstTerm + lastTerm)) / 2;
    }

    public static double arithmeticSequenceNthTerm(double firstTerm, double commonDifference, int n) {
        return firstTerm + (n - 1) * commonDifference;
    }

    public static double geometricSequenceSum(double firstTerm, double commonRatio, int numberOfTerms) {
        if (commonRatio == 1) return firstTerm * numberOfTerms;
        return firstTerm * (1 - Math.pow(commonRatio, numberOfTerms)) / (1 - commonRatio);
    }

    public static double geometricSequenceNthTerm(double firstTerm, double commonRatio, int n) {
        return firstTerm * Math.pow(commonRatio, n - 1);
    }

}
