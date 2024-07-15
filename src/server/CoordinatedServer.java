package server;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.function.Consumer;

public interface CoordinatedServer extends Server {

  boolean canCommit(String key, Consumer<Map<String, String>> storeOperation) throws RemoteException;

  boolean doCommit(String key) throws RemoteException;

  boolean doAbort(String key) throws RemoteException;


}
