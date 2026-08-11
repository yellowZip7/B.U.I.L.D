package utility;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputHelper {

    public static int getInt(Scanner sc) {

        while (true) {

            try {
                int value = Integer.parseInt(sc.nextLine());
                return value;

            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    public static double getDouble(Scanner sc) {

        while (true) {

            try {
                double value = Double.parseDouble(sc.nextLine());
                return value;

            } catch (NumberFormatException e) {
                System.out.println("Invalid decimal number. Try again.");
            }
        }
    }

    public static String getString(Scanner sc) {

        while (true) {

            String value = sc.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty. Try again.");
        }
    }
}