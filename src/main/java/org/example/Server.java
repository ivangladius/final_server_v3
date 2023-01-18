
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

    public void handle_connection(Socket cSocket) {

        System.out.println("active threads: " + executor.getActiveCount());

        System.out.println("\nClient connected: " + cSocket.getRemoteSocketAddress().toString());

        PrintWriter out;
        BufferedReader in;
        try {
            out = new PrintWriter(cSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

            String result = in.readLine();

            // TODO : CAN CRASH IF GARBAGE IS SEND
            int i = result.indexOf("{");
            result = result.substring(i);
            JSONObject json = new JSONObject(result.trim());
            System.out.println(json.toString(4));

//
            System.out.println("operation: " + json.get("operation"));

            // answer
            JSONObject reply = new JSONObject();
            reply.put("key", json.get("key"));
            reply.put("operation", json.get("operation"));

            String operation = json.get("operation").toString();

            if (operation.equals("getIdByUsername"))
                reply.put("payload", getIdByUsername(json.get("payload").toString()));

            else if (operation.equals("getUsername"))
                reply.put("payload", getUsername(json.get("payload").toString()));

            else if (operation.equals("createUser"))
                reply.put("payload", createUser(json.get("payload").toString()));

            else if (operation.equals("login"))
                reply.put("payload", login(json.get("payload").toString()));

            else if (operation.equals("searchUsers"))
                reply.put("payload", searchUsers(json.get("payload").toString()));

            else if (operation.equals("sendMessage"))
                reply.put("payload", sendMessage(json.get("payload").toString()));

            else if (operation.equals("listFriends"))
                reply.put("payload", listFriends(json.get("payload").toString()));

            else if (operation.equals("getMessages"))
                reply.put("payload", getMessages(json.get("payload").toString()));

            else if (operation.equals("getEmailByUsername"))
                reply.put("payload", getEmailByUsername(json.get("payload").toString()));

            else if (operation.equals("changeUsername"))
                reply.put("payload", changeUsername(json.get("payload").toString()));

            else if (operation.equals("changeEmail"))
                reply.put("payload", changeEmail(json.get("payload").toString()));

            else if (operation.equals("changePassword"))
                reply.put("payload", changePassword(json.get("payload").toString()));


            else
                reply.put("payload", "empty");

            out.println(reply.toString());
            System.out.println("data send back");

            out.close();
            in.close();

//            ResultSet result2 = db.executeQuery("SELECT * FROM users;");
//
//            String id, username, email, password;
//            id = username = email = password = null;
//            while (result2.next()) {
//
//                id = result2.getString(1);
//                username = result2.getString(2);
//                email = result2.getString(3);
//                password = result2.getString(4);
//
//                System.out.println(id + ", " + username + " " + email + " " + password);
//
//            }
//            showAllMessages();

//            System.out.println
//                    ("new user: " + id + ", " + username + " " + email + " " + password);
//
//            result2.close();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

        closeClient(cSocket);
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

        db.queryChangeUsername(Integer.valueOf(info[0]), info[1]);

        return null;
    }

    private String changeEmail(String payload) {

        // getting "userid nameToChangeEmailTo"
        String[] info = payload.split(" ");

        db.queryChangeEmail(Integer.valueOf(info[0]), info[1]);

        return null;
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
        String id = db.queryFindIDByUsername(username);
        System.out.println("SERVER ID: " + id);
        return id;
    }

    public String getUsername(String payload) {
        return db.queryFindUsernameByID(Integer.valueOf(payload));
    }

    public String createUser(String payload) {
        if (payload == null)
            return " ";
        System.out.println("client payload: " + payload);
        String[] data = payload.split(" ");
        String userId = db.queryInsertUser(data[0], data[1], data[2]);

        if (userId.equals("1"))
            return "1";
        else if (userId.equals("2"))
            return "2";
        else if (userId.equals("3"))
            return "3";


        System.out.println("### USERID: " + userId);
        return userId;
    }

    public String login(String payload) {
        System.out.println("HELLLO LOGIN");
        System.out.println("XXX PAYLOAD: " + payload);
        String[] cred = payload.split(" ");
        if (db.verifyLoginCredentials(cred[0], cred[1]) == 1) {
            System.out.println("QUERY_GET_USERNAME: " + db.queryGetUsername(cred[0]));
            return db.queryFindIDByEmail(cred[0]) + " " + db.queryGetUsername(cred[0]);
        } else {
            System.out.println("ELSE ELSE ELSE");
        }
        return null;
    }

    public String searchUsers(String payload) {

        List<String[]> foundUsers = db.querySearchUsers(payload);
        List<String> users = new ArrayList<>();

        for (String[] st : foundUsers) {
            users.add(st[0]);
        }
        Collections.sort(users);

        String usersPayload = "";
        for (String u : users)
            usersPayload += u + " ";

        return usersPayload;
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

                String prep = "";
                for (String f : usernames)
                    prep += f + " ";

                return prep;
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
        System.out.println("CALLED ME");
        String[] prep = payload.split(" ");
        Integer id = Integer.valueOf(prep[0]);
        String partner = prep[1];
        System.out.println("PRIM: " + id + " PARTNER: " + partner);
        List<String> messages = db.queryGetMessages(id, partner);
        System.out.println("MEESAGE SIZE: " + messages.size());
        if (messages.size() == 0)
            return null;
        else if (messages == null)
            return null;

        String reply = new String();
        for (String m : messages) {
            System.out.println("m: " + m);
//            m = new String("[".concat(m).concat("]"));
            reply += m;
//            System.out.println("BUILD: " + m);
        }

//        System.out.println("$$$ SENDING: \n" + reply);
        return reply;
    }

    class MyRunnable implements Runnable {

        private Server server;
        private Socket client;

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
