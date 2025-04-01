import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

// Remarque, ça va prendre 2 secondes à afficher les informations, car on fait un Thread.sleep(2000) dans le serveur

public class Main_Client_Thread implements Runnable{
    private ExecutorService executor;
    private Socket client;
    private ObjectOutputStream output_client;
    private ObjectInputStream  input_client;
    private HashMap<Integer,Object> position;
    private int debut;
    private int fin;
    private int x;
    private String finalString;
    private int numBlock;
    private static ArrayList<String> l;
    public Main_Client_Thread(int x, int debut, int fin, int numBlock) throws IOException {
        try {

            this.executor = Executors.newFixedThreadPool(6);
            client = new Socket("127.0.0.1", 12345);
            this.output_client = new ObjectOutputStream(client.getOutputStream());
            this.input_client = new ObjectInputStream(client.getInputStream());
            this.position = new HashMap<Integer,Object>();
            this.x = x;
            this.debut = debut;
            this.fin = fin;
            this.finalString="";
            this.numBlock = numBlock;
            this.l = new ArrayList<String>();
            l.add("");
            l.add("");
            l.add("");
            l.add("");
            //this.req = req;
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    public Main_Client_Thread() throws IOException {
        this(0, 0, -1,0);
    }

    @Override
    public void run() {
        System.out.println(" Je suis un thread !");

        try{
            // On initialise la socket
            this.output_client.writeObject("-1");
            // On lit ce qu'on reçoit
            // On pourrait directement cast, car on est censer connaitre les types reçues
            // On pourrait donc faire : ArrayList<Integer> liste = ArrayList<Integer> request;
            Object req = this.input_client.readObject();

            this.output_client.writeObject(x);

            ArrayList<Integer> offsets = new ArrayList<Integer>();
            offsets.add(debut);
            offsets.add(fin);
            this.output_client.writeObject(offsets);

            //this.output_client.writeObject(this.numBlock);

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
            }
            //System.out.println(finale);
            finalString = finalString + finale;
            l.set(this.numBlock,finalString);
            //System.out.println(finalString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void manage(){
        try {
            // On initialise la socket
            this.output_client.writeObject("-1");
            // On lit ce qu'on reçoit
            // On pourrait directement cast, car on est censer connaitre les types reçues
            // On pourrait donc faire : ArrayList<Integer> liste = ArrayList<Integer> request;
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

            executor.submit(new Main_Client_Thread(x, 0, 1287,0));
            executor.submit(new Main_Client_Thread(x, 1287, 2000,1));
            executor.submit(new Main_Client_Thread(x, 2000, 3000,2));
            executor.submit(new Main_Client_Thread(x, 3000, -1,3));
            executor.awaitTermination(2, TimeUnit.SECONDS);
            String f = "";
            for(String st : this.l){
                f = f + st;
            }
            System.out.println("----------------------------------");
            System.out.println(f);


            byte[] t = f.getBytes();
            // We clean the byte array (removing the null elements)
            t = cleanByteArray(t);
            //System.out.println("---" + Arrays.toString(t));
            // We write the byte array into the requested file
            System.out.println(request.get(x));
            String requestedFile = "__"+request.get(x);
            FileOutputStream fl = new FileOutputStream(requestedFile);
            fl.write((t));


            //System.out.println(position.toString());

                //ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            /*
            // If it takes more than 0.5 seconds to receive => we stop reading
            client.setSoTimeout(500);
            try {
                // We can only read a maximum of (20 * blockSize) byte
                while(true) {
                    Object req2 = this.input_client.readObject();
                    // We transform the byte Array into a String using the UTF-8 standard
                    String s = new String((byte[]) req2, StandardCharsets.UTF_8);
                    // We merge the different texts received
                    finale =  finale + s;

                }
            }
            catch(SocketTimeoutException e){
                System.out.println(" Temps d'attente écoulé !");
            }

            */
            //System.out.println("habibi " + finale);
            // We retransform the final string into a byte array
            //byte [] t = finale.getBytes();
            /*
            byte[] t = finalString.getBytes();
            // We clean the byte array (removing the null elements)
            t = cleanByteArray(t);
            //System.out.println("---" + Arrays.toString(t));
            // We write the byte array into the requested file
            FileOutputStream fl = new FileOutputStream(requestedFile);
            fl.write((t));
            */

            this.output_client.writeObject(String.valueOf(x));
            // we generate the hashcode of the file
            byte[] bytesOfMessage = Files.readAllBytes(Paths.get(requestedFile));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] theMD5digest = md.digest(bytesOfMessage);

            //System.out.println("My hashcode is"  + Arrays.toString(theMD5digest));
            // We send the hashCode
            output_client.writeObject(theMD5digest);



        }
        catch (Exception e) {
            System.out.println(e);
        }

    }

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
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--file=")) {
                filename = args[i].substring(7); // Extrait la partie après "--file="
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


