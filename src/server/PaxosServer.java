package server;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface PaxosServer extends Server {
  PreparePromise prepare(long prepareId) throws RemoteException;

  //respond with accepted or fail
  AcceptMessage accept(long id, Consumer<Map<String,String>> mapConsumer) throws RemoteException;

  void updateLearner(AcceptMessage message) throws RemoteException;

  void updateConnectedServers(List<PaxosServer> servers) throws RemoteException;

}
