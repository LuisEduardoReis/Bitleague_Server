package helper;

import com.fasterxml.jackson.databind.JsonNode;
import play.api.libs.json.Json;

import java.util.HashMap;

public class ParameterParser {

    private HashMap<String,String> hash = this.hash = new HashMap<>();;
    public String reason = "";
    public boolean success = false;

    public ParameterParser(JsonNode json, String[] parameters) {
        success = false;
        for(String param : parameters) {
            JsonNode node = json.findValue(param);
            if (node == null) {reason = "Missing value '" + param + "'."; return;}
            String value = node.textValue();
            if (value == "") {reason = "Parameter '" + param + "' can't be null."; return;}

            hash.put(param, value);
        }
        success = true;
    }

    public String get(String key) {return hash.get(key);}

    public static String checkParameters(JsonNode json, String[] parameters) {
        for(String param : parameters) {
            JsonNode node = json.findValue(param);
            if (node == null) {return"Missing value '" + param + "'.";}
        }
        return "ok";
    }
}
