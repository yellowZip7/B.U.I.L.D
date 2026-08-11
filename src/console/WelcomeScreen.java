package console;

import java.util.Scanner;

import utility.InputHelper;

public class WelcomeScreen {
    public static int index = 0;
    public Scanner sc = new Scanner(System.in);
    public int ch;
    public void welScreen() {

        System.out.println("\n╔════[WELCOME TO B.U.I.L.D.!]═════╗");
        System.out.println("║ 1. Sign Up                      ║");
        System.out.println("║ 2. Sign In                      ║");
        System.out.println("║ 3. Exit                         ║");
        System.out.println("╚═════════════════════════════════╝");
        System.out.print("Choose an option: ");
        ch = InputHelper.getInt(sc);
        System.out.println();
    }}