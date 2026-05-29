package pl.michallysak.notes.application.cli.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleIOTest {
  private final PrintStream standardOut = System.out;
  private final InputStream standardIn = System.in;
  private ByteArrayOutputStream outputStreamCaptor;

  @BeforeEach
  void setUp() {
    outputStreamCaptor = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @AfterEach
  void tearDown() {
    System.setOut(standardOut);
    System.setIn(standardIn);
  }

  @Test
  void print_shouldPrintMessage() {
    // given
    ConsoleIO consoleIO = new ConsoleIO();
    String message = "Test message";
    // when
    consoleIO.print(message);
    // then
    assertEquals(message, outputStreamCaptor.toString());
  }

  @Test
  void println_shouldPrintMessageWithNewline() {
    // given
    ConsoleIO consoleIO = new ConsoleIO();
    String message = "Test message";
    // when
    consoleIO.println(message);
    // then
    assertEquals(message + "\n", outputStreamCaptor.toString());
  }

  @Test
  void readLine_shouldPrintPromptAndReturnInput() {
    // given
    String input = "test input\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    ConsoleIO consoleIO = new ConsoleIO();
    String prompt = "Enter something: ";
    // when
    String result = consoleIO.readLine(prompt);
    // then
    assertEquals("test input", result);
    assertTrue(outputStreamCaptor.toString().contains(prompt));
  }

  @Test
  void readLine_shouldHandleEmptyInput() {
    // given
    String input = "\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    ConsoleIO consoleIO = new ConsoleIO();
    String prompt = "Prompt: ";
    // when
    String result = consoleIO.readLine(prompt);
    // then
    assertEquals("", result);
  }
}
