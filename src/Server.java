import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * Server class: Handles client connections and distributes tasks to threads.
 */
public class Server {
    private ServerSocket serverSocket; // ServerSocket to listen for incoming client connections
    private ExecutorService pool; // ExecutorService to manage a pool of threads handling client requests concurrently
    private int port; // Port number for the server to listen on
    private int poolSize; // Size of the thread pool
    private boolean isFinished; // Flag to check if the server has finished its tasks
    private boolean isRunning; // Flag to check if the server is running
    public static HashMap<Integer,String> container; // A HashMap storing file IDs and corresponding file names
    public HashMap<String,ArrayList<Socket>> trusted; // A HashMap storing the list of trusted clients for each file

    /**
     * Server's constructor
     * @param port port number
     * @param poolSize number of thread allowed
     * @throws IOException If there is an error creating the ServerSocket
     */
    public Server(int port, int poolSize) throws IOException {
        this.pool = Executors.newFixedThreadPool(poolSize);
        this.port = port;
        this.poolSize = poolSize;
        this.trusted = new HashMap<String,ArrayList<Socket>>();
        container = new HashMap<Integer,String>();
        int id = 0;
        // Set the default directory to current one
        System.setProperty("user.dir", ".");
        for (String s : new File(".").list()){
            id = id + 1;
            container.put(id,s);
        }
        this.serverSocket = new ServerSocket(port);

    }
    /**
     * Manages the server by launching slaves
     */
    public void manageRequest() {
        while(true) {
            try {
                System.out.println("Waiting for connection...");
                Socket client = this.serverSocket.accept();
                System.out.println("Accepted connection from " + client.getInetAddress().getHostAddress());

                ObjectInputStream input_client_obj = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream output_client_obj = new ObjectOutputStream(client.getOutputStream());

                pool.submit(new Slave(client,3,input_client_obj,output_client_obj, trusted));
                //pool.submit(new Slave());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getPoolSize() {
        return poolSize;
    }
    public int getPort() {
        return port;
    }
    public boolean isFinished() {
        return isFinished;
    }

    public ExecutorService getExecutor() {
        return pool;
    }

    public static void main(String[] args) throws IOException  {
        System.out.println("Server World");
        Server server = new Server(12345,10);
        server.manageRequest();
    }
}
