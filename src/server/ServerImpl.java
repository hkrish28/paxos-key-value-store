package server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import shared.Logger;

/**
 * ServerImpl is a concrete implementation of the Server interface for handling
 * GET, PUT, and DELETE operations on a concurrent key-value store.
 */
public class ServerImpl implements CoordinatedServer, Serializable {

  private Map<String, String > mapStore = new ConcurrentHashMap<>();
  private Map<String, String> backup = new ConcurrentHashMap<>();
  private static Logger logger = new Logger(System.out);

  private final Coordinator coordinator;
  private final int port;

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
  public synchronized String get(String key) throws RemoteException {
    log("Received GET request: " + key);
    if(mapStore.containsKey(key)) {
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
  public synchronized String put(String key, String value) throws RemoteException {
    log("Received PUT request - Key: " + key + " Value - " + value);

    boolean status = coordinator.update(key,value);

    String response = status? "Value updated for " + key + " successfully"
            : "Value updated for " + key + " failed. Please try again.";
    logResponse(response);
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
  public synchronized String delete(String key) throws RemoteException {
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
    logger.log(message);
  }

  /**
   * Logs the processed response message.
   *
   * @param response the response message to log
   */
  private void logResponse(String response) {
    logger.log("Sending Response: " + response);
  }

  @Override
  public boolean canCommit(String key, Consumer<Map<String,String>> storeOperation) {
    if (backup.containsKey(key)){
      return false;
    }
    storeOperation.accept(mapStore);
    return true;
  }

  @Override
  public boolean doCommit(String key) {
    try {
      backup.remove(key);
      log("Server committed in port:" + port + " for key:" + key + " current value:" + mapStore.get(key));
      return true;
    } catch (RuntimeException e){
      log("Error encountered during execution: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean doAbort(String key) {
    try{
      mapStore.put(key, backup.get(key));
      backup.remove(key);
      log("Server aborted in port:" + port + " for key:" + key + " current value:" + mapStore.get(key));
      return true;
    } catch (RuntimeException e){
      log("Error encountered during execution: " + e.getMessage());
      return false;
    }
  }

}
