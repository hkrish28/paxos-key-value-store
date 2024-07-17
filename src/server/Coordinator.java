package server;

/**
 * Coordinator is an interface for managing transactions across multiple servers.
 * It defines methods for shutting down the coordinator and updating or deleting
 * keys in the distributed store.
 */
public interface Coordinator {

  /**
   * Shuts down the coordinator and all associated servers.
   * This method ensures that all resources are properly released and the servers are
   * cleanly shut down.
   *
   * @throws IllegalArgumentException if there is an error during the shutdown process.
   */
  void shutdown() throws IllegalArgumentException;

  /**
   * Updates the value associated with the specified key across all coordinated servers.
   * This method initiates a two-phase commit process to ensure consistency.
   *
   * @param key the key to be updated.
   * @param value the new value to be associated with the key.
   * @return true if the update was successful across all servers, false otherwise.
   */
  boolean update(String key, String value);

  /**
   * Deletes the key and its associated value across all coordinated servers.
   * This method initiates a two-phase commit process to ensure consistency.
   *
   * @param key the key to be deleted.
   * @return true if the delete operation was successful across all servers, false otherwise.
   */
  boolean delete(String key);

}
