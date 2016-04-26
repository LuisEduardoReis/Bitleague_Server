package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helper.ParameterParser;
import play.api.libs.ws.*;
import play.mvc.*;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class AuthenticationController extends Controller{

    @Inject WSClient ws;

    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        JsonNode json = request().body().asJson();
        String args[] = {"id", "access_token"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        /*
        WSRequest access_token_request = ws.url("https://graph.facebook.com/oauth/access_token");
        Future<WSResponse> response = access_token_request.get();

        URL url; URLConnection conn; BufferedReader in; String inputLine;
        try {
            url = new URL("https://graph.facebook.com/oauth/access_token");
            conn = url.openConnection();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();

            url = new URL("https://graph.facebook.com/debug_token");
            conn = url.openConnection();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();

        } catch (Exception e) {
            return internalServerError();
        }

        WSRequest debug_token_request = ws.url("https://graph.facebook.com/debug_token");
        */

        return ok();
    }

}
