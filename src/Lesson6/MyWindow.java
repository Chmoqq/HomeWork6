package Lesson6;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyWindow extends JFrame {

    private JTextField jtf;
    private JTextArea jta;

    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket sock;
    private Scanner in;
    private PrintWriter out;

    public static void main(String[] args) {
        MyServer myServer = new MyServer();
        MyWindow myWindow = new MyWindow();
    }

    public MyWindow() {
        Thread t1 = new Thread(() -> {
            try {
                sock = new Socket(SERVER_ADDR, SERVER_PORT);
                in = new Scanner(sock.getInputStream());
                out = new PrintWriter(sock.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            setBounds(600, 300, 500, 500);
            setTitle("Client");
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
            bottomPanel.add(jtf, BorderLayout.CENTER);

            jbSend.addActionListener(e -> sendMsgFromUI());
            jtf.addActionListener(e -> sendMsgFromUI());

            new Thread(() -> {
                try {
                    while (true) {
                        if (in.hasNext()) {
                            String w = in.nextLine();
                            if (w.equalsIgnoreCase("end session")) break;
                            jta.append(w + System.lineSeparator());
                        }
                    }
                } catch (Exception e) {
                }
            }).start();

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
                        out.println("end");
                        sock.close();
                        out.close();
                        in.close();
                    } catch (IOException exc) {
                    }
                }
            });

            setVisible(true);
        });
        t1.start();
    }

    private void sendMsg(String msg) {
        if (!msg.trim().isEmpty()) {
            out.println(msg);
            System.out.println("Console: " + msg);
        }
    }

    private void sendMsgFromUI() {
        if (!jtf.getText().trim().isEmpty()) {
            String a = jtf.getText();
            out.println(a);
            jtf.setText("");
            jtf.grabFocus(); //Focusing on jTextField
        }
    }
}

