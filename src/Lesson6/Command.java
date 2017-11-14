package Lesson6;

public enum Command {
    SEND_CHAT_MESSAGE("/clients");

    private String text;

    public String getText() {
        return text;
    }

    Command(String s) {
        text = s;
    }

}
