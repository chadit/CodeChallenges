import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Checks DateLogPanel by choosing its menu items the way a user does. A panel and its menu bar
 * need no display until they are put in a window, so these run headless under "make test",
 * which exits non-zero when any check fails.
 */
public final class DateLogChecks {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-30T14:05:09Z"), ZoneOffset.UTC);
    private static final String FIXED_LINE = "2026-09-30 14:05:09\n";

    // The panel draws 90 through 150 degrees; one degree of slack covers the 8-bit RGB round trip.
    private static final float GREEN_HUE_LOW = 89f / 360f;
    private static final float GREEN_HUE_HIGH = 151f / 360f;

    private static final Pattern HEX_CODE = Pattern.compile("#[0-9A-F]{6}");

    private static int failures = 0;

    /** Check methods that ended early by throwing. */
    private static int aborted = 0;

    private record NamedCheck(String name, Runnable body) {}

    private DateLogChecks() {}

    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "the menu bar holds one menu with the four items in order",
                        DateLogChecks::menuBarHoldsOneMenuWithTheFourItemsInOrder),
                new NamedCheck(
                        "Show Date and Time prints the clock's date and time in the text box",
                        DateLogChecks::showDateTimePrintsTheDateAndTime),
                new NamedCheck(
                        "Show Date and Time adds a line each time instead of replacing",
                        DateLogChecks::showDateTimeAddsALineEachTime),
                new NamedCheck(
                        "Save to log.txt writes the text box contents to the file",
                        DateLogChecks::saveWritesTheTextBoxToTheFile),
                new NamedCheck(
                        "Save to log.txt with an empty text box writes an empty file",
                        DateLogChecks::saveWithAnEmptyTextBoxWritesAnEmptyFile),
                new NamedCheck(
                        "Save to log.txt replaces the earlier file instead of appending",
                        DateLogChecks::saveReplacesTheEarlierFile),
                new NamedCheck(
                        "Save to log.txt reports a file it cannot write and why",
                        DateLogChecks::saveReportsAFileItCannotWriteAndWhy),
                new NamedCheck(
                        "a failure with no message is described by its name alone",
                        DateLogChecks::failureWithNoMessageIsDescribedByItsNameAlone),
                new NamedCheck(
                        "Green Background turns the panel green and names the color code",
                        DateLogChecks::greenBackgroundTurnsThePanelGreen),
                new NamedCheck(
                        "Green Background keeps the same hue for the whole run",
                        DateLogChecks::greenBackgroundKeepsTheSameHueForTheRun),
                new NamedCheck(
                        "the hue differs between runs but stays green",
                        DateLogChecks::hueDiffersBetweenRunsButStaysGreen),
                new NamedCheck(
                        "Exit runs the exit action and the other items do not",
                        DateLogChecks::exitRunsTheExitActionAndTheOtherItemsDoNot));

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

    /** Keeps one unexpected throw from cancelling every later check and the tally. */
    private static void runContained(NamedCheck check) {
        try {
            check.body().run();
        } catch (AssertionError | RuntimeException thrown) {
            aborted++;
            String report = "ABORTED: " + check.name() + " threw " + thrown;
            System.out.println(report);
            System.err.println(report);
        }
    }

    private static void menuBarHoldsOneMenuWithTheFourItemsInOrder() {
        DateLogPanel panel = newPanel();

        checkEquals(1, panel.menuBar.getMenuCount(), "one menu on the bar");
        JMenu menu = panel.menuBar.getMenu(0);
        checkEquals("Actions", menu.getText(), "the menu is named Actions");
        checkEquals(4, menu.getItemCount(), "four items on the menu");
        checkEquals(List.of("Show Date and Time", "Save to log.txt", "Green Background", "Exit"),
                itemLabels(menu), "items appear in the assignment's order");
    }

    private static void showDateTimePrintsTheDateAndTime() {
        DateLogPanel panel = newPanel();

        choose(panel.showDateTimeItem);

        checkEquals(FIXED_LINE, panel.textArea.getText(), "text box shows the date and time");
        checkEquals("Added the date and time.", panel.statusLabel.getText(), "status confirms it");
    }

    private static void showDateTimeAddsALineEachTime() {
        DateLogPanel panel = newPanel();

        choose(panel.showDateTimeItem);
        choose(panel.showDateTimeItem);

        checkEquals(FIXED_LINE + FIXED_LINE, panel.textArea.getText(), "two selections, two lines");
    }

    private static void saveWritesTheTextBoxToTheFile() {
        Path logFile = tempLogFile();
        DateLogPanel panel = newPanel(logFile);
        choose(panel.showDateTimeItem);
        panel.textArea.append("typed note\n");

        choose(panel.saveLogItem);

        checkEquals(FIXED_LINE + "typed note\n", readText(logFile),
                "file holds the text box contents");
        checkEquals("Saved the text box to log.txt.", panel.statusLabel.getText(),
                "status confirms the save");
    }

    private static void saveWithAnEmptyTextBoxWritesAnEmptyFile() {
        Path logFile = tempLogFile();
        DateLogPanel panel = newPanel(logFile);

        choose(panel.saveLogItem);

        checkEquals("", readText(logFile), "file exists and is empty");
    }

    private static void saveReplacesTheEarlierFile() {
        Path logFile = tempLogFile();
        DateLogPanel panel = newPanel(logFile);
        choose(panel.showDateTimeItem);
        choose(panel.saveLogItem);

        panel.textArea.setText("second\n");
        choose(panel.saveLogItem);

        checkEquals("second\n", readText(logFile), "second save replaces the first");
    }

    private static void saveReportsAFileItCannotWriteAndWhy() {
        Path missingFolder = tempLogFile().resolveSibling("missing-folder");
        DateLogPanel panel = newPanel(missingFolder.resolve("log.txt"));
        choose(panel.showDateTimeItem);

        choose(panel.saveLogItem);

        String status = panel.statusLabel.getText();
        checkContains(status, "Could not write log.txt", "the failed save is reported");
        checkContains(status, "NoSuchFileException", "the reason is named");
        checkContains(status, missingFolder.getFileName().toString(), "the missing folder is named");
    }

    private static void failureWithNoMessageIsDescribedByItsNameAlone() {
        checkEquals("ClosedByInterruptException",
                DateLogPanel.describe(new ClosedByInterruptException()),
                "no message means no trailing text");
    }

    private static void greenBackgroundTurnsThePanelGreen() {
        DateLogPanel panel = newPanel();
        Color before = panel.getBackground();

        choose(panel.greenBackgroundItem);

        Color after = panel.getBackground();
        check(!before.equals(after), "background changed from " + before);
        check(isGreen(after), "background hue is in the green band (" + after + ")");
        String status = panel.statusLabel.getText();
        checkContains(status, "hue", "status names the hue");
        Matcher code = HEX_CODE.matcher(status);
        boolean named = code.find();
        check(named, "status names a hex color code (\"" + status + "\")");
        if (!named) {
            return;
        }
        checkEquals(after, Color.decode(code.group()), "status hex code matches the background");
    }

    private static void greenBackgroundKeepsTheSameHueForTheRun() {
        DateLogPanel panel = newPanel();

        choose(panel.greenBackgroundItem);
        Color first = panel.getBackground();
        String firstStatus = panel.statusLabel.getText();
        choose(panel.greenBackgroundItem);

        checkEquals(first, panel.getBackground(), "second selection shows the first hue again");
        checkEquals(firstStatus, panel.statusLabel.getText(), "status names the same color");
    }

    private static void hueDiffersBetweenRunsButStaysGreen() {
        Path logFile = tempLogFile();
        Set<Color> distinct = new HashSet<>();
        int greenRuns = 0;
        for (long seed = 1; seed <= 50; seed++) {
            DateLogPanel panel = new DateLogPanel(FIXED_CLOCK, new Random(seed), logFile, () -> {});
            choose(panel.greenBackgroundItem);
            distinct.add(panel.getBackground());
            if (isGreen(panel.getBackground())) {
                greenRuns++;
            }
        }

        checkEquals(50, greenRuns, "every run's background is green");
        check(distinct.size() > 1,
                "runs do not all share one hue (" + distinct.size() + " distinct color(s))");
    }

    private static void exitRunsTheExitActionAndTheOtherItemsDoNot() {
        AtomicBoolean exited = new AtomicBoolean();
        DateLogPanel panel =
                new DateLogPanel(FIXED_CLOCK, new Random(7), tempLogFile(), () -> exited.set(true));

        choose(panel.showDateTimeItem);
        choose(panel.saveLogItem);
        choose(panel.greenBackgroundItem);
        checkEquals(false, exited.get(), "the first three items do not exit");

        choose(panel.exitItem);
        checkEquals(true, exited.get(), "Exit runs the exit action");
    }

    private static DateLogPanel newPanel() {
        return newPanel(tempLogFile());
    }

    private static DateLogPanel newPanel(Path logFile) {
        return new DateLogPanel(FIXED_CLOCK, new Random(7), logFile, () -> {});
    }

    /** A fresh log.txt inside a temp folder, both removed when the checks exit. */
    private static Path tempLogFile() {
        try {
            Path folder = Files.createTempDirectory("datelog");
            Path logFile = folder.resolve("log.txt");
            // deleteOnExit runs in reverse registration order, so the file goes before its folder.
            folder.toFile().deleteOnExit();
            logFile.toFile().deleteOnExit();
            return logFile;
        } catch (IOException failed) {
            throw new UncheckedIOException(failed);
        }
    }

    private static String readText(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException failed) {
            throw new UncheckedIOException(failed);
        }
    }

    private static List<String> itemLabels(JMenu menu) {
        List<String> labels = new ArrayList<>();
        for (int index = 0; index < menu.getItemCount(); index++) {
            labels.add(menu.getItem(index).getText());
        }
        return labels;
    }

    private static boolean isGreen(Color color) {
        float hue = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
        return hue >= GREEN_HUE_LOW && hue <= GREEN_HUE_HIGH;
    }

    private static void choose(JMenuItem item) {
        // A zero hold time skips the pause doClick() adds so a person can see the press.
        item.doClick(0);
    }

    private static void checkContains(String actual, String expected, String description) {
        check(actual.contains(expected),
                description + " (missing \"" + expected + "\" in \"" + actual + "\")");
    }

    private static void checkEquals(Object expected, Object actual, String description) {
        check(expected.equals(actual),
                description + " (expected \"" + expected + "\", got \"" + actual + "\")");
    }

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
