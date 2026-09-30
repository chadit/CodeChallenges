import java.awt.Image;
import java.awt.Taskbar;
import java.net.URL;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/** Opens the date log window: a text box under an Actions menu with four items. */
public final class DateLogApp {

    private DateLogApp() {}

    public static void main(String[] args) {
        // Swing components are only safe to touch from the event dispatch thread.
        SwingUtilities.invokeLater(DateLogApp::showWindow);
    }

    private static void showWindow() {
        JFrame frame = new JFrame("Date Log");
        // log.txt lands in the folder the program was started from. Exit calls System.exit because
        // on this Mac the JVM kept running after its last window was disposed.
        DateLogPanel panel = new DateLogPanel(
                Clock.systemDefaultZone(), new Random(), Path.of("log.txt"), () -> System.exit(0));

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(panel.menuBar);
        // The panel is the content pane, so its background is what shows as the frame's.
        frame.setContentPane(panel);
        applyIcon(frame);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** The icon is decoration, so a missing image keeps the default instead of stopping. */
    private static void applyIcon(JFrame frame) {
        URL iconLocation = DateLogApp.class.getResource("/icon.png");
        if (iconLocation == null) {
            return;
        }

        Image icon = new ImageIcon(iconLocation).getImage();
        frame.setIconImage(icon);

        // macOS windows have no title-bar icon; its Dock icon is only reachable through Taskbar.
        if (Taskbar.isTaskbarSupported()
                && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
            Taskbar.getTaskbar().setIconImage(icon);
        }
    }
}
