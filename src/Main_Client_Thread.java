import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
/**
 * This class represents a client-side worker that handles communication with the server
 * for downloading parts of a file in a multithreaded environment.
 */
public class Main_Client_Thread implements Runnable{
    private ExecutorService executor;
    private Socket client;
    private ObjectOutputStream output_client;
    private ObjectInputStream  input_client;
    private int debut;
    private int fin;
    private int x; // id of the wanted file
    private String finalString; // Is going to contain the file received
    private int numBlock; // Number associated to each thread (0 for the main)
    private static ArrayList<String> l; // Is going to contain all the file parts that the threads receive
    /**
     * Constructor to initialize the client thread with necessary data.
     * @param x File ID requested
     * @param debut Start byte index for file chunk
     * @param fin End byte index for file chunk
     * @param numBlock Block number for this request
     */
    public Main_Client_Thread(int x, int debut, int fin, int numBlock) {
        try {
            this.executor = Executors.newFixedThreadPool(6);
            client = new Socket("127.0.0.1", 12345);
            this.output_client = new ObjectOutputStream(client.getOutputStream());
            this.input_client = new ObjectInputStream(client.getInputStream());
            this.x = x;
            this.debut = debut;
            this.fin = fin;
            this.finalString="";
            this.numBlock = numBlock;
            l = new ArrayList<>();
            l.add("");
            l.add("");
            l.add("");
            l.add("");
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    /**
     * Default constructor for creating a Main_Client_Thread without parameters.
     * @throws IOException If an I/O error occurs
     */
    public Main_Client_Thread() throws IOException {
        this(0, 0, -1,0);
    }

    /**
     * Run method for executing the client-side thread.
     * It connects to the server and processes the file chunks.
     */
    @Override
    public void run() {
        System.out.println(" Je suis un thread !");

        try{
            // On initialise la socket
            this.output_client.writeObject("-1");
            // On lit ce qu'on reçoit,
            // On pourrait directement cast, car on est censer connaitre les types reçus
            // On pourrait donc faire : ArrayList<Integer> liste = ArrayList<Integer> request.
            Object req = this.input_client.readObject();

            this.output_client.writeObject(x);

            ArrayList<Integer> offsets = new ArrayList<>();
            offsets.add(debut);
            offsets.add(fin);
            this.output_client.writeObject(offsets);

            @SuppressWarnings("unchecked")
            HashMap<Integer, String> request = (HashMap<Integer, String>) req;
            String s = "__" + request.get(x);
            String finale ="";
            System.out.println(s);
            client.setSoTimeout(500);
            try {
                while (true) {
                    Object req2 = this.input_client.readObject();
                    // We transform the byte Array into a String using the UTF-8 standard
                    String s2 = new String((byte[]) req2, StandardCharsets.UTF_8);
                    // We merge the different texts received
                    finale = finale + s2;
                }
            }
            catch(SocketTimeoutException sck){
                System.out.println(" Temps d'attente écoulé !");
                System.out.println("J'ai reçu : " + finale + " | And I am the thread number " + numBlock);
                // We close the connection
                output_client.writeObject("0");
            }
            finalString = finalString + finale;
            l.set(this.numBlock-1,finalString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Method to manage the client-server interaction and file download process.
     */
    public void manage(){
        while(true){
        try {
            if (Objects.equals((String) this.input_client.readObject(), "?")){
                // The server needs me as a worker !
                double value = Math.random();
                if(value < 0.5){
                    // I refuse

                }
            }

            // On initialise la socket
            this.output_client.writeObject("-1");
            // On lit ce qu'on reçoit,
            // On pourrait directement cast, car on est censer connaitre les types reçus
            // On pourrait donc faire : ArrayList<Integer> liste = ArrayList<Integer> request.
            Object req = this.input_client.readObject();
            @SuppressWarnings("unchecked")
            HashMap<Integer, String> request = (HashMap<Integer, String>) req;
            request.forEach((k, v) -> {
                System.out.println("J'ai reçu " + v + " - " + k);
            });

            this.output_client.writeObject("Client");

            // If no file chosen
            while (x == 0) {
                Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                System.out.println("Enter file id");

                String fileId = myObj.nextLine();  // Read user input
                x = Integer.parseInt(fileId);
                System.out.println("file id chosen is: " + x);  // Output user input
            }

            // I need to send the file that the client wants
            this.output_client.writeObject(x);

            executor.submit(new Main_Client_Thread(x, 0, 10000, 1));
            executor.submit(new Main_Client_Thread(x, 10000, 20000, 2));
            executor.submit(new Main_Client_Thread(x, 20000, 30000, 3));
            executor.submit(new Main_Client_Thread(x, 30000, -1, 4));
            executor.awaitTermination(2, TimeUnit.SECONDS);
            String f = "";
            for(String st : this.l){
                f = f + st;
            }

            byte[] t = f.getBytes();
            // We clean the byte array (removing the null elements)
            t = cleanByteArray(t);
            // We write the byte array into the requested file
            System.out.println(request.get(x));
            String requestedFile = "__"+request.get(x);
            FileOutputStream fl = new FileOutputStream(requestedFile);
            fl.write((t));

            this.output_client.writeObject(String.valueOf(x));
            // we generate the hashcode of the file
            byte[] bytesOfMessage = Files.readAllBytes(Paths.get(requestedFile));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] theMD5digest = md.digest(bytesOfMessage);
            // We send the hashCode
            output_client.writeObject(theMD5digest);

            // We set x to 0 so that the user can input a value again
            x = 0;
        }
        catch (Exception e) {
            System.out.println(e);
        }}


    }

    /**
     * Cleans the byte array by removing any null (0) values.
     * @param b The byte array to clean
     * @return A new byte array without null values
     */
    public byte[] cleanByteArray(byte[] b){
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (Byte bt : b){
            if( bt != 0){
                o.write(bt);
            }
        }
        return o.toByteArray();
    }

    //public byte[] cleanByteArray(byte[])
    public static void main(String[] args) throws IOException {
        String filename = null;

        // This block of code (to handle parameters) has been generated by AI
        for (String arg : args) {
            if (arg.startsWith("--file=")) {
                filename = arg.substring(7); // Extrait la partie après "--file="
                break;
            }
            if (arg.startsWith("--DC=")) {
                filename = arg.substring(4); // Extrait la partie après "--file="
                break;
            }
        }
        if (filename != null) {
            // Utiliser la valeur du fichier ici
            System.out.println("Fichier spécifié : " + filename);
        } else {
            System.out.println("Aucun fichier spécifié");
        }

        System.out.println("Client_Thread ! " );
        Main_Client_Thread m = new Main_Client_Thread();
        m.manage();

    }
}


