package Lesson6;

public interface AuthService {

    boolean login(String login, String pass);

    String getNick(String login, String password);
}
