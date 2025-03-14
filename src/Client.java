import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1",2000 );
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("What are the files available?");
            String c = in.readUTF();
            System.out.println(c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
