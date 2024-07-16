package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import shared.Logger;

public class CoordinatorImpl implements Coordinator {

  private static Logger logger = new Logger(System.out);
  private int serverCount;
  private List<CoordinatedServer> servers = new ArrayList<>();
  private List<Server> stubs = new ArrayList<>();

  private List<Integer> serverPorts;
  //TODO: try removing stub
  public CoordinatorImpl(List<Integer> serverPorts) throws IllegalStateException {
    serverCount = serverPorts.size();
    for (int i = 0; i < serverCount; i++) {
      int retryCount = 1;
      int retryMax = 5;
      while (retryCount <= retryMax) {
        try {
          Registry registry = LocateRegistry.createRegistry(serverPorts.get(i));
          retryCount = retryMax + 2;
          CoordinatedServer obj = new ServerImpl(this, serverPorts.get(i));
          Server stub = (Server) UnicastRemoteObject.exportObject(obj, 0);
          stubs.add(stub);
          servers.add(obj);
          // Bind the remote object's stub in the registry
          registry.rebind("Store", stub);
        } catch (RemoteException e) {
          log("Error creating server:" + i + " at port:" + serverPorts.get(i));
          log("Error:" + e.getMessage());
          serverPorts.set(i, getRandomNumber(5500, 6000));
          retryCount++;
        }
      }
      if (retryCount != retryMax + 2) {
        log("Exiting since server not set up.");
        throw new IllegalStateException("Server set up failed");
      }
    }
    this.serverPorts = serverPorts;
  }


  @Override
  public boolean update(String key, String value) {
    Function<CoordinatedServer, Boolean> phaseOneFunction =
            server -> {
              try {
                return server.canCommit(key, store -> store.put(key, value));
              } catch (RemoteException e) {
                return false;
              }
            };

    return proceed2PC(key, phaseOneFunction);
  }

  @Override
  public boolean delete(String key) {
    Function<CoordinatedServer, Boolean> phaseOneFunction =
            server -> {
              try {
                return server.canCommit(key, store -> store.remove(key));
              } catch (RemoteException e) {
                return false;
              }
            };


    return proceed2PC(key, phaseOneFunction);
  }

  private int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  @Override
  public void shutdown() throws IllegalArgumentException {
    for (int i = 0; i < serverCount; i++) {
      try {
        Registry registry = LocateRegistry.getRegistry(serverPorts.get(i));
        registry.unbind("Store");

      } catch (RemoteException | NotBoundException e) {
        log("Error during server shutdown, server port:" + serverPorts.get(i) + " Error:" + e.getMessage());
      }
    }
    log("All servers shut down successfully.");
  }


  private boolean proceed2PC(String key, Function<CoordinatedServer, Boolean> phaseOneFunction) {
    int failedCommits = phaseOne(phaseOneFunction);
    return phaseTwo(key, failedCommits);
  }


  private int phaseOne(Function<CoordinatedServer,
          Boolean> phaseOneFunction) {

    int failedCommit = 0;

    for (int i = 0; i < serverCount; i++) {
      if (!phaseOneFunction.apply(servers.get(i))) { // if the server responds with false for canCommit
        failedCommit++;
      }
    }
    return failedCommit;

  }

  private boolean phaseTwo(String key, int failedCommit) {
    Function<CoordinatedServer, Boolean> phaseTwoFunction;
    int goAckFail = 0;
    String operation = failedCommit == 0 ? "commit" : "abort";
    try {
      phaseTwoFunction = failedCommit == 0 ? server -> {
        try {
          return server.doCommit(key);
        } catch (RemoteException e) {
          return false;
        }
      }
              : server -> {
        try {
          return server.doAbort(key);
        } catch (RemoteException e) {
          return false;
        }
      };

      for (int i = 0; i < serverCount; i++) {
        if(phaseTwoFunction.apply(servers.get(i))){
          log("Replica:" + i + " acknowledged " + operation);
        }
        else{
          goAckFail++;
          log("Replica:" + i + "  could not acknowledge " + operation);
        }
        if (goAckFail > 0){
          //rollback would go here. out of scope for this project since assumption is servers do not fail
          log("Phase two failed");
        }
      }

      return failedCommit == 0;
    } catch (RuntimeException e){
      return false;
    }
  }

  private void log(String message) {
    logger.log(message);
  }

}
