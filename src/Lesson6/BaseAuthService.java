package Lesson6;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPass() {
            return pass;
        }

        public String getNick() {
            return nick;
        }
    }

    private List<Entry> entries = new ArrayList<>();
    private String fileLocation = "clients.txt";
    private File clients = new File(fileLocation);

    public BaseAuthService() {
        try {
            Files.lines(Paths.get(fileLocation), StandardCharsets.UTF_8).forEach((meme) -> {
                String[] credentials = meme.split(":", 2);
                addClient(credentials[0], credentials[1]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void addClient(String log, String pass) {
        entries.add(new Entry(log, pass, log));

        List<String> clients_raw = new ArrayList<>();
        entries.forEach((entry -> {
            clients_raw.add(entry.getLogin() + ":" + entry.getPass());
        }));

        try {
            Files.write(Paths.get(fileLocation), clients_raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNick(String login, String password) {

        for (Entry e : entries) {
            if (e.getLogin().equals(login) && e.getPass().equals(password)) return e.getNick();
        }
        return null;
    }
}
