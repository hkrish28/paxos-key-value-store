package server;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a message that can be accepted by a server.
 * It contains an identifier and a consumer function to process a map of string key-value pairs.
 */
public class AcceptMessage {

  private long id;

  private Consumer<Map<String,String>> consumer;

  /**
   * Constructs an AcceptMessage with the specified id and consumer.
   *
   * @param id the unique identifier for this message
   * @param consumer the consumer function to process the message's data
   */
  public AcceptMessage(long id, Consumer<Map<String,String>> consumer){
    this.id = id;
    this.consumer = consumer;
  }

  /**
   * Returns the unique identifier for this message.
   *
   * @return the unique identifier
   */
  public long getId() {
    return id;
  }

  /**
   * Returns the consumer function associated with this message.
   *
   * @return the consumer function
   */
  public Consumer<Map<String, String>> getConsumer() {
    return consumer;
  }
}
