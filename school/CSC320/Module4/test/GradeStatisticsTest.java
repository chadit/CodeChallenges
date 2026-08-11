import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Verifies GradeStatistics without a test framework, since the coursework build is plain javac.
 * Run with "make test"; a non-zero exit code means at least one check failed.
 */
public class GradeStatisticsTest {

  private static int failures = 0;

  /**
   * Runs every check and exits non-zero when any of them fail.
   *
   * @param args command-line arguments, which aren't used
   * @throws Exception when the subprocess checks cannot launch the program
   */
  public static void main(String[] args) throws Exception {

    readGradeReturnsFirstValidGrade();
    readGradeAcceptsBoundaryGrades();
    readGradeSkipsNonNumericInputThenAcceptsGrade();
    readGradeRejectsOutOfRangeGradesThenAcceptsGrade();
    mainPrintsCorrectStatisticsForTenGrades();
    attemptLimitStopsEndlessLoopOnBadInput();
    earlyEndOfInputExitsInsteadOfLooping();

    if (failures > 0) {
      System.out.printf("%d check(s) failed.%n", failures);
      System.exit(1);
    }

    System.out.println("All checks passed.");
  }

  /** Checks that a clean numeric entry is returned on the first attempt. */
  private static void readGradeReturnsFirstValidGrade() {
    double grade = readGradeQuietly("95.5\n");
    check(closeTo(grade, 95.5), "valid grade is accepted on the first attempt");
  }

  /** Checks the edges of the valid range, since off-by-one rejections hide at the boundaries. */
  private static void readGradeAcceptsBoundaryGrades() {
    check(closeTo(readGradeQuietly("0\n"), 0.0), "lowest valid grade 0 is accepted");
    check(closeTo(readGradeQuietly("100\n"), 100.0), "highest valid grade 100 is accepted");
  }

  /** Checks that a non-numeric token is discarded and the next attempt still succeeds. */
  private static void readGradeSkipsNonNumericInputThenAcceptsGrade() {
    double grade = readGradeQuietly("abc\n88\n");
    check(closeTo(grade, 88.0), "non-numeric input is discarded and the retry is accepted");
  }

  /** Checks that grades outside 0-100 on either side are rejected until a valid one arrives. */
  private static void readGradeRejectsOutOfRangeGradesThenAcceptsGrade() {
    double grade = readGradeQuietly("105\n-1\n72.5\n");
    check(closeTo(grade, 72.5), "out-of-range grades are rejected and the retry is accepted");
  }

  /** Feeds ten grades through the whole program and checks the printed statistics. */
  private static void mainPrintsCorrectStatisticsForTenGrades() {
    String output =
        runMainWithInput("95.5\n88\n72.25\n100\n65\n90\n83.5\n77\n98\n55.75\n");

    check(output.contains("Average: 82.50"), "average of the ten grades is 82.50");
    check(output.contains("Maximum: 100.00"), "maximum of the ten grades is 100.00");
    check(output.contains("Minimum: 55.75"), "minimum of the ten grades is 55.75");
  }

  /**
   * Runs the program in a subprocess with nothing but bad input. The attempt limit calls
   * System.exit, which would kill this test JVM if exercised in-process.
   *
   * @throws Exception when the subprocess cannot be launched
   */
  private static void attemptLimitStopsEndlessLoopOnBadInput() throws Exception {
    int exitCode = runProgramWithInput("abc\nxyz\n999\n");
    check(exitCode == 1, "three bad attempts exit with code 1 instead of looping");
  }

  /**
   * Runs the program in a subprocess with empty input to prove that end-of-stream also exits
   * rather than spinning on a Scanner that can never produce a grade.
   *
   * @throws Exception when the subprocess cannot be launched
   */
  private static void earlyEndOfInputExitsInsteadOfLooping() throws Exception {
    int exitCode = runProgramWithInput("");
    check(exitCode == 1, "end of input exits with code 1 instead of looping");
  }

  /**
   * Calls readGrade with canned input while silencing its prompts, so test output stays readable.
   *
   * @param inputText simulated user input, one entry per line
   * @return grade returned by readGrade
   */
  private static double readGradeQuietly(String inputText) {
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(new ByteArrayOutputStream()));

    try {
      return GradeStatistics.readGrade(new Scanner(inputText), 1);
    } finally {
      System.setOut(originalOut);
    }
  }

  /**
   * Runs GradeStatistics.main in-process with simulated input and captures everything it prints.
   *
   * @param inputText simulated user input, one entry per line
   * @return full program output
   */
  private static String runMainWithInput(String inputText) {
    InputStream originalIn = System.in;
    PrintStream originalOut = System.out;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();

    System.setIn(new ByteArrayInputStream(inputText.getBytes()));
    System.setOut(new PrintStream(captured));

    try {
      GradeStatistics.main(new String[0]);
    } finally {
      System.setIn(originalIn);
      System.setOut(originalOut);
    }

    return captured.toString();
  }

  /**
   * Runs the program as a subprocess with the given input and reports its exit code. The
   * subprocess reuses this JVM's classpath, so it finds the same compiled classes.
   *
   * @param inputText simulated user input, closed afterward to signal end of stream
   * @return exit code of the program, or -1 when it hangs past the timeout
   * @throws Exception when the subprocess cannot be launched
   */
  private static int runProgramWithInput(String inputText) throws Exception {
    ProcessBuilder builder =
        new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "GradeStatistics");
    builder.redirectErrorStream(true);

    Process process = builder.start();
    process.getOutputStream().write(inputText.getBytes());
    process.getOutputStream().close();

    // A hang here is exactly the endless loop the assignment guards against, so a timeout
    // failure is a meaningful result rather than test flakiness.
    if (!process.waitFor(10, TimeUnit.SECONDS)) {
      process.destroyForcibly();
      return -1;
    }

    return process.exitValue();
  }

  /**
   * Compares doubles with a small tolerance instead of ==, since the grades pass through
   * floating-point parsing.
   *
   * @param actual value produced by the code under test
   * @param expected value the check requires
   * @return true when the values match within tolerance
   */
  private static boolean closeTo(double actual, double expected) {
    return Math.abs(actual - expected) < 0.0001;
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
