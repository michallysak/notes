package pl.michallysak.notes.application.cli.io;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class TestTextIO implements IO<String> {
  private final Queue<String> inputs;
  private final StringBuilder outputs;

  public static TestTextIO create(String... inputs) {
    Queue<String> inputQueue =
        Arrays.stream(inputs).collect(Collectors.toCollection(LinkedList::new));

    return new TestTextIO(inputQueue);
  }

  private TestTextIO(Queue<String> inputs) {
    this.inputs = inputs;
    this.outputs = new StringBuilder();
  }

  @Override
  public String toString() {
    return outputs.toString();
  }

  @Override
  public String readLine(String prompt) {
    String poll = inputs.poll();
    outputs.append(prompt).append(poll).append("\n");
    return poll;
  }

  @Override
  public void print(String message) {
    outputs.append(message);
  }

  @Override
  public void println(String message) {
    print(message + "\n");
  }

  public void addInputs(List<String> moreInputs) {
    this.inputs.addAll(moreInputs);
  }
}
