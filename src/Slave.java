import java.io.*;
import java.net.Socket;

public class Slave implements Runnable {
    private Socket socket;

    public Slave(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        System.out.println("I am taking care of the client !");
        try {
            DataOutputStream output = new DataOutputStream(this.socket.getOutputStream());
            output.writeUTF("Hello, this is the server !");

            DataInputStream input = new DataInputStream(this.socket.getInputStream());
            System.out.println(input.readUTF());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("I am the slave !");
    }
}
