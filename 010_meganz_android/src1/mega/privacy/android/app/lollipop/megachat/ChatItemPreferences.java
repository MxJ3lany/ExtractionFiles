package mega.privacy.android.app.lollipop.megachat;


public class ChatItemPreferences {

    String chatHandle = "";
    String notificationsEnabled = "";
    String writtenText = "";

    public ChatItemPreferences(String chatHandle, String notificationsEnabled, String writtenText) {
        this.chatHandle = chatHandle;
        this.notificationsEnabled = notificationsEnabled;
        this.writtenText = writtenText;
    }

    public String getChatHandle() {
        return chatHandle;
    }

    public void setChatHandle(String chatHandle) {
        this.chatHandle = chatHandle;
    }

    public String getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(String notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getWrittenText() {
        return writtenText;
    }

    public void setWrittenText(String writtenText) {
        this.writtenText = writtenText;
    }
}
