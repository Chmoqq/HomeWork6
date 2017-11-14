package Lesson6;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class MyWindow extends JFrame {

    private JTextField jtf;
    private JTextArea jta;
    private JTextField login = new JTextField("Login");
    private JTextField pass = new JTextField("Password");
    private JButton authBtn = new JButton("Auth");

    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket sock;
    private DataInputStream in;
    private DataOutputStream out;

    public static void main(String[] args) {
        MyWindow myWindow = new MyWindow();
    }

    public MyWindow() {
        setBounds(600, 300, 500, 500);
        setTitle("Client");
        login.setToolTipText("Enter your login");
        pass.setToolTipText("Enter your password");
        authBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect(login.getText(), pass.getText());
            }
        });
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

    private void connect(String log, String pass) {
        if (log.trim().isEmpty() || pass.trim().isEmpty()) {
            System.out.println("Auth field is empty");
            return;
        }
        jtf.setEnabled(true);
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

                            if (username == log)
                                username = "Me";

                            jta.append(username + ": " + message + System.lineSeparator());
                            break;
                        case 5:
                            String w_username = in.readUTF();
                            String w_message = in.readUTF();

                            jta.append("> [" + w_username + "] " + w_message + System.lineSeparator());
                            break;
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
            }
        }).start();
    }

    private void sendMsg(String msg) {
        try {
            out.writeByte(2); // message
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send" + e.getLocalizedMessage());
        }
    }

    private void sendWhisper(String username, String msg) {
        try {
            out.writeByte(4); // whisper
            out.writeUTF(username);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send" + e.getLocalizedMessage());
        }

        jta.append("< [" + username + "] " + msg + System.lineSeparator());
    }

    private void sendMsgFromUI() {
        if (!jtf.getText().trim().isEmpty()) {
            String a = jtf.getText();

            String[] args = a.split(" ");

            if (a.startsWith("/w ") && args.length >= 3)
                sendWhisper(args[1], a.split(" ", 3)[2]);
            else
                sendMsg(a);

            jtf.setText("");
            jtf.grabFocus(); //Focusing on jTextField
        }
    }
}

