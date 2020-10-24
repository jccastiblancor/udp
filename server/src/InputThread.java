
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class InputThread extends Thread {

    ArrayList<ServerThread> conexiones;
    String logName;

    public InputThread(ArrayList<ServerThread> conexiones, String logName) {
        this.conexiones = conexiones;
        this.logName = logName;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            String[] campos = input.split(";");
            try {
                if (campos[0].equals("send") && campos.length==3){
                    sendFile(Integer.parseInt(campos[1]), campos[2]);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFile(int maxClientes, String archivo) throws IOException, NoSuchAlgorithmException {
        File file = new File("../../../"+archivo);
        int clientesActivos = 0;
        for (ServerThread conexion : conexiones) {
            if (conexion.isAlive()){
                clientesActivos ++;
            }
        }
        int envios = 0;
        if (clientesActivos >= maxClientes){
            writeLog(" Sending  file " + archivo + " to " + clientesActivos + " clients");
            for (ServerThread conexion : conexiones) {
                if (conexion.isAlive() && envios < maxClientes){
                    new Thread() {
                        public void run() {
                            System.out.println("enviando");
                            try {
                                conexion.sendFile(file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    envios++;

                }
            }
        } else {
            System.out.println("Solo hay " + clientesActivos+ " clientes conectados");
            writeLog(" cant send file not enough clients connected ");
        }
    }

    public void writeLog(String msj) throws IOException {
        FileWriter fw = new FileWriter(logName, true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        fw.write(dtf.format(now) + msj + "\n");

        fw.close();
    }
}