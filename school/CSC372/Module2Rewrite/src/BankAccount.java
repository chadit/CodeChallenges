import java.util.Objects;

/**
 * A customer's bank account: an owner's name, an account ID, and a balance that deposits raise and
 * withdrawals lower. A plain account never overdraws; {@link #withdrawal} refuses any amount the
 * balance cannot cover.
 *
 * <p>Designed for extension by account types with their own withdrawal policy. A subclass changes
 * the money it moves through {@link #debit}, which skips the funds check, and keeps the balance
 * itself private so every change still passes through amount validation.
 */
public class BankAccount {

    private String firstName;
    private String lastName;
    private int accountID;
    private double balance;

    /**
     * Opens an account with a zero balance, as the assignment requires; money arrives through
     * {@link #deposit}.
     *
     * @param firstName owner's first name, not blank
     * @param lastName owner's last name, not blank
     * @param accountID number that identifies the account
     */
    public BankAccount(String firstName, String lastName, int accountID) {
        setFirstName(firstName);
        setLastName(lastName);
        setAccountID(accountID);
        balance = 0.0;
    }

    /**
     * Adds an amount to the balance.
     *
     * @param amount dollars to add, greater than zero
     * @throws IllegalArgumentException when the amount is zero, negative, or not a number
     */
    public void deposit(double amount) {
        requirePositiveAmount(amount);
        balance += amount;
    }

    /**
     * Subtracts an amount from the balance when the balance covers it. A refused withdrawal is an
     * everyday outcome for the caller, so it comes back as a return value instead of an exception.
     *
     * @param amount dollars to remove, greater than zero
     * @return true when the balance was reduced, false when the amount exceeded the balance
     * @throws IllegalArgumentException when the amount is zero, negative, or not a number
     */
    public boolean withdrawal(double amount) {
        requirePositiveAmount(amount);
        if (amount > balance) {
            return false;
        }

        debit(amount);
        return true;
    }

    /**
     * Subtracts an amount without checking that the balance covers it. Subclasses that permit
     * overdrafts use this for the withdrawal and the fee.
     *
     * @param amount dollars to remove, greater than zero
     * @throws IllegalArgumentException when the amount is zero, negative, or not a number
     */
    protected final void debit(double amount) {
        requirePositiveAmount(amount);
        balance -= amount;
    }

    /**
     * Prints every attribute of the account, one per line, to standard output.
     */
    public void accountSummary() {
        System.out.println("Account ID: " + accountID);
        System.out.println("First name: " + firstName);
        System.out.println("Last name: " + lastName);
        System.out.println("Balance: " + formatMoney(balance));
    }

    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName owner's first name, not blank
     * @throws IllegalArgumentException when the name is blank
     */
    public void setFirstName(String firstName) {
        this.firstName = requireText(firstName, "first name");
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName owner's last name, not blank
     * @throws IllegalArgumentException when the name is blank
     */
    public void setLastName(String lastName) {
        this.lastName = requireText(lastName, "last name");
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Formats dollars the way a statement does, with the sign ahead of the currency symbol so an
     * overdrawn balance reads as -$30.00 rather than $-30.00.
     *
     * @param dollars amount to format
     * @return the amount with a currency symbol, thousands separators, and two decimals
     */
    protected static String formatMoney(double dollars) {
        String sign = dollars < 0 ? "-" : "";
        return String.format("%s$%,.2f", sign, Math.abs(dollars));
    }

    /**
     * Rejects amounts that would make a deposit or withdrawal meaningless. NaN fails both
     * comparisons below silently, so it gets its own check.
     */
    private static void requirePositiveAmount(double amount) {
        if (Double.isNaN(amount) || amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero, got " + amount);
        }
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label);
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }

        return value;
    }
}
