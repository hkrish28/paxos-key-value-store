package server;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a promise response to a prepare request in the Paxos consensus protocol.
 * It contains information about the prepare request and any accepted proposal.
 */
public class PreparePromise {

  private long acceptedId;
  private long id;
  private Consumer<Map<String, String>> acceptedCommand;

  /**
   * Constructs a PreparePromise with the specified prepare ID, accepted ID, and accepted command.
   *
   * @param id the ID of the prepare request
   * @param acceptedId the ID of the accepted proposal, or -1 if no proposal has been accepted
   * @param acceptedCommand the command associated with the accepted proposal, or null if none
   */
  public PreparePromise(long id, long acceptedId, Consumer<Map<String, String>> acceptedCommand) {
    this.id = id;
    this.acceptedId = acceptedId;
    this.acceptedCommand = acceptedCommand;
  }

  /**
   * Constructs a PreparePromise with the specified prepare ID.
   * This constructor is used when no proposal has been accepted yet.
   *
   * @param id the ID of the prepare request
   */
  public PreparePromise(long id) {
    this.id = id;
    this.acceptedId = -1;
  }

  /**
   * Checks if the promise corresponds to the given prepare ID.
   *
   * @return true if the prepare ID matches the accepted ID, false otherwise
   */
  public boolean getStatus() {
    return id == acceptedId;
  }

  /**
   * Returns the ID of the accepted proposal.
   *
   * @return the ID of the accepted proposal, or -1 if no proposal has been accepted
   */
  public Long getAcceptedId() {
    return acceptedId;
  }

  /**
   * Returns the ID of the prepare request.
   *
   * @return the ID of the prepare request
   */
  public long getId() {
    return id;
  }

  /**
   * Returns the command associated with the accepted proposal.
   *
   * @return the accepted command, or null if no proposal has been accepted
   */
  public Consumer<Map<String, String>> getAcceptedCommand() {
    return acceptedCommand;
  }

}
