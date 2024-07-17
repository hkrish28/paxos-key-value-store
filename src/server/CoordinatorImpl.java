package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import shared.Logger;

/**
 * CoordinatorImpl is a concrete implementation of the Coordinator interface.
 * It manages the coordination of transactions across multiple servers using the
 * two-phase commit protocol.
 */
public class CoordinatorImpl implements Coordinator {

  private static Logger logger = new Logger(System.out);
  private int serverCount;
  private List<CoordinatedServer> servers = new ArrayList<>();
  private List<Server> stubs = new ArrayList<>();

  private List<Integer> serverPorts;

  /**
   * Constructs a CoordinatorImpl with the specified server ports.
   * It initializes the servers and binds their stubs in the RMI registry.
   *
   * @param serverPorts the list of server ports.
   * @throws IllegalStateException if any server fails to set up after the maximum retries.
   */
  public CoordinatorImpl(List<Integer> serverPorts) throws IllegalStateException {
    serverCount = serverPorts.size();
    for (int i = 0; i < serverCount; i++) {
      int retryCount = 1;
      int retryMax = 5;
      while (retryCount <= retryMax) {
        try {
          Registry registry = LocateRegistry.createRegistry(serverPorts.get(i));
          retryCount = retryMax + 2;
          CoordinatedServer obj = new ServerImpl(this, serverPorts.get(i));
          Server stub = (Server) UnicastRemoteObject.exportObject(obj, 0);
          stubs.add(stub);
          servers.add(obj);
          // Bind the remote object's stub in the registry
          registry.rebind("Store", stub);
        } catch (RemoteException e) {
          log("Error creating server:" + i + " at port:" + serverPorts.get(i));
          log("Error:" + e.getMessage());
          serverPorts.set(i, getRandomNumber(5500, 6000));
          retryCount++;
        }
      }
      if (retryCount != retryMax + 2) {
        log("Exiting since server not set up.");
        throw new IllegalStateException("Server set up failed");
      }
    }
    this.serverPorts = serverPorts;
  }


  /**
   * Updates the value associated with the specified key across all coordinated servers.
   * This method initiates a two-phase commit process to ensure consistency.
   *
   * @param key the key to be updated.
   * @param value the new value to be associated with the key.
   * @return true if the update was successful across all servers, false otherwise.
   */
  @Override
  public boolean update(String key, String value) {
    Function<CoordinatedServer, Boolean> phaseOneFunction =
            server -> {
              try {
                return server.canCommit(key, store -> store.put(key, value));
              } catch (RemoteException e) {
                return false;
              }
            };
    //TODO1
    return proceed2PC(key, phaseOneFunction);
  }

  /**
   * Deletes the key and its associated value across all coordinated servers.
   * This method initiates a two-phase commit process to ensure consistency.
   *
   * @param key the key to be deleted.
   * @return true if the delete operation was successful across all servers, false otherwise.
   */
  @Override
  public boolean delete(String key) {
    Function<CoordinatedServer, Boolean> phaseOneFunction =
            server -> {
              try {
                return server.canCommit(key, store -> store.remove(key));
              } catch (RemoteException e) {
                return false;
              }
            };


    return proceed2PC(key, phaseOneFunction);
  }

  /**
   * Generates a random number between the specified min and max values.
   *
   * @param min the minimum value (inclusive).
   * @param max the maximum value (exclusive).
   * @return a random number between min (inclusive) and max (exclusive).
   */
  private int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  /**
   * Shuts down the coordinator and all associated servers.
   * This method ensures that all resources are properly released and the servers are
   * cleanly shut down.
   *
   * @throws IllegalArgumentException if there is an error during the shutdown process.
   */
  @Override
  public void shutdown() throws IllegalArgumentException {
    for (int i = 0; i < serverCount; i++) {
      try {
        Registry registry = LocateRegistry.getRegistry(serverPorts.get(i));
        registry.unbind("Store");

      } catch (RemoteException | NotBoundException e) {
        log("Error during server shutdown, server port:" + serverPorts.get(i) + " Error:" + e.getMessage());
      }
    }
    log("All servers shut down successfully.");
  }


  /**
   * Initiates the two-phase commit process for a given key and phase one function.
   *
   * @param key the key involved in the transaction.
   * @param phaseOneFunction the function to execute in phase one of the commit process.
   * @return true if the transaction was successful, false otherwise.
   */
  private boolean proceed2PC(String key, Function<CoordinatedServer, Boolean> phaseOneFunction) {
    int failedCommits = phaseOne(phaseOneFunction);
    return phaseTwo(key, failedCommits);
  }

  /**
   * Executes phase one of the two-phase commit process.
   *
   * @param phaseOneFunction the function to execute in phase one of the commit process.
   * @return the number of failed commits during phase one.
   */
  private int phaseOne(Function<CoordinatedServer,
          Boolean> phaseOneFunction) {

    int failedCommit = 0;

    ExecutorService executor = Executors.newFixedThreadPool(serverCount); // To add timeout for any server taking too long

    List<Future<Boolean>> futures = new ArrayList<>();
    for (CoordinatedServer server : servers) {
      futures.add(executor.submit(() -> phaseOneFunction.apply(server)));
    }

    for (int i = 0; i < serverCount; i++) {
      try {
        boolean serverCanCommit = futures.get(i).get(1, TimeUnit.SECONDS);
        if (!serverCanCommit) { // if the server responds with false for canCommit
          failedCommit++;
          log("Received can not commit transaction from replica:" + i);
        }
      } catch (ExecutionException | InterruptedException | TimeoutException e) {
        futures.get(i).cancel(true);
        failedCommit++;
        e.printStackTrace();
        log("Did not receive acknowledgement to commit from replica:" + i);
      }
    }
    return failedCommit;

  }

  /**
   * Executes phase two of the two-phase commit process.
   *
   * @param key the key involved in the transaction.
   * @param failedCommit the number of failed commits during phase one.
   * @return true if the transaction was successful, false otherwise.
   */
  private boolean phaseTwo(String key, int failedCommit) {
    Function<CoordinatedServer, Boolean> phaseTwoFunction;
    int goAckFail = 0;
    String operation = failedCommit == 0 ? "commit" : "abort";
    try {
      phaseTwoFunction = failedCommit == 0 ? server -> {
        try {
          return server.doCommit(key);
        } catch (RemoteException e) {
          return false;
        }
      }
              : server -> {
        try {
          return server.doAbort(key);
        } catch (RemoteException e) {
          return false;
        }
      };

      for (int i = 0; i < serverCount; i++) {
        boolean serverAcknowledged = phaseTwoFunction.apply(servers.get(i));
        if (serverAcknowledged) {
          log("Replica:" + i + " acknowledged " + operation);
        } else {
          goAckFail++;
          log("Replica:" + i + "  could not acknowledge " + operation);
        }
        if (goAckFail > 0) {
          //rollback would go here. out of scope for this project since assumption is servers do not fail
          log("Phase two failed");
        }
      }
      return failedCommit == 0; //return true for zero failures
    } catch (RuntimeException e) {
      return false;
    }
  }

  /**
   * Logs a message using the coordinator's logger.
   *
   * @param message the message to log.
   */
  private void log(String message) {
    logger.log("Coordinator:" + message);
  }

}
