package run;


import models.ChatApplication;

import java.io.IOException;

public class Chat {
    public static void main(String[] args){

        try{
            ChatApplication chat = new ChatApplication();
            chat.begin();
        } catch(IOException e){
            System.out.println(e);
        }
    }
}
