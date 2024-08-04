package server;

import java.util.Map;
import java.util.function.Consumer;

public class PreparePromise {

  private long acceptedId;
  private long id;

  private Consumer<Map<String,String>> acceptedCommand;


  public PreparePromise(long id, long acceptedId, Consumer<Map<String,String>> acceptedCommand ){
    this.id = id;
    this.acceptedId = acceptedId;
    this.acceptedCommand = acceptedCommand;
  }

  public PreparePromise(long id){
    this.id = id;
    this.acceptedId = -1;
  }

  public boolean getStatus(){
    return id == acceptedId;
  }

  public Long getAcceptedId(){
    return acceptedId;
  }

  public long getId(){
    return id;
  }

  public Consumer<Map<String,String>> getAcceptedCommand(){
    return acceptedCommand;
  }


}
