import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Server class: Handles client connections and distributes tasks to threads.
 */
public class Server {
    private final ServerSocket serverSocket; // ServerSocket to listen for incoming client connections
    private final ExecutorService pool; // ExecutorService to manage a pool of threads handling client requests concurrently
    public static HashMap<Integer,String> container; // A HashMap storing file IDs and corresponding file names
    public HashMap<String,ArrayList<Socket>> trusted; // A HashMap storing the list of trusted clients for each file
    public static int poolSize;
    //private int nbClients;
    //private static ArrayList<String> usedTokens;

    /**
     * Server's constructor
     * @param port port number
     * @param plSize number of thread allowed
     * @throws IOException If there is an error creating the ServerSocket
     */
    public Server(int port, int plSize) throws IOException {
        this.trusted = new HashMap<>();
        this.pool = Executors.newFixedThreadPool(plSize);
        poolSize = plSize;
        container = new HashMap<>();
        int id = 0;
        // Set the default directory to current one
        System.setProperty("user.dir", ".");
        String[] files = new File(".").list();
        assert files != null;
        for (String file : files){
            id = id + 1;
            container.put(id,file);
        }
        this.serverSocket = new ServerSocket(port);
        //this.nbClients = 1;
        //usedTokens = new ArrayList<>();

    }
    /*
    public void setNbClients(int nbClients) {
        this.nbClients = nbClients;
    }

     */

    /**
     * Manages the server by launching slaves
     */
    public void manageRequest() {
        int nb;
        while(true) {
            try {
                // On compte le nombre de requêtes actives
                nb = ((ThreadPoolExecutor)pool).getActiveCount();
                System.out.println("Le nombre de requêtes est " + nb);
                System.out.println("Waiting for connection...");
                Socket client = this.serverSocket.accept();
                System.out.println("Accepted connection from " + client.getInetAddress().getHostAddress());

                ObjectOutputStream output_client_obj = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream input_client_obj = new ObjectInputStream(client.getInputStream());

                //output_client_obj.writeObject("-");
                System.out.println("J'envoie la liste de fichiers !");
                output_client_obj.writeObject(Server.container);

                // Fichier choisi par le client
                String request = null;
                request = input_client_obj.readObject().toString();
                if(nb <= poolSize-1) {
                    pool.submit(new Slave(client, 1000, input_client_obj, output_client_obj, trusted, request));
                }
                else{
                    int index = 0;
                    System.out.println(" Too much threads !");
                    //String request = input_client_obj.readObject().toString();
                    // On récupère les clients de confiance pour le fichier request
                    if(trusted.get(request) != null) {
                        ArrayList<Socket> a = trusted.get(request);
                        // on génère un nombre aléatoire pour choisir dans la liste
                        Random rand = new Random();
                        if(a.size() != 1) {
                            index = rand.nextInt(0, a.size() - 1);
                        }
                        Socket s = a.get(index);
                        // On ouvre les canaux
                        ObjectOutputStream to = new ObjectOutputStream(s.getOutputStream());
                        //ObjectInputStream ti = new ObjectInputStream(s.getInputStream());
                        // I ask the client if he can support the server
                        //ti.readObject();
                        to.writeObject("?");
                        // I collect the client's response
                        /*
                        String response = (String) ti.readObject();
                        if (!response.equals("No")) {
                            String token = (String) ti.readObject();
                            //usedTokens.add(token);
                            System.out.println("The token is " + token);
                        }

                         */
                    }
                }
                //else{
                    //System.out.println("Hey !");
                //}
                    /*
                    // I ask the client if he can support the server
                    output_client_obj.writeObject("?");
                    // I collect the client's response
                    String response = (String) input_client_obj.readObject();
                    if(! response.equals("No")){
                        String token = (String) input_client_obj.readObject();
                        usedTokens.add(token);
                    }

                     */
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException  {
        System.out.println("Server World");
        Server server = new Server(12345,2);
        server.manageRequest();
    }
}