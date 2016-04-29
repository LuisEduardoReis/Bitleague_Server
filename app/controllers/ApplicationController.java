package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import org.jongo.MongoCursor;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.chat;


public class ApplicationController extends Controller {

    public Result index() { return ok(index.render()); }

    public Result chat() { return ok(chat.render()); }

}