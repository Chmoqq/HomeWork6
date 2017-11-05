package Lesson6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    public MyServer() {
        Thread t1 = new Thread(() -> {
            ServerSocket server = null;
            Socket s = null;
            try {
                server = new ServerSocket(8189);
                System.out.println("Server created. Waiting for client...");
                while (true) {
                    s = server.accept();
                    System.out.println("Client connected");
                    new Thread(new ClientHandler(s)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    server.close();
                    System.out.println("Server closed");
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }
}
