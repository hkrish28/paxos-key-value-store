package server;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Defines the methods for a Paxos server in the Paxos consensus protocol.
 * This interface extends the generic {@link Server} interface and provides
 * specific methods for handling Paxos protocol operations.
 */
public interface PaxosServer extends Server {

  /**
   * Handles a prepare request with the specified prepare ID.
   *
   * @param prepareId the ID of the prepare request
   * @return a {@link PreparePromise} containing the promise response
   * @throws RemoteException if there is an error during remote method invocation
   */
  PreparePromise prepare(long prepareId) throws RemoteException;

  /**
   * Handles an accept request with the specified ID and consumer for the message map.
   *
   * @param id the ID of the accept request
   * @param mapConsumer the consumer function to process the map of string key-value pairs
   * @return an {@link AcceptMessage} containing the result of the accept operation
   * @throws RemoteException if there is an error during remote method invocation
   */
  AcceptMessage accept(long id, Consumer<Map<String,String>> mapConsumer) throws RemoteException;

  /**
   * Updates the learner with the specified accept message.
   *
   * @param message the {@link AcceptMessage} to update the learner with
   * @throws RemoteException if there is an error during remote method invocation
   */
  void updateLearner(AcceptMessage message) throws RemoteException;

  /**
   * Updates the list of connected servers that have been set up for the agreement.
   *
   * @param servers the updated list of {@link PaxosServer} instances
   * @throws RemoteException if there is an error during remote method invocation
   */
  void updateConnectedServers(List<PaxosServer> servers) throws RemoteException;

}
