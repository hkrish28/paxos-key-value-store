package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.Server;

/**
 * ClientApp is the entry point for the client application.
 * It connects to the remote server and sends requests based on input.
 */
public class ClientApp {

  /**
   * The main method initializes the client application, connects to the remote server,
   * and sends requests from both a file and the standard input.
   *
   * @param args command-line arguments specifying the host of the RMI registry
   */
  public static void main(String[] args) {

    String host = (args.length < 1) ? null : args[0];
    Registry registry = null;
    try {
      registry = LocateRegistry.getRegistry(host);
      Server stub = (Server) registry.lookup("Store");
      Client client = new ClientImpl(stub);
      InputStream in = null;
      try {
        in = new FileInputStream("ClientInput.txt");
        client.sendRequests(in);
      } catch (FileNotFoundException e) {
        System.out.println("Error encountered during initial file read. " + e.getMessage());
      }
      in = System.in;
      client.sendRequests(in);

    } catch (RemoteException | NotBoundException e) {
      System.out.println("Error encountered while fetching remote object from registry. " + e.getMessage());
    }
  }
}
