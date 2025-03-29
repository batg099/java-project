import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Slave  implements Runnable {
    private final Socket socket;

    public static void main(String[] args) {
        System.out.println("Slave World");
    }

    public Slave(Socket client){
        this.socket = client;

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
                    case "2":
                        System.out.println("Le client a tapé 2 !");
                        //System.out.println("Le salaire du professeur est : " + Server.getSalary(1));
                        String t = input_client_obj.readObject().toString();
                        int id = Integer.parseInt(t);
                        output_client_obj.writeObject("Server.getSalary(id)");
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



}
