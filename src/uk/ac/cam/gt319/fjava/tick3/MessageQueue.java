package uk.ac.cam.gt319.fjava.tick3;

public interface MessageQueue<T> { //A FIFO queue of items of type T
  public abstract void put(T msg); //place msg on back of queue
  public abstract T take(); //block until queue length >0; return head of queue
}