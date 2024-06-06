package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * ServerApp is the entry point for starting the server.
 */
public class ServerApp {

  /**
   * The main method starts the server by initializing and binding the RMI server implementation.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    try {

      Server obj = new ServerImpl();
      Server stub = (Server) UnicastRemoteObject.exportObject(obj, 0);
      // Bind the remote object's stub in the registry
      Registry registry = LocateRegistry.getRegistry();
      registry.rebind("Store", stub);

      System.err.println("Server ready");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.getMessage());
      e.printStackTrace();

    }
  }
}
