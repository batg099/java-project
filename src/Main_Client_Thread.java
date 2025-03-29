import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

// Remarque, ça va prendre 2 secondes à afficher les informations, car on fait un Thread.sleep(2000) dans le serveur

public class Main_Client_Thread implements Runnable{
    private ExecutorService executor;
    private Socket client;
    private ObjectOutputStream output_client;
    private ObjectInputStream  input_client;
    private int x;
    public Main_Client_Thread(int x) throws IOException {
        try {
            this.executor = Executors.newFixedThreadPool(6);
            client = new Socket("127.0.0.1", 12345);
            this.output_client = new ObjectOutputStream(client.getOutputStream());
            this.input_client = new ObjectInputStream(client.getInputStream());
            this.x = x;
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    public Main_Client_Thread() throws IOException {
        this(0);
    }

    @Override
    public void run() {
        // On demande le salaire de l'employé x
        try {
            output_client.writeObject("2");
            output_client.writeObject(String.valueOf(x));
            String salaire = input_client.readObject().toString();
            // On demande le nom de l'employé x
            output_client.writeObject("3");
            output_client.writeObject(String.valueOf(x));
            String name = input_client.readObject().toString();

            System.out.println("Le salaire du professeur " + x + "-" + name +  " est : " + salaire);
        }catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void manage(){
        try {
            // On initialise la socket


            // On envoie la requête
            this.output_client.writeObject("1");
            System.out.println("hi");

            // On lit ce qu'on reçoit
            // On pourrait directement cast, car on est censer connaitre les types reçues
            // On pourrait donc faire : ArrayList<Integer> liste = ArrayList<Integer> request;
            Object req = this.input_client.readObject();
            HashMap<Integer, String> request = (HashMap<Integer, String>) req;
            request.forEach((k,v) -> {
                System.out.println("J'ai reçu " + v + " - " + k);
            });



        }
        catch (Exception e) {
            System.out.println(e);
        }

    }
    public static void main(String[] args) throws IOException {
        System.out.println("Client_Thread !");
        Main_Client_Thread m = new Main_Client_Thread();
        m.manage();
    }
}


