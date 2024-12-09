import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    Socket client;
    ChatroomServer server;
    BufferedReader input = null;
    BufferedOutputStream output = null;
    Broadcaster broadcaster;
    Boolean isRunning = true;

    String username = null;

    public ClientHandler(Socket client, ChatroomServer server, Broadcaster broadcaster) {
        this.client = client;
        this.server = server;
        this.broadcaster = broadcaster;
    }

    public String getUsername() {
        return username;
    }

    public void sendmessage(String senderUsername, String message, Boolean individual) {
        try {
            output = new BufferedOutputStream(client.getOutputStream());

            String type = "MessageAll";
            if (individual) {
                type = "MessageIndividual";
            }
            System.out.println("Sending: " + type + "¤" + senderUsername + "¤" + message + "\n");
            output.write((type + "¤" + senderUsername + "¤" + message + "\n").getBytes());
            output.flush();
        } catch (IOException e) {
            System.out.println("Message failed to send to client " + e);
        }
    }

    public void sendOther(String serverReturn) {
        try {
            output = new BufferedOutputStream(client.getOutputStream());

            output.write((serverReturn + "\n").getBytes());
            output.flush();
        } catch (IOException e) {
            System.out.println("Message failed to send to client");
        }
    }

    public void stopRunning() {
        isRunning = false;
        endConnection();
    }

    public void endConnection() {
        try {
            if (output != null) {
                output.flush();
                output.close();
            }
            if (input != null) {
                input.close();
            }
            server.deregisterUser(this);
            RequestParser message = new RequestParser("OnlineUsers¤");
            RequestHandler request = new RequestHandler(message, broadcaster, server, username);
            request.process();
            client.close();
        } catch (IOException e) {
            System.out.println("Error safely closing Connection");
        }
    }

    public void run() {
        System.out.println("Client passed to handler class successful");
        while (isRunning) {
            String rawMessage;
            try {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));

                rawMessage = input.readLine();

                System.out.println("Captured Request: " + rawMessage);
                if (rawMessage == null) {
                    stopRunning();
                    break;
                }
//                System.out.println("Skipping If Condition");
                RequestParser message = new RequestParser(rawMessage);

                if (username != null) {
                    RequestHandler request = new RequestHandler(message, broadcaster, server, username);
                    if (message.getType().equalsIgnoreCase("Leave")) {
                        stopRunning();
                        server.deregisterUser(this);
                    }
                    request.process();
                } else if (("join".compareToIgnoreCase(message.getType()) == 0)) {
                    this.username = message.getData()[0];
                    server.registerUser(this, username);
                    if (!isRunning) {
                        endConnection();

                        return;
                    }
                    System.out.println("Client secured connection with username: " + username);
                    message = new RequestParser("OnlineUsers¤");
                    RequestHandler request = new RequestHandler(message, broadcaster, server, username);
                    request.process();
                } else {
                    client.close();
                    break;
                }


            } catch (IOException e) {
                System.out.println("Client closing connection");
                stopRunning();
                server.deregisterUser(this);
                endConnection();
//                endConnection();
            }
        }
    }
}
