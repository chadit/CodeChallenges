import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Checks Home and HomeInventory without a test framework, since the coursework build is plain
 * javac. Run with "make test"; a non-zero exit code means at least one check failed.
 */
public class HomeInventoryTest {

    /** Number of failed checks, used for the exit code. */
    private static int failures = 0;

    /** Number of check methods that ended early by throwing, also used for the exit code. */
    private static int aborted = 0;

    /**
     * A check method. It may throw: the runner contains a throw so one bad check cannot cancel the
     * ones after it.
     */
    @FunctionalInterface
    private interface Check {
        void run() throws Exception;
    }

    /** A check paired with the name the report uses when it fails or throws. */
    private record NamedCheck(String name, Check body) {}

    /** The sample home's listing, used wherever a check compares the whole stored state. */
    private static final String[] SAMPLE_LISTING = {
        "Square feet: 2450",
        "Address: 1187 Aspen Ridge Drive",
        "City: Fort Collins",
        "State: CO",
        "ZIP code: 80525",
        "Model name: Aspen",
        "Sale status: available"
    };

    /** The text arguments Home routes through its shared blank check, in addHome order. */
    private static final String[] VALID_TEXT = {
        "4402 Larkspur Court", "Boise", "ID", "Larkspur", "available"
    };

    /** The label Home puts in the failure message, one per VALID_TEXT slot. */
    private static final String[] TEXT_LABELS = {
        "address", "city", "state", "model name", "sale status"
    };

    /** Runs every check and exits non-zero when any fail or throw. */
    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "constructor lists every attribute in assignment order",
                        HomeInventoryTest::constructorListsEveryAttributeInAssignmentOrder),
                new NamedCheck(
                        "listing keeps the leading zero in a ZIP code",
                        HomeInventoryTest::listingKeepsLeadingZeroInZipCode),
                new NamedCheck(
                        "rejected constructor leaves the home empty and reports why",
                        HomeInventoryTest::constructorWithUnknownStatusLeavesHomeEmptyAndReportsWhy),
                new NamedCheck(
                        "removeHome empties the slot and names the address",
                        HomeInventoryTest::removeHomeEmptiesTheSlotAndNamesTheAddress),
                new NamedCheck(
                        "removing twice reports failure",
                        HomeInventoryTest::removingTwiceReportsFailure),
                new NamedCheck(
                        "addHome refuses while a home is stored",
                        HomeInventoryTest::addHomeRefusesWhileAHomeIsStored),
                new NamedCheck(
                        "addHome after remove stores the new home",
                        HomeInventoryTest::addHomeAfterRemoveStoresTheNewHome),
                new NamedCheck(
                        "addHome rejects zero square feet",
                        HomeInventoryTest::addHomeRejectsZeroSquareFeet),
                new NamedCheck(
                        "addHome rejects a ZIP code above five digits",
                        HomeInventoryTest::addHomeRejectsZipCodeAboveFiveDigits),
                new NamedCheck(
                        "addHome accepts both ZIP code boundaries",
                        HomeInventoryTest::addHomeAcceptsTheZipCodeBoundaries),
                new NamedCheck(
                        "addHome rejects missing text in every required field",
                        HomeInventoryTest::addHomeRejectsMissingTextInEveryRequiredField),
                new NamedCheck(
                        "addHome rejects an unknown sale status",
                        HomeInventoryTest::addHomeRejectsUnknownSaleStatus),
                new NamedCheck(
                        "updateSaleStatus normalizes capitalization and spaces",
                        HomeInventoryTest::updateSaleStatusNormalizesCapitalizationAndSpaces),
                new NamedCheck(
                        "updateSaleStatus accepts non-ASCII whitespace padding",
                        HomeInventoryTest::updateSaleStatusAcceptsUnicodeWhitespacePadding),
                new NamedCheck(
                        "updateSaleStatus rejects an unknown status and keeps the old one",
                        HomeInventoryTest::updateSaleStatusRejectsUnknownStatusAndKeepsTheOldOne),
                new NamedCheck(
                        "updateSaleStatus fails on an empty home",
                        HomeInventoryTest::updateSaleStatusFailsOnEmptyHome),
                new NamedCheck(
                        "updateHome replaces every attribute",
                        HomeInventoryTest::updateHomeReplacesEveryAttribute),
                new NamedCheck(
                        "updateHome rejects an invalid value and keeps the old home",
                        HomeInventoryTest::updateHomeRejectsInvalidValueAndKeepsTheOldHome),
                new NamedCheck(
                        "updateHome fails on an empty home",
                        HomeInventoryTest::updateHomeFailsOnEmptyHome),
                new NamedCheck(
                        "run prints every step in assignment order",
                        HomeInventoryTest::runPrintsEveryStepInAssignmentOrder),
                new NamedCheck(
                        "answering Y writes the listing to the file",
                        HomeInventoryTest::answeringYWritesTheListingToTheFile),
                new NamedCheck(
                        "answering Y with surrounding spaces still writes the file",
                        HomeInventoryTest::answeringYWithSurroundingSpacesStillWritesTheFile),
                new NamedCheck(
                        "answering N skips the file",
                        HomeInventoryTest::answeringNSkipsTheFile),
                new NamedCheck(
                        "input ending before the answer skips the file",
                        HomeInventoryTest::inputEndingBeforeTheAnswerSkipsTheFile),
                new NamedCheck(
                        "writeListing reports failure when the directory is a file",
                        HomeInventoryTest::writeListingReportsFailureWhenTheDirectoryIsAFile),
                new NamedCheck(
                        "displayPath names a file inside the working directory relatively",
                        HomeInventoryTest::displayPathNamesAFileInsideTheWorkingDirectoryRelatively),
                new NamedCheck(
                        "displayPath keeps an absolute path outside the working directory",
                        HomeInventoryTest::displayPathKeepsAnAbsolutePathOutsideTheWorkingDirectory));

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
     * exception cancels every later check and the tally never prints, which reports a fraction of
     * the damage and points the reader at the wrong place.
     *
     * @param check check to run
     */
    private static void runContained(NamedCheck check) {
        try {
            check.body().run();
        } catch (AssertionError | Exception thrown) {
            // Error other than AssertionError is deliberately not caught: a JVM that is already
            // unwell should stop, not keep reporting checks.
            aborted++;
            String report = "ABORTED: " + check.name() + " threw " + describe(thrown);
            System.out.println(report);
            System.err.println(report);
        }
    }

    /**
     * Names a throwable the way a failure report needs it, since getMessage alone is null for
     * several of the exceptions these checks can raise.
     *
     * @param thrown throwable to describe
     * @return the simple class name, with the message when there is one
     */
    private static String describe(Throwable thrown) {
        String detail = thrown.getMessage();

        if (detail == null) {
            return thrown.getClass().getSimpleName();
        }

        return thrown.getClass().getSimpleName() + ": " + detail;
    }

    /** A valid home so each check starts from the same state. */
    private static Home sampleHome() {
        return new Home(2450, "1187 Aspen Ridge Drive", "Fort Collins", "CO", 80525, "Aspen",
                "available");
    }

    /** Checks one labeled line per attribute, in the assignment's order. */
    private static void constructorListsEveryAttributeInAssignmentOrder() {
        checkListing(sampleHome(), SAMPLE_LISTING, "constructor lists all seven attributes in order");
    }

    /** An int drops the leading zero; the listing must put it back. */
    private static void listingKeepsLeadingZeroInZipCode() {
        Home home = new Home(1200, "9 Beacon Street", "Boston", "MA", 2108, "Beacon", "sold");
        check(
                home.listHome()[4].equals("ZIP code: 02108"),
                "ZIP code prints with its leading zero");
    }

    /** The constructor cannot return a message, so the reason is read from the error stream. */
    private static void constructorWithUnknownStatusLeavesHomeEmptyAndReportsWhy() {
        ByteArrayOutputStream capturedErrors = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        Home home;

        System.setErr(new PrintStream(capturedErrors, true, StandardCharsets.UTF_8));
        try {
            home = new Home(2450, "1187 Aspen Ridge Drive", "Fort Collins", "CO", 80525, "Aspen",
                    "pending");
        } finally {
            System.setErr(originalErr);
        }

        String errors = capturedErrors.toString(StandardCharsets.UTF_8);

        check(
                errors.contains(
                        "Failed to create home: sale status must be one of: sold, available,"
                                + " under contract"),
                "rejected constructor explains the allowed sale statuses on the error stream");
        check(
                home.listHome()[0].equals("Failed to list home: no home is stored"),
                "rejected constructor leaves the home empty");
    }

    /**
     * Named for what the checks can actually observe. Home exposes no attribute except through
     * listHome, which reports the empty slot before reading a field, so "cleared the attributes"
     * is not a property any check here can see.
     */
    private static void removeHomeEmptiesTheSlotAndNamesTheAddress() {
        Home home = sampleHome();

        check(
                home.removeHome().equals("Home removed: 1187 Aspen Ridge Drive"),
                "removeHome reports the removed address");
        check(
                home.listHome()[0].equals("Failed to list home: no home is stored"),
                "listing after removeHome reports no home");
    }

    /** A second remove has nothing to clear. */
    private static void removingTwiceReportsFailure() {
        Home home = sampleHome();
        home.removeHome();

        check(
                home.removeHome().equals("Failed to remove home: no home is stored"),
                "removing an empty home reports failure");
    }

    /** Adding over a stored home would silently lose it. */
    private static void addHomeRefusesWhileAHomeIsStored() {
        Home home = sampleHome();
        String message =
                home.addHome(1880, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "available");

        check(
                message.equals("Failed to add home: a home is already stored; remove it first"),
                "addHome refuses while a home is stored");
        checkListing(home, SAMPLE_LISTING, "refused addHome leaves every attribute untouched");
    }

    /** Checks the remove-then-add path the assignment script follows. */
    private static void addHomeAfterRemoveStoresTheNewHome() {
        Home home = sampleHome();
        home.removeHome();
        String message =
                home.addHome(1880, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "available");

        check(message.equals("Home added: 4402 Larkspur Court"), "addHome reports the new address");
        checkListing(
                home,
                new String[] {
                    "Square feet: 1880",
                    "Address: 4402 Larkspur Court",
                    "City: Boise",
                    "State: ID",
                    "ZIP code: 83704",
                    "Model name: Larkspur",
                    "Sale status: available"
                },
                "listing shows every attribute of the added home");
    }

    /** Zero is the boundary for square feet. */
    private static void addHomeRejectsZeroSquareFeet() {
        Home home = sampleHome();
        home.removeHome();
        String message =
                home.addHome(0, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "available");

        check(
                message.equals("Failed to add home: square feet must be greater than zero"),
                "addHome rejects zero square feet");
    }

    /** 100000 is the first value that cannot be a five-digit ZIP code. */
    private static void addHomeRejectsZipCodeAboveFiveDigits() {
        Home home = sampleHome();
        home.removeHome();
        String message =
                home.addHome(1880, "4402 Larkspur Court", "Boise", "ID", 100000, "Larkspur",
                        "available");

        check(
                message.equals("Failed to add home: zip code must be between 1 and 99999"),
                "addHome rejects a six-digit ZIP code");
    }

    /**
     * The rejection checks cover 0 and 100000, which leaves the accepted range verified only from
     * outside. Narrowing the range by one at either end would otherwise pass unnoticed.
     */
    private static void addHomeAcceptsTheZipCodeBoundaries() {
        int[] boundaries = {1, 99999};
        String[] listed = {"ZIP code: 00001", "ZIP code: 99999"};

        for (int index = 0; index < boundaries.length; index++) {
            Home home = sampleHome();
            home.removeHome();
            String message =
                    home.addHome(1880, "4402 Larkspur Court", "Boise", "ID", boundaries[index],
                            "Larkspur", "available");

            check(
                    message.equals("Home added: 4402 Larkspur Court"),
                    "addHome accepts ZIP code " + boundaries[index]);
            check(
                    home.listHome()[4].equals(listed[index]),
                    "listing shows " + listed[index]);
        }
    }

    /**
     * Address, city, state, model name, and sale status all reach the same blank check, so they get
     * the same two cases. A table keeps a new attribute one row away instead of one method away.
     */
    private static void addHomeRejectsMissingTextInEveryRequiredField() {
        String[] missingValues = {"   ", null};
        String[] missingNames = {"blank", "null"};

        for (int slot = 0; slot < VALID_TEXT.length; slot++) {
            for (int kind = 0; kind < missingValues.length; kind++) {
                checkMissingTextRejected(slot, missingValues[kind], missingNames[kind]);
            }
        }
    }

    /**
     * One row of the table: one slot is unusable and every other value is valid, so the failure
     * message must name that slot.
     *
     * @param slot index into VALID_TEXT that gets the unusable value
     * @param missingValue the unusable value, blank or null
     * @param kindName word for the kind of unusable value, used in the check description
     */
    private static void checkMissingTextRejected(int slot, String missingValue, String kindName) {
        String[] text = VALID_TEXT.clone();
        text[slot] = missingValue;

        Home home = sampleHome();
        home.removeHome();
        String message = home.addHome(1880, text[0], text[1], text[2], 83704, text[3], text[4]);
        String row = kindName + " " + TEXT_LABELS[slot];

        check(
                message.equals("Failed to add home: " + TEXT_LABELS[slot] + " must not be blank"),
                "addHome rejects a " + row + " (message was \"" + message + "\")");
        check(
                home.listHome()[0].equals("Failed to list home: no home is stored"),
                "addHome with a " + row + " leaves the slot empty");
    }

    /** Only the three assignment statuses are allowed. */
    private static void addHomeRejectsUnknownSaleStatus() {
        Home home = sampleHome();
        home.removeHome();
        String message =
                home.addHome(1880, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "pending");

        check(
                message.equals(
                        "Failed to add home: sale status must be one of: sold, available,"
                                + " under contract"),
                "addHome rejects an unknown sale status");
    }

    /** Checks the assignment's update step with input a person would type. */
    private static void updateSaleStatusNormalizesCapitalizationAndSpaces() {
        Home home = sampleHome();

        check(
                home.updateSaleStatus("  Under Contract ")
                        .equals("Sale status changed from available to under contract"),
                "updateSaleStatus reports the old and new status");
        check(
                home.listHome()[6].equals("Sale status: under contract"),
                "listing shows the normalized sale status");
    }

    /**
     * The blank check counts every whitespace character, so the cleanup has to as well. An
     * ideographic space is the case that separates strip() from trim(): with trim() the padding
     * survives and an allowed status stops matching.
     */
    private static void updateSaleStatusAcceptsUnicodeWhitespacePadding() {
        Home home = sampleHome();

        check(
                home.updateSaleStatus("　under contract　")
                        .equals("Sale status changed from available to under contract"),
                "updateSaleStatus accepts a status padded with ideographic spaces");
        check(
                home.listHome()[6].equals("Sale status: under contract"),
                "listing shows the status with the padding removed");
    }

    /** A rejected update must not blank the status. */
    private static void updateSaleStatusRejectsUnknownStatusAndKeepsTheOldOne() {
        Home home = sampleHome();

        check(
                home.updateSaleStatus("pending")
                        .equals(
                                "Failed to update sale status: sale status must be one of: sold,"
                                        + " available, under contract"),
                "updateSaleStatus rejects an unknown status");
        check(
                home.listHome()[6].equals("Sale status: available"),
                "rejected update keeps the previous sale status");
    }

    /** Updating nothing is a failure, not a silent no-op. */
    private static void updateSaleStatusFailsOnEmptyHome() {
        Home home = sampleHome();
        home.removeHome();

        check(
                home.updateSaleStatus("sold")
                        .equals("Failed to update sale status: no home is stored"),
                "updateSaleStatus on an empty home reports failure");
    }

    /**
     * "Replaces every attribute" is a claim about all seven, so the check compares all seven.
     * Reading three of them let an update that silently kept the old city, state, and model name
     * pass.
     */
    private static void updateHomeReplacesEveryAttribute() {
        Home home = sampleHome();
        String message =
                home.updateHome(1880, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "sold");

        check(message.equals("Home updated: 4402 Larkspur Court"), "updateHome names the address");
        checkListing(
                home,
                new String[] {
                    "Square feet: 1880",
                    "Address: 4402 Larkspur Court",
                    "City: Boise",
                    "State: ID",
                    "ZIP code: 83704",
                    "Model name: Larkspur",
                    "Sale status: sold"
                },
                "updateHome replaces every attribute");
    }

    /**
     * The stored home promises it is never left half-written, which is a claim about all seven
     * attributes, so the check compares the whole listing rather than the address alone.
     */
    private static void updateHomeRejectsInvalidValueAndKeepsTheOldHome() {
        Home home = sampleHome();
        String message =
                home.updateHome(1880, "4402 Larkspur Court", "Boise", "ID", 0, "Larkspur", "sold");

        check(
                message.equals("Failed to update home: zip code must be between 1 and 99999"),
                "updateHome rejects a zero ZIP code");
        checkListing(home, SAMPLE_LISTING, "rejected updateHome leaves every attribute unchanged");
    }

    /** There is nothing to update after a remove. */
    private static void updateHomeFailsOnEmptyHome() {
        Home home = sampleHome();
        home.removeHome();
        String message =
                home.updateHome(1880, "4402 Larkspur Court", "Boise", "ID", 83704, "Larkspur",
                        "sold");

        check(
                message.equals("Failed to update home: no home is stored"),
                "updateHome on an empty home reports failure");
    }

    /** Drives the whole script and checks each step appears in order. */
    private static void runPrintsEveryStepInAssignmentOrder() throws IOException {
        Path scratch = Files.createTempDirectory("home-inventory-test");

        try {
            String output = runWithInput("N\n", scratch.resolve("Home.txt"));
            String[] expectedInOrder = {
                "Home from the constructor:",
                "Address: 1187 Aspen Ridge Drive",
                "Home removed: 1187 Aspen Ridge Drive",
                "Home added: 4402 Larkspur Court",
                "Sale status: available",
                "Sale status changed from available to under contract",
                "Sale status: under contract",
                "Print this information to a file? (Y or N): "
            };
            int previousPosition = -1;

            for (String expected : expectedInOrder) {
                int position = output.indexOf(expected, previousPosition + 1);
                check(
                        position > previousPosition,
                        "output contains \"" + expected.trim() + "\" in order");
                previousPosition = Math.max(position, previousPosition);
            }
        } finally {
            deleteScratch(scratch);
        }
    }

    /** Checks the Y path writes the final listing and names the file. */
    private static void answeringYWritesTheListingToTheFile() throws IOException {
        Path scratch = Files.createTempDirectory("home-inventory-test");
        Path reportFile = scratch.resolve("Home.txt");

        try {
            String output = runWithInput("y\n", reportFile);

            check(
                    output.contains(
                            "Home information printed to " + HomeInventory.displayPath(reportFile)),
                    "answering Y reports where the file went");
            check(Files.exists(reportFile), "answering Y creates the file");

            String contents = Files.readString(reportFile, StandardCharsets.UTF_8);
            check(
                    contents.contains("Address: 4402 Larkspur Court")
                            && contents.contains("Sale status: under contract"),
                    "file holds the listing of the updated home");
        } finally {
            deleteScratch(scratch);
        }
    }

    /**
     * A person types the answer, so it can arrive padded. The trim in the prompt handling is what
     * covers that, and nothing else exercises it.
     */
    private static void answeringYWithSurroundingSpacesStillWritesTheFile() throws IOException {
        Path scratch = Files.createTempDirectory("home-inventory-test");
        Path reportFile = scratch.resolve("Home.txt");

        try {
            runWithInput("  y  \n", reportFile);

            check(Files.exists(reportFile), "answering Y with spaces around it creates the file");
        } finally {
            deleteScratch(scratch);
        }
    }

    /** Checks the N path says so and leaves no file. */
    private static void answeringNSkipsTheFile() throws IOException {
        Path scratch = Files.createTempDirectory("home-inventory-test");
        Path reportFile = scratch.resolve("Home.txt");

        try {
            String output = runWithInput("n\n", reportFile);

            check(
                    output.contains("A file will not be printed."),
                    "answering N says no file is printed");
            check(!Files.exists(reportFile), "answering N leaves no file behind");
        } finally {
            deleteScratch(scratch);
        }
    }

    /** Piped input can end before the prompt; that must read as N. */
    private static void inputEndingBeforeTheAnswerSkipsTheFile() throws IOException {
        Path scratch = Files.createTempDirectory("home-inventory-test");
        Path reportFile = scratch.resolve("Home.txt");

        try {
            String output = runWithInput("", reportFile);

            check(
                    output.contains("A file will not be printed."),
                    "input that ends early counts as no");
            check(!Files.exists(reportFile), "input that ends early leaves no file behind");
        } finally {
            deleteScratch(scratch);
        }
    }

    /** A regular file where a directory is needed is the one write failure the tests can force. */
    private static void writeListingReportsFailureWhenTheDirectoryIsAFile() throws IOException {
        Path blocker = Files.createTempFile("home-inventory-test", ".blocker");

        try {
            String prefix = "Failed to print the file: ";
            String message = HomeInventory.writeListing(sampleHome(), blocker.resolve("Home.txt"));

            check(
                    message.startsWith(prefix),
                    "writeListing reports failure instead of throwing");
            check(
                    message.length() > prefix.length(),
                    "writeListing failure message says which file (message was \"" + message + "\")");
        } finally {
            Files.deleteIfExists(blocker);
        }
    }

    /**
     * Under "make run" the absolute path names the container's mount point, which the host shell
     * cannot open, so a file inside the working directory has to be reported relative to it.
     */
    private static void displayPathNamesAFileInsideTheWorkingDirectoryRelatively() {
        Path insideWorkingDirectory = Path.of("Home.txt").toAbsolutePath();

        check(
                HomeInventory.displayPath(insideWorkingDirectory).equals("Home.txt"),
                "displayPath names a file in the working directory relative to it");
    }

    /** Outside the working directory a relative path would not resolve, so it stays absolute. */
    private static void displayPathKeepsAnAbsolutePathOutsideTheWorkingDirectory()
            throws IOException {
        Path scratch = Files.createTempDirectory("home-inventory-test");
        Path outside = scratch.resolve("Home.txt");

        try {
            check(
                    HomeInventory.displayPath(outside)
                            .equals(outside.toAbsolutePath().normalize().toString()),
                    "displayPath keeps the absolute path for a file outside the working directory");
        } finally {
            deleteScratch(scratch);
        }
    }

    /** Runs the program against canned input and captures its output. */
    private static String runWithInput(String inputText, Path reportFile) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try (Scanner input = new Scanner(inputText);
                PrintStream output = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            HomeInventory.run(input, output, reportFile);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Compares the whole listing, so an update that keeps one stale attribute cannot hide behind
     * the attributes a check happens to read.
     *
     * @param home home whose listing is compared
     * @param expected the listing the home should produce
     * @param description what the comparison is checking
     */
    private static void checkListing(Home home, String[] expected, String description) {
        String[] actual = home.listHome();

        check(Arrays.equals(expected, actual), description + firstDifference(expected, actual));
    }

    /**
     * Names the first line that differs, so comparing the whole listing still points at one line
     * instead of printing two arrays.
     *
     * @param expected the listing the home should produce
     * @param actual the listing it produced
     * @return a parenthesized description of the first difference, or an empty string when equal
     */
    private static String firstDifference(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return " (expected " + expected.length + " lines, got " + actual.length + ")";
        }

        for (int line = 0; line < expected.length; line++) {
            if (!expected[line].equals(actual[line])) {
                return " (line " + line + ": expected \"" + expected[line] + "\", got \""
                        + actual[line] + "\")";
            }
        }

        return "";
    }

    /**
     * Removes a scratch directory and anything a check left in it. Deleting the directory alone
     * fails when a check wrote a file, and that failure used to end the whole run.
     *
     * @param scratch directory to remove
     */
    private static void deleteScratch(Path scratch) {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(scratch)) {
            for (Path entry : entries) {
                Files.deleteIfExists(entry);
            }

            Files.deleteIfExists(scratch);
        } catch (IOException leftBehind) {
            // Cleanup is best effort: a temp directory that outlives the run must not fail a check
            // or mask the check that actually failed.
            System.err.println("Could not remove " + scratch + ": " + leftBehind.getMessage());
        }
    }

    /** Prints PASS or FAIL and counts failures for the exit code. */
    private static void check(boolean condition, String description) {
        if (condition) {
            System.out.println("PASS: " + description);
            return;
        }

        failures++;
        System.out.println("FAIL: " + description);
        System.err.println("FAIL: " + description);
    }
}
