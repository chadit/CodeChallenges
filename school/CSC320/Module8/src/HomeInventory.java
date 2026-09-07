import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Runs the assignment's script against Home: build, list, remove, add, list, update, list, then
 * offer to write the listing to a file.
 */
public class HomeInventory {

    /**
     * Home.txt in the platform's temp directory, the portable form of the assignment's
     * C:\Temp\Home.txt example.
     */
    private static final Path REPORT_FILE =
            Path.of(System.getProperty("java.io.tmpdir"), "Home.txt");

    /**
     * Starts the home inventory application.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        // The catch turns an unexpected failure into one line instead of a stack trace.
        try (Scanner input = new Scanner(System.in)) {
            run(input, System.out, REPORT_FILE);
        } catch (RuntimeException problem) {
            System.out.println("The program stopped: " + problem.getMessage());
        }
    }

    /**
     * Runs every step and prints each result. Input, output, and the file path are parameters so
     * the tests can drive it.
     *
     * @param input source the Y or N answer is read from
     * @param output destination every message is written to
     * @param reportFile file the listing is written to on Y
     */
    static void run(Scanner input, PrintStream output, Path reportFile) {
        Home home =
                new Home(2450, "1187 Aspen Ridge Drive", "Fort Collins", "CO", 80525, "Aspen",
                        "available");

        output.println("Home from the constructor:");
        printListing(home, output);

        output.println();
        output.println(home.removeHome());

        output.println();
        output.println(
                home.addHome(1880, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "available"));
        printListing(home, output);

        output.println();
        output.println(home.updateSaleStatus("under contract"));
        printListing(home, output);

        output.println();
        output.print("Print this information to a file? (Y or N): ");

        if (wantsFile(input)) {
            output.println(writeListing(home, reportFile));
            return;
        }

        output.println("A file will not be printed.");
    }

    /**
     * Lives here rather than in Home because the assignment wants the main class to loop.
     *
     * @param home home to list
     * @param output destination every line is written to
     */
    private static void printListing(Home home, PrintStream output) {
        for (String line : home.listHome()) {
            output.println(line);
        }
    }

    /**
     * Writes the listing, one attribute per line.
     *
     * @param home home to write
     * @param reportFile destination file
     * @return a success message naming the file, or a failure message
     */
    static String writeListing(Home home, Path reportFile) {
        try {
            Files.write(reportFile, List.of(home.listHome()), StandardCharsets.UTF_8);
            return "Home information printed to " + displayPath(reportFile);
        } catch (IOException problem) {
            return "Failed to print the file: " + problem.getMessage();
        }
    }

    /**
     * Names the file the way the shell that started the program can reach it. Under "make run" the
     * absolute path is the container's mount point, which does not exist on the host, so a file
     * inside the working directory is named relative to it and resolves the same on both sides.
     *
     * @param file file to name
     * @return a path relative to the working directory when the file is under it, else absolute
     */
    static String displayPath(Path file) {
        Path absolute = file.toAbsolutePath().normalize();
        Path workingDirectory = Path.of("").toAbsolutePath();

        if (absolute.startsWith(workingDirectory)) {
            return workingDirectory.relativize(absolute).toString();
        }

        return absolute.toString();
    }

    /**
     * Y means yes; anything else, including input that ends early, means no.
     *
     * @param input source the answer is read from
     * @return true when the user asked for the file
     */
    private static boolean wantsFile(Scanner input) {
        if (!input.hasNextLine()) {
            return false;
        }

        return input.nextLine().trim().equalsIgnoreCase("Y");
    }
}
