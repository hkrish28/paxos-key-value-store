package server;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * CoordinatedServer is an extension of the Server interface that includes methods for
 * coordinating two-phase commit protocols.
 */
public interface CoordinatedServer extends Server {

  /**
   * Requests to prepare for a commit operation. The server performs the necessary
   * operations to ensure it can commit the transaction, storing the current state
   * for potential rollback if necessary.
   *
   * @param key the key involved in the transaction.
   * @param storeOperation the operation to be performed on the store if the commit is successful.
   * @return true if the server can commit the transaction, false otherwise.
   * @throws RemoteException if a remote communication error occurs.
   */
  boolean canCommit(String key, Consumer<Map<String, String>> storeOperation) throws RemoteException;

  /**
   * Commits the transaction for the specified key. The server applies the
   * transaction changes permanently.
   *
   * @param key the key involved in the transaction.
   * @return true if the commit was successful, false otherwise.
   * @throws RemoteException if a remote communication error occurs.
   */
  boolean doCommit(String key) throws RemoteException;

  /**
   * Aborts the transaction for the specified key. The server reverts to the previous
   * state stored during the prepare phase.
   *
   * @param key the key involved in the transaction.
   * @return true if the abort was successful, false otherwise.
   * @throws RemoteException if a remote communication error occurs.
   */
  boolean doAbort(String key) throws RemoteException;


}
