package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helper.ParameterParser;
import models.User;
import org.jongo.MongoCursor;
import play.Logger;
import play.api.libs.ws.*;
import play.libs.Json;
import play.mvc.*;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;


public class AuthenticationController extends Controller{

    private String appAccessToken = null;
    private String appClientId = "1001329453281741";
    private String appClientSecret = "aaa9b8bf2a7e4c5463831e644676d39b";

    private SecureRandom random = new SecureRandom();
    private String nextToken() {
        return new BigInteger(250, random).toString(32);
    }

    private String getAppAccessToken() {
        if (appAccessToken != null) return appAccessToken;
        try {
            String access_token_url = "https://graph.facebook.com/oauth/access_token";
            access_token_url += "?client_id=" + appClientId;
            access_token_url += "&client_secret=" + appClientSecret;
            access_token_url += "&grant_type=client_credentials";
            URL url = new URL(access_token_url);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String res = in.readLine();
            in.close();
            appAccessToken = res.split("=")[1];
            return appAccessToken;
        } catch(Exception e) {
            return "";
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        JsonNode json = request().body().asJson();
        String args[] = {"id", "access_token"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        JsonNode fb_json;
        try {
            String debug_token_url = "https://graph.facebook.com/debug_token";
            debug_token_url += "?input_token=" + params.get("access_token");
            debug_token_url += "&access_token=" + getAppAccessToken();
            URL url = new URL(debug_token_url);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String res = in.readLine();
            in.close();

            fb_json = Json.parse(res);

            if (!fb_json.findValue("data").findValue("app_id").textValue().equals(appClientId)) return badRequest("Invalid Token!");
            if (!fb_json.findValue("user_id").textValue().equals(params.get("id"))) return badRequest("Invalid User!");
        } catch (Exception e) {
            return internalServerError(e.toString());
        }
        String fb_id = fb_json.findValue("user_id").textValue();
        User user = User.findOrCreateByFacebookId(fb_id);

        String token = nextToken();
        user.token = token;
        user.insert();

        response().setHeader("Access-Control-Allow-Origin", "*");
        return ok(token);
    }

    public static boolean checkUserToken(String user_id, String token) {
        return User.findByToken(token).id.equals(user_id);
    }


}
