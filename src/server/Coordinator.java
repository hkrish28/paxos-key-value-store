package server;

public interface Coordinator {

  void shutdown();

  boolean update(String key, String value);

  boolean delete(String key);

}
