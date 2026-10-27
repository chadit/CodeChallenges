import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Asks the user for five numbers and prints their product. The product comes from recursion rather
 * than a loop, which is the point of the exercise: the product of a list is the product of its
 * first half times the product of its second half. A list of one number is that number, and the
 * product of an empty list is 1.
 *
 * <p>The numbers are BigDecimal so decimal input multiplies exactly. Five tenths multiplied
 * together print as 0.00001, where doubles would print 1.0000000000000004E-5.
 */
public final class RecursiveProduct {

    /** How many numbers the assignment asks for. */
    static final int COUNT = 5;

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
        NumberReader reader = new NumberReader(in, COUNT);
        BigDecimal[] numbers = new BigDecimal[COUNT];
        for (int i = 0; i < COUNT; i++) {
            Optional<BigDecimal> number = reader.read(i + 1);
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
        return productOf(numbers, 0, numbers.length);
    }

    /**
     * The recursive step: the product of the numbers from index from up to, not including, index
     * to is the product of the left half of that range times the product of the right half.
     * Halving keeps the recursion about log2(n) calls deep, so a long array can't run out of stack
     * the way one call per number would. Regrouping can't change the result, because BigDecimal
     * multiplication without a MathContext is exact.
     */
    private static BigDecimal productOf(BigDecimal[] numbers, int from, int to) {
        int size = to - from;
        // Only an empty array reaches an empty range, since every split leaves a number on each
        // side. The product of nothing is 1 so that answer is still right.
        if (size == 0) {
            return BigDecimal.ONE;
        }

        if (size == 1) {
            return numbers[from];
        }

        int middle = from + size / 2;
        return productOf(numbers, from, middle).multiply(productOf(numbers, middle, to));
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

    /**
     * Drops trailing zeros and exponents, so 2.50 prints as 2.5, and 0.01 to the fifth prints as
     * 0.0000000001 rather than 1E-10. Without the stripping, 2.50 times 4 would print as 10.00.
     */
    private static String formatNumber(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString();
    }
}
