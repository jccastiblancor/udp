import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

public class clientTest extends Thread {
    private static String ip = "54.234.96.150";
    private static int port = 5000;
    private static int bufferSize = 4;

    public clientTest(){

    }
    public void run(){

        System.out.println("This code is outside of the thread");
        Socket sock = null;
        try {
            sock = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(sock.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fromServer = "";

        try {
            if ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Hello")) {
                    writeLog(" Server: Hello");
                    writeLog(" Client ready to receive file");
                    out.println("Ready");
                    fromServer = in.readLine();
                    if (fromServer.contains(".")) {
                        String[] splitted= fromServer.split(",");

                        fromServer=splitted[0];
                        String checksum=splitted[1];
                        System.out.println("Recibiendo archivo");
                        System.out.println("Server: " + fromServer);
                        writeLog(" Receiving " + fromServer + " file from server");
                        Random rand = new Random();
                        int act= rand.nextInt(500);
                        FileOutputStream outFile = new FileOutputStream("./" + fromServer+act);
                        InputStream inFile = sock.getInputStream();

                        int count;
                        byte[] buffer = new byte[bufferSize * 1024]; // or 4096, or more
                        int total= 0;
                        int i=0;
                        long startTime = System.nanoTime();
                        long elapsedTime;
                        while ((count = inFile.read(buffer)) >0) {
                            outFile.write(buffer, 0, count);
                            total+=count;
                            if (i%1000==0) {
                                System.out.println("recibido:" + total/(Math.pow(10,6))+"MB");
                                elapsedTime = System.nanoTime() - startTime;
                                writeLog(" File transmission, time elapsed: " +  elapsedTime/1000000000 + "s");
                                writeLog(" File transmission, bytes received: " +  total/(Math.pow(10,6))+"MB");
                            }
                            i++;
                        }
                        elapsedTime = System.nanoTime() - startTime;
                        System.out.println("Tiempo para recibir el archivo: "
                                + elapsedTime/1000000000+ "s");
                        writeLog(" File transmission finished, time elapsed: " +  elapsedTime/1000000000 + "s");
                        writeLog(" File transmission, total bytes received: " +  total/(Math.pow(10,6))+"MB");

                        System.out.println("Archivo recibido");
                        out.println("OK");
                        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");

                        //SHA-1 checksum
                        File file = new File("./" + fromServer+act);
                        String shaChecksum = getFileChecksum(shaDigest,file );

                        if (shaChecksum.equals(checksum)){
                            System.out.println("Integridad: OK");
                            writeLog(" file integrity ok");
                        }else{
                            System.out.println("Integridad: F");
                            writeLog(" file integrity F super ded :c");
                        }

                        System.out.println("Cerrando conexión");
                        writeLog(" Closing connection to server");

                        out.println("bye");
                        outFile.close();
                        inFile.close();

                    }

                    // out.println("Bye");
                    out.close();
                    in.close();
                    sock.close();
                    System.out.println("Conexión cerrada");

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public  void writeLog(String msj) throws IOException {
        FileWriter fw = new FileWriter("./clientLog_" + bufferSize + ".txt", true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        fw.write(dtf.format(now) + msj + "\n");

        fw.close();
    }

    private  String getFileChecksum(MessageDigest digest, File file) throws IOException
    {

        FileInputStream fis = new FileInputStream(file);


        byte[] byteArray = new byte[1024];
        int bytesCount = 0;


        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };


        fis.close();


        byte[] bytes = digest.digest();


        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
