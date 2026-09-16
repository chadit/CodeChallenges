import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Checks BankAccount and CheckingAccount without a test framework, since the coursework build is
 * plain javac. Run with "make test"; a non-zero exit code means at least one check failed.
 */
public final class BankAccountChecks {

    /** Number of failed checks, used for the exit code. */
    private static int failures = 0;

    /** Number of check methods that ended early by throwing, also used for the exit code. */
    private static int aborted = 0;

    /**
     * A check method. It may throw: the runner contains a throw so one bad check cannot cancel the
     * ones after it.
     */
    @FunctionalInterface
    private interface Check {
        void run() throws Exception;
    }

    /** A check paired with the name the report uses when it fails or throws. */
    private record NamedCheck(String name, Check body) {}

    private BankAccountChecks() {}

    /** Runs every check and exits non-zero when any fail or throw. */
    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "new account stores the owner and starts at zero",
                        BankAccountChecks::newAccountStoresTheOwnerAndStartsAtZero),
                new NamedCheck(
                        "deposit adds to the balance",
                        BankAccountChecks::depositAddsToTheBalance),
                new NamedCheck(
                        "deposit rejects zero, negative, and NaN amounts",
                        BankAccountChecks::depositRejectsNonPositiveAmounts),
                new NamedCheck(
                        "withdrawal subtracts when the balance covers it",
                        BankAccountChecks::withdrawalSubtractsWhenTheBalanceCoversIt),
                new NamedCheck(
                        "withdrawal of the whole balance leaves zero",
                        BankAccountChecks::withdrawalOfTheWholeBalanceLeavesZero),
                new NamedCheck(
                        "withdrawal refuses an overdraft and keeps the balance",
                        BankAccountChecks::withdrawalRefusesAnOverdraftAndKeepsTheBalance),
                new NamedCheck(
                        "withdrawal rejects zero, negative, and NaN amounts",
                        BankAccountChecks::withdrawalRejectsNonPositiveAmounts),
                new NamedCheck(
                        "setters replace the owner and account ID",
                        BankAccountChecks::settersReplaceTheOwnerAndAccountID),
                new NamedCheck(
                        "name setters reject null and blank text",
                        BankAccountChecks::nameSettersRejectNullAndBlankText),
                new NamedCheck(
                        "accountSummary prints every attribute",
                        BankAccountChecks::accountSummaryPrintsEveryAttribute),
                new NamedCheck(
                        "checking constructor rejects a negative interest rate",
                        BankAccountChecks::checkingConstructorRejectsNegativeInterestRate),
                new NamedCheck(
                        "processWithdrawal within funds charges no fee",
                        BankAccountChecks::processWithdrawalWithinFundsChargesNoFee),
                new NamedCheck(
                        "processWithdrawal overdraft charges the fee and says so",
                        BankAccountChecks::processWithdrawalOverdraftChargesTheFeeAndSaysSo),
                new NamedCheck(
                        "checking withdrawal through the superclass type still overdraws",
                        BankAccountChecks::checkingWithdrawalThroughSuperclassTypeStillOverdraws),
                new NamedCheck(
                        "displayAccount prints the summary and the interest rate",
                        BankAccountChecks::displayAccountPrintsTheSummaryAndTheInterestRate),
                new NamedCheck(
                        "demo main prints both accounts",
                        BankAccountChecks::demoMainPrintsBothAccounts));

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

    /**
     * Runs one check and keeps a throw from ending the run. Without this a single unexpected
     * exception cancels every later check and the tally never prints.
     *
     * @param check check to run
     */
    private static void runContained(NamedCheck check) {
        try {
            check.body().run();
        } catch (AssertionError | Exception thrown) {
            // Error other than AssertionError is deliberately not caught: a JVM that is already
            // unwell should stop, not keep reporting checks.
            aborted++;
            String report = "ABORTED: " + check.name() + " threw " + thrown;
            System.out.println(report);
            System.err.println(report);
        }
    }

    private static void newAccountStoresTheOwnerAndStartsAtZero() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);

        checkEquals("Ada", account.getFirstName(), "first name from the constructor");
        checkEquals("Lovelace", account.getLastName(), "last name from the constructor");
        checkEquals(1001, account.getAccountID(), "account ID from the constructor");
        checkEquals(0.0, account.getBalance(), "balance starts at zero");
    }

    private static void depositAddsToTheBalance() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);

        account.deposit(100.25);
        account.deposit(0.75);

        checkEquals(101.0, account.getBalance(), "two deposits add up");
    }

    private static void depositRejectsNonPositiveAmounts() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);
        account.deposit(50.0);

        checkRejected(() -> account.deposit(0.0), "deposit of zero");
        checkRejected(() -> account.deposit(-1.0), "negative deposit");
        checkRejected(() -> account.deposit(Double.NaN), "NaN deposit");
        checkEquals(50.0, account.getBalance(), "rejected deposits leave the balance alone");
    }

    private static void withdrawalSubtractsWhenTheBalanceCoversIt() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);
        account.deposit(100.0);

        boolean applied = account.withdrawal(40.5);

        checkEquals(true, applied, "covered withdrawal reports success");
        checkEquals(59.5, account.getBalance(), "covered withdrawal lowers the balance");
    }

    private static void withdrawalOfTheWholeBalanceLeavesZero() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);
        account.deposit(100.0);

        boolean applied = account.withdrawal(100.0);

        checkEquals(true, applied, "withdrawing the exact balance is allowed");
        checkEquals(0.0, account.getBalance(), "balance is zero after withdrawing it all");
    }

    private static void withdrawalRefusesAnOverdraftAndKeepsTheBalance() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);
        account.deposit(100.0);

        boolean applied = account.withdrawal(100.01);

        checkEquals(false, applied, "overdraft on a plain account is refused");
        checkEquals(100.0, account.getBalance(), "refused withdrawal leaves the balance alone");
    }

    private static void withdrawalRejectsNonPositiveAmounts() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);
        account.deposit(50.0);

        checkRejected(() -> account.withdrawal(0.0), "withdrawal of zero");
        checkRejected(() -> account.withdrawal(-1.0), "negative withdrawal");
        checkRejected(() -> account.withdrawal(Double.NaN), "NaN withdrawal");
        checkEquals(50.0, account.getBalance(), "rejected withdrawals leave the balance alone");
    }

    private static void settersReplaceTheOwnerAndAccountID() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);

        account.setFirstName("Augusta");
        account.setLastName("King");
        account.setAccountID(1002);

        checkEquals("Augusta", account.getFirstName(), "setFirstName replaces the first name");
        checkEquals("King", account.getLastName(), "setLastName replaces the last name");
        checkEquals(1002, account.getAccountID(), "setAccountID replaces the account ID");
    }

    private static void nameSettersRejectNullAndBlankText() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);

        checkRejected(() -> account.setFirstName(null), "null first name");
        checkRejected(() -> account.setFirstName("   "), "blank first name");
        checkRejected(() -> account.setLastName(null), "null last name");
        checkRejected(() -> account.setLastName(""), "empty last name");
        checkRejected(() -> new BankAccount("", "Lovelace", 1001), "constructor with blank name");
        checkEquals("Ada", account.getFirstName(), "rejected first name leaves the old one");
        checkEquals("Lovelace", account.getLastName(), "rejected last name leaves the old one");
    }

    private static void accountSummaryPrintsEveryAttribute() {
        BankAccount account = new BankAccount("Ada", "Lovelace", 1001);
        account.deposit(1234.5);

        String printed = captureOutput(account::accountSummary);

        checkEquals(
                lines("Account ID: 1001", "First name: Ada", "Last name: Lovelace",
                        "Balance: $1,234.50"),
                printed,
                "accountSummary lists ID, names, and balance");
    }

    private static void checkingConstructorRejectsNegativeInterestRate() {
        checkRejected(
                () -> new CheckingAccount("Grace", "Hopper", 2002, -0.01),
                "negative interest rate");
        checkRejected(
                () -> new CheckingAccount("Grace", "Hopper", 2002, Double.NaN),
                "NaN interest rate");
        checkEquals(
                0.0,
                new CheckingAccount("Grace", "Hopper", 2002, 0.0).getInterestRate(),
                "zero interest rate is allowed");
    }

    private static void processWithdrawalWithinFundsChargesNoFee() {
        CheckingAccount account = new CheckingAccount("Grace", "Hopper", 2002, 1.25);
        account.deposit(100.0);

        String printed = captureOutput(() -> account.processWithdrawal(100.0));

        checkEquals(0.0, account.getBalance(), "withdrawing the whole balance charges no fee");
        checkEquals(lines("Withdrew $100.00. Balance: $0.00"), printed,
                "covered withdrawal prints the new balance without a fee notice");
    }

    private static void processWithdrawalOverdraftChargesTheFeeAndSaysSo() {
        CheckingAccount account = new CheckingAccount("Grace", "Hopper", 2002, 1.25);
        account.deposit(100.0);

        String printed = captureOutput(() -> account.processWithdrawal(120.0));

        checkEquals(-50.0, account.getBalance(), "overdraft subtracts the amount and the $30 fee");
        checkEquals(
                lines("Withdrew $120.00. Balance: -$50.00 (a $30.00 overdraft fee has been"
                        + " assessed)"),
                printed,
                "overdraft prints the negative balance and the fee notice");
    }

    private static void checkingWithdrawalThroughSuperclassTypeStillOverdraws() {
        BankAccount account = new CheckingAccount("Grace", "Hopper", 2002, 1.25);
        account.deposit(10.0);

        boolean applied = captureResult(() -> account.withdrawal(25.0));

        checkEquals(true, applied, "checking withdrawal via BankAccount reference succeeds");
        checkEquals(-45.0, account.getBalance(), "overdraft policy applies through the superclass");
    }

    private static void displayAccountPrintsTheSummaryAndTheInterestRate() {
        CheckingAccount account = new CheckingAccount("Grace", "Hopper", 2002, 1.25);
        account.deposit(20.0);
        captureOutput(() -> account.processWithdrawal(50.0));

        String printed = captureOutput(account::displayAccount);

        checkEquals(
                lines("Account ID: 2002", "First name: Grace", "Last name: Hopper",
                        "Balance: -$60.00", "Interest rate: 1.25%"),
                printed,
                "displayAccount adds the interest rate after the superclass summary");
    }

    private static void demoMainPrintsBothAccounts() {
        String printed = captureOutput(() -> BankAccountTest.main(new String[0]));

        checkContains(printed, "Refused withdrawal of $1,000.00: insufficient funds",
                "demo shows the plain account refusing an overdraft");
        checkContains(printed, "Balance: $379.50", "demo shows the plain account's final balance");
        checkContains(printed, "Balance: -$55.00 (a $30.00 overdraft fee has been assessed)",
                "demo shows the checking account overdraft with the fee");
        checkContains(printed, "Interest rate: 1.25%", "demo shows the interest rate");
    }

    /** Joins lines with the platform separator, matching what println produces. */
    private static String lines(String... text) {
        return String.join(System.lineSeparator(), text) + System.lineSeparator();
    }

    /** Runs an action with System.out redirected and returns what it printed. */
    private static String captureOutput(Runnable action) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }

    /** Like captureOutput but for a call whose return value the check needs; output is dropped. */
    private static boolean captureResult(java.util.function.BooleanSupplier action) {
        boolean[] result = new boolean[1];
        captureOutput(() -> result[0] = action.getAsBoolean());
        return result[0];
    }

    /**
     * Passes when the action refuses its input. A null name is refused with NullPointerException
     * from Objects.requireNonNull; every other bad value gets IllegalArgumentException.
     */
    private static void checkRejected(Runnable action, String description) {
        try {
            action.run();
        } catch (IllegalArgumentException | NullPointerException rejected) {
            check(true, description + " is rejected");
            return;
        }

        check(false, description + " is rejected");
    }

    private static void checkContains(String printed, String expected, String description) {
        check(printed.contains(expected),
                description + " (missing \"" + expected + "\" in:\n" + printed + ")");
    }

    private static void checkEquals(Object expected, Object actual, String description) {
        check(expected.equals(actual),
                description + " (expected \"" + expected + "\", got \"" + actual + "\")");
    }

    /** Prints PASS or FAIL and counts failures for the exit code. */
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
