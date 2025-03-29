import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        //Container container = new Container();


        Server server = new Server(12345,10);
        server.manageRequest();

        //System.out.println("Le salaire du professeur est : " + server.getSalary(1));
        System.out.println("Main World!");



    }
}