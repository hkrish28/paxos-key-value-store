package client;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import server.Server;
import shared.Logger;

/**
 * ClientImpl is the implementation of the Client interface.
 * It handles sending requests to the remote server and logging the responses.
 */
public class ClientImpl implements Client {

  private static Logger logger = new Logger(System.out);
  private Server server;

  /**
   * Constructs a ClientImpl with the specified server.
   *
   * @param server the remote server to which requests are sent
   */
  public ClientImpl(Server server) {
    this.server = server;
  }

  /**
   * Sends requests to the server and handles responses.
   *
   * @param in the input stream from which to read requests
   */
  @Override
  public boolean sendRequests(InputStream in) {
    Scanner inputScanner = new Scanner(in);
    while (inputScanner.hasNext()) {
      String inputText = inputScanner.nextLine();
      if (inputText.equalsIgnoreCase("exit")) {
        System.out.println("Stopping client application...");
        return false;
      }
      if (inputText.equalsIgnoreCase("disconnect")){
        System.out.println("Disconnected from server");
        return true;
      }
      log("Request to be sent to server - " + inputText);
      dispatchRequest(inputText.split(" "));
      System.out.println("Please enter your command (put, get, delete)");
    }
    return false;
  }

  /**
   * Parses a command based on the command arguments and dispatches the request via server object.
   *
   * @param command the array of command arguments
   */
  private void dispatchRequest(String[] command) {

    try {
      switch (command[0].toLowerCase()) {
        case "put":
          if (command.length != 3) {
            log("Invalid command. Put command requires 2 arguments");
          } else {
            logResponse(server.put(command[1], command[2]));
          }
          break;
        case "get":
          if (command.length != 2) {
            log("Invalid command. Get command require 1 argument");
          } else {
            logResponse(server.get(command[1]));
          }
          break;
        case "delete":
          if (command.length != 2) {
            log("Invalid command. Get and Delete command require 1 argument");
          } else {
            logResponse(server.delete(command[1]));
          }
          break;
        default:
          log("Invalid command provided");
      }
    } catch (RemoteException | ExecutionException | InterruptedException e) {
      log("Error encountered while performing operation on server : " + e.getMessage());
    }
  }

  /**
   * Logs a message using the logger.
   *
   * @param message the message to log
   */
  private void log(String message) {
    logger.log(message);
  }

  /**
   * Logs a response message from the server using the logger.
   *
   * @param message the response message to log
   */
  private void logResponse(String message) {
    logger.log("Response from server : " + message);
  }
}
