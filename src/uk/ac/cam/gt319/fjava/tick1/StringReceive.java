package uk.ac.cam.gt319.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class StringReceive {

  private static Socket socket;
  
  public static void main(String[] args) throws IOException, InterruptedException {
    if (!parseArgs(args)) {
      return;
    };
    InputStream in = socket.getInputStream();
    byte[] bytes;
    String m;
    while (true) {
      bytes = new byte[1024];
      in.read(bytes);
      m = new String(bytes);
      System.out.println(m);
      Thread.sleep(100);
    }
  }
  
  private static boolean parseArgs(String[] args) {
    int port = 0;
    if (args.length != 2) {
      argsError();
      return false;
    }
    try {
      port = Integer.parseInt(args[1]);
      socket = new Socket(args[0], port);
    } catch (NumberFormatException e) {
      argsError();
      return false;
    } catch (Exception e) {
      connectionError(args[0], port);
      return false;
    }
    return true;
  }
  
  private static void argsError() {
    System.err.println("This application requires two arguments: <machine> <port>");
  }
  
  private static void connectionError(String server, int port) {
    System.err.println(String.format("Cannot connect to %s on port %s", server, port));
  }
}
