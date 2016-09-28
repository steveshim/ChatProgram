package models;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
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
}
