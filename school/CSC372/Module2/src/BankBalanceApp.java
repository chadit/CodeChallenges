import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Opens the bank balance window. Leaving through the Exit button or the window's close control
 * shows the remaining balance first.
 */
public final class BankBalanceApp {

    private BankBalanceApp() {}

    public static void main(String[] args) {
        // Swing components are only safe to touch from the event dispatch thread.
        SwingUtilities.invokeLater(BankBalanceApp::showWindow);
    }

    private static void showWindow() {
        BankAccount account = new BankAccount("Chad", "England", 1001);
        JFrame frame = new JFrame("Bank Balance");

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(event -> exit(frame, account));

        // The default close would skip the remaining-balance message, so closing goes through
        // the same exit as the button.
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exit(frame, account);
            }
        });

        applyIcon(frame);
        frame.add(new BankBalancePanel(account), BorderLayout.CENTER);
        frame.add(exitButton, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** The icon is decoration, so a missing image keeps the default instead of stopping. */
    private static void applyIcon(JFrame frame) {
        URL iconLocation = BankBalanceApp.class.getResource("/icon.png");
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

    private static void exit(JFrame frame, BankAccount account) {
        JOptionPane.showMessageDialog(
                frame,
                "Remaining balance: " + BankAccount.formatMoney(account.getBalance()),
                "Goodbye",
                JOptionPane.INFORMATION_MESSAGE);
        account.accountSummary();
        frame.dispose();
    }
}
