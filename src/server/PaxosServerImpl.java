package server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import shared.Logger;

public class PaxosServerImpl implements PaxosServer, Server {

  private static Logger logger = new Logger(System.out);
  final int port;
  private Map<String, String > mapStore;
  private List<PaxosServer> serverList;
  private long lastPrepared;
  private long lastAcceptedId;
  private Consumer<Map<String, String>> lastAcceptedCommand;

  private AcceptorRunnable acceptorRunnable;
  private Thread acceptorRunnableThread;

  public PaxosServerImpl(int port) {
    mapStore = new ConcurrentHashMap<>();
    serverList = new ArrayList<>();
    lastAcceptedId = -1;
    lastPrepared = -1;
    mapStore = new ConcurrentHashMap<>();
    this.port = port;
    acceptorRunnable = new AcceptorRunnable(this);
    acceptorRunnableThread = new Thread(acceptorRunnable);
    acceptorRunnableThread.start();

    log("Server started at port:" + port);
  }

  @Override
  public String get(String key) throws RemoteException {
    log("Received GET request: " + key);

    if (mapStore.containsKey(key)) {
      String response = mapStore.get(key);
      logResponse(response);
      return response;
    }


    return "Key " + key + " not present in store";
  }

  @Override
  public String put(String key, String value) throws RemoteException, ExecutionException, InterruptedException {

    boolean status = proceedPaxos(mapStore -> mapStore.put(key, value));
    String response = status ? "Value updated for " + key + " successfully"
            : "Value update for " + key + " failed. Please try again.";
    logResponse(response);
    log("Current value:" + mapStore.get(key));
    return response;

  }

  @Override
  public String delete(String key) throws RemoteException, ExecutionException, InterruptedException {
    log("Received DELETE request - Key: " + key);
    if (mapStore.containsKey(key)) {
      boolean status = proceedPaxos(mapStore -> mapStore.remove(key));

      String response = status ? key + " deleted successfully"
              : key + " was unable to be deleted. Please try again.";
      logResponse(response);
      return response;
    } else {
      String response = key + " not present in the store to be removed";
      logResponse(response);
      return response;
    }
  }


  @Override
  public PreparePromise prepare(long prepareId) {
    if (!acceptorRunnable.isRunning()) {
      log("Acceptor thread not running, skipping prepare request.");
      return null;
    }

    if (lastPrepared > prepareId) {
      return null;
    }
    lastPrepared = prepareId;
    if (lastAcceptedId == -1) {
      return new PreparePromise(prepareId);
    } else
      return new PreparePromise(prepareId, lastAcceptedId, lastAcceptedCommand);
  }

  @Override
  public AcceptMessage accept(long id, Consumer<Map<String, String>> mapConsumer) {
    if (!acceptorRunnable.isRunning()) {
      log("Acceptor thread not running, skipping accept request.");
      return null;
    }

    if (id < lastPrepared || id < lastAcceptedId)
      return null;
    lastAcceptedId = id;
    lastAcceptedCommand = mapConsumer;
    AcceptMessage acceptMessage = new AcceptMessage(id, mapConsumer);
    for (PaxosServer server : serverList) {
      try {
        server.updateLearner(acceptMessage);
      } catch (RemoteException e) {
        log("Remote exception encountered");
      }
    }

    return acceptMessage;
  }

  @Override
  public void updateLearner(AcceptMessage message) {
    lastAcceptedId = -1;
    lastPrepared = -1;
    message.getConsumer().accept(mapStore); //learner updates map store
//    log("Learner updated value successfully.");
  }

  @Override
  public void updateConnectedServers(List<PaxosServer> servers) {
    serverList = servers;
  }

  private void log(String message) {
    logger.log("Port:" + port + ":" + message);
  }

  private void logResponse(String response) {
    logger.log("Sending Response: " + response);
  }

  private boolean proceedPaxos(Consumer<Map<String, String>> commandIssued) {
    long id;
    int prepareAccepted = 0;
    id = System.currentTimeMillis();
    Consumer<Map<String, String>> command = commandIssued;
    long currGreatestAccepted = -1;
    for (PaxosServer server : serverList) {
      try {
        PreparePromise promise = server.prepare(id);
        if (promise != null) {
          prepareAccepted++;
          if (promise.getAcceptedId() > currGreatestAccepted) {
            command = promise.getAcceptedCommand();
          }
        }
      } catch (RemoteException e) {
        log("Remote Exception occurred.");
      }

    }

    if (prepareAccepted <= Math.floor(serverList.size() / 2)) {
      log("Majority not received. Aborting...");
      return false; //majority not acquired
    }
    log("Majority of " + prepareAccepted + " received. Accepting phase initiating...");
    runPhaseTwo(id, command);

    return true;

  }

  private boolean runPhaseTwo(long id, Consumer<Map<String, String>> command) {
    int acceptedCount = 0;

    for (PaxosServer server : serverList) {
      try {
        if (server.accept(id, command) != null) {
          acceptedCount++;
        }
      } catch (RemoteException e){
        log("Remote exception encountered");
      }
    }

    if (acceptedCount > Math.floor(serverList.size() / 2)) {
      log("Consensus reached");
    } else {
      log("Majority did not accept");
    }
    return true;
  }

}
