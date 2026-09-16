/**
 * A bank account that earns interest and permits overdrafts. A withdrawal the balance cannot
 * cover still goes through, the balance goes negative, and the account charges a fixed fee.
 */
public final class CheckingAccount extends BankAccount {

    /** Charged once per withdrawal that leaves the balance below zero. */
    public static final double OVERDRAFT_FEE = 30.0;

    private final double interestRate;

    /**
     * Opens a checking account with a zero balance.
     *
     * @param firstName owner's first name, not blank
     * @param lastName owner's last name, not blank
     * @param accountID number that identifies the account
     * @param interestRate annual rate as a percentage, zero or more
     * @throws IllegalArgumentException when the rate is negative or not a number
     */
    public CheckingAccount(String firstName, String lastName, int accountID, double interestRate) {
        super(firstName, lastName, accountID);
        if (Double.isNaN(interestRate) || interestRate < 0) {
            throw new IllegalArgumentException(
                    "interest rate must be zero or more, got " + interestRate);
        }

        this.interestRate = interestRate;
    }

    /**
     * Withdraws an amount even when the balance cannot cover it. An overdraft charges the
     * {@link #OVERDRAFT_FEE} on top of the amount, and the printed result says so.
     *
     * @param amount dollars to remove, greater than zero
     * @throws IllegalArgumentException when the amount is zero, negative, or not a number
     */
    public void processWithdrawal(double amount) {
        debit(amount);
        if (getBalance() >= 0) {
            System.out.println("Withdrew " + formatMoney(amount) + ". Balance: "
                    + formatMoney(getBalance()));
            return;
        }

        debit(OVERDRAFT_FEE);
        System.out.println("Withdrew " + formatMoney(amount) + ". Balance: "
                + formatMoney(getBalance()) + " (a " + formatMoney(OVERDRAFT_FEE)
                + " overdraft fee has been assessed)");
    }

    /**
     * Routes a withdrawal through the overdraft policy, so code holding a BankAccount reference
     * gets checking-account behavior instead of the refusal the superclass gives.
     *
     * @param amount dollars to remove, greater than zero
     * @return always true, since an overdraft is permitted
     * @throws IllegalArgumentException when the amount is zero, negative, or not a number
     */
    @Override
    public boolean withdrawal(double amount) {
        processWithdrawal(amount);
        return true;
    }

    /**
     * Prints the superclass summary followed by the interest rate.
     */
    public void displayAccount() {
        accountSummary();
        System.out.println(String.format("Interest rate: %.2f%%", interestRate));
    }

    public double getInterestRate() {
        return interestRate;
    }
}
