import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This thread is responsible to handle client connection.
 */
public class ServerThread extends Thread {
    private Socket socket;
    private InputStream input;
    private BufferedReader reader;
    private OutputStream output;
    private PrintWriter writer;
    private boolean isReady;
    private int index;
    private String logName;
    private int bufferSize;


    public ServerThread(Socket socket, int index, String logName, int bufferSize) {
        this.socket = socket;
        this.isReady = false;
        this.index = index;
        this.logName = logName;
        this.bufferSize = bufferSize;
    }

    public void run() {

        try {
            input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            writer.println("Hello");

            String text;

            do {
                text = reader.readLine();
                String read = new StringBuilder(text).toString();

                if (read.equals("Ready")) {
                    isReady = true;
                    writeLog(" client with Id: " + index + " is ready to receive data");
                }
                if (read.equals("OK")){
                    System.out.println("Confirmacion Recivida");
                }

            } while (!text.equals("bye"));


            writeLog(" client with Id: " + index + " disconnected");
            System.out.println("Conexion cerrada");
            socket.close();

        } catch (IOException ex) {
            System.out.println("Excepcion del Servidor: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendFile(File file) throws IOException, NoSuchAlgorithmException {
        if (isReady) {
            System.out.println("Usuario " + index + "listo");
            // Envia el nombre del archivo que se va a mandar
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            String shaChecksum = getFileChecksum(shaDigest, file);

            writer.println("Usuario" + index + file.getName()+","+shaChecksum);
            writeLog("User " + index + " File checksum: " + shaChecksum);

            byte[] bytes = new byte[bufferSize * 1024];
            InputStream in = new FileInputStream(file);
            writeLog("User " + index +" Buffer size: " + bytes.toString());

            int count;
            long startTime = System.nanoTime();
            while ((count = in.read(bytes)) > 0) {
                output.write(bytes, 0, count);
            }

            long elapsedTime = System.nanoTime() - startTime;
            System.out.println("User " + index + ": Tiempo para enviar el archivo: "
                    + elapsedTime/1000000000+ "s");
            System.out.println("User " + index + ": Archivo Enviado!");
            writeLog("User " + index + " Elapsed time: " + elapsedTime/1000000000 + " s");
            socket.shutdownOutput();
            //output.close();
        }

    }

    public void writeLog(String msj) throws IOException {
        FileWriter fw = new FileWriter(logName, true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        fw.write(dtf.format(now) + msj + "\n");

        fw.close();
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