public class Main {
    public static void main(String[] args) {

        Server server = new Server(2000,5);
        server.manageRequest();
        System.out.println("I am the main !");
    }
}