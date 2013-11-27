package uk.ac.cam.gt319.fjava.tick3;

public class UnsafeMessageQueue<T> implements MessageQueue<T> {
  private Link<T> first = null;
  private Link<T> last = null;

  public void put(T val) {
    Link<T> l = new Link<T>(val);
    if (first == null) {
      first = l;
      last = l;
    } else {
      last.next = l;
      last = l;
    }
  }

  public T take() {
    //use a loop to block thread until data is available
    while (first == null) {
      try {
        Thread.sleep(100);
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