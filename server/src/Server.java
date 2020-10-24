import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Server {

    int port;
    ArrayList<ServerThread> conexiones;
    int currentId;
    String logName;
    int bufferSize;

    public Server(String logName, String bufferSize) {
        port = 5000;
        conexiones = new ArrayList<>();
        currentId = 0;
        this.logName = "./" + logName + "_" + bufferSize + ".txt";
        this.bufferSize =  Integer.parseInt(bufferSize);
    }

    public void main() {
        System.out.println("Server log: " + logName);
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            writeLog(" Server is listening on port " + port);
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected, assigned Id: " + currentId);
                writeLog(" New client connected, assigned Id: " + currentId);

                // Agrega la conexion a la lista de conexiones activas
                ServerThread thread = new ServerThread(socket, currentId, logName, bufferSize);
                thread.start();
                conexiones.add(thread);

                InputThread consoleInput = new InputThread(conexiones, logName);
                consoleInput.start();

                currentId++;
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    public void writeLog(String msj) throws IOException {
        FileWriter fw = new FileWriter(logName, true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        fw.write(dtf.format(now) + msj + "\n");

        fw.close();
    }

    public static void main(String[] args) {
        System.out.println(args);
        System.out.println(args.length);
        Server servidor;
        if (args.length ==1 ){
            System.out.println(args[0]);
            servidor = new Server("log",args[0]);
        } else {
            servidor = new Server("log", "4");
        }

        servidor.main();
    }

}
