import java.io.PrintStream;
import java.util.Scanner;

/**
 * Stores one year of average monthly temperatures and displays either a single month or a full-year
 * report, depending on what the user asks for.
 *
 * <p>The month names and the temperatures are held in two separate arrays that are kept parallel:
 * index 0 of each array describes January, index 1 describes February, and so on. Nothing in the
 * language enforces that pairing, so every lookup here works the same way: find the index once, then
 * use that same index in both arrays.
 */
public class MonthlyTemperatures {

    /** Month names in calendar order, so an index into this array is also a position in the year. */
    static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    /**
     * Average temperature in degrees Fahrenheit for each month, parallel to MONTHS. Index 2 holds
     * 42.5 because index 2 of MONTHS holds "March". The values are fixed sample data, so the program
     * never asks the user to type twelve temperatures.
     */
    static final double[] MONTHLY_AVERAGE_TEMPERATURES = {
        31.5, 34.0, 42.5, 53.0, 63.5, 72.0,
        77.5, 75.0, 67.0, 55.5, 44.0, 32.5
    };

    /**
     * Reported by findMonthIndex when the text names no month. -1 works as the "nothing found"
     * answer because it is never a valid array index, so a caller cannot mistake it for a real
     * month. The String and List classes in the standard library signal a failed search the same
     * way.
     */
    static final int MONTH_NOT_FOUND = -1;

    /**
     * Starts the monthly temperature application.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        // try-with-resources closes the Scanner even if the report throws partway through.
        try (Scanner input = new Scanner(System.in)) {
            run(input, System.out);
        }
    }

    /**
     * Prompts for a selection and displays whatever that selection calls for.
     *
     * <p>The Scanner and PrintStream are passed in rather than read from System.in and System.out
     * directly. That keeps the program identical when a human runs it and lets the test class feed
     * canned input and capture the output.
     *
     * @param input source the selection is read from
     * @param output destination every message is written to
     */
    static void run(Scanner input, PrintStream output) {
        output.print("Enter a full month name or year: ");

        // Piped or redirected input can end before a line arrives. Treating that as a bad
        // selection avoids a NoSuchElementException from nextLine().
        if (!input.hasNextLine()) {
            displayInvalidSelection(output);
            return;
        }

        processSelection(input.nextLine(), output);
    }

    /**
     * Decides which report the user asked for and displays it.
     *
     * <p>The assignment calls for a single pass, so an unrecognized entry explains what is accepted
     * and the program ends instead of prompting again.
     *
     * @param selection raw text the user typed, which may have stray spaces or odd capitalization
     * @param output destination every message is written to
     */
    static void processSelection(String selection, PrintStream output) {
        // "year" is checked before the month lookup because it is the one selection that is not a
        // month name. It gets the same trim and case-insensitive treatment the months get.
        if (selection != null && selection.trim().equalsIgnoreCase("year")) {
            displayYearReport(MONTHS, MONTHLY_AVERAGE_TEMPERATURES, output);
            return;
        }

        int monthIndex = findMonthIndex(selection, MONTHS);

        if (monthIndex == MONTH_NOT_FOUND) {
            displayInvalidSelection(output);
            return;
        }

        displayMonth(MONTHS, MONTHLY_AVERAGE_TEMPERATURES, monthIndex, output);
    }

    /**
     * Finds where a typed month name sits in the month array.
     *
     * <p>This is the method that accepts a String value. Surrounding spaces are trimmed and the
     * comparison ignores capitalization, so "  mArCh  " and "March" both resolve to the same index.
     *
     * @param selection raw text the user typed
     * @param monthNames month names to search, in calendar order
     * @return index of the matching month, or MONTH_NOT_FOUND when nothing matches
     */
    static int findMonthIndex(String selection, String[] monthNames) {
        if (selection == null) {
            return MONTH_NOT_FOUND;
        }

        String normalizedSelection = selection.trim();

        for (int index = 0; index < monthNames.length; index++) {
            // equalsIgnoreCase does the case-insensitive comparison without building a second
            // lowercase copy of either string.
            if (monthNames[index].equalsIgnoreCase(normalizedSelection)) {
                return index;
            }
        }

        return MONTH_NOT_FOUND;
    }

    /**
     * Adds every temperature and divides by how many there are.
     *
     * @param temperatures temperatures to average
     * @return mean of the temperatures, in the same unit they were given in
     */
    static double calculateYearlyAverage(double[] temperatures) {
        double total = 0.0;

        for (double temperature : temperatures) {
            total += temperature;
        }

        // Dividing by length rather than a hardcoded 12 keeps this correct for the shorter arrays
        // the test class passes in.
        return total / temperatures.length;
    }

    /**
     * Finds the warmest month by walking the array and remembering the best index seen so far.
     *
     * <p>An index is returned rather than a temperature because the caller needs both halves of the
     * pair, and one index reaches the month name and the temperature. Nothing here reorders or
     * writes to the array, so the calendar order of the parallel arrays survives.
     *
     * @param temperatures temperatures to search
     * @return index of the highest temperature
     */
    static int findHighestTemperatureIndex(double[] temperatures) {
        // Index 0 seeds the comparison, so the loop can start at 1 and compare against a real value
        // instead of a made-up starting temperature that real data might never beat.
        int highestIndex = 0;

        for (int index = 1; index < temperatures.length; index++) {
            if (temperatures[index] > temperatures[highestIndex]) {
                highestIndex = index;
            }
        }

        return highestIndex;
    }

    /**
     * Finds the coldest month the same way findHighestTemperatureIndex finds the warmest, with the
     * comparison reversed.
     *
     * @param temperatures temperatures to search
     * @return index of the lowest temperature
     */
    static int findLowestTemperatureIndex(double[] temperatures) {
        int lowestIndex = 0;

        for (int index = 1; index < temperatures.length; index++) {
            if (temperatures[index] < temperatures[lowestIndex]) {
                lowestIndex = index;
            }
        }

        return lowestIndex;
    }

    /**
     * Displays one month and its average temperature.
     *
     * @param monthNames month names, parallel to temperatures
     * @param temperatures average temperatures, parallel to monthNames
     * @param monthIndex index of the month to display, valid in both arrays
     * @param output destination the line is written to
     */
    static void displayMonth(
            String[] monthNames, double[] temperatures, int monthIndex, PrintStream output) {
        // %.1f keeps one decimal place so 34.0 does not print as 34, which would make the column
        // ragged next to values like 42.5.
        output.printf(
                "%s: %.1f degrees Fahrenheit%n",
                monthNames[monthIndex], temperatures[monthIndex]);
    }

    /**
     * Displays every month in calendar order, then the yearly average and the warmest and coldest
     * months.
     *
     * <p>Walking the arrays by index rather than with an enhanced for-loop is what keeps the pairing
     * intact: the same index pulls the name from one array and its temperature from the other.
     *
     * @param monthNames month names, parallel to temperatures
     * @param temperatures average temperatures, parallel to monthNames
     * @param output destination every line is written to
     */
    static void displayYearReport(
            String[] monthNames, double[] temperatures, PrintStream output) {
        output.println("Monthly Temperature Report");

        for (int index = 0; index < monthNames.length; index++) {
            output.printf(
                    "%s: %.1f degrees Fahrenheit%n",
                    monthNames[index], temperatures[index]);
        }

        int highestIndex = findHighestTemperatureIndex(temperatures);
        int lowestIndex = findLowestTemperatureIndex(temperatures);

        output.printf(
                "Yearly average: %.1f degrees Fahrenheit%n",
                calculateYearlyAverage(temperatures));
        output.printf(
                "Highest monthly average: %s, %.1f degrees Fahrenheit%n",
                monthNames[highestIndex], temperatures[highestIndex]);
        output.printf(
                "Lowest monthly average: %s, %.1f degrees Fahrenheit%n",
                monthNames[lowestIndex], temperatures[lowestIndex]);
    }

    /**
     * Explains what the program accepts. Shared by the two ways a selection can fail, so both paths
     * give the user the same wording.
     *
     * @param output destination the message is written to
     */
    private static void displayInvalidSelection(PrintStream output) {
        output.println("Invalid selection. Enter a full month name or year.");
    }
}
