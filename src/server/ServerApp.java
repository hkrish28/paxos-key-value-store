package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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

  private static List<PaxosServer> servers = new ArrayList<>();

  private static List<Server> stubs = new ArrayList<>();
  private static List<Integer> serverPorts = new ArrayList<>();

  /**
   * The main method starts the server by initializing and binding the RMI server implementation.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {


    if (args.length == SERVER_COUNT) {
      try {
        for (int i = 0; i < SERVER_COUNT; i++) {
          serverPorts.add(Integer.parseInt(args[i]));
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid port provided. Reverting to defaults...");
      }
    } else
      serverPorts = Arrays.asList(5000, 5001, 5002, 5003, 5004);

    serverSetup(serverPorts);
    ServerOperator serverOperator = new ServerOperator();
    serverOperator.start();
    try {
      serverOperator.join(); // Wait for ServerOperator to complete
    } catch (InterruptedException e) {
      System.out.println("Main thread interrupted while waiting for server operator to close.");
    }
    System.out.println("Main thread exiting");
    serverShutDown();
    System.exit(0);
    }

  private static void serverShutDown() {
    for (int i = 0; i < SERVER_COUNT; i++) {
      try {
        Registry registry = LocateRegistry.getRegistry(serverPorts.get(i));
        registry.unbind("Store");

      } catch (RemoteException | NotBoundException e) {
        System.out.println("Error during server shutdown, server port:" + serverPorts.get(i) + " Error:" + e.getMessage());
      }
    }
    System.out.println("All servers shut down successfully.");
  }

  private static void serverSetup(List<Integer> serverPorts){
      for (int i = 0; i < SERVER_COUNT; i++) {
        int retryCount = 1;
        int retryMax = 5;
        while (retryCount <= retryMax) {
          try {
            Registry registry = LocateRegistry.createRegistry(serverPorts.get(i));
            retryCount = retryMax + 2;
            PaxosServer obj = new PaxosServerImpl(serverPorts.get(i));
            Server stub = (Server) UnicastRemoteObject.exportObject(obj, 0);
            stubs.add(stub);
            servers.add(obj);
            // Bind the remote object's stub in the registry
            registry.rebind("Store", stub);
          } catch (RemoteException e) {
            System.out.println("Error creating server:" + i + " at port:" + serverPorts.get(i));
            System.out.println("Error:" + e.getMessage());
            serverPorts.set(i, getRandomNumber(5500, 6000));
            retryCount++;
          }
        }
        if (retryCount != retryMax + 2) {
          System.out.println("Exiting since server not set up.");
          System.exit(1);
        }
        for (PaxosServer server: servers){
          try {
            server.updateConnectedServers(servers);
          } catch (RemoteException e){
            System.out.println("Remote exception occurred while connecting servers");
          }

        }
      }
    }

  private static int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }
}
