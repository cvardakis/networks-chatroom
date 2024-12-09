import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatroomServer {
    private static final int PORT = 8888;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Group> groups = new ConcurrentHashMap<>();

    public void startServer(){
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            Broadcaster broadcaster = new Broadcaster(clientMap, groups);
            Thread broadcasterThread = new Thread(broadcaster);
            broadcasterThread.start();

            while(true){
                Socket client = serverSocket.accept();
                System.out.println("New client attempting connection");

                ClientHandler clientHandler = new ClientHandler(client, this, broadcaster);
                threadPool.execute(clientHandler);
            }

        } catch (IOException e) {
            System.out.println("Server could not be started");
            throw new RuntimeException(e);
        }

    }

    public void registerUser(ClientHandler clientHandler, String username){
        if (clientMap.containsKey(username)){
                System.out.println("Username is already in use");
                clientHandler.stopRunning();
        } else if(isInt(username)) {
            System.out.println("Username was single numerical character");
            clientHandler.stopRunning();
        } else {
            clientMap.put(username, clientHandler);
        }
    }

    public void deregisterUser(ClientHandler clientHandler){
        if (clientMap.containsKey(clientHandler.getUsername())){
            clientMap.remove(clientHandler.getUsername());
            System.out.println("Username has been deregistered " + clientHandler.getUsername());
        }
    }

    public void registerGroup(String recipients){
        int idNumber = groups.size() + 1;
        Group group = new Group(idNumber, recipients, clientMap);
        groups.put(idNumber, group);
    }

    public Boolean isInt(String s){
        try{
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //This method is ChatGPT to skip network interfaces that are IPV6 or personal loopback
    public static String getIPv4(){
        String hostIP = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Skip loopback and disabled interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // Skip loopback addresses and IPv6
                    if (inetAddress.isLoopbackAddress() || !(inetAddress instanceof Inet4Address)) {
                        continue;
                    }

                    // Use the first valid IPv4 address found
                    hostIP = inetAddress.getHostAddress();
                    break;
                }

                // Exit outer loop if an IP has been found
                if (hostIP != null) {
                    break;
                }
            }

            if (hostIP != null) {
                System.out.println("Host IPv4 Address: " + hostIP);
            } else {
                System.out.println("No valid IPv4 address found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostIP;
    }

    //This method is ChatGPT to push IP to online database
    public static void shareIP(){
        try {

            String hostIP = getIPv4();

            String urlString = "https://junglegenius.deno.dev/api/messageIP?type=add&ip=" + hostIP;

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Request successful! Host IP (" + hostIP + ") stored.");
            } else {
                System.out.println("Request failed. Response Code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            System.out.println("Server could not be share IP");
        }
    }

    public static void main(String[] args) {
        shareIP();
        ChatroomServer server = new ChatroomServer();
        server.startServer();
    }


}
