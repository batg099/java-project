import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Server class: Handles client connections and distributes tasks to threads.
 */
public class Server {
    private final ServerSocket serverSocket; // ServerSocket to listen for incoming client connections
    private final ExecutorService pool; // ExecutorService to manage a pool of threads handling client requests concurrently
    public static HashMap<Integer,String> container; // A HashMap storing file IDs and corresponding file names
    public HashMap<String,ArrayList<Socket>> trusted; // A HashMap storing the list of trusted clients for each file
    private int poolSize;

    /**
     * Server's constructor
     * @param port port number
     * @param poolSize number of thread allowed
     * @throws IOException If there is an error creating the ServerSocket
     */
    public Server(int port, int poolSize) throws IOException {
        this.trusted = new HashMap<>();
        this.pool = Executors.newFixedThreadPool(poolSize);
        this.poolSize = poolSize;
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

    }
    /**
     * Manages the server by launching slaves
     */
    public void manageRequest() {
        int nbClients = 0;
        while(true) {
            try {
                if(nbClients <= poolSize) {
                    System.out.println("Waiting for connection...");
                    Socket client = this.serverSocket.accept();
                    System.out.println("Accepted connection from " + client.getInetAddress().getHostAddress());

                    ObjectOutputStream output_client_obj = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream input_client_obj = new ObjectInputStream(client.getInputStream());

                    pool.submit(new Slave(client, 1000, input_client_obj, output_client_obj, trusted));
                    nbClients = nbClients + 1;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException  {
        System.out.println("Server World");
        Server server = new Server(12345,3);
        server.manageRequest();
    }
}