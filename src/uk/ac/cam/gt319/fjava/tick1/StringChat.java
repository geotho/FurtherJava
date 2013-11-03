package uk.ac.cam.gt319.fjava.tick1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;


public class StringChat {
  
  public static void main(String[] args) throws IOException, InterruptedException {
    Socket socket = null;
    // Parse args
    int port = 0;
    if (args.length != 2) {
      argsError();
      return;
    }
    try {
      port = Integer.parseInt(args[1]);
      // Socket s is final because objects must be final to be referenced from anonymous classes
      socket = new Socket(args[0], port);
    } catch (NumberFormatException e) {
      argsError();
      return;
    } catch (Exception e) {
      connectionError(args[0], port);
      return;
    }
    
    final Socket s = socket;
    Thread output = new Thread() {
      @Override
      public void run() {
        try {
          InputStream in = s.getInputStream();
          byte[] bytes;
          String m;
          while (true) {
            bytes = new byte[1024];
            in.read(bytes);
            m = new String(bytes);
            System.out.println(m);
            Thread.sleep(100);
          }
        } catch (Exception e) {
          e.printStackTrace();
          System.exit(0);
        }
      }
    };
    output.setDaemon(true);
    output.start();
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    while(true) {
      Thread.sleep(10);
      System.out.print("YOU: ");
      String userText = r.readLine();
      byte[] bytes = userText.getBytes();
      OutputStream os = s.getOutputStream();
      os.write(bytes);
      os.flush();
    }
  }
  
  private static void argsError() {
    System.err.println("This application requires two arguments: <machine> <port>");
  }
  
  private static void connectionError(String server, int port) {
    System.err.println(String.format("Cannot connect to %s on port %s", server, port));
  }
}
