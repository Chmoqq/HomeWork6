package Lesson6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MyServer {

    private ClientHandler client;

    public static void main(String[] args) {
        MyServer myServer = new MyServer();
    }

    public MyServer() {
            System.out.println("test");
            ServerSocket server = null;
            Socket s = null;
            try {
                server = new ServerSocket(8189);
                System.out.println("Server created. Waiting for client...");

                s = server.accept();
                System.out.println("Client connected");
                client = new ClientHandler(s);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyServer.this.consoleChecker();
                    }
                }).start();
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
    }

    private void consoleChecker() {
        Scanner s = new Scanner(System.in);

        while (true) {
            try {
                if (s.hasNext()) {
                    String message = s.nextLine();
                    String msg = "Console: " + message;
                    client.sendMessage(msg);
                    System.out.println(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
