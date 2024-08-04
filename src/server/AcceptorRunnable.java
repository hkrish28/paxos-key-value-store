package server;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Random;

import shared.Logger;

public class AcceptorRunnable implements Runnable {

  private final PaxosServerImpl server;
  private final AtomicBoolean running;
  private final Random random;
  private static final int MIN_FAILURE_INTERVAL = 5000; // Min time (in ms) before the thread fails
  private static final int MAX_FAILURE_INTERVAL = 10000; // Max time (in ms) before the thread fails
  private static final int MIN_RESTART_DELAY = 2000; // Min delay (in ms) before the thread restarts
  private static final int MAX_RESTART_DELAY = 5000; // Max delay (in ms) before the thread restarts

  private static Logger logger = new Logger(System.out);
  public AcceptorRunnable(PaxosServerImpl server) {
    this.server = server;
    this.running = new AtomicBoolean(true);
    this.random = new Random();
  }

  @Override
  public void run() {
    while (true) {
      if (running.get()) {
        try {
          // Check if any other acceptor thread is sleeping
          synchronized (AcceptorState.class) {
            while (!AcceptorState.canAcceptorSleep()) {
              // Wait if maximum number of acceptors are already sleeping
              AcceptorState.class.wait();
            }
            // Set state to sleeping
            AcceptorState.setAcceptorSleeping(true);
          }

          // Simulate normal operation
          Thread.sleep(random.nextInt((MAX_FAILURE_INTERVAL - MIN_FAILURE_INTERVAL) + 1) + MIN_FAILURE_INTERVAL);

          // Fail the thread
          running.set(false);
          log("Acceptor thread failed, restarting..." );

          // Restart after a random delay
          Thread.sleep(random.nextInt((MAX_RESTART_DELAY - MIN_RESTART_DELAY) + 1) + MIN_RESTART_DELAY);

          // Set state to not sleeping
          synchronized (AcceptorState.class) {
            AcceptorState.setAcceptorSleeping(false);
            // Notify other waiting threads
            AcceptorState.class.notifyAll();
          }


          running.set(true);
          log("Acceptor thread restarted");

        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public boolean isRunning() {
    return running.get();
  }

  private void log(String message) {
    logger.log("Port:" + server.port + ":" + message);
  }
}
