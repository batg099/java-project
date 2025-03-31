import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Slave  implements Runnable {
    private final Socket socket;
    private final int blockSize;
    private final ObjectInputStream input_client_obj;
    private final ObjectOutputStream output_client_obj;
    // A HashMap of File/Trusted clients
    public HashMap<String,ArrayList<Socket>> trusted;



    public static void main(String[] args) {
        System.out.println("Slave World");
    }

    public Slave(Socket client, int blockSize, ObjectInputStream i, ObjectOutputStream o, HashMap<String,ArrayList<Socket>> trusted){
        this.socket = client;
        this.blockSize = blockSize;
        this.input_client_obj = i;
        this.output_client_obj = o;
        this.trusted = trusted;
    }

    public void run() {
        try {
            //output_client_obj.writeObject("Hi Client !");
            String request = null;
            while(request != "0"){

                request = input_client_obj.readObject().toString();
                System.out.println("Hi - coco");
                //System.out.println("My request is " + request);
                //ArrayList<Teacher> liste = new ArrayList<>();

                switch(request) {
                    case "0":
                        System.out.println("Le client veut arrêter la connection !");
                        this.socket.close();
                        break;
                        //
                    case "-1":
                        System.out.println("Le client veut connaitre la liste des fichiers !");
                        // output_client_obj.writeObject("La liste des Id est: " + Server.getListId());
                        output_client_obj.writeObject(Server.container);
                        break;
                    default:
                        System.out.println("Le client veut le fichier " + request);
                        File file = new File(request);
                        int hashServer = file.hashCode();
                        // We send the file
                        writeFile(request,blockSize, output_client_obj);
                        // If (our file's MD5) = (the client's MD5)
                        if(verifyMD5(request)){
                            // We add the client into the trusted list for the requested file
                            addTrusted(request);
                        }
                        System.out.println(trusted.toString());
                        //output_client_obj.writeObject(test);
                        break;
                }

            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
    // add the client into the trusted list for the requested file
    // This method is synchronized because we modify a static element
    public synchronized void addTrusted(String request){
        // If no client associated previously, we create an array list
        trusted.putIfAbsent(request,new ArrayList<Socket>());
        // We get the Array List
        ArrayList<Socket> l = trusted.get(request);
        // We add the client into the list
        l.add(socket);
        // We replace the old Array List with the new one
        trusted.replace(request,l);
    }
    // Verify if the Server's MD5 for the file = the client's one
    public boolean verifyMD5(String id) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        // We receive the hashCode from the client
        Object hashClient = input_client_obj.readObject();
        // We transform the server's file into a byte table
        byte[] bytesOfMessage = Files.readAllBytes(Paths.get(Server.container.get(Integer.valueOf(id))));
        //System.out.println("----" + Arrays.toString(bytesOfMessage));
        // We get the MD5 of the byte table
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytesOfMessage);
        byte[] theMD5digest = md.digest();

        //System.out.println(Arrays.toString((byte[])hashClient));
        // We verify if it's equal to what the client sent
        if (Arrays.equals(theMD5digest,(byte[])hashClient)){
            System.out.println("The file has been successfully received");
            return true;
        }
        return false;
    }
    // Va servir à écrire le fichier dans un tableau de bytes, puis d'envoyer les sizeBlocks
    public void writeFile(String request, int blockSize, ObjectOutputStream d ) throws IOException {
        //System.out.println(Server.container.get(file));
        File file = new File(Server.container.get(Integer.parseInt(request)));
        byte[] b = Files.readAllBytes(file.toPath());
        int current = 0;
        byte[] b2 = new byte[blockSize];
        //System.out.println(b.length);
        // We only send blockSize bytes
        for (int i = 0; i <= b.length - 1; i = i + 1) {
            //System.out.println(i + " et " + (blockSize - 1));
            // If we've reached the necessary number of bytes
            if(i % (blockSize - 1) == 0 && i!= 0){
                //System.out.println("Je rentre !");
                b2[current] = b[i];
                d.writeObject(b2);
                b2 = new byte[blockSize];
                current =0;
            }
            else{
                //System.out.println("Je rentre 2 !");
                b2[current] = b[i];
                current = current + 1;
            }
            // For the last character, the program go in both else and this if
            if(i == b.length - 1){
                //System.out.println("Je rentre 3 !");
                // Previous version : ne laisser que b2
                byte [] b3 = new byte[current];
                int current2 = 0;
                for (int j=0;j<=b2.length-1;j=j+1){
                    if(b2[j] != 0){
                        b3[current2] = b2[j];
                        current2 =current2 +1;
                    }
                }
                d.writeObject(b2);
                break;
            }
        }
        //d.writeObject(b2);
    }
}
