package Lesson6;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket s;
    private PrintWriter out;
    private Scanner in;
    private static int CLIENTS_COUNT = 0;
    private String name;

    public ClientHandler(Socket s) {


        try {
            this.s = s;
            out = new PrintWriter(s.getOutputStream(), true);
            in = new Scanner(s.getInputStream());
            CLIENTS_COUNT++;
            name = "Client #" + CLIENTS_COUNT;
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        while (true) {
            if (in.hasNext()) { // hasNext() - передает ли сервер какие-то данные
                String w = in.nextLine(); // Считывание этих данных
                System.out.println(name + ": " + w); // Печать данных
                out.println("echo: " + w);
                if (w.equalsIgnoreCase("END")) break;
            }
        }
        try {
            System.out.println("Client disconnected");
            // MyServer.clients.remove(this);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        boolean contains = msg.contains("end");
        if (!contains) {
            out.println(msg);
        } else {
            System.out.println("System interrupted connection");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}

