package server;

import java.util.Map;
import java.util.function.Consumer;

public class AcceptMessage {

  private long id;

  private Consumer<Map<String,String>> consumer;

  public AcceptMessage(long id, Consumer<Map<String,String>> consumer){
    this.id = id;
    this.consumer = consumer;
  }

  public long getId() {
    return id;
  }

  public Consumer<Map<String, String>> getConsumer() {
    return consumer;
  }
}
