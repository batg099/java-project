
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService executor;
    public Server(int port, int pool){
        try {
            this.serverSocket = new ServerSocket(port);
            this.executor = Executors.newFixedThreadPool(pool);
        } catch (Exception e) {
            System.out.println("Server failed to open port " + port);
        }
    }

    public void manageRequest()
    {
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                this.executor.submit(new Slave(socket));
            } catch (Exception e) {
                System.out.println("Server failed to accept connection");
            }
        }
    }
        public static void main(String[] args) {
        System.out.println("I am the server !");
    }
}
