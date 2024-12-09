import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Group {

    int id;
    String[] members;
    ConcurrentHashMap<String, ClientHandler> clientMap;

    public Group(int id, String recipients, ConcurrentHashMap clientMap) {
        this.id = id;
        members = recipients.split(",");
        this.clientMap = clientMap;
        initializeGroup();
    }

    public void sendGroupMessage(String sender, String message) {
        for (String member : members) {
            ClientHandler client = clientMap.get(member);
            client.sendOther("MessageGroup¤" + id + "¤" + sender + "¤" + message);
        }
    }

    public void initializeGroup(){
        String memberList = String.join(",", members);
        System.out.println("Notifying members of Group Creation: " + memberList);
        for (String member : members) {
            try {
                ClientHandler client = clientMap.get(member);
                client.sendOther("CreateGroup¤" + id + "¤" + memberList);
            } catch (Exception e) {
                System.out.println("Skipped member: " + member);
            }
        }
    }
}
