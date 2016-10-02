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
                    else{
                        //do something
                    }
                    break;
                case "list":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else{
                        //do something
                    }
                    break;
                case "terminate":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else{
                        //do something
                    }
                    break;
                case "send":
                    if (socket == null)
                        System.out.println("ERROR: Not connected");
                    else{
                        //do something
                    }
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("That is not a valid command. Type 'help' for a list of commands.");
            }
        }
    }

    private void initiateChat(String ui){
        
    }

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
                    String ip = JSONHelper.parse(jsonString, "ip");
                    int port = Integer.valueOf(JSONHelper.parse(jsonString, "port"));

                    InteractionType it = InteractionType.valueOf(JSONHelper.parse(jsonString, "interaction_type"));
                    switch(it){
                        case CONNECT:
                            break;
                        case MESSAGE:
                            break;
                        case TERMINATE:
                            break;

                    }

                }
            } catch (IOException e){
                System.out.println("Message: Connection dropped.");
            }
        }
    }

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

    private boolean isValidConnectString(String input){
        String[] connectString = input.split(" ");
        return (connectString.length == 3 && connectString[0].equals("connect") && isValidPort(connectString[2]));
    }

}
