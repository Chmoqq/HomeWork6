package Lesson6;

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

    private List<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("Login", "Password", "abc"));
        entries.add(new Entry("login2", "password2", "nickname2"));
        entries.add(new Entry("login3", "password3", "nickname3"));
    }


    @Override
    public boolean login(String login, String pass) {
        for (Entry e : entries) {
            if (e.getLogin().equals(login) && e.getPass().equals(pass)) return true;
        }
        return false;
    }

    @Override
    public String getNick(String login, String password) {

        for (Entry e : entries) {
            if (e.getLogin().equals(login) && e.getPass().equals(password)) return e.getNick();
        }
        return null;
    }
}
