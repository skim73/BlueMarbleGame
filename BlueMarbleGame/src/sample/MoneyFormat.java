package sample;

/**
 * Just a utility class containing a method that formats a Player's money amount into "$#.##M"
 */
public class MoneyFormat {
    public static String format(double amount) {
        if (amount <= -1.000)
            return String.format("-$%.3fM", -amount);
        else if (amount < 0)
            return String.format("-$%.0fK", -amount * 1000);
        else if (amount < 1.000)
            return String.format("$%.0fK", amount * 1000);
        return String.format("$%.3fM", amount);
    }
}