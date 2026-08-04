import java.util.Scanner;

/*
 * Calculates the monthly grocery total and weekly average
 * before and after a coupon is applied.
 */
public class GroceryBillCalculator {

    public static void main(String[] args) {

        final double DEFAULT_COUPON_RATE = 0.10;
        final int NUMBER_OF_WEEKS = 4;

        Scanner input = new Scanner(System.in);

        System.out.print("Enter the coupon amount as a decimal: ");
        double couponRate = input.nextDouble();

        if (couponRate > 1.0 || couponRate <= 0.0) {
            couponRate = DEFAULT_COUPON_RATE;
            System.out.println(
                    "Invalid coupon amount. The coupon was set to 10%.");
        }

        System.out.print("Enter the grocery bill for week 1: $");
        double weekOneBill = input.nextDouble();

        System.out.print("Enter the grocery bill for week 2: $");
        double weekTwoBill = input.nextDouble();

        System.out.print("Enter the grocery bill for week 3: $");
        double weekThreeBill = input.nextDouble();

        System.out.print("Enter the grocery bill for week 4: $");
        double weekFourBill = input.nextDouble();

        double monthlyTotal = weekOneBill + weekTwoBill
                + weekThreeBill + weekFourBill;

        double weeklyAverage = monthlyTotal / NUMBER_OF_WEEKS;

        double couponSavings = monthlyTotal * couponRate;

        double monthlyTotalWithCoupon =
                monthlyTotal - couponSavings;

        double weeklyAverageWithCoupon =
                monthlyTotalWithCoupon / NUMBER_OF_WEEKS;

        System.out.println("\nGrocery Bill Summary");

        System.out.printf(
                "Monthly total without coupon: $%.2f%n",
                monthlyTotal);

        System.out.printf(
                "Weekly average without coupon: $%.2f%n",
                weeklyAverage);

        System.out.printf(
                "Monthly total with coupon: $%.2f%n",
                monthlyTotalWithCoupon);

        System.out.printf(
                "Weekly average with coupon: $%.2f%n",
                weeklyAverageWithCoupon);

        input.close();
    }
}