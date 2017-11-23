package Lesson6;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private MyServer server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name = null;
    private boolean isAuth = false;

     ClientHandler(MyServer server, Socket socket) {
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
                    case 1: // Authorization
                        String username = in.readUTF();
                        String password = in.readUTF();

                        name = server.getAuthService().getNick(username, password);

                        if (server.getAuthService().contains(name)) {
                            isAuth = true;
                            out.writeByte(6);
                            server.sendBroadcastMessage("> [Server] <", name + " зашел в чат!");
                        } else {
                            if (!(server.getAuthService().isLoginMatch(username))) {
                                serverMessage("User with the same login has registered");
                            } else {
                                sendMessage("> [Server] <", "Неверные логин/пароль" + "Идет регистрация нового пользователя...");
                                server.getAuthService().addClient(username, password);
                                sendMessage("> [Server] <", "Пользователь зарегистрирован, пройдите авторизацию повторно");
                            }
                        }
                        break;
                    case 2: // message
                        String message = in.readUTF();
                        if (isAuth)
                            server.sendBroadcastMessage(name, message);
                        break;
                    case 4: //private message
                        String target = in.readUTF();
                        String w_message = in.readUTF();
                        if (isAuth) {
                            server.sendWhisper(this, target, w_message);
                        }
                        break;
                    case 6: // multiple private messages
                        int targets_count = in.readShort();
                        String[] targets = new String[targets_count];

                        for (int i = 0; i < targets_count; i++)
                            targets[i] = in.readUTF();

                        String mw_message = in.readUTF();

                        List<String> successful_targets = new ArrayList<>();
                        List<String> unsuccesful_targets = new ArrayList<>();
                        for (String mw_target : targets) {
                            if (server.sendWhisper(this, mw_target, mw_message)) {
                                successful_targets.add(mw_target);
                            } else {
                                unsuccesful_targets.add(mw_target);
                            }

                            this.serverMessage("Ваше сообщение отправлено пользователям: " + String.join(", ", successful_targets));
                            this.serverMessage("Ваше сообщение не было отправлено пользователям: " + String.join(", ", unsuccesful_targets));
                            break;
                        }
                    case 7:
                        serverMessage(server.isOnline());
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

     void sendMessage(String username, String msg) {
        try {
            out.writeByte(3); //server_message
            out.writeUTF(username);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     void sendWhisper(String source, String msg) {
        try {
            out.writeByte(5); // new_whisper
            out.writeUTF(source);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     boolean isActive() {
        return isAuth;
    }

     Socket getSocket() {
        return socket;
    }

     String getName() {
        return this.name;
    }

     void serverMessage(String msg) {
        this.sendMessage("Server", msg);
    }
}

