package models;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONHelper {

    public static String parse(String jsonString, String key){
        JSONParser parser = new JSONParser();
        try{
            JSONObject object = (JSONObject) parser.parse(jsonString);
            return object.get(key).toString();
        }catch (ParseException e){
            return null;
        }
    }

    public static JSONObject makeJson(InteractionType it, String ip, int port){
        JSONObject object = new JSONObject();
        object.put("interaction_type", it.name());
        object.put("ip", ip);
        object.put("port", port);
        return object;
    }

    public static JSONObject makeJson(InteractionType it, String ip, int port, String message){
        JSONObject object = makeJson(it, ip, port);
        object.put("message", message);
        return object;
    }
}
