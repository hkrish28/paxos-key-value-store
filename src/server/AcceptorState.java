package server;

public class AcceptorState {

  private static int numAcceptorSleeping = 0;
  private static final int MAX_SLEEPING_ACCEPTERS = 2;

  // Synchronize access to the shared state
  public synchronized static boolean canAcceptorSleep() {
    return numAcceptorSleeping < MAX_SLEEPING_ACCEPTERS;
  }

  public synchronized static void setAcceptorSleeping(boolean value) {
    if (value) {
      numAcceptorSleeping++;
    } else {
      numAcceptorSleeping--;
    }
  }

}
