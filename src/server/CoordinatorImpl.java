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
          CoordinatedServer obj = new ServerImpl(this, i);
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
  }


  @Override
  public boolean update(String key, String value) {
    Function<CoordinatedServer, Boolean> phaseOneFunction =
            server -> server.canCommit(key, store -> store.put(key, value));

    return proceed2PC(key, phaseOneFunction);
  }

  @Override
  public boolean delete(String key) {
    Function<CoordinatedServer, Boolean> phaseOneFunction =
            server -> server.canCommit(key, store -> store.remove(key));


    return proceed2PC(key, phaseOneFunction);
  }

  private int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  @Override
  public void shutdown() throws IllegalArgumentException {
    for(int i = 0; i < serverCount; i++){
      try {
        LocateRegistry.getRegistry(servers.get(i).getPort()).unbind("Store");
        UnicastRemoteObject.unexportObject(stubs.get(i), true);
      } catch (RemoteException | NotBoundException e){
        log("Error during server shutdown at port:" + servers.get(i).getPort() + " Error:" + e.getMessage() );
      }
    }
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
    Consumer<CoordinatedServer> phaseTwoFunction;
    phaseTwoFunction = failedCommit == 0 ? server -> server.doCommit(key)
            : server -> server.doAbort(key);

    for (int i = 0; i < serverCount; i++) {
      phaseTwoFunction.accept(servers.get(i));
    }

    return failedCommit > 0;
  }

  private void log(String message) {
    logger.log(message);
  }

}
