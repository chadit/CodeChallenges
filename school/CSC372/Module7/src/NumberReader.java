import java.math.BigDecimal;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads the numbers a user types at the terminal, one per line. Text that is not a number gets a
 * hint and the same prompt again, so a read ends only with a number or with the end of the input.
 */
final class NumberReader {

    /**
     * An optional sign, then digits with at most one decimal point. Exponents are left out on
     * purpose: BigDecimal would accept 1e999999999, and printing that in plain digits exhausts the
     * heap. Without exponents a number can't be longer than what was typed.
     */
    private static final Pattern NUMBER = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    private final Scanner in;

    private final int total;

    /**
     * The reader does not close the scanner, since whoever opened it may still be reading from it.
     *
     * @param in where the user's answers come from, one per line
     * @param total how many numbers the prompts count up to, as in "Enter number 2 of 5"
     */
    NumberReader(Scanner in, int total) {
        this.in = in;
        this.total = total;
    }

    /**
     * Prompts until the user types a number. The retry is a loop instead of a recursive call
     * because the user decides how many wrong answers come first, and each one would cost a stack
     * frame that is never given back until a number arrives.
     *
     * @param position which number this is, counting from 1, as shown in the prompt
     * @return the number, or empty once the input ends
     */
    Optional<BigDecimal> read(int position) {
        while (true) {
            System.out.print("Enter number " + position + " of " + total + ": ");
            if (!in.hasNextLine()) {
                return Optional.empty();
            }

            String typed = in.nextLine().trim();
            Optional<BigDecimal> number = parse(typed);
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
    private static Optional<BigDecimal> parse(String typed) {
        if (!NUMBER.matcher(typed).matches()) {
            return Optional.empty();
        }

        return Optional.of(new BigDecimal(typed));
    }
}
