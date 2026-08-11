import java.util.Scanner;

/** Reads ten grades and reports the average, maximum, and minimum. */
public class GradeStatistics {

    private static final int NUMBER_OF_GRADES = 10;
    private static final int MAX_ATTEMPTS = 3;
    private static final double LOWEST_VALID_GRADE = 0.0;
    private static final double HIGHEST_VALID_GRADE = 100.0;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        displayInstructions();

        try {
            double[] grades = readGrades(input);
            double average = calculateAverage(grades);
            double maximum = findMaximum(grades);
            double minimum = findMinimum(grades);

            displayResults(average, maximum, minimum);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }

        input.close();
    }

    /** Displays the number and valid range of the grades the program expects. */
    private static void displayInstructions() {
        System.out.printf(
                "Enter %d grades between %.1f and %.1f.%n",
                NUMBER_OF_GRADES,
                LOWEST_VALID_GRADE,
                HIGHEST_VALID_GRADE);
    }

    /** Reads the required number of grades with a bounded for-loop. */
    private static double[] readGrades(Scanner input) {
        double[] grades = new double[NUMBER_OF_GRADES];

        for (int index = 0; index < grades.length; index++) {
            grades[index] = readGrade(input, index + 1);
        }

        return grades;
    }

    /** Reads one valid grade. Three attempts keep bad input from causing an endless loop. */
    private static double readGrade(Scanner input, int gradeNumber) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.printf(
                    "Enter grade %d of %d: ",
                    gradeNumber,
                    NUMBER_OF_GRADES);

            if (!input.hasNext()) {
                throw new IllegalArgumentException(
                        "Input ended before all grades were entered.");
            }

            if (!input.hasNextDouble()) {
                input.next();
                System.out.println(
                        "That is not a number. Please enter a numeric grade.");
                continue;
            }

            double grade = input.nextDouble();

            if (isValidGrade(grade)) {
                return grade;
            }

            System.out.printf(
                    "Grades must be between %.1f and %.1f.%n",
                    LOWEST_VALID_GRADE,
                    HIGHEST_VALID_GRADE);
        }

        throw new IllegalArgumentException(
                "No valid grade entered after "
                        + MAX_ATTEMPTS
                        + " attempts. Exiting.");
    }

    /** Checks whether a grade falls inside the accepted range. */
    private static boolean isValidGrade(double grade) {
        return grade >= LOWEST_VALID_GRADE
                && grade <= HIGHEST_VALID_GRADE;
    }

    /** Calculates the average of the grades. */
    private static double calculateAverage(double[] grades) {
        double total = 0.0;

        for (double grade : grades) {
            total += grade;
        }

        return total / grades.length;
    }

    /** Finds the highest grade. */
    private static double findMaximum(double[] grades) {
        double maximum = grades[0];

        for (int index = 1; index < grades.length; index++) {
            if (grades[index] > maximum) {
                maximum = grades[index];
            }
        }

        return maximum;
    }

    /** Finds the lowest grade. */
    private static double findMinimum(double[] grades) {
        double minimum = grades[0];

        for (int index = 1; index < grades.length; index++) {
            if (grades[index] < minimum) {
                minimum = grades[index];
            }
        }

        return minimum;
    }

    /** Displays the three required statistics. */
    private static void displayResults(
            double average,
            double maximum,
            double minimum) {
        System.out.println("\nGrade Statistics");
        System.out.printf("Average: %.2f%n", average);
        System.out.printf("Maximum: %.2f%n", maximum);
        System.out.printf("Minimum: %.2f%n", minimum);
    }
}
