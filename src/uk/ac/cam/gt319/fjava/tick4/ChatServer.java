package uk.ac.cam.gt319.fjava.tick4;

import java.io.IOException;
import java.net.ServerSocket;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
  
  public static void main(String args[]) {
    if (args.length != 1) {
      System.out.println("Usage: java ChatServer <port>");
      return;
    }
    int port = 0;
    try {
      port = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      System.out.println("Usage: java ChatServer <port>");
      return;
    }
    
    try {
      ServerSocket socket = new ServerSocket(port);
    } catch (IOException e) {
      System.out.println("Cannot use port number " + port);
      return;
    }
    
    MultiQueue<Message> mq = new MultiQueue<Message>();
    
    while(true) {
      
    }
  }
}
