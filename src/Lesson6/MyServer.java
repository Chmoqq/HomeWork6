package Lesson6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private List<ClientHandler> clients = new ArrayList<>();
    private AuthService authService;

    public static void main(String[] args) {
        new MyServer(new BaseAuthService());
    }

     MyServer(AuthService authService) {
        this.authService = authService;

        Socket s = null;
        ServerSocket server = null;
        try {
            server = new ServerSocket(8189);
            System.out.println("Server created. Waiting for client...");
            while (true) {
                s = server.accept();
                ClientHandler client = new ClientHandler(this, s);
                new Thread(client).start();
                clients.add(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (server != null) server.close();
                System.out.println("Server closed");
                if (s != null) s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

     AuthService getAuthService() {
        return authService;
    }

     void sendBroadcastMessage(String username, String msg) {
        for (ClientHandler c : clients) {
            if (c.isActive())

                c.sendMessage(username, msg);
        }
    }

     boolean sendWhisper(ClientHandler source, String target, String msg) {
        for (ClientHandler c : clients) {
            if (c.isActive() && c.getName().equals(target)) {
                c.sendWhisper(source.getName(), msg);
                return true;
            }
        }

        return false;
    }

     void close(Socket socket) {
        clients.removeIf(clientHandler -> clientHandler.getSocket().equals(socket));
        //FIXME
    }
     String isOnline() {
        List<String> onlineUsers = new ArrayList<>();
        for (ClientHandler c: clients) {
            if (c.isActive()) {
                onlineUsers.add(c.getName());
            }
        }
        if (onlineUsers.size() > 0) {
            return onlineUsers.toString();
        } else {
            return new String("No online users found");
        }
    }
}
