package uk.ac.cam.gt319.fjava.tick4;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MultiQueue<T> {
  private Set<MessageQueue<T>> outputs = 
      Collections.synchronizedSet(new HashSet<MessageQueue<T>>());
  
  public void register(MessageQueue<T> q) { 
    outputs.add(q);
  }
  
  public void deregister(MessageQueue<T> q) {
    outputs.remove(q);
  }
  
  public void put(T message) {
    synchronized(outputs) {
      for(MessageQueue<T> mq : outputs) {
        mq.put(message);
      }
    }
  }
}