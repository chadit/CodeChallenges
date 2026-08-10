import java.util.Scanner;

/** Calculates the monthly grocery total and weekly average before and after a coupon is applied. */
public class GroceryBillCalculator {

  /**
   * Reads the coupon rate and weekly grocery bills, calculates the totals, and displays the
   * results.
   *
   * @param args command-line arguments, which aren't used
   */
  public static void main(String[] args) {

    final double defaultCouponRate = 0.10;
    final int numberOfWeeks = 4;

    Scanner input = new Scanner(System.in);

    double couponRate = readValue(input, "Enter the coupon amount as a decimal: ");
    couponRate = validateCouponRate(couponRate, defaultCouponRate);

    double weekOneBill = readValue(input, "Enter the grocery bill for week 1: $");
    double weekTwoBill = readValue(input, "Enter the grocery bill for week 2: $");
    double weekThreeBill = readValue(input, "Enter the grocery bill for week 3: $");
    double weekFourBill = readValue(input, "Enter the grocery bill for week 4: $");

    double monthlyTotal =
        calculateMonthlyTotal(weekOneBill, weekTwoBill, weekThreeBill, weekFourBill);
    double weeklyAverage = calculateWeeklyAverage(monthlyTotal, numberOfWeeks);
    double monthlyTotalWithCoupon = calculateTotalWithCoupon(monthlyTotal, couponRate);
    double weeklyAverageWithCoupon = calculateWeeklyAverage(monthlyTotalWithCoupon, numberOfWeeks);

    displayResults(monthlyTotal, weeklyAverage, monthlyTotalWithCoupon, weeklyAverageWithCoupon);

    input.close();
  }

  /**
   * Displays a prompt and reads one decimal value from the input.
   *
   * @param input source used to read the value
   * @param prompt message displayed before reading the value
   * @return value entered by the user
   */
  public static double readValue(Scanner input, String prompt) {
    System.out.print(prompt);
    return input.nextDouble();
  }

  /**
   * Checks whether a coupon rate is greater than zero and no greater than one. An invalid rate is
   * replaced with the default rate.
   *
   * @param couponRate coupon rate entered by the user
   * @param defaultCouponRate rate used when the entered rate is invalid
   * @return the entered coupon rate when valid, or the default rate otherwise
   */
  public static double validateCouponRate(double couponRate, double defaultCouponRate) {

    if (couponRate > 1.0 || couponRate <= 0.0) {
      System.out.println("Invalid coupon amount. The coupon was set to 10%.");
      return defaultCouponRate;
    }

    return couponRate;
  }

  /**
   * Adds the four weekly grocery bills.
   *
   * @param weekOneBill grocery bill for the first week
   * @param weekTwoBill grocery bill for the second week
   * @param weekThreeBill grocery bill for the third week
   * @param weekFourBill grocery bill for the fourth week
   * @return total grocery bill for the month
   */
  public static double calculateMonthlyTotal(
      double weekOneBill, double weekTwoBill, double weekThreeBill, double weekFourBill) {

    return weekOneBill + weekTwoBill + weekThreeBill + weekFourBill;
  }

  /**
   * Calculates the average grocery bill for one week.
   *
   * @param monthlyTotal total grocery bill for the month
   * @param numberOfWeeks number of weeks included in the total
   * @return average grocery bill for one week
   */
  public static double calculateWeeklyAverage(double monthlyTotal, int numberOfWeeks) {

    return monthlyTotal / numberOfWeeks;
  }

  /**
   * Subtracts the coupon savings from the monthly grocery total.
   *
   * @param monthlyTotal grocery total before the coupon is applied
   * @param couponRate coupon rate expressed as a decimal
   * @return monthly grocery total after the coupon is applied
   */
  public static double calculateTotalWithCoupon(double monthlyTotal, double couponRate) {

    double couponSavings = monthlyTotal * couponRate;
    return monthlyTotal - couponSavings;
  }

  /**
   * Displays the monthly totals and weekly averages before and after the coupon is applied.
   *
   * @param monthlyTotal monthly total before the coupon is applied
   * @param weeklyAverage weekly average before the coupon is applied
   * @param monthlyTotalWithCoupon monthly total after the coupon is applied
   * @param weeklyAverageWithCoupon weekly average after the coupon is applied
   */
  public static void displayResults(
      double monthlyTotal,
      double weeklyAverage,
      double monthlyTotalWithCoupon,
      double weeklyAverageWithCoupon) {

    System.out.println("\nGrocery Bill Summary");

    System.out.printf("Monthly total without coupon: $%.2f%n", monthlyTotal);
    System.out.printf("Weekly average without coupon: $%.2f%n", weeklyAverage);
    System.out.printf("Monthly total with coupon: $%.2f%n", monthlyTotalWithCoupon);
    System.out.printf("Weekly average with coupon: $%.2f%n", weeklyAverageWithCoupon);
  }
}
