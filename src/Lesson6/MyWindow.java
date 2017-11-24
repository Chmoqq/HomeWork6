package Lesson6;


import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MyWindow extends JFrame {

    private JTextField jtf;
    private JTextArea jta;
    private JTextField login = new JTextField("login");
    private JPasswordField pass = new JPasswordField("password");
    private JButton authBtn = new JButton("Auth");

    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket sock;
    private DataInputStream in;
    private DataOutputStream out;

    public static void main(String[] args) throws MalformedURLException, AWTException {
        MyWindow myWindow = new MyWindow();
    }

    MyWindow() {
        setBounds(600, 300, 500, 500);
        setTitle("Client");
        login.setToolTipText("Enter your login");
        pass.setToolTipText("Enter your password");
        authBtn.addActionListener(e -> connect(login.getText(), getPass()));
        JPanel authPanel = new JPanel(new GridLayout());
        authPanel.add(login);
        authPanel.add(pass);
        authPanel.add(authBtn);
        add(authPanel, BorderLayout.NORTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jta = new JTextArea();
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        add(jsp, BorderLayout.CENTER);
        pass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                pass.setEchoChar((char)0);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                pass.setEchoChar((char)8226);
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSend = new JButton("SEND");
        bottomPanel.add(jbSend, BorderLayout.EAST);
        jtf = new JTextField();
        jtf.setEnabled(false);
        bottomPanel.add(jtf, BorderLayout.CENTER);

        jbSend.addActionListener(e -> sendMsgFromUI());
        jtf.addActionListener(e -> sendMsgFromUI());


        new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while (true) {
                try {
                    if (s.hasNext()) {
                        sendMsg(s.nextLine());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    sock.close();
                    out.close();
                    in.close();
                } catch (IOException exc) {
                    System.out.println("Something went wrong");
                }
            }
        });

        setVisible(true);
    }

    void connect(String log, String pass) {
        if (log.trim().isEmpty() || pass.trim().isEmpty()) {
            System.out.println("Auth field is empty");
            return;
        }

        try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            out.writeByte(1); // auth
            out.writeUTF(log);
            out.writeUTF(pass);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                while (true) {
                    switch (in.readByte()) {
                        case 3:
                            String username = in.readUTF();
                            String message = in.readUTF();


                            if (username.equals(log)) {
                                username = "Me";
                                jta.append(username + ": " + message + System.lineSeparator());
                            } else {
                                SystemNotifications.getInstance().DisplayTray("Server", username + " sent you a message");
                                jta.append(username + ": " + message + System.lineSeparator());
                            }
                            break;
                        case 5:
                            String w_username = in.readUTF();
                            String w_message = in.readUTF();

                            jta.append("> [" + w_username + "] <" + " : " + w_message + System.lineSeparator());
                            SystemNotifications.getInstance().DisplayTray("Server", w_username + " sent you a whisper");
                            break;
                        case 6:
                            jtf.setEnabled(true);
                            setTitle("Client: " + log);
                            login.setEnabled(false);
                            this.pass.setEnabled(false);
                            SystemNotifications.getInstance().DisplayTray("Server", "Logged as " + log);
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    void sendMsg(String msg) {
        try {
            out.writeByte(2); // message
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send" + e.getLocalizedMessage());
        }
    }

    void sendWhisper(String username, String msg) {
        try {
            out.writeByte(4); // whisper
            out.writeUTF(username);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send" + e.getLocalizedMessage());
        }

        jta.append("> [" + username + "] < : " + msg + System.lineSeparator());
    }

    void sendMsgFromUI() {
        if (!jtf.getText().trim().isEmpty()) {
            String a = jtf.getText();

            String[] args = a.split(" ");

            if (a.startsWith("/w ") && args.length >= 3)
                sendWhisper(args[1], a.split(" ", 3)[2]);
            else if (a.startsWith("/clients ") && a.contains(" -m ") && args.length >= 4) {
                sendChatMessage(a);
            } else if (a.startsWith("/online")) {
                isOnline();
            } else {
                sendMsg(a);
            }

            jtf.setText("");
            jtf.grabFocus(); //Focusing on jTextField
        }
    }

    void sendChatMessage(String msg) {
        String recepients_data = msg.split(" -m ")[0];
        String message = msg.split(" -m ")[1];
        String[] targets_old = recepients_data.substring(Command.SEND_CHAT_MESSAGE.getText().length()).split(" ");

        ArrayList<String> targets = new ArrayList(Arrays.asList(recepients_data.split(" ")));
        targets.remove(0);

        String[] data = msg.substring(Command.SEND_CHAT_MESSAGE.getText().length()).split("-m ");
        if (data.length == 2) {
//            String data0 = data[0];
//            data0.trim(' ');
//            data[0] = data0;
            /*for (String recipient : recievers) {
                sendWhisper(recipient, message);
            }*/

            try {
                out.writeByte(6); // multiple whisper
                out.writeShort(targets.size());
                for (String target : targets) {
                    out.writeUTF(target);
                }
                out.writeUTF(message);
                out.flush();
            } catch (IOException e) {
                System.out.println("Failed to send" + e.getLocalizedMessage());
            }
        } else {
            System.out.println("Invalid chat message command");
        }
    }

    void isOnline() {
        try {
            out.writeByte(7);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getPass() {
        StringBuilder password = new StringBuilder();
        char[] passMassive = pass.getPassword();
        for (char c : passMassive) {
            password.append(c);
        }
        return password.toString();
    }
}

