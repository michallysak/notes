package pl.michallysak.notes.application.cli.presenter;

import java.util.Scanner;

public class CliPresenter {
    private final Scanner scanner = new Scanner(System.in);

    public String prompt(String message) {
        show(message);
        return scanner.nextLine();
    }

    public void show(String message) {
        System.out.print(message);
    }

    public void showln(String message) {
        show(message + "\n");
    }

    public void showln() {
        show("\n");
    }
}

