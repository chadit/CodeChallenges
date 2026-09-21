import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The bank balance form: an amount field with Set Balance, Deposit, and Withdraw buttons, the
 * current balance in its own panel, and a status line that explains every refused amount.
 */
public final class BankBalancePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    // Package-private so the checks can type and click the way a user does.
    final JTextField amountField = new JTextField(12);
    final JButton setBalanceButton = new JButton("Set Balance");
    final JButton depositButton = new JButton("Deposit");
    final JButton withdrawButton = new JButton("Withdraw");
    final JLabel balanceLabel = new JLabel("Balance: not set");
    final JLabel statusLabel = new JLabel("Enter your current balance, then choose Set Balance.");

    /** Transient because JPanel is Serializable and BankAccount is not. */
    private final transient BankAccount account;

    /** @param account account the buttons act on, newly opened with a zero balance */
    public BankBalancePanel(BankAccount account) {
        super(new GridLayout(0, 1, 0, 8));
        this.account = account;

        JPanel amountRow = new JPanel();
        amountRow.add(new JLabel("Amount: $"));
        amountRow.add(amountField);

        JPanel buttonRow = new JPanel();
        for (JButton button : new JButton[] {setBalanceButton, depositButton, withdrawButton}) {
            button.addActionListener(this);
            buttonRow.add(button);
        }

        JPanel balancePanel = new JPanel();
        balancePanel.setBorder(BorderFactory.createTitledBorder(
                account.getFirstName() + " " + account.getLastName() + ", account "
                        + account.getAccountID()));
        balancePanel.add(balanceLabel);

        // There is no balance to move money against until the user supplies one.
        depositButton.setEnabled(false);
        withdrawButton.setEnabled(false);

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(amountRow);
        add(buttonRow);
        add(balancePanel);
        add(statusLabel);
    }

    /** A refused amount leaves the field as typed so the user can correct it. */
    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            apply(event.getSource(), parseAmount(amountField.getText()));
        } catch (IllegalArgumentException refused) {
            statusLabel.setText(refused.getMessage());
            return;
        }

        amountField.setText("");
        balanceLabel.setText("Balance: " + BankAccount.formatMoney(account.getBalance()));
    }

    private void apply(Object button, double amount) {
        if (button == setBalanceButton) {
            openWith(amount);
        } else if (button == depositButton) {
            account.deposit(amount);
            statusLabel.setText("Deposited " + BankAccount.formatMoney(amount) + ".");
        } else if (account.withdrawal(amount)) {
            statusLabel.setText("Withdrew " + BankAccount.formatMoney(amount) + ".");
        } else {
            throw new IllegalArgumentException(
                    "Insufficient funds to withdraw " + BankAccount.formatMoney(amount) + ".");
        }
    }

    /** An account opens at zero and rejects a zero deposit, so a zero start skips the deposit. */
    private void openWith(double startingBalance) {
        if (startingBalance < 0) {
            throw new IllegalArgumentException("A starting balance cannot be negative.");
        }

        if (startingBalance > 0) {
            account.deposit(startingBalance);
        }

        setBalanceButton.setEnabled(false);
        depositButton.setEnabled(true);
        withdrawButton.setEnabled(true);
        statusLabel.setText("Balance set. Deposit or withdraw any amount.");
    }

    /** Double.parseDouble accepts "NaN" and "Infinity", so parsed text must also be finite. */
    private static double parseAmount(String text) {
        double amount;
        try {
            amount = Double.parseDouble(text.trim());
        } catch (NumberFormatException notANumber) {
            amount = Double.NaN;
        }

        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Enter a dollar amount such as 125.50.");
        }

        return amount;
    }
}
