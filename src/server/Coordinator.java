package server;

public interface Coordinator {

  public void shutdown();

  public boolean update(String key, String value);

  public boolean delete(String key);

}
