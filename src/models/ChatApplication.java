package models;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatApplication {

    private ArrayList<User> users;
    private Integer port;
    private String ip;
    private ServerSocket socket;
    private BufferedReader input;
    private HashMap<User, DataOutputStream> userOutput;


    public ChatApplication() throws IOException {
        ip = Inet4Address.getLocalHost().getHostAddress();
        users = new ArrayList();

        input = new BufferedReader(new InputStreamReader(System.in));
        userOutput = new HashMap();
    }

    /**
     * BufferedReader waits for user input
     */
    public void begin() throws IOException{
        System.out.println("Beginning Chat Program for CS4470.");

        while(true){
            String userInput = input.readLine();
            String choice = userInput.split(" ")[0].toLowerCase();

            switch(choice){
                case "chat":
                    if (socket == null)
                        initiateChat(userInput);
                    else
                        System.out.println("ERROR: Can only listen to one port at a time.");
                    break;
                case "help":
                    helpMessage();
                    break;
                case "myip":
                    System.out.println("Your IP address is: " + ip);
                    break;
                case "myport":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else
                        System.out.println("Listening on port: " + port);
                    break;
                case "connect":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else
                        connectValidator(userInput);
                    break;
                case "list":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else
                        listUsers();
                    break;
                case "terminate":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else
                        terminateValidator(userInput);
                    break;
                case "send":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else
                        sendValidator(userInput);
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("That is not a valid command. Type 'help' for a list of commands.");
            }
        }
    }

    /**
     * When user types "chat"
     */
    private void initiateChat(String ui) throws IOException{
        String[] check = ui.split(" ");

        if (check.length != 2){
            System.out.println("Invalid arguments. Given: " + check.length + ", expected: 2.");
            socket = null;
        } else if (!isValidPort(check[1])){
            System.out.println("Port number is not valid.");
            socket = null;
        } else{
            int portNumber = Integer.valueOf(check[1]);
            try{
                socket = new ServerSocket(portNumber);
            }catch(IOException e){
                socket = null;
            }
        }

        if (socket != null){
            port = socket.getLocalPort();
            ip = Inet4Address.getLocalHost().getHostAddress();
            System.out.println("Listening on port: " + port);
            startServer();
        }
    }

    //Checks if input string is valid port number
    private boolean isValidPort(String input){
        //check if input is number
        for (int i=0; i<input.length(); i++){
            if (!Character.isDigit(input.charAt(i))){
                return false;
            }
        }
        //check if is within valid port range of 1024 through 65535
        int port = Integer.parseInt(input);
        return (port>=1024 && port<=65535);
    }

    private void startServer() throws IOException {
        new Thread(() -> {
            while (true) {
                try{
                    Socket connectionSocket = socket.accept();
                    new Thread(new UserHandler(connectionSocket)).start();
                } catch(IOException e){

                }
            }
        }).start();
    }

    /**
     * When user types "help"
     */
    private void helpMessage(){
        System.out.println("\n");
        System.out.println("Command \t \t \t \t Function");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("chat <port #> \t \t Begins listening on <port #>.");
        System.out.println("help \t \t \t Displays valid commands for chat application.");
        System.out.println("myip \t \t \t Displays your IP address.");
        System.out.println("myport \t \t \t Displays port # you are currently listening on.");
        System.out.println("connect <ip> <port #> \t Establishes TCP connection to <ip> on <port #>.");
        System.out.println("list \t \t \t Display list of all open connections and their ids.");
        System.out.println("terminate <id> \t \t Terminates connection of <id>.");
        System.out.println("send <id> <message> \t Sends <message> to connection <id>.");
        System.out.println("exit \t \t \t Close all connections and terminate application.");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("\n");
    }

    /**
     * When user types "list"
     */
    public void listUsers(){
        if (users.isEmpty())
            System.out.println("There are no connections.");
        else{
            System.out.println("ID \t IP Address \t Port Number");
            System.out.println("-------------------------------------");
            for (int i=0; i<users.size(); i++){
                User temp = users.get(i);
                System.out.println((i+1) + " \t " + temp.getHost() + " \t " + temp.getPort());
            }
            System.out.println("Total connections = " + users.size());
        }
    }

    //Handles connecting, messaging, and terminating with other users
    private class UserHandler implements Runnable{
        private Socket userSocket;
        public UserHandler(Socket socket){
            this.userSocket = socket;
        }

        public void run(){
            try{
                BufferedReader input = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));

                while(true){
                    String jsonString = input.readLine();

                    if (jsonString == null){
                        return;
                    }
                    String tempIp = JSONHelper.parse(jsonString, "ip");
                    int tempPort = Integer.valueOf(JSONHelper.parse(jsonString, "port"));

                    InteractionType it = InteractionType.valueOf(JSONHelper.parse(jsonString, "interaction_type"));
                    switch(it){
                        case CONNECT:
                            connectSuccessfulMessage(jsonString);
                            break;
                        case MESSAGE:
                            String message = JSONHelper.parse(jsonString, "message");
                            displayMessage(ip, port, message);
                            break;
                        case TERMINATE:
                            terminationSuccessfulMessage(tempIp, tempPort);
                            User tempUser = findUser(tempIp, tempPort);
                            terminateConnection(tempUser);
                            users.remove(tempUser);
                            userOutput.remove(tempUser);
                            input.close();
                            return;

                    }

                }
            } catch (IOException e){
                System.out.println("Message: Connection dropped.");
            }
        }
    }

    private void sendMessage(User user, String jsonString){
        try{
            userOutput.get(user).writeBytes(jsonString + "\r\n");
        } catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * When user types "connect"
     */
    private void connectValidator(String userInput) throws IOException{
        boolean valid = isValidConnectString(userInput);
        if (!valid){
            System.out.println("Connection failed: invalid arguments.");
            return;
        }
        String[] temp = userInput.split(" ");
        String tempIp = temp[1];
        int tempPort = Integer.valueOf(temp[2]);

        for (User user : users){
            if (user.getHost().equals(tempIp) && user.getPort() == tempPort)
                valid = false;
        }
        if (!valid){
            System.out.println("Connection failed: no duplicate connections.");
            return;
        }

        if (tempIp.equals(ip) && port==tempPort)
            valid = false;
        if (!valid){
            System.out.println("Connection failed: cannot connect to self.");
            return;
        } else{
            connect(tempIp, tempPort);
        }
    }

    private void connect(String tempIp, int tempPort) throws IOException{
        int attempts = 0;
        final int MAX_ATTEMPTS = 5;
        final int SLEEP_TIME = 1000;
        Socket userSocket = null;

        do{
            try{
                userSocket = new Socket(tempIp, tempPort);
            } catch(IOException e){
                System.out.println("Connection failed: Attempt #: " + (++attempts));
                try{
                    Thread.sleep(SLEEP_TIME);
                } catch(InterruptedException e1){
                }
            }
        } while(userSocket==null && attempts<MAX_ATTEMPTS);

        if (attempts >= MAX_ATTEMPTS){
            System.out.println("Connection has failed, try again later.");
        } else{
            System.out.println("Connected to ip:" + tempIp + " on port:" + tempPort);
            User tempUser = new User(tempIp, tempPort);
            users.add(tempUser);

            userOutput.put(tempUser, new DataOutputStream(userSocket.getOutputStream()));

            sendMessage(tempUser, JSONHelper.makeJson(InteractionType.CONNECT, ip, port).toJSONString());
        }
    }

    private boolean isValidConnectString(String input){
        String[] connectString = input.split(" ");
        return (connectString.length == 3 && connectString[0].equals("connect") && isValidPort(connectString[2]));
    }

    private void connectSuccessfulMessage(String jsonString) throws IOException{
        String tempIp = JSONHelper.parse(jsonString, "ip");
        int tempPort = Integer.valueOf(JSONHelper.parse(jsonString, "port"));
        System.out.println("\nUser with ip: " + tempIp + ", on port: " + tempPort + " has connected to you.");
        User tempUser = new User(tempIp, tempPort);
        users.add(tempUser);
        userOutput.put(tempUser, new DataOutputStream(tempUser.getSocket().getOutputStream()));
    }

    /**
     * When user types "send"
     */
    private void sendValidator(String choice){
        String[] temp = choice.split(" ");
        if (temp.length >= 3){
            try{
                int id = Integer.valueOf(temp[1]) - 1;
                if (id>=0 && id<users.size()){
                    String message = "";
                    for (int i=2; i<temp.length; i++)
                        message += temp[i] + " ";
                    User tempUser = users.get(id);
                    sendMessage(tempUser, JSONHelper.makeJson(InteractionType.MESSAGE, ip, port, message).toJSONString());
                } else
                    System.out.println("ERROR: id must be a valid user id. Type 'list' to see connection ids.");
            } catch(NumberFormatException e){
                System.out.println("ERROR: second argument must be an integer.");
            }
        } else
            System.out.println("ERROR: invalid number of arguments with 'send' command.");
    }

    private void displayMessage(String senderIp, int senderPort, String message){
        System.out.println("\nMessage received from IP: " + senderIp);
        System.out.println("Sender's port number: " + senderPort);
        System.out.println("Message: " + message);
    }

    /**
     * When user types "terminate"
     */
    private void terminateValidator(String choice){
        String[] temp = choice.split(" ");
        if (temp.length == 2){
            try{
                int id = Integer.valueOf(temp[1]) - 1;
                if(id>=0 && id<users.size()){
                    User tempUser = users.get(id);
                    sendMessage(tempUser, JSONHelper.makeJson(InteractionType.TERMINATE, ip, port).toJSONString());
                    System.out.println("You dropped connection with ip: " + tempUser.getHost() + " port: " + tempUser.getPort() + ".");
                    terminateConnection(tempUser);
                    users.remove(tempUser);
                    userOutput.remove(tempUser);
                }
            } catch (NumberFormatException e){
                System.out.println("ERROR: second argument must be an integer.");
            }
        } else
            System.out.println("ERROR: invalid number of arguments with 'terminate' command.");
    }

    private void terminateConnection(User tempUser){
        try{
            tempUser.getSocket().close();
            userOutput.get(tempUser).close();
        } catch(IOException e){
        }
    }

    private User findUser(String userIp, int userPort){
        for (User tempUser : users) {
            if (tempUser.getHost().equals(userIp) && tempUser.getPort() == userPort)
                return tempUser;
        }
        return null;
    }

    private void terminationSuccessfulMessage(String userIp, int userPort){
        System.out.println("\nConnection with ip: " + userIp + " port: " + userPort + " has been terminated.");
    }
}
