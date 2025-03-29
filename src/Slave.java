import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Slave  implements Runnable {
    private final Socket socket;
    private final int blockSize;

    public static void main(String[] args) {
        System.out.println("Slave World");
    }

    public Slave(Socket client, int blockSize){
        this.socket = client;
        this.blockSize = blockSize;
    }

    public void run() {
        try {
            ObjectInputStream input_client_obj = new ObjectInputStream(socket.getInputStream());
            //DataOutputStream output_client = new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream output_client_obj = new ObjectOutputStream(socket.getOutputStream());

            //output_client_obj.writeObject("Hi Client !");
            String request = null;
            while(request != "0"){

                request = input_client_obj.readObject().toString();
                System.out.println("Hi - coco");
                //System.out.println("My request is " + request);
                //ArrayList<Teacher> liste = new ArrayList<>();

                switch(request) {
                    case "0":
                        System.out.println("Le client a tapé 0 !");
                        this.socket.close();
                        break;
                    case "1":
                        System.out.println("Le client a tapé 1 !");
                        // output_client_obj.writeObject("La liste des Id est: " + Server.getListId());


                        output_client_obj.writeObject(Server.container);
                        break;
                    // Cas ou le client demande le salaire
                    case "5":
                        System.out.println("Le client a tapé 5 !");
                        byte[] test = writeFile(request,blockSize, output_client_obj);
                        output_client_obj.writeObject(test);
                        break;
                    case "3":
                        String t2 = input_client_obj.readObject().toString();
                        int id2 = Integer.parseInt(t2);

                        System.out.println("Le client a tapé 3 !");
                        output_client_obj.writeObject("Server.getName(id2)");

                }

            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
    public byte[] writeFile(String request, int blockSize, ObjectOutputStream d ) throws IOException {
        //System.out.println(Server.container.get(file));
        File file = new File(Server.container.get(Integer.parseInt(request)));
        byte[] b = Files.readAllBytes(file.toPath());
        //System.out.println("Le salaire du professeur est : " + Server.getSalary(1));
        return b;
    }



}
