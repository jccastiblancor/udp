import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This program demonstrates how to implement a UDP client program.
 *
 *
 * @author www.codejava.net
 */
public class client {
    public static int bufferSize=512;
    public static void main(String[] args) {


        int port = 17;

        try {
            InetAddress address = InetAddress.getByName("54.237.42.236") ;
            DatagramSocket socket = new DatagramSocket();
            System.out.println("Connected");
            writeLog("Connected");
            while (true) {




                DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);
                socket.send(request);
                writeLog("Hello Sent!");
                byte[] buffer = new byte[512];
                DatagramPacket response= new DatagramPacket(buffer, buffer.length);
                socket.receive(response);
                String name_checksum= new String (buffer,0,response.getLength());

                String[] splitted= name_checksum.split(",");
                System.out.println(name_checksum);

                String name= splitted[0];
                String checksum=splitted[1];
                writeLog("Prepared to recieve file "+ name+" with checksum "+checksum );

                //buffer = new byte[512];
                //response = new DatagramPacket(buffer, buffer.length);
                //socket.receive(response);

                ////////////////
                int count;
                int total=0;
                FileOutputStream outFile = new FileOutputStream("./" + name);
                //String quote = new String(buffer, 0, response.getLength());
                byte[] receiveData = new byte[1000000];
                while (receiveData != null) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    count= receivePacket.getLength();
                    outFile.write(receivePacket.getData(), 0, count);
                    total+=count;
                    System.out.printf("RECEIVED: %s ", new String(receivePacket.getData()));
                }

                //String myCheck=getFileChecksum()
                //System.out.println(quote);

                System.out.println();

                Thread.sleep(10000);
            }

        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    public static void writeLog(String msj) throws IOException {
        FileWriter fw = new FileWriter("./clientLog_" + bufferSize+ ".txt", true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        fw.write(dtf.format(now) + msj + "\n");

        fw.close();
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
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
