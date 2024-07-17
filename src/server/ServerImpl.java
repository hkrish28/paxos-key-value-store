package server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import shared.Logger;

/**
 * ServerImpl is a concrete implementation of the Server interface for handling
 * GET, PUT, and DELETE operations on a concurrent key-value store.
 */
public class ServerImpl implements CoordinatedServer, Serializable {

  private Map<String, String > mapStore = new ConcurrentHashMap<>();
  private Map<String, Optional<String>> activeTransaction = new ConcurrentHashMap<>();

  private static Logger logger = new Logger(System.out);

  private final Coordinator coordinator;
  private final int port;

  /**
   * Constructs a ServerImpl instance with the specified coordinator and port.
   *
   * @param coordinator the coordinator instance managing this server
   * @param port the port number on which this server is running
   */
  public ServerImpl(Coordinator coordinator, int port){
     this.coordinator = coordinator;
     this.port = port;
     log("Server started at port:" + port);
  }

  /**
   * Retrieves the value associated with the specified key from the store.
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the specified key, or a message if the key is not present
   * @throws RemoteException if a remote communication error occurs
   */
  @Override
  public String get(String key) throws RemoteException {
    log("Received GET request: " + key);
    while (activeTransaction.get(key) != null) {
      System.out.println("Rollback value:" + activeTransaction.get(key));
      System.out.println("Map value:" +mapStore.get(key));
    }
      if(mapStore.containsKey(key)) {
        log("WITHIN GET" + mapStore.size());
        String response = mapStore.get(key);
        logResponse(response);
        return response;
      }


    return "Key " + key + " not present in store";
  }

  /**
   * Puts the specified key-value pair into the store. If the key already exists, its value is updated.
   *
   * @param key the key with which the specified value is to be associated
   * @param value the value to be associated with the specified key
   * @return a message indicating the result of the operation
   * @throws RemoteException if a remote communication error occurs
   */
  @Override
  public String put(String key, String value) throws RemoteException {
    log("Received PUT request - Key: " + key + " Value - " + value);

    boolean status = coordinator.update(key,value);

    String response = status? "Value updated for " + key + " successfully"
            : "Value update for " + key + " failed. Please try again.";
    logResponse(response);
    log("Current value:" + mapStore.get(key));
    return response;
  }



  /**
   * Deletes the key-value pair associated with the specified key from the store.
   *
   * @param key the key whose key-value pair is to be removed
   * @return a message indicating the result of the operation
   * @throws RemoteException if a remote communication error occurs
   */
  @Override
  public String delete(String key) throws RemoteException {
    log("Received DELETE request - Key: " + key);
    if(mapStore.containsKey(key)) {
      boolean status = coordinator.delete(key);

      String response = status? key + " deleted successfully"
              : key + " was unable to be deleted. Please try again.";
      logResponse(response);
      return response;
    }
    else {
      String response = key + " not present in the store to be removed";
      logResponse(response);
      return response;
    }
  }

  /**
   * Logs a message using the logger.
   *
   * @param message the message to log
   */
  private void log(String message) {
    logger.log("Port:"+ port +":" + message);
  }

  /**
   * Logs the processed response message.
   *
   * @param response the response message to log
   */
  private void logResponse(String response) {
    logger.log("Sending Response: " + response);
  }

  /**
   * Checks if the server can commit a transaction involving the specified key.
   * If the key is involved in an active transaction, it returns false.
   * Otherwise, it starts the transaction and applies the store operation.
   *
   * @param key the key involved in the transaction
   * @param storeOperation the operation to apply to the store
   * @return true if the server can commit the transaction, false otherwise
   */
  @Override
  public boolean canCommit(String key, Consumer<Map<String,String>> storeOperation) {

    /**
     * Uncomment the  below line to mock a failure case
     */
//        demoFailure();

    if (activeTransaction.containsKey(key)){
      return false;
    }
    activeTransaction.put(key, Optional.ofNullable(mapStore.get(key)));

    storeOperation.accept(mapStore);
    return true;
  }

  /**
   * Introduces an artificial failure for demonstration purposes by putting the server to sleep.
   */
  private void demoFailure() {
    if (port % 5 == 0){
      try {
        log("Replica at port " + port + " sleeping for 1.5s");
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        log("Replica sleep interrupted");
        throw new RuntimeException("Replica sleep interupted", e);
      }
    }
  }

  /**
   * Commits the transaction involving the specified key.
   * This method removes the key from the active transactions.
   *
   * @param key the key involved in the transaction
   * @return true if the transaction was committed successfully, false otherwise
   */
  @Override
  public boolean doCommit(String key) {
    try {
      activeTransaction.remove(key);
      log("Server committed for key:" + key );
      return true;
    } catch (RuntimeException e){
      log("Error encountered during execution: " + e.getMessage());
      return false;
    }
  }

  /**
   * Aborts the transaction involving the specified key.
   * This method rolls back the store to the previous value associated with the key.
   *
   * @param key the key involved in the transaction
   * @return true if the transaction was aborted successfully, false otherwise
   */
  @Override
  public boolean doAbort(String key) {
    try{
      if (activeTransaction.get(key).isPresent()){
        mapStore.put(key, activeTransaction.get(key).get());
        activeTransaction.remove(key);
      }
      else{
        mapStore.remove(key);
        activeTransaction.remove(key);
      }
      log("Server aborted for key:" + key + " current value:" + mapStore.get(key) +
              " rollback value: " + activeTransaction.get(key));
      return true;
    } catch (RuntimeException e){
      log("Error encountered during execution: " + e.getMessage());
      return false;
    }
  }

}
