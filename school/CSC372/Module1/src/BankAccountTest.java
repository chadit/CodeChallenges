/**
 * Exercises both account types and prints what happens at each step: a plain account that refuses
 * an overdraft, then a checking account that permits one and charges the fee.
 */
public final class BankAccountTest {

    private BankAccountTest() {}

    /**
     * Runs the demonstration.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        System.out.println("== Bank account ==");
        BankAccount savings = new BankAccount("Ada", "Lovelace", 1001);
        savings.deposit(500.00);
        System.out.println("Deposited $500.00");

        reportWithdrawal(savings, 120.50);
        reportWithdrawal(savings, 1000.00);
        savings.accountSummary();

        System.out.println();
        System.out.println("== Checking account ==");
        CheckingAccount checking = new CheckingAccount("Grace", "Hopper", 2002, 1.25);
        checking.deposit(200.00);
        System.out.println("Deposited $200.00");

        checking.processWithdrawal(50.00);
        checking.processWithdrawal(175.00);
        checking.displayAccount();
    }

    /** Prints whether a plain account accepted a withdrawal, since withdrawal itself is silent. */
    private static void reportWithdrawal(BankAccount account, double amount) {
        if (account.withdrawal(amount)) {
            System.out.println("Withdrew $" + String.format("%,.2f", amount));
            return;
        }

        System.out.println("Refused withdrawal of $" + String.format("%,.2f", amount)
                + ": insufficient funds");
    }
}
