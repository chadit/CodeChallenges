import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The window body and its Actions menu. The four items print the date and time in the text box,
 * save the text box to log.txt, turn the background a green drawn once per run, and exit.
 */
public final class DateLogPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Pure green sits at 120 degrees on the HSB wheel, and 90 through 150 still reads as green.
    // The fixed saturation and brightness keep the text box and status line readable on top.
    private static final float GREEN_HUE_LOW = 90f / 360f;
    private static final float GREEN_HUE_HIGH = 150f / 360f;
    private static final float GREEN_SATURATION = 0.55f;
    private static final float GREEN_BRIGHTNESS = 0.9f;

    // Package-private so the checks can choose menu items the way a user does.
    final JMenuBar menuBar = new JMenuBar();
    final JMenuItem showDateTimeItem = new JMenuItem("Show Date and Time", KeyEvent.VK_D);
    final JMenuItem saveLogItem = new JMenuItem("Save to log.txt", KeyEvent.VK_S);
    final JMenuItem greenBackgroundItem = new JMenuItem("Green Background", KeyEvent.VK_G);
    final JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
    final JTextArea textArea = new JTextArea(12, 40);
    final JLabel statusLabel = new JLabel("Choose an item from the Actions menu.");

    /** Drawn once per run so every Green Background selection shows the same hue. */
    private final Color greenBackground;

    /** Names the hue in degrees and the hex code an HTML color picker would show for it. */
    private final String greenStatus;

    /** Transient because JPanel is Serializable and neither Clock nor Path is. */
    private final transient Clock clock;
    private final transient Path logFile;

    /**
     * @param clock source of the date and time the first item prints
     * @param random draws this run's green hue
     * @param logFile where the second item writes the text box
     * @param exit what the fourth item runs
     */
    public DateLogPanel(Clock clock, Random random, Path logFile, Runnable exit) {
        super(new BorderLayout(0, 8));
        this.clock = Objects.requireNonNull(clock, "clock");
        this.logFile = Objects.requireNonNull(logFile, "logFile");
        Objects.requireNonNull(exit, "exit");

        float greenHue = Objects.requireNonNull(random, "random")
                .nextFloat(GREEN_HUE_LOW, GREEN_HUE_HIGH);
        greenBackground = Color.getHSBColor(greenHue, GREEN_SATURATION, GREEN_BRIGHTNESS);
        // Locale.ROOT keeps the digits ASCII on every machine, matching the hex part.
        greenStatus = String.format(Locale.ROOT,
                "Green background: hue %d degrees, #%02X%02X%02X.", Math.round(greenHue * 360),
                greenBackground.getRed(), greenBackground.getGreen(), greenBackground.getBlue());

        JMenu actions = new JMenu("Actions");
        actions.setMnemonic(KeyEvent.VK_A);
        actions.add(showDateTimeItem);
        actions.add(saveLogItem);
        actions.add(greenBackgroundItem);
        actions.add(exitItem);
        menuBar.add(actions);

        showDateTimeItem.addActionListener(event -> showDateTime());
        saveLogItem.addActionListener(event -> saveLog());
        greenBackgroundItem.addActionListener(event -> showGreenBackground());
        exitItem.addActionListener(event -> exit.run());

        // The text box covers the middle, so the margin is where the background shows.
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void showDateTime() {
        textArea.append(LocalDateTime.now(clock).format(TIMESTAMP) + "\n");
        statusLabel.setText("Added the date and time.");
    }

    /** Writes the whole text box, replacing an earlier log.txt rather than appending to it. */
    private void saveLog() {
        try {
            Files.writeString(logFile, textArea.getText(), StandardCharsets.UTF_8);
        } catch (IOException failed) {
            statusLabel.setText(
                    "Could not write " + logFile.getFileName() + ": " + describe(failed));
            return;
        }

        statusLabel.setText("Saved the text box to " + logFile.getFileName() + ".");
    }

    private void showGreenBackground() {
        setBackground(greenBackground);
        statusLabel.setText(greenStatus);
    }

    /**
     * NoSuchFileException and AccessDeniedException carry only the path as their message and
     * some IOExceptions carry none, so the exception's name is what says why the write failed.
     * Package-private so the checks can reach the no-message case, which Files never raises.
     */
    static String describe(IOException failed) {
        String reason = failed.getClass().getSimpleName();
        return failed.getMessage() == null ? reason : reason + " " + failed.getMessage();
    }
}
