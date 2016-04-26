package helper;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;

public class ParameterParser {

    private HashMap<String,String> hash = this.hash = new HashMap<>();;
    public String reason = "";
    public boolean success = false;

    public ParameterParser(JsonNode json, String[] args) {
        success = false;
        for(String arg : args) {
            String value = json.findValue(arg).textValue();
            if (value == null) {reason = "Missing value'" + arg + "'."; return;}
            if (value == "") {reason = "Parameter '" + arg + "' can't be null."; return;}

            hash.put(arg, value);
        }
        success = false;
    }

    public String get(String key) {return hash.get(key);}
}