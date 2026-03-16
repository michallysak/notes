package pl.michallysak.notes.application.cli.io;

import java.util.Scanner;

public class ConsoleIO implements IO<String> {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String readLine(String prompt) {
        print(prompt);
        return scanner.nextLine();
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        print(message + "\n");
    }
}
