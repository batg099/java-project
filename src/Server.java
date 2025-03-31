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

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private int port;
    private int poolSize;
    private boolean isFinished;
    private boolean isRunning;
    // A HashMap of File_id/File_name
    public static HashMap<Integer,String> container;
    // A HashMap of File/Trusted clients
    private HashMap<String,ArrayList<Socket>> trusted;

    public Server(int port, int poolSize) {
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

        try{
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void manageRequest() {
        while(true) {
            try {
                System.out.println("Waiting for connection...");
                Socket client = this.serverSocket.accept();
                System.out.println("Accepted connection from " + client.getInetAddress().getHostAddress());

                ObjectInputStream input_client_obj = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream output_client_obj = new ObjectOutputStream(client.getOutputStream());

                pool.submit(new Slave(client,1000,input_client_obj,output_client_obj, trusted));
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
