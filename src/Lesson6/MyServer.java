package Lesson6;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MyServer {

    private Scanner in;
    private PrintWriter out;

    public MyServer() {
        Thread t1 = new Thread(() -> {
            System.out.println("test");
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMsg();
                }
            }).start();
        });
        t1.start();
    }

    void sendMsg() {
        while (true) {
            if (!System.console().readLine().trim().isEmpty()) {
                String w = System.console().readLine();
                out.println(w);
                out.flush();
            }
        }
    }
}
