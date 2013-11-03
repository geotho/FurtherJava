package uk.ac.cam.gt319.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class StringReceive {

  private static Socket socket;
  
  public static void main(String[] args) throws IOException, InterruptedException {
    parseArgs(args);
    InputStream in = socket.getInputStream();
    byte[] bytes;
    int bytesRead;
    String m;
    while (true) {
      bytes = new byte[1024];
      bytesRead = in.read(bytes);
      m = new String(bytes);
      System.out.println(m);
      Thread.sleep(100);
    }
  }
  
  private static void parseArgs(String[] args) {
    int port = 0;
    if (args.length != 2) {
      argsErrorAndExit();
    }
    try {
      port = Integer.parseInt(args[1]);
      socket = new Socket(args[0], port);
    } catch (NumberFormatException e) {
      argsErrorAndExit();
    } catch (Exception e) {
      connectionErrorAndExit(args[0], port);
    }
  }
  
  private static void argsErrorAndExit() {
    System.err.println("This application requires two arguments: <machine> <port>");
    System.exit(1);
  }
  
  private static void connectionErrorAndExit(String server, int port) {
    System.err.println(String.format("Cannot connect to %s on port %s", server, port));
    System.exit(1);
  }
}
