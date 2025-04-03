import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
/**
 * The Slave class represents a worker that handles communication with the client.
 * It processes client requests, such as sending files or verifying file integrity.
 */
public class Slave  implements Runnable {
    private final Socket socket;
    private final int blockSize;
    private final ObjectInputStream input_client_obj;
    private final ObjectOutputStream output_client_obj;
    public HashMap<String,ArrayList<Socket>> trusted; // A HashMap of File/Trusted clients

    /**
     * Constructor for the Slave class.
     * @param client The client socket
     * @param blockSize The block size for file transfers
     * @param i The input stream from the client
     * @param o The output stream to the client
     * @param trusted The HashMap of trusted clients
     */
    public Slave(Socket client, int blockSize, ObjectInputStream i, ObjectOutputStream o, HashMap<String,ArrayList<Socket>> trusted){
        this.socket = client;
        this.blockSize = blockSize;
        this.input_client_obj = i;
        this.output_client_obj = o;
        this.trusted = trusted;
    }

    /**
     * Run method for handling client requests in a loop.
     * It processes requests such as getting the list of files, sending files, or verifying file integrity.
     */
    public void run() {
        try {
            String request = null;
            String fileName = null;
            do {
                output_client_obj.writeObject("?");
                // We listen from the client
                request = input_client_obj.readObject().toString();
                switch (request) {
                    case "0":
                        System.out.println("Le client veut arrêter la connection !");
                        this.socket.close();
                        break;
                    case "-1":
                        System.out.println("Le client veut connaitre la liste des fichiers !");
                        output_client_obj.writeObject(Server.container);
                        break;
                    default:
                        System.out.println("Le client veut le fichier " + request);

                        // If thread
                        if (!(request.equals("Client"))) {
                            fileName = Server.container.get(Integer.valueOf(request));
                            Object oi = input_client_obj.readObject();
                            @SuppressWarnings("unchecked")
                            ArrayList<Integer> offsets = (ArrayList<Integer>) oi;
                            System.out.println("I received " + oi.toString());

                            File file = new File(request);
                            int hashServer = file.hashCode();
                            // We send the file
                            writeFile(Files.readAllBytes(Paths.get(fileName)),
                                    offsets.get(0), offsets.get(1));
                        } else {
                            // We want the file's name
                            Object file = input_client_obj.readObject();
                            fileName = Server.container.get((Integer) file);
                            // We wait for the client's file
                            Object oz = input_client_obj.readObject();
                            // If (our file's MD5) = (the client's MD5)
                            if (verifyMD5((String) oz)) {
                                // We add the client into the trusted list for the requested file
                                addTrusted(fileName);
                            }
                            System.out.println(trusted.toString());
                        }
                        break;
                }

            } while (!request.equals("0"));
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * Writes file content to the client in chunks.
     * @param file The file content as a byte array
     * @param debut The start byte index
     * @param fin The end byte index
     * @throws IOException If writing the file fails
     */
    public void writeFile(byte[] file, int debut, int fin) throws IOException {
        if(fin == -1) fin = file.length ;
        if(debut > file.length) return;
        if(fin - debut > file.length){
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            b.write(file, debut, file.length);
            output_client_obj.writeObject(b.toByteArray());
        }
        else {
            for (int i = debut; i <= fin; i = i + blockSize) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                if (i + blockSize > fin) {
                    b = new ByteArrayOutputStream();
                    b.write(file, i, fin - i );
                    output_client_obj.writeObject(b.toByteArray());
                    break;
                }
                b.write(file, i, blockSize);
                output_client_obj.writeObject(b.toByteArray());
            }
        }
    }
    /**
     * Adds the client into the trusted list for the requested file
     * This method is synchronized because we modify a static element (trusted)
     * @param request The requested file
     */
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
    /**
     * Verifies if the MD5 checksum of the file on the server matches the MD5 checksum provided by the client.
     * @param id The file ID to verify
     * @return true if the checksums match, otherwise false
     * @throws IOException If file reading fails
     * @throws ClassNotFoundException If object deserialization fails
     * @throws NoSuchAlgorithmException If MD5 algorithm is not available
     */
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

        // We verify if it's equal to what the client sent
        if (Arrays.equals(theMD5digest,(byte[])hashClient)){
            System.out.println("The file has been successfully received");
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        System.out.println("Slave World");
    }
}
