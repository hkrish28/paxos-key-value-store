package server;

import java.util.Map;
import java.util.function.Consumer;

public interface CoordinatedServer extends Server {

  public boolean canCommit(String key, Consumer<Map<String,String>> storeOperation);

  public void doCommit(String key);

  public void doAbort(String key);

  public int getPort();

}
