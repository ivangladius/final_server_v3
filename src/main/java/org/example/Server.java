
package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class Server {

    private Database db;

    private int port;
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;

    public Server(int port, int threadPoolSize) {

        //            db = HSQLDatabase.getInstance();
        db = DatabaseFactory.getDatabase();

        System.out.println("Database running...");

        this.port = port;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started...");
        } catch (IOException e) {
            stop();
            e.printStackTrace();
        }
    }

    public void loop() {
        for (; ; ) {
            try {
                System.out.println("Waiting for Connections..");
                Socket clientSocket = serverSocket.accept();
                executor.execute(new MyRunnable(this, clientSocket));
            } catch (IOException ignore) {
            }
        }
    }

    public String[] readFromClient(Socket clientSocket, BufferedReader reader) throws IOException {

        // read data from client (json as string format)
        String result = reader.readLine();

        // trim object ready to extract information
        int i = result.indexOf("{");
        result = result.substring(i);
        JSONObject json = new JSONObject(result.trim());
//        System.out.println(json.toString(4));


        return new String[]{
                json.get("operation").toString(),
                json.get("payload").toString()
        };
    }

    public void sendToClient(Socket clientSocket, String operation, String payload,
                             PrintWriter writer) throws IOException {

        JSONObject json = new JSONObject();
        json.put("operation", operation);
        json.put("payload", payload);

        // write data to client (json will be converted to a String
        writer.println(json);

    }

    public void handle_connection(Socket clientSocket) {

        System.out.println("\nClient connected: " + clientSocket.getRemoteSocketAddress().toString());

        // initialized here so we can close the streams and avoid
        // memory leaks
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            reader =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer =
                    new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String receivedOperation;
        String receivedPayload;

        try {

            String[] receivedData = readFromClient(clientSocket, reader);
            receivedOperation = receivedData[0];
            receivedPayload = receivedData[1];


            String sendingOperation = receivedOperation;
            String sendingPayload; // assigned in the switch statement below


            switch (receivedOperation) {
                case "getIdByUsername":
                    sendingPayload = getIdByUsername(receivedPayload);
                    break;
                case "getUsername":
                    sendingPayload = getUsername(receivedPayload);
                    break;
                case "createUser":
                    sendingPayload = createUser(receivedPayload);
                    break;
                case "login":
                    sendingPayload = login(receivedPayload);
                    break;
                case "searchUsers":
                    sendingPayload = searchUsers(receivedPayload);
                    break;
                case "sendMessage":
                    sendingPayload = sendMessage(receivedPayload);
                    break;
                case "listFriends":
                    sendingPayload = listFriends(receivedPayload);
                    break;
                case "getMessages":
                    sendingPayload = getMessages(receivedPayload);
                    break;
                case "getEmailByUsername":
                    sendingPayload = getEmailByUsername(receivedPayload);
                    break;
                case "changeUsername":
                    sendingPayload = changeUsername(receivedPayload);
                    break;
                case "changeEmail":
                    sendingPayload = changeEmail(receivedPayload);
                    break;
                case "changePassword":
                    sendingPayload = changePassword(receivedPayload);
                    break;
                default:
                    sendingPayload = "empty";
                    break;
            }


            sendToClient(clientSocket, sendingOperation, sendingPayload, writer);

            if (reader != null && writer != null) {
                reader.close();
                writer.close();
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        closeClient(clientSocket);
    }

    private String changePassword(String payload) {

        // getting "userid passwordToChangeTo"
        String[] info = payload.split(" ");

        db.queryChangePassword(Integer.valueOf(info[0]), info[1]);

        return null;
    }

    private String changeUsername(String payload) {

        // getting "userid nameToChangeTheUsernameInto"
        String[] info = payload.split(" ");

        return String.valueOf(db.queryChangeUsername(Integer.valueOf(info[0]), info[1]));
    }

    private String changeEmail(String payload) {

        // getting "userid nameToChangeEmailTo"
        String[] info = payload.split(" ");

        return String.valueOf(db.queryChangeEmail(Integer.valueOf(info[0]), info[1]));

    }


    private String getEmailByUsername(String payload) {
        System.out.println("GOT CALLED");
        return db.queryFindEmailByUsername(payload);
    }

    private void closeClient(Socket cSocket) {
        try {
            cSocket.close();
        } catch (IOException ignore) {
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public String getIdByUsername(String username) {
        return db.queryFindIDByUsername(username);
    }

    public String getUsername(String payload) {
        return db.queryFindUsernameByID(Integer.valueOf(payload));
    }

    public String createUser(String payload) {
        if (payload == null)
            return " ";

        // payload is as follows
        // "username email password
        String[] data = payload.split(" ");

        // if username already exist:           return 1
        // if email already exist:              return 2
        // if username and email already exist: return 3
        // else return primary key of created user
        return db.queryInsertUser(data[0], data[1], data[2]);
    }

    public String login(String payload) {
        String[] cred = payload.split(" ");

        // return userId (primary key) if login credentials are correct
        if (db.verifyLoginCredentials(cred[0], cred[1]) == 1) {
            return db.queryFindIDByEmail(cred[0]) + " " + db.queryGetUsername(cred[0]);
        }
        return null;
    }

    public String searchUsers(String payload) {

        // search for all users containing the letters in payload
        // returning username and profile picture (profile picture not implemented in the app)

        List<String[]> foundUsers = db.querySearchUsers(payload);
        List<String> users = new ArrayList<>();

        // add only the username to users List, not the profile pictures
        // where st[0] is the username and st[1] the profile_picture
        // leaving profile picture in the code, in case we wanted to implement it later

        for (String[] st : foundUsers) {
            users.add(st[0]);
        }
        // now sort the username alphabetic
        Collections.sort(users);

        // StringBuilder concatenating will be faster because compiler can't optimize
        // Strings in a loop
        StringBuilder usersPayload = new StringBuilder();
        for (String u : users)
            usersPayload.append(u).append(" ");

        // sending user a String with all users found
        // separated by spaces
        // e.g: "user1 user2 user3"
        return usersPayload.toString();
    }

    public String listFriends(String payload) {


        // lookup primary key by the given username
        String username = db.queryFindIDByUsername(payload);
        if (username != null) {
            Integer key = Integer.parseInt(username);

            // look up in chats table which user has he texted with.
            // also by adding a friends 2 two entries in the chats table are made
            // e.g: 7 13 .
            //     13 7  .
            // thus we know they are friends

            // get all friends, by Id
            List<Integer> friendsKeys = db.queryListFriendsIDs(key);

            if (friendsKeys != null) {

                // now we got something like this : {4, 7, 19, 333}
                // where those numbers are the primary keys of the friends
                // now convert them to usernames and display them later in UsersActivity.java

                List<String> usernames = new ArrayList<>();
                for (Integer fk : friendsKeys)
                    usernames.add(db.queryFindUsernameByID(fk));

                StringBuilder prep = new StringBuilder();
                for (String f : usernames)
                    prep.append(f).append(" ");

                return prep.toString();
            } else
                return "";
        }
        return "";
    }

    public String sendMessage(String payload) {

        String msg = " ";
        Matcher x = Pattern.compile("\\[(.*?)\\]").matcher(payload);
        if (x.find())
            msg = x.group(1);

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        String[] info = payload.split(" ");

        db.queryAddMessage(Integer.parseInt(info[0]), msg, Integer.parseInt(info[1]), timeStamp);

        return info[2];
    }

    public void showAllMessages() throws SQLException {

        System.out.println("$$$ MESSAGES $$$$\n");

        ResultSet result2 = db.executeQuery("SELECT * FROM chats;");

        String prim, sec, msg, time;
        while (result2.next()) {

            prim = result2.getString(1);
            sec = result2.getString(3);
            msg = result2.getString(2);
            time = result2.getString(4);

            System.out.println(prim + ", " + sec + " " + msg + " " + time);

        }

        result2.close();
    }

    public String getMessages(String payload) {
        String[] prep = payload.split(" ");
        Integer id = Integer.valueOf(prep[0]);
        String partner = prep[1];
        List<String> messages = db.queryGetMessages(id, partner);
        if (messages.size() == 0)
            return null;

        // now build message in a String like that
        // "[user time] [message1]\t[user2 time] [message2]\t
        // the \t is important for separating the message in the Client class

        StringBuilder reply = new StringBuilder();
        for (String m : messages)
            reply.append(m);

        return reply.toString();
    }

    static class MyRunnable implements Runnable {

        private final Server server;
        private final Socket client;

        // passing original server object to this constructor
        public MyRunnable(Server server, Socket client) {
            this.server = server;
            this.client = client;
        }

        public void run() {
            this.server.handle_connection(this.client);
            // long threadId = Thread.currentThread().getId();
            // System.out.print("ID: " + threadId + " ");
        }
    }
}
