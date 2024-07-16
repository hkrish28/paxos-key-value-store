package client;

import java.io.InputStream;

/**
 * Client interface represents a client that can send requests to a server.
 */
public interface Client {

  /**
   * Sends requests to the server.
   *
   * @param in the input stream from which to read requests
   */
  boolean sendRequests(InputStream in);
}
