package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.chat;
import views.html.index;


public class ApplicationController extends Controller {

    public Result index() { return ok(index.render()); }

    public Result chat() { return ok(chat.render()); }

}