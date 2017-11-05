package Lesson6;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class MyServer {

    private ClientHandler client;

    public MyServer() {
        Thread t1 = new Thread(() -> {
            System.out.println("test");
            ServerSocket server = null;
            Socket s = null;
            try {
                server = new ServerSocket(8189);
                System.out.println("Server created. Waiting for client...");

                s = server.accept();
                System.out.println("Client connected");
                client = new ClientHandler(s);

                new Thread(() -> sendMsg()).start();
                client.run();
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

    private void sendMsg() {
        Scanner s = new Scanner(System.in);

        while (true) {
            if (s.hasNext()) {
                client.sendMessage("Console: " + s.nextLine());
            }
        }
    }
}
