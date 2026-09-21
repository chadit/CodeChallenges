import java.util.List;
import javax.swing.JButton;

/**
 * Checks BankBalancePanel by typing into its field and clicking its buttons. A panel needs no
 * display until it is put in a window, so these run headless under "make test", which exits
 * non-zero when any check fails.
 */
public final class BankBalanceChecks {

    private static int failures = 0;

    /** Check methods that ended early by throwing. */
    private static int aborted = 0;

    private record NamedCheck(String name, Runnable body) {}

    private BankBalanceChecks() {}

    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "new panel shows no balance and only offers Set Balance",
                        BankBalanceChecks::newPanelShowsNoBalanceAndOnlyOffersSetBalance),
                new NamedCheck(
                        "Set Balance displays the balance and unlocks deposit and withdraw",
                        BankBalanceChecks::setBalanceDisplaysTheBalanceAndUnlocksTheRest),
                new NamedCheck(
                        "Set Balance accepts a starting balance of zero",
                        BankBalanceChecks::setBalanceAcceptsZero),
                new NamedCheck(
                        "Set Balance refuses a negative balance and stays available",
                        BankBalanceChecks::setBalanceRefusesNegativeAndStaysAvailable),
                new NamedCheck(
                        "text that is not a finite number is refused",
                        BankBalanceChecks::textThatIsNotAFiniteNumberIsRefused),
                new NamedCheck(
                        "Deposit adds to the displayed balance",
                        BankBalanceChecks::depositAddsToTheDisplayedBalance),
                new NamedCheck(
                        "Deposit refuses zero and negative amounts",
                        BankBalanceChecks::depositRefusesZeroAndNegativeAmounts),
                new NamedCheck(
                        "Withdraw subtracts from the displayed balance",
                        BankBalanceChecks::withdrawSubtractsFromTheDisplayedBalance),
                new NamedCheck(
                        "Withdraw of the whole balance leaves zero",
                        BankBalanceChecks::withdrawOfTheWholeBalanceLeavesZero),
                new NamedCheck(
                        "Withdraw beyond the balance is refused and changes nothing",
                        BankBalanceChecks::withdrawBeyondTheBalanceIsRefused),
                new NamedCheck(
                        "amount field clears after success and keeps refused text",
                        BankBalanceChecks::amountFieldClearsAfterSuccessAndKeepsRefusedText));

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

    private static void newPanelShowsNoBalanceAndOnlyOffersSetBalance() {
        BankAccount account = newAccount();
        BankBalancePanel panel = new BankBalancePanel(account);

        checkEquals("Balance: not set", panel.balanceLabel.getText(), "no balance shown yet");
        checkEquals(true, panel.setBalanceButton.isEnabled(), "Set Balance starts enabled");
        checkEquals(false, panel.depositButton.isEnabled(), "Deposit starts disabled");
        checkEquals(false, panel.withdrawButton.isEnabled(), "Withdraw starts disabled");

        enter(panel, "50", panel.depositButton);
        checkEquals(0.0, account.getBalance(), "clicking a disabled Deposit moves no money");
    }

    private static void setBalanceDisplaysTheBalanceAndUnlocksTheRest() {
        BankAccount account = newAccount();
        BankBalancePanel panel = new BankBalancePanel(account);

        enter(panel, " 1500.75 ", panel.setBalanceButton);

        checkEquals(1500.75, account.getBalance(), "account holds the entered balance");
        checkEquals("Balance: $1,500.75", panel.balanceLabel.getText(), "balance is displayed");
        checkEquals(false, panel.setBalanceButton.isEnabled(), "Set Balance locks after use");
        checkEquals(true, panel.depositButton.isEnabled(), "Deposit unlocks");
        checkEquals(true, panel.withdrawButton.isEnabled(), "Withdraw unlocks");
    }

    private static void setBalanceAcceptsZero() {
        BankBalancePanel panel = new BankBalancePanel(newAccount());

        enter(panel, "0", panel.setBalanceButton);

        checkEquals("Balance: $0.00", panel.balanceLabel.getText(), "zero balance is displayed");
        checkEquals(true, panel.depositButton.isEnabled(), "Deposit unlocks at a zero balance");
    }

    private static void setBalanceRefusesNegativeAndStaysAvailable() {
        BankBalancePanel panel = new BankBalancePanel(newAccount());

        enter(panel, "-20", panel.setBalanceButton);

        checkEquals("A starting balance cannot be negative.", panel.statusLabel.getText(),
                "negative starting balance is explained");
        checkEquals("Balance: not set", panel.balanceLabel.getText(), "no balance shown");
        checkEquals(true, panel.setBalanceButton.isEnabled(), "Set Balance can be tried again");
        checkEquals(false, panel.depositButton.isEnabled(), "Deposit stays disabled");
    }

    private static void textThatIsNotAFiniteNumberIsRefused() {
        BankAccount account = newAccount();
        BankBalancePanel panel = new BankBalancePanel(account);
        enter(panel, "100", panel.setBalanceButton);

        for (String text : List.of("", "   ", "abc", "12.5.1", "$40", "NaN", "Infinity")) {
            panel.statusLabel.setText("");
            enter(panel, text, panel.depositButton);
            checkEquals("Enter a dollar amount such as 125.50.", panel.statusLabel.getText(),
                    "\"" + text + "\" is refused with a hint");
        }

        checkEquals(100.0, account.getBalance(), "refused text leaves the balance alone");
    }

    private static void depositAddsToTheDisplayedBalance() {
        BankBalancePanel panel = openedPanel("100");

        enter(panel, "25.50", panel.depositButton);

        checkEquals("Balance: $125.50", panel.balanceLabel.getText(), "deposit raises the balance");
        checkEquals("Deposited $25.50.", panel.statusLabel.getText(), "deposit is confirmed");
    }

    private static void depositRefusesZeroAndNegativeAmounts() {
        BankBalancePanel panel = openedPanel("100");

        enter(panel, "0", panel.depositButton);
        enter(panel, "-5", panel.depositButton);

        checkEquals("Balance: $100.00", panel.balanceLabel.getText(),
                "refused deposits leave the balance alone");
        checkContains(panel.statusLabel.getText(), "greater than zero", "refusal is explained");
    }

    private static void withdrawSubtractsFromTheDisplayedBalance() {
        BankBalancePanel panel = openedPanel("100");

        enter(panel, "40.25", panel.withdrawButton);

        checkEquals("Balance: $59.75", panel.balanceLabel.getText(), "withdrawal lowers the balance");
        checkEquals("Withdrew $40.25.", panel.statusLabel.getText(), "withdrawal is confirmed");
    }

    private static void withdrawOfTheWholeBalanceLeavesZero() {
        BankBalancePanel panel = openedPanel("100");

        enter(panel, "100", panel.withdrawButton);

        checkEquals("Balance: $0.00", panel.balanceLabel.getText(), "whole balance can be withdrawn");
    }

    private static void withdrawBeyondTheBalanceIsRefused() {
        BankBalancePanel panel = openedPanel("100");

        enter(panel, "100.01", panel.withdrawButton);

        checkEquals("Balance: $100.00", panel.balanceLabel.getText(),
                "refused withdrawal leaves the balance alone");
        checkEquals("Insufficient funds to withdraw $100.01.", panel.statusLabel.getText(),
                "refused withdrawal is explained");
    }

    private static void amountFieldClearsAfterSuccessAndKeepsRefusedText() {
        BankBalancePanel panel = openedPanel("100");

        enter(panel, "10", panel.depositButton);
        checkEquals("", panel.amountField.getText(), "field clears after an accepted amount");

        enter(panel, "ten", panel.depositButton);
        checkEquals("ten", panel.amountField.getText(), "refused text stays for correction");
    }

    private static BankAccount newAccount() {
        return new BankAccount("Ada", "Lovelace", 1001);
    }

    private static BankBalancePanel openedPanel(String startingBalance) {
        BankBalancePanel panel = new BankBalancePanel(newAccount());
        enter(panel, startingBalance, panel.setBalanceButton);
        return panel;
    }

    private static void enter(BankBalancePanel panel, String text, JButton button) {
        panel.amountField.setText(text);
        // A zero hold time skips the pause doClick() adds so a person can see the press.
        button.doClick(0);
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
