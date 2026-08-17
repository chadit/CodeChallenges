import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Verifies MonthlyTemperatures without a test framework, since the coursework build is plain javac.
 * Run with "make test"; a non-zero exit code means at least one check failed.
 */
public class MonthlyTemperaturesTest {

    /** How far a double may drift before a check counts as failed. */
    private static final double TOLERANCE = 0.0001;

    /** Number of failed checks, used to decide the exit code once everything has run. */
    private static int failures = 0;

    /**
     * Runs every check and exits non-zero when any of them fail.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {

        findsExactMonthName();
        findsMonthWithoutRegardToCapitalization();
        findsMonthAfterTrimmingSpaces();
        returnsNotFoundForInvalidMonth();
        calculatesKnownYearlyAverage();
        findsHighestTemperatureIndex();
        findsLowestTemperatureIndex();
        productionArraysContainTwelveEntries();
        productionArraysHaveEqualLengths();
        validMonthSelectionDisplaysFormattedMonthAndTemperature();
        yearSelectionListsEveryMonthInCalendarOrder();
        yearSelectionDisplaysAverageAndExtremes();
        invalidSelectionDisplaysInstructions();

        if (failures > 0) {
            System.out.printf("%d check(s) failed.%n", failures);
            System.exit(1);
        }

        System.out.println("All checks passed.");
    }

    /** Checks the ordinary lookup path with a name typed exactly as it is stored. */
    private static void findsExactMonthName() {
        int index = MonthlyTemperatures.findMonthIndex("March", MonthlyTemperatures.MONTHS);
        check(index == 2, "exact month name returns its index");
    }

    /** Checks the rubric requirement that capitalization does not affect the lookup. */
    private static void findsMonthWithoutRegardToCapitalization() {
        int index = MonthlyTemperatures.findMonthIndex("mArCh", MonthlyTemperatures.MONTHS);
        check(index == 2, "mixed-case month name returns its index");
    }

    /** Checks that stray spaces around a pasted or fat-fingered entry are tolerated. */
    private static void findsMonthAfterTrimmingSpaces() {
        int index = MonthlyTemperatures.findMonthIndex("  March  ", MonthlyTemperatures.MONTHS);
        check(index == 2, "surrounding spaces are trimmed before the lookup");
    }

    /**
     * Checks that an unmatched entry reports the not-found value instead of guessing, and pins that
     * value to -1 because the assignment asks for it specifically.
     */
    private static void returnsNotFoundForInvalidMonth() {
        int index = MonthlyTemperatures.findMonthIndex("Spring", MonthlyTemperatures.MONTHS);

        check(
                index == MonthlyTemperatures.MONTH_NOT_FOUND,
                "unknown month name reports the not-found value");
        check(
                MonthlyTemperatures.MONTH_NOT_FOUND == -1,
                "the not-found value is -1, which is never a valid array index");
    }

    /** Uses a short array whose average is obvious by hand, so the check is easy to verify. */
    private static void calculatesKnownYearlyAverage() {
        double[] temperatures = {10.0, 20.0, 30.0, 40.0};
        double average = MonthlyTemperatures.calculateYearlyAverage(temperatures);

        check(closeTo(average, 25.0), "yearly average of 10/20/30/40 is 25.0");
    }

    /** Places the maximum in the middle so a first-or-last-element bug cannot pass. */
    private static void findsHighestTemperatureIndex() {
        double[] temperatures = {12.5, 18.0, 14.5};
        check(
                MonthlyTemperatures.findHighestTemperatureIndex(temperatures) == 1,
                "highest temperature index is found away from the array edges");
    }

    /** Places the minimum last so a first-element bug cannot pass. */
    private static void findsLowestTemperatureIndex() {
        double[] temperatures = {12.5, 18.0, 9.5};
        check(
                MonthlyTemperatures.findLowestTemperatureIndex(temperatures) == 2,
                "lowest temperature index is found at the end of the array");
    }

    /** Checks the assignment requirement that a full calendar year is stored. */
    private static void productionArraysContainTwelveEntries() {
        check(MonthlyTemperatures.MONTHS.length == 12, "month array holds twelve entries");
        check(
                MonthlyTemperatures.MONTHLY_AVERAGE_TEMPERATURES.length == 12,
                "temperature array holds twelve entries");
    }

    /** Guards the parallel-array contract, since a length mismatch would index out of bounds. */
    private static void productionArraysHaveEqualLengths() {
        check(
                MonthlyTemperatures.MONTHS.length
                        == MonthlyTemperatures.MONTHLY_AVERAGE_TEMPERATURES.length,
                "month and temperature arrays have equal lengths");
    }

    /** Drives the whole program with mixed-case input to prove the lookup reaches the output. */
    private static void validMonthSelectionDisplaysFormattedMonthAndTemperature() {
        String output = runWithInput("mArCh\n");
        check(
                output.contains("March: 42.5 degrees Fahrenheit"),
                "valid month prints its properly formatted name and temperature");
    }

    /** Checks that the year report covers every month exactly once, in calendar order. */
    private static void yearSelectionListsEveryMonthInCalendarOrder() {
        String output = runWithInput("  YEAR  \n");
        String problemMonth = findFirstMonthOutOfOrder(output);
        String description = "year report lists all twelve months once, in calendar order";

        if (problemMonth != null) {
            description = description + " (problem at " + problemMonth + ")";
        }

        check(problemMonth == null, description);
    }

    /** Checks the three summary lines against values computed by hand from the stored data. */
    private static void yearSelectionDisplaysAverageAndExtremes() {
        String output = runWithInput("year\n");

        check(
                output.contains("Yearly average: 54.0 degrees Fahrenheit"),
                "year report shows the 54.0 yearly average");
        check(
                output.contains("Highest monthly average: July, 77.5 degrees Fahrenheit"),
                "year report names July as the highest month");
        check(
                output.contains("Lowest monthly average: January, 31.5 degrees Fahrenheit"),
                "year report names January as the lowest month");
    }

    /** Checks that bad input explains what is accepted rather than retrying forever. */
    private static void invalidSelectionDisplaysInstructions() {
        String output = runWithInput("Spring\n");
        check(
                output.contains("Invalid selection. Enter a full month name or year."),
                "invalid selection explains that a full month name or year is required");
    }

    /**
     * Finds the first month the report gets wrong, treating missing, duplicated, and out-of-sequence
     * entries as the same kind of failure.
     *
     * @param report captured output of the full-year report
     * @return the offending month name, or null when all twelve are correct
     */
    private static String findFirstMonthOutOfOrder(String report) {
        int previousPosition = -1;

        for (String month : MonthlyTemperatures.MONTHS) {
            int position = report.indexOf(month + ":");

            if (position <= previousPosition || position != report.lastIndexOf(month + ":")) {
                return month;
            }

            previousPosition = position;
        }

        return null;
    }

    /**
     * Runs the program against canned input and captures everything it prints.
     *
     * @param inputText simulated user input, one entry per line
     * @return full program output
     */
    private static String runWithInput(String inputText) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try (Scanner input = new Scanner(inputText);
                PrintStream output = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            MonthlyTemperatures.run(input, output);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Compares doubles with a small tolerance instead of ==, since the averages pass through
     * floating-point division.
     *
     * @param actual value produced by the code under test
     * @param expected value the check requires
     * @return true when the values match within tolerance
     */
    private static boolean closeTo(double actual, double expected) {
        return Math.abs(actual - expected) < TOLERANCE;
    }

    /**
     * Records one check result, printing PASS or FAIL and counting failures for the exit code.
     *
     * @param condition outcome of the check
     * @param description what the check verifies, shown in the result line
     */
    private static void check(boolean condition, String description) {
        if (condition) {
            System.out.println("PASS: " + description);
            return;
        }

        failures++;
        System.out.println("FAIL: " + description);
    }
}
