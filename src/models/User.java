package models;


import java.net.Socket;
import java.net.UnknownHostException;

public class User {

    private String host;
    private int port;
    private Socket socket;

    public User(String host, int port){
        this.host = host;
        this.port = port;

        try{
            socket = new Socket(host, port);
        } catch (UnknownHostException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
