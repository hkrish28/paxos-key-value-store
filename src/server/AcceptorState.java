package server;

/**
 * Manages the state of acceptor threads in the Paxos protocol.
 * It tracks the number of acceptor threads that are currently sleeping
 * and ensures that no more than a specified maximum number of acceptors sleep at the same time.
 * This class is solely for the purposes of demonstrating failure.
 */
public class AcceptorState {

  private static int numAcceptorSleeping = 0;
  private static final int MAX_SLEEPING_ACCEPTERS = 2;

  /**
   * Checks if an acceptor can enter the sleeping state.
   * An acceptor can sleep if the number of acceptors currently sleeping
   * is less than the maximum allowed number.
   *
   * @return true if an acceptor can sleep, false otherwise
   */
  public synchronized static boolean canAcceptorSleep() {
    return numAcceptorSleeping < MAX_SLEEPING_ACCEPTERS;
  }

  /**
   * Sets the state of an acceptor to sleeping or not sleeping.
   * If the value is true, the number of sleeping acceptors is incremented.
   * If the value is false, the number of sleeping acceptors is decremented.
   *
   * @param value true to set the acceptor to sleeping, false to set it to not sleeping
   */
  public synchronized static void setAcceptorSleeping(boolean value) {
    if (value) {
      numAcceptorSleeping++;
    } else {
      numAcceptorSleeping--;
    }
  }

}
