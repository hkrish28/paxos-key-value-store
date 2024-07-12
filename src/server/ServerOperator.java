package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerOperator extends Thread {

  private Coordinator coordinator;

  public ServerOperator(Coordinator coordinator) {
    this.coordinator = coordinator;
  }

  public void run() {
    String input = "";
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    while (!input.equalsIgnoreCase("exit")) {
      System.out.println("The server will close when user inputs \"exit\"");
      try {
        input = in.readLine();
      } catch (IOException e) {
        System.out.println("Server Operator Error:" + e.getMessage());
      }
    }
    coordinator.shutdown();
  }
}
