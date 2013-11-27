package uk.ac.cam.gt319.fjava.tick3;

public class SafeMessageQueue<T> implements MessageQueue<T> {
  private Link<T> first = null;
  private Link<T> last = null;

  public synchronized void put(T val) {
    Link<T> l = new Link<T>(val);
    if (first == null) {
      first = l;
      last = l;
    } else {
      last.next = l;
      last = l;
    }
    this.notify();
  }

  public synchronized T take() {
    //use a loop to block thread until data is available
    while (first == null) {
      try {
        this.wait();
      } catch(InterruptedException ie) {
      }
    }
    T val = first.val;
    first = first.next;
    return val;
  }
  
  private static class Link<L> {
    L val;
    Link<L> next;
    Link(L val) {
      this.val = val;
      this.next = null;
    }
  }
}