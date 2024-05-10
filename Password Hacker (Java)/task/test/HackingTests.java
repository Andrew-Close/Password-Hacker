import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.TestCase;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

class PasswordGenerator implements Iterator<String> {
  private final String abc = "abcdefghijklmnopqrstuvwxyz1234567890";
  private int index = 1;
  private Iterator<String> currentIterator;

  @Override
  public boolean hasNext() {
    if (currentIterator == null || !currentIterator.hasNext()) {
      Stream<String> stream = Stream.of("");
      for (int i = 0; i < index; i++) {
        stream = stream.flatMap(s -> abc.chars().mapToObj(c -> s + (char) c));
      }
      currentIterator = stream.iterator();
      index++;
    }
    return true;
  }

  @Override
  public String next() {
    return currentIterator.next();
  }
}

public class HackingTests extends StageTest {

  boolean ready = false;
  ServerHack serverHack = null;
  Thread serverThread = null;
  String password = null;

  String randomPassword() {
    String abc = "abcdefghijklmnopqrstuvwxyz1234567890";
    Random ran = new Random();
    int length = ran.nextInt(2) + 2;
    String ret = "";
    for (int i = 0; i < length; i++) {
      ret = ret.concat(String.valueOf(abc.charAt(ran.nextInt(abc.length()))));
    }
    return ret;
  }

  void startServer() throws IOException {
    serverHack = new ServerHack(this);
    serverThread = new Thread(serverHack);
    serverThread.start();

    while (!ready) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {
      }
    }
  }

  void stopServer() throws InterruptedException {
    serverHack.disconnect();
    serverThread.join();
  }

  @Override
  public List<TestCase<String>> generate() {
    try {
      startServer();
    } catch (IOException ignored) {
    }
    password = randomPassword();
    return List.of(new TestCase<String>()
            .addArguments("localhost", "9090")
            .setAttach(password)
            .setTimeLimit(25000)
    );
  }

  public CheckResult check(String reply, Object attach) {
    try {
      stopServer();
    } catch (Exception ignored) {
    }
    if (serverHack == null || !serverHack.connected) {
      return CheckResult.wrong("You didn't connect to the server");
    }
    if (serverHack.message.size() == 0) {
      return CheckResult.wrong("You sent nothing to the server");
    }
    if (reply.length() == 0 || reply.split("\n").length == 0) {
      return CheckResult.wrong("You did not print anything");
    }
    String realPassword = attach.toString();
    if (!reply.split("\n")[0].equals(realPassword)) {
      return CheckResult.wrong("The password you printed is not correct. The password is \"" + realPassword + "\"");
    }

    boolean success = true;
    PasswordGenerator generator = new PasswordGenerator();
    String pass = "";
    while (generator.hasNext()) {
      pass = generator.next();
      if (pass.length() == realPassword.length()) {
        break;
      }
      if (!serverHack.message.remove(pass)) {
        success = false;
        break;
      }
    }

    if (success) {
      return CheckResult.correct();
    }
    return CheckResult.wrong("Your generator algorithm does not include all the variants: " + pass);
  }
}