public class Message {

    String sender;
    String receiver;
    String message;

    public Message(String sender, String message, String recipient){
        this.sender = sender;
        this.receiver = recipient;
        this.message = message;

    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }
}
