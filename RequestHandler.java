public class RequestHandler {

    RequestParser request;
    Broadcaster broadcaster;
    ChatroomServer chatroomServer;
    String username;

    public RequestHandler(RequestParser message, Broadcaster broadcaster, ChatroomServer server, String username) {
        this.request = message;
        this.broadcaster = broadcaster;
        this.chatroomServer = server;
        this.username = username;
    }

    public void SendMessage() {
        String recipient = "MessageAll";
        String message = request.getData()[0];
        Message datapacket = new Message(username, message, recipient);
        broadcaster.queueMessage(datapacket);
        System.out.println("Message Queued");
    }

    public void SendPrivateMessage() {
        String recipient = request.getData()[0];
        String message = request.getData()[1];
        Message datapacket = new Message(username, message, recipient);
        broadcaster.queueMessage(datapacket);
        System.out.println("Message Queued - Private");
    }

    public void SendPrivateGroupMessage() {
        String recipient = request.getData()[0];
        String message = request.getData()[1];
        Message datapacket = new Message(username, message, recipient);
        broadcaster.queueMessage(datapacket);
        System.out.println("Message Queued - Group");
    }

    public void CreatePrivateGroup() {
        String recipient = request.getData()[0];
        if (containsInt(recipient)) {
            return;
        }
        chatroomServer.registerGroup(recipient);
    }

    public void SendOnlineUsers() {
        broadcaster.sendOnlineUsers();
    }

    public boolean containsInt(String input) {
        String[] people = input.split(",");
        for (String p : people) {
            if (isInt(p)){
                System.out.println("Rejecting group chat creation: nested chats");
                return true;
            }
        }

        return false;
    }

    public Boolean isInt(String s){
        try{
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void process() {
        String type = request.getType();

        switch (type.toLowerCase()) {
            case "messageall":
                SendMessage();
                break;

            case "messageindividual":
                SendPrivateMessage();
                break;

            case "onlineusers":
                SendOnlineUsers();
                break;

            case "creategroup":
                CreatePrivateGroup();
                break;

            case "messagegroup":
                SendPrivateGroupMessage();
                break;

            default:
                System.out.println("Unknown type: " + type);
                break;
        }

    }

}
