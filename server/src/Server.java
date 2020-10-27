import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class Server {
    private DatagramSocket socket;
    private List<String> listQuotes = new ArrayList<String>();
    private Random random;

    public Server(int port) throws SocketException, IOException {
        socket = new DatagramSocket(port);
        random = new Random();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: Server <file> <port>");
            return;
        }

        String quoteFile = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Server server = new Server(port);
            server.loadQuotesFromFile(quoteFile);
            //writeLog(" Server is listening on port " + port);
            System.out.println("Server is listening on port " + port);
            server.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void service() throws IOException, NoSuchAlgorithmException {
        while (true) {
            DatagramPacket request = new DatagramPacket(new byte[1], 1);
            socket.receive(request);
            System.out.println("Conectado");

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            File file = new File("./Quotes.txt");
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            String shaChecksum = getFileChecksum(shaDigest, file);
            String send =  file.getName()+","+shaChecksum;
            DatagramPacket sendPacket = new DatagramPacket(send.getBytes(), send.length(), clientAddress, clientPort);
            socket.send(sendPacket);
            int numberPackets = (int) Math.ceil((double) file.length()/(double) 512);

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] sendData = new byte[512];
            long current = 0;
            // Se inicia transmision del archivo hasta que se envien todos los paquetes
            while(current != numberPackets) {
                sendData = new byte[512];
                bis.read(sendData);
                sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                socket.send(sendPacket);
                current++;
            }

        }




    }


    private void loadQuotesFromFile(String quoteFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(quoteFile));
        String aQuote;

        while ((aQuote = reader.readLine()) != null) {
            listQuotes.add(aQuote);
        }

        reader.close();
    }

    private String getRandomQuote() {
        int randomIndex = random.nextInt(listQuotes.size());
        String randomQuote = listQuotes.get(randomIndex);
        return randomQuote;
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();
        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }


}