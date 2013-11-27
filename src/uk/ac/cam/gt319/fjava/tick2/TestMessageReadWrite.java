package uk.ac.cam.gt319.fjava.tick2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

class TestMessageReadWrite {
  
  static boolean writeMessage(String message, String filename) {
    TestMessage testMessage = new TestMessage();
    testMessage.setMessage(message);
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
      out.writeObject(testMessage);
      out.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  static String readMessage(String location) {
    InputStream in = null;
    ObjectInputStream objIn = null;
    try { 
      if (location.startsWith("http://")) {
        URL url = new URL(location);
        URLConnection connection = url.openConnection();
        in = connection.getInputStream();
      } else {
        in = new FileInputStream(location);
      }
      objIn = new ObjectInputStream(in);
      TestMessage tm = (TestMessage) objIn.readObject();
      objIn.close();
      in.close();
      return tm.getMessage();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  public static void main(String args[]) {
    final String location = "http://www.cl.cam.ac.uk/teaching/1314/FJava/testmessage-gt319.jobj";
    System.out.println(String.format("The message in the file is: '%s'.", readMessage(location)));
  }
}