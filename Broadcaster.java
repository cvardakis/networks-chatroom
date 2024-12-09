import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Broadcaster implements Runnable {

    Boolean isRunning = true;
    ConcurrentHashMap<String, ClientHandler> clientMap;
    ConcurrentHashMap<Integer, Group> groupMap;
    Queue<Message> messageQueue = new LinkedList<>();

    public Broadcaster(ConcurrentHashMap<String, ClientHandler> clientMap, ConcurrentHashMap<Integer, Group> groupMap) {
        this.clientMap = clientMap;
        this.groupMap = groupMap;
    }

    public void queueMessage(Message message) {
        messageQueue.add(message);
    }

    public void sendOnlineUsers(){
        ArrayList<String> usernameArray = new ArrayList<>();
        for(ClientHandler clientHandler : clientMap.values()) {
            usernameArray.add(clientHandler.getUsername());
        }
        String usernames = String.join(",", usernameArray);
        for(ClientHandler clientHandler : clientMap.values()){
            clientHandler.sendOther("OnlineUsers¤" + usernames);
        }
    }

    public void run() {
        System.out.println("Broadcaster started");
        while (isRunning){
            while (!messageQueue.isEmpty()) {
                System.out.println("Queue has item");
                Message currentMessage = messageQueue.poll();
                String sender = currentMessage.getSender();
                String receiver = currentMessage.getReceiver();
                String message = currentMessage.getMessage();

                if ("MessageAll".compareToIgnoreCase(receiver)==0) {
                    for (ClientHandler clientHandler : clientMap.values()) {
                        if(clientHandler.getUsername().equals(sender)) {
                            continue;
                        }
                        clientHandler.sendmessage(sender, message, false);
                        System.out.println("Triggering Send");
                    }
                } else if(isInt(receiver)){
                    Group group = groupMap.get(Integer.parseInt(receiver));
                    group.sendGroupMessage(sender, message);
                } else {
                    clientMap.get(receiver).sendmessage(sender, message, true);
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Boolean isInt(String s){
        try{
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void stopRunning() {
        isRunning = false;
    }
}
