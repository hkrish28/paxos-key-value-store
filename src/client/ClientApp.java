package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    Integer port = (args.length < 1) ? 5000 : Integer.parseInt(args[0]);
    Registry registry = null;
    boolean proceed = false;
    int retry = 1;
    do {
      if (proceed){
        port = readPort();
      }
      try {
        registry = LocateRegistry.getRegistry(port);
        Server stub = (Server) registry.lookup("Store");
        Client client = new ClientImpl(stub);
        InputStream in = null;
        System.out.println("Connected to server in port:" + port);
        try {
          in = new FileInputStream("ClientInput.txt");
          client.sendRequests(in);
        } catch (FileNotFoundException e) {
          System.out.println("Error encountered during initial file read. " + e.getMessage());
          retry++;
        }
        retry = 1;
        in = System.in;
        proceed = client.sendRequests(in);
      } catch (RemoteException | NotBoundException e) {
        proceed = true;
        System.out.println("Error encountered while fetching remote object from registry. " + e.getMessage());
        retry++;
      }
    } while (proceed && retry <= 3);

    if (retry > 3) {
      System.out.println("Retry limit exceeded. Exiting app");
    }
  }

  private static int readPort(){
    try {
      System.out.println("Enter the port number of the server to connect to");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      return Integer.parseInt(in.readLine());
    } catch (NumberFormatException | IOException e){
      System.out.println("Port number invalid. Trying to connect to default port 5000...");
      return 5000;
    }

  }
}
