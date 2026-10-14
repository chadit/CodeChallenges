import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Asks the user for five numbers and prints their product. The product comes from recursion rather
 * than a loop, which is the point of the exercise: the product of a list is its first number times
 * the product of the rest, and the product of an empty list is 1.
 *
 * <p>The numbers are BigDecimal so decimal input multiplies exactly. Five tenths multiplied
 * together print as 0.00001, where doubles would print 1.0000000000000004E-5.
 */
public final class RecursiveProduct {

    /** How many numbers the assignment asks for. */
    static final int COUNT = 5;

    /**
     * An optional sign, then digits with at most one decimal point. Exponents are left out on
     * purpose: BigDecimal would accept 1e999999999, and printing that in plain digits exhausts the
     * heap. Without exponents a number can't be longer than what was typed.
     */
    private static final Pattern NUMBER = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    private RecursiveProduct() {}

    /**
     * Prompts on the terminal, and exits non-zero when the input ends before all the numbers arrive.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            if (!run(in)) {
                System.exit(1);
            }
        }
    }

    /**
     * Asks for the numbers one at a time, asking again after anything that isn't a number, then
     * prints the product as an equation.
     *
     * @param in where the user's answers come from, one per line
     * @return whether a product was printed; false means the input ended first
     */
    static boolean run(Scanner in) {
        BigDecimal[] numbers = new BigDecimal[COUNT];
        for (int i = 0; i < COUNT; i++) {
            Optional<BigDecimal> number = readNumber(in, i + 1);
            if (number.isEmpty()) {
                System.out.println();
                System.out.println("Input ended before all " + COUNT
                        + " numbers were entered, so there is no product.");
                return false;
            }
            numbers[i] = number.orElseThrow();
        }

        System.out.println();
        System.out.println(equation(numbers));
        return true;
    }

    /**
     * Multiplies every number in the array.
     *
     * @param numbers the factors, any count including none
     * @return their product, which is 1 for an empty array
     */
    static BigDecimal product(BigDecimal[] numbers) {
        return productFrom(numbers, 0);
    }

    /**
     * The recursive step: the product of the numbers from start onward is the number at start
     * times the product of the numbers after it. The recursion ends where there are no numbers
     * left, and the product of nothing is 1 so the multiplications above it are unchanged.
     */
    private static BigDecimal productFrom(BigDecimal[] numbers, int start) {
        if (start == numbers.length) {
            return BigDecimal.ONE;
        }

        return numbers[start].multiply(productFrom(numbers, start + 1));
    }

    /**
     * Writes the numbers and their product as one line of arithmetic.
     *
     * @param numbers the factors
     * @return text such as "2 * 3 * 4 * 5 * 6 = 720"
     */
    static String equation(BigDecimal[] numbers) {
        String factors = Arrays.stream(numbers)
                .map(RecursiveProduct::formatNumber)
                .collect(Collectors.joining(" * "));

        return factors + " = " + formatNumber(product(numbers));
    }

    /** Prompts until the user types a number, or returns empty once the input ends. */
    private static Optional<BigDecimal> readNumber(Scanner in, int position) {
        while (true) {
            System.out.print("Enter number " + position + " of " + COUNT + ": ");
            if (!in.hasNextLine()) {
                return Optional.empty();
            }

            String typed = in.nextLine().trim();
            Optional<BigDecimal> number = parseNumber(typed);
            if (number.isPresent()) {
                return number;
            }

            System.out.println(
                    "Not a number: \"" + typed + "\". Try something like 4, -2.5, or 0.75.");
        }
    }

    /**
     * Reads a number from what the user typed, or returns empty when the text isn't one. The
     * pattern is checked first because BigDecimal can only report a bad string by throwing.
     */
    private static Optional<BigDecimal> parseNumber(String typed) {
        if (!NUMBER.matcher(typed).matches()) {
            return Optional.empty();
        }

        return Optional.of(new BigDecimal(typed));
    }

    /**
     * Drops trailing zeros and exponents, so 2.50 prints as 2.5, and 0.01 to the fifth prints as
     * 0.0000000001 rather than 1E-10. Without the stripping, 2.50 times 4 would print as 10.00.
     */
    private static String formatNumber(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString();
    }
}
