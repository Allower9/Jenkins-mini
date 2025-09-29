package hello;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello from Jenkins!");
    }

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
