import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Checks the recursive product and the prompting around it without a test framework, since the
 * coursework build is plain javac. Run with "make test"; a non-zero exit code means at least one
 * check failed.
 */
public final class ProductChecks {

    /** What a run prints while the user answers five prompts without a mistake. */
    private static final String ALL_PROMPTS = "Enter number 1 of 5: Enter number 2 of 5: "
            + "Enter number 3 of 5: Enter number 4 of 5: Enter number 5 of 5: ";

    private static final String NOT_A_NUMBER_HINT = " Try something like 4, -2.5, or 0.75.";

    /** Number of failed checks, used for the exit code. */
    private static int failures = 0;

    /** Number of check methods that ended early by throwing, also used for the exit code. */
    private static int aborted = 0;

    /** A check paired with the name the report uses when it throws. */
    private record NamedCheck(String name, Runnable body) {}

    /** What one run printed, and whether it reached a product. */
    private record Session(boolean completed, String printed) {}

    private ProductChecks() {}

    /**
     * Runs every check and exits non-zero when any fail or throw.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "product multiplies every number",
                        ProductChecks::productMultipliesEveryNumber),
                new NamedCheck(
                        "product of no numbers is one and of one number is itself",
                        ProductChecks::productOfNoNumbersIsOneAndOfOneNumberIsItself),
                new NamedCheck(
                        "product is zero with a zero factor and negative for odd negatives",
                        ProductChecks::productIsZeroWithAZeroFactorAndNegativeForOddNegatives),
                new NamedCheck(
                        "product of decimals is exact",
                        ProductChecks::productOfDecimalsIsExact),
                new NamedCheck(
                        "equation prints numbers without trailing zeros or exponents",
                        ProductChecks::equationPrintsNumbersWithoutTrailingZerosOrExponents),
                new NamedCheck(
                        "run prints the prompts and the product",
                        ProductChecks::runPrintsThePromptsAndTheProduct),
                new NamedCheck(
                        "run asks again after input that is not a number",
                        ProductChecks::runAsksAgainAfterInputThatIsNotANumber),
                new NamedCheck(
                        "run accepts signs, spaces, and a bare decimal point",
                        ProductChecks::runAcceptsSignsSpacesAndABareDecimalPoint),
                new NamedCheck(
                        "run stops when the input ends early",
                        ProductChecks::runStopsWhenTheInputEndsEarly),
                new NamedCheck(
                        "main reads the terminal and prints the product",
                        ProductChecks::mainReadsTheTerminalAndPrintsTheProduct));

        for (NamedCheck check : checks) {
            runContained(check);
        }

        if (failures > 0 || aborted > 0) {
            System.out.printf(
                    "%d check(s) failed and %d of %d check method(s) aborted.%n",
                    failures, aborted, checks.size());
            System.exit(1);
        }

        System.out.printf("All checks passed. (%d check methods)%n", checks.size());
    }

    /**
     * Runs one check and keeps a throw from ending the run. Without this a single unexpected
     * exception cancels every later check and the tally never prints.
     *
     * @param check check to run
     */
    private static void runContained(NamedCheck check) {
        try {
            check.body().run();
        } catch (RuntimeException thrown) {
            // Errors are deliberately not caught: a JVM that is already unwell should stop, not
            // keep reporting checks.
            aborted++;
            System.out.println("ABORTED: " + check.name() + " threw " + thrown);
        }
    }

    private static void productMultipliesEveryNumber() {
        checkSameValue("720", product("2", "3", "4", "5", "6"), "2 through 6");
        checkSameValue("1", product("1", "1", "1", "1", "1"), "five ones");
        checkSameValue("1000000000000000", product("1e3", "1e3", "1e3", "1e3", "1e3"),
                "five thousands");
        checkSameValue("3628800", product("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
                "ten numbers, since the recursion is not tied to five");
    }

    private static void productOfNoNumbersIsOneAndOfOneNumberIsItself() {
        checkSameValue("1", product(), "no numbers");
        checkSameValue("7", product("7"), "one number");
        checkSameValue("-2.5", product("-2.5"), "one negative decimal");
    }

    private static void productIsZeroWithAZeroFactorAndNegativeForOddNegatives() {
        checkSameValue("0", product("0", "5", "5", "5", "5"), "a zero first");
        checkSameValue("0", product("5", "5", "5", "5", "0"), "a zero last");
        checkSameValue("-6", product("-1", "-1", "-1", "2", "3"), "three negatives");
        checkSameValue("24", product("-1", "-1", "2", "3", "4"), "two negatives");
        checkSameValue("-120", product("1", "2", "3", "4", "-5"), "one negative");
    }

    private static void productOfDecimalsIsExact() {
        checkSameValue("0.00001", product("0.1", "0.1", "0.1", "0.1", "0.1"), "five tenths");
        checkSameValue("0.3", product("0.1", "3", "1", "1", "1"), "a tenth times three");
        checkSameValue("3.375", product("1.5", "1.5", "1.5"), "one and a half cubed");
    }

    private static void equationPrintsNumbersWithoutTrailingZerosOrExponents() {
        checkEquals(
                "2 * 3 * 4 * 5 * 6 = 720",
                RecursiveProduct.equation(numbers("2", "3", "4", "5", "6")),
                "whole numbers");
        checkEquals(
                "2.5 * 4 * 10 * 1 * 1 = 100",
                RecursiveProduct.equation(numbers("2.50", "4", "1e1", "1", "1.0")),
                "trailing zeros and an exponent in the input");
        checkEquals(
                "0 * -0.5 * 3 * 7 * 7 = 0",
                RecursiveProduct.equation(numbers("0.00", "-0.5", "3", "7", "7")),
                "a zero factor and a zero product that both carry a scale");
        // 0.01 to the fifth is 1E-10 in BigDecimal's own toString, so this case fails if the
        // formatting ever falls back to it.
        checkEquals(
                "0.01 * 0.01 * 0.01 * 0.01 * 0.01 = 0.0000000001",
                RecursiveProduct.equation(numbers("0.01", "0.01", "0.01", "0.01", "0.01")),
                "a small product stays in plain notation");
    }

    private static void runPrintsThePromptsAndTheProduct() {
        Session session = runWith("2\n3\n4\n5\n6\n");

        check(session.completed(), "run reports that it printed a product");
        checkEquals(
                lines(ALL_PROMPTS, "2 * 3 * 4 * 5 * 6 = 720"),
                session.printed(),
                "transcript of five valid numbers");
    }

    private static void runAsksAgainAfterInputThatIsNotANumber() {
        Session session = runWith("abc\n1e3\n\n2\n3\n4\n5\n6\n");

        check(session.completed(), "run still reaches a product after bad input");
        checkEquals(
                lines(
                        "Enter number 1 of 5: Not a number: \"abc\"." + NOT_A_NUMBER_HINT,
                        "Enter number 1 of 5: Not a number: \"1e3\"." + NOT_A_NUMBER_HINT,
                        "Enter number 1 of 5: Not a number: \"\"." + NOT_A_NUMBER_HINT,
                        ALL_PROMPTS,
                        "2 * 3 * 4 * 5 * 6 = 720"),
                session.printed(),
                "transcript with a word, an exponent, and a blank line before the first number");
    }

    private static void runAcceptsSignsSpacesAndABareDecimalPoint() {
        // The last answer has no newline after it, which is how a redirected file often ends.
        Session session = runWith("  7  \n+2\n-1\n.5\n5.");

        check(session.completed(), "run accepts every spelling");
        checkEquals(
                lines(ALL_PROMPTS, "7 * 2 * -1 * 0.5 * 5 = -35"),
                session.printed(),
                "transcript with padded, signed, and bare-decimal-point input");
    }

    private static void runStopsWhenTheInputEndsEarly() {
        Session session = runWith("2\n3\n");

        check(!session.completed(), "run reports that no product was printed");
        checkEquals(
                lines(
                        "Enter number 1 of 5: Enter number 2 of 5: Enter number 3 of 5: ",
                        "Input ended before all 5 numbers were entered, so there is no product."),
                session.printed(),
                "transcript of input that ends after two numbers");

        Session afterBadInput = runWith("abc\n");
        check(!afterBadInput.completed(), "run reports no product when input ends after a word");
        checkEquals(
                lines(
                        "Enter number 1 of 5: Not a number: \"abc\"." + NOT_A_NUMBER_HINT,
                        "Enter number 1 of 5: ",
                        "Input ended before all 5 numbers were entered, so there is no product."),
                afterBadInput.printed(),
                "transcript of input that ends right after a word");

        Session empty = runWith("");
        check(!empty.completed(), "run reports no product for empty input");
    }

    private static void mainReadsTheTerminalAndPrintsTheProduct() {
        String printed = mainWith("1\n2\n3\n4\n5\n");

        checkEquals(lines(ALL_PROMPTS, "1 * 2 * 3 * 4 * 5 = 120"), printed, "main transcript");
    }

    private static BigDecimal[] numbers(String... text) {
        return Arrays.stream(text).map(BigDecimal::new).toArray(BigDecimal[]::new);
    }

    private static BigDecimal product(String... text) {
        return RecursiveProduct.product(numbers(text));
    }

    /** Runs the prompting against typed text instead of the keyboard. */
    private static Session runWith(String typed) {
        // A one-slot array because a lambda can only write to something it does not reassign.
        boolean[] completed = new boolean[1];
        String printed = captureOutput(() -> {
            try (Scanner in = new Scanner(typed)) {
                completed[0] = RecursiveProduct.run(in);
            }
        });

        return new Session(completed[0], printed);
    }

    /**
     * Runs main with the typed text as standard input. Only a complete session is safe here, since
     * main exits the JVM when the input ends early.
     */
    private static String mainWith(String typed) {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8)));
            return captureOutput(() -> RecursiveProduct.main(new String[0]));
        } finally {
            System.setIn(original);
        }
    }

    /** Joins lines with the platform separator, matching what println produces. */
    private static String lines(String... text) {
        return String.join(System.lineSeparator(), text) + System.lineSeparator();
    }

    /** Runs an action with System.out redirected and returns what it printed. */
    private static String captureOutput(Runnable action) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream redirect = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(redirect);
            action.run();
        } finally {
            System.setOut(original);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Compares by value, because BigDecimal.equals also compares scale and would call 720 and
     * 720.0 different.
     */
    private static void checkSameValue(String expected, BigDecimal actual, String description) {
        check(new BigDecimal(expected).compareTo(actual) == 0, description,
                "expected " + expected + ", got " + actual);
    }

    private static void checkEquals(String expected, String actual, String description) {
        check(expected.equals(actual), description,
                "expected \"" + expected + "\", got \"" + actual + "\"");
    }

    private static void check(boolean condition, String description) {
        check(condition, description, "");
    }

    /**
     * Prints PASS or FAIL and counts failures for the exit code. The detail is shown only on FAIL,
     * because the transcript expectations span several lines and would bury the PASS list.
     */
    private static void check(boolean condition, String description, String detail) {
        if (condition) {
            System.out.println("PASS: " + description);
            return;
        }

        failures++;
        System.out.println("FAIL: " + description + (detail.isEmpty() ? "" : " (" + detail + ")"));
    }
}
