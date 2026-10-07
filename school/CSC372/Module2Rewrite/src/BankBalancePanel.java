import java.awt.GridLayout;
import java.util.Objects;
import java.util.OptionalDouble;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The bank balance form: an amount field with Set Balance, Deposit, and Withdraw buttons, the
 * current balance in its own panel, and a status line that explains every refused amount.
 *
 * <p>Each button has its own handler method, and every handler ends in either {@code accept} or
 * {@code refuse}, which also color the status line green or red. A refusal is an everyday
 * outcome here, so it is a return path with a message written for the user rather than a
 * thrown exception.
 */
public final class BankBalancePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String AMOUNT_HINT = "Enter a dollar amount such as 125.50.";

    // Package-private so the checks can type and click the way a user does.
    final JTextField amountField = new JTextField(12);
    final JButton setBalanceButton = new JButton("Set Balance");
    final JButton depositButton = new JButton("Deposit");
    final JButton withdrawButton = new JButton("Withdraw");
    final JLabel balanceLabel = new JLabel("Balance: not set");
    final JLabel statusLabel = new JLabel("Enter your current balance, then choose Set Balance.");

    /** Transient because JPanel is Serializable and BankAccount is not. */
    private final transient BankAccount account;

    /**
     * Builds the form with only Set Balance enabled.
     *
     * @param account account the buttons act on, newly opened with a zero balance
     */
    public BankBalancePanel(BankAccount account) {
        super(new GridLayout(0, 1, 0, 8));
        this.account = Objects.requireNonNull(account, "account");

        setBalanceButton.addActionListener(event -> readAmount().ifPresent(this::setBalance));
        depositButton.addActionListener(event -> readAmount().ifPresent(this::deposit));
        withdrawButton.addActionListener(event -> readAmount().ifPresent(this::withdraw));

        // There is no balance to move money against until the user supplies one.
        depositButton.setEnabled(false);
        withdrawButton.setEnabled(false);

        JPanel balancePanel = row(balanceLabel);
        balancePanel.setBorder(BorderFactory.createTitledBorder(
                account.getFirstName() + " " + account.getLastName() + ", account "
                        + account.getAccountID()));

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(row(new JLabel("Amount: $"), amountField));
        add(row(setBalanceButton, depositButton, withdrawButton));
        add(balancePanel);
        add(statusLabel);
    }

    /** An account opens at zero and rejects a zero deposit, so a zero start skips the deposit. */
    private void setBalance(double startingBalance) {
        if (startingBalance < 0) {
            refuse("A starting balance cannot be negative.");
            return;
        }

        if (startingBalance > 0) {
            account.deposit(startingBalance);
        }

        setBalanceButton.setEnabled(false);
        depositButton.setEnabled(true);
        withdrawButton.setEnabled(true);
        accept("Balance set. Deposit or withdraw any amount.");
    }

    /**
     * The account refuses a non-positive amount too, but by throwing. Checking here first keeps
     * that exception off the screen and gives the user a plain sentence instead.
     */
    private void deposit(double amount) {
        if (amount <= 0) {
            refuse("A deposit must be greater than zero.");
            return;
        }

        account.deposit(amount);
        accept("Deposited " + BankAccount.formatMoney(amount) + ".");
    }

    private void withdraw(double amount) {
        if (amount <= 0) {
            refuse("A withdrawal must be greater than zero.");
            return;
        }

        if (!account.withdrawal(amount)) {
            refuse("Insufficient funds to withdraw " + BankAccount.formatMoney(amount) + ".");
            return;
        }

        accept("Withdrew " + BankAccount.formatMoney(amount) + ".");
    }

    /** Clears the field after an accepted amount so a second click cannot apply it twice. */
    private void accept(String message) {
        statusLabel.setForeground(MochaTheme.GREEN);
        statusLabel.setText(message);
        amountField.setText("");
        balanceLabel.setText("Balance: " + BankAccount.formatMoney(account.getBalance()));
    }

    /** Reports a refused amount and leaves it in the field so the user can correct it. */
    private void refuse(String message) {
        statusLabel.setForeground(MochaTheme.RED);
        statusLabel.setText(message);
    }

    /** Reads the amount field, refusing text that is not a finite number. */
    private OptionalDouble readAmount() {
        OptionalDouble amount = parseAmount(amountField.getText());
        if (amount.isEmpty()) {
            refuse(AMOUNT_HINT);
        }

        return amount;
    }

    /** Double.parseDouble accepts "NaN" and "Infinity", so parsed text must also be finite. */
    private static OptionalDouble parseAmount(String text) {
        double amount;
        try {
            amount = Double.parseDouble(text.trim());
        } catch (NumberFormatException notANumber) {
            return OptionalDouble.empty();
        }

        return Double.isFinite(amount) ? OptionalDouble.of(amount) : OptionalDouble.empty();
    }

    /** A flow-layout panel, which centers its parts on one line. */
    private static JPanel row(JComponent... parts) {
        JPanel panel = new JPanel();
        for (JComponent part : parts) {
            panel.add(part);
        }

        return panel;
    }
}
