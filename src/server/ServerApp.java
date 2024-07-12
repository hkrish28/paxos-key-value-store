package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ServerApp is the entry point for starting the server.
 */
public class ServerApp {

  private static final int SERVER_COUNT = 5;

  /**
   * The main method starts the server by initializing and binding the RMI server implementation.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {

    List<Integer> serverPorts = new ArrayList<>();
    if (args.length == SERVER_COUNT) {
      try {
        for (int i = 0; i < SERVER_COUNT; i++) {
          serverPorts.add(Integer.parseInt(args[i]));
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid port provided. Reverting to defaults...");
      }
    } else
      serverPorts = Arrays.asList(5000, 5001, 5002, 5003, 5004, 5005);

    Coordinator coordinator = new CoordinatorImpl(serverPorts);
    new ServerOperator(coordinator).start();
    }

}
