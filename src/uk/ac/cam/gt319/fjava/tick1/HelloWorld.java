package uk.ac.cam.gt319.fjava.tick1;

public class HelloWorld {
  public static void main(String[] args) {
    String name = "world";
    if (args.length == 1) {
      name = args[0];
    }
    System.out.println(String.format("Hello, %s", name));
  }
}