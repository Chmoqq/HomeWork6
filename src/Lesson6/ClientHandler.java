package Lesson6;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private MyServer server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name = null;
    private boolean isAuth = false;

    public ClientHandler(MyServer server, Socket socket) {
        this.server = server;
        try {
            this.socket = socket;
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Client handler initialization failed: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                switch (in.readByte()) {
                    case 1:
                        // login
                        String username = in.readUTF();
                        String password = in.readUTF();

                        name = server.getAuthService().getNick(username, password);
                        if (name != null) {
                            isAuth = true;
                            server.sendBroadcastMessage("> [Server] <", name + " зашел в чат!");
                        } else {
                            sendMessage("> [Server] <", "Неверные логин/пароль" + "Идет регистрация нового пользователя...");
                            server.getAuthService().addClient(username, password);
                            sendMessage("> [Server] <", "Пользователь зарегистрирован, пройдите авторизацию повторно");
                        }
                        break;
                    case 2:
                        // message
                        String message = in.readUTF();
                        if (isAuth)
                            server.sendBroadcastMessage(name, message);
                        break;
                    case 4:
                        String target = in.readUTF();
                        String w_message = in.readUTF();
                        if (isAuth) {
                            server.sendWhisper(this, target, w_message);
                        } else
                            System.out.println("Bug");
                        break;
                    case 6:


                    default:
                        server.close(socket);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Client disconnected");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String username, String msg) {
        try {
            out.writeByte(3); //server_message
            out.writeUTF(username);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendWhisper(String source, String msg) {
        try {
            out.writeByte(5); // new_whisper
            out.writeUTF(source);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isActive() {
        return isAuth;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return this.name;
    }

    public boolean userExist(String username) {
        return server.getAuthService().contains(username);
    }
}

