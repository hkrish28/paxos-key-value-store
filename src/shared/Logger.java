package shared;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger is a simple utility class for logging messages to a specified output stream.
 * Each message is prefixed with the current system time in milliseconds.
 */
public class Logger {
  private final PrintStream outputStream;

  /**
   * Constructs a Logger that logs messages to the specified PrintStream.
   *
   * @param out the PrintStream to which log messages will be written
   */
  public Logger(PrintStream out) {
    this.outputStream = out;
  }

  /**
   * Logs a message to the output stream. The message is prefixed with the current system time in milliseconds.
   *
   * @param message the message to log
   */
  public void log(String message) {
    outputStream.println(getCurrentTimeFormatted() + ":" + message);
  }

  private String getCurrentTimeFormatted() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    long currentTimeMillis = System.currentTimeMillis();
    Date resultDate = new Date(currentTimeMillis);
    return sdf.format(resultDate);
  }
}
