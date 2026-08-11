import java.util.Scanner;

/** Reads ten grades with a for-loop and reports the class average, maximum, and minimum. */
public class GradeStatistics {

  /** Number of grades the assignment requires the program to read. */
  private static final int NUMBER_OF_GRADES = 10;

  /** Attempt limit per grade so invalid input cannot trap the program in an endless loop. */
  private static final int MAX_ATTEMPTS = 3;

  private static final double LOWEST_VALID_GRADE = 0.0;
  private static final double HIGHEST_VALID_GRADE = 100.0;

  /**
   * Reads ten grades from user input, tracks the running total, maximum, and minimum, and displays
   * the statistics.
   *
   * @param args command-line arguments, which aren't used
   */
  public static void main(String[] args) {

    Scanner input = new Scanner(System.in);

    System.out.printf(
        "Enter %d grades between %.1f and %.1f.%n",
        NUMBER_OF_GRADES, LOWEST_VALID_GRADE, HIGHEST_VALID_GRADE);

    double total = 0.0;

    // Every accepted grade falls inside the valid range, so the range bounds are safe
    // starting values: the first grade always replaces both.
    double maximum = LOWEST_VALID_GRADE;
    double minimum = HIGHEST_VALID_GRADE;

    for (int gradeNumber = 1; gradeNumber <= NUMBER_OF_GRADES; gradeNumber++) {
      double grade = readGrade(input, gradeNumber);

      total += grade;
      maximum = Math.max(maximum, grade);
      minimum = Math.min(minimum, grade);
    }

    double average = total / NUMBER_OF_GRADES;

    displayResults(average, maximum, minimum);

    input.close();
  }

  /**
   * Prompts for one grade and validates it, allowing a limited number of attempts so bad input
   * cannot cause an endless loop. The program exits when no valid grade is entered in time or when
   * the input stream ends early.
   *
   * @param input source used to read the grade
   * @param gradeNumber position of the grade being read, shown in the prompt
   * @return a grade between the lowest and highest valid values
   */
  public static double readGrade(Scanner input, int gradeNumber) {

    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      System.out.printf("Enter grade %d of %d: ", gradeNumber, NUMBER_OF_GRADES);

      if (!input.hasNext()) {
        break; // the input stream ended, so another attempt can never succeed
      }

      if (!input.hasNextDouble()) {
        input.next(); // discard the non-numeric token so the next attempt reads fresh input
        System.out.println("That is not a number. Please enter a numeric grade.");
        continue;
      }

      double grade = input.nextDouble();

      if (grade >= LOWEST_VALID_GRADE && grade <= HIGHEST_VALID_GRADE) {
        return grade;
      }

      System.out.printf(
          "Grades must be between %.1f and %.1f.%n", LOWEST_VALID_GRADE, HIGHEST_VALID_GRADE);
    }

    System.out.printf("No valid grade entered after %d attempts. Exiting.%n", MAX_ATTEMPTS);
    System.exit(1);
    return LOWEST_VALID_GRADE; // unreachable, but javac requires a return on every path
  }

  /**
   * Displays the average, maximum, and minimum of the grades that were entered.
   *
   * @param average average of the entered grades
   * @param maximum highest entered grade
   * @param minimum lowest entered grade
   */
  public static void displayResults(double average, double maximum, double minimum) {

    System.out.println("\nGrade Statistics");

    System.out.printf("Average: %.2f%n", average);
    System.out.printf("Maximum: %.2f%n", maximum);
    System.out.printf("Minimum: %.2f%n", minimum);
  }
}
