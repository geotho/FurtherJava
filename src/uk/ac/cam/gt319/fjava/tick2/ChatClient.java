package uk.ac.cam.gt319.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;


@FurtherJavaPreamble(
  author = "George Thomas",
  date = "12th November 2013",
  crsid = "gt319",
  summary = "Ticklet 2",
  ticker = FurtherJavaPreamble.Ticker.A
)

public class ChatClient {

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
    
    System.out.println(messageToClient(String.format("Connected to %s on port %s.", args[0], port)));
    
    final Socket s = socket;
    Thread output = new Thread() {
      @Override
      public void run() {
        try {
          InputStream in = s.getInputStream();
          DynamicObjectInputStream ois = new DynamicObjectInputStream(in);
          while (true) {
            Message recievedMessage = (Message) ois.readObject();
            System.out.println(messageToString(recievedMessage));
            
            if (recievedMessage instanceof NewMessageType) {
              NewMessageType m = (NewMessageType) recievedMessage;
              ois.addClass(m.getName(), m.getClassData());
            }
            
            invokeMethods(recievedMessage);
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
    OutputStream os = s.getOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);
    while(true) {
      Thread.sleep(10);
      String userText = r.readLine();
      
      if (userText.equals("\\quit")) {
        output.stop();
        System.out.println(messageToClient("Connection Terminated"));
        break;
      } else {
        Message message = interpretCommand(userText);
        if (message == null) {
          System.out.println(
              messageToClient(String.format("Unknown command \"%s\"", userText.substring(1))));
        } else {
          oos.writeObject(message);
          oos.flush();
        }
      }
    }
  }
  
  // Interprets any commands and returns a correct message type to return to the server, or
  // null if the user did not enter a valid command. 
  private static Message interpretCommand(String command) {
    if (command.startsWith("\\")) {
      String justCommand = "";
      String arguments = "";
      if (command.indexOf(" ") == -1) {
        justCommand = command;
      } else {
        justCommand = command.substring(0, command.indexOf(" "));
        arguments = command.substring(command.indexOf(" ")+1);
      }
      switch (justCommand) {
        case "\\nick":
          return new ChangeNickMessage(arguments);
      }
      return null;
    }
    return new ChatMessage(command);
  }
  
  private static void argsError() {
    System.err.println("This application requires two arguments: <machine> <port>");
  }
  
  private static void connectionError(String server, int port) {
    System.err.println(String.format("Cannot connect to %s on port %s", server, port));
  }
  
  private static String messageToString(Message message) {
    if (message instanceof RelayMessage) {
      RelayMessage m = (RelayMessage) message;
      return messageToString(m.getMessage(), m.getFrom(), m.getCreationTime());
    } else if (message instanceof StatusMessage) {
      StatusMessage m = (StatusMessage) message;
      return messageToString(m.getMessage(), "Server", new Date());
    } else if (message instanceof NewMessageType) {
      NewMessageType m = (NewMessageType) message;
      return messageToString(
          String.format("New class %s loaded.", m.getName()), "Client", m.getCreationTime());
    }
    Class<? extends Message> newMessageClass = message.getClass();
    Set<Field> fields = new HashSet<Field>(Arrays.asList(newMessageClass.getDeclaredFields()));
    String fieldAndValue = "";
    String fieldsAndValues = "";
    for (Field f : fields) {
      try {
        f.setAccessible(true);
        fieldAndValue = String.format("%s(%s), ", f.getName(), f.get(message));
        fieldsAndValues = fieldsAndValues + fieldAndValue;
      } catch (IllegalArgumentException e) {
      } catch (IllegalAccessException e) {
      }
    }
    fieldsAndValues = fieldsAndValues.substring(0, fieldsAndValues.length() - 2);
    return messageToString(newMessageClass.getSimpleName() + ": " + fieldsAndValues,
        "Client", message.getCreationTime());
  }
  
  private static void invokeMethods(Message m) {
    for(Method method : m.getClass().getMethods()) {
      try {
        if (method.getParameterTypes().length == 0) {
          if (method.isAnnotationPresent(Execute.class)) {
            method.invoke(m);
          }
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }
  
  private static String messageToString(String message, String from, Date date) {
    DateFormat df = new SimpleDateFormat("HH:mm:ss");
    return String.format("%s [%s] %s", df.format(date), from, message); 
  }
  
  private static String messageToClient(String message) {
    return messageToString(message, "Client", new Date()); 
  }

}
