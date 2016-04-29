package controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.*;

import com.fasterxml.jackson.databind.JsonNode;
import org.jongo.MongoCursor;

import models.User;


public class UserController extends Controller {

    // User

    public Result getUsers(int page, int page_size) {
        MongoCursor<User> users = User.users()
                .find().skip(page*page_size).limit(page_size)
                .as(User.class);

        return ok(Json.toJson(users));
    }

    public Result getUser(String id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");
        User user = User.findById(id);
        if (user == null) return notFound("User not found");
        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");
        if (!(user_t.isAdmin || user_t.id.equals(user.id))) return unauthorized("Invalid authorization token!");

        return ok(Json.toJson(user));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser() {
        JsonNode json = request().body().asJson();
        String name = json.findPath("name").textValue();
        if (name == null)  return badRequest("Missing parameter 'name'.");
        if (name == "")  return badRequest("Parameter 'name' can't be null.");
        if (User.findByName(name) != null) return badRequest("User " + name + " already exists.");

        User user = new User();
        user.name = name;
        user.insert();

        return created("User created");
    }

    public Result deleteUser(String id) {
        User user = User.findById(id);
        if (user != null) {
            user.remove();
            return ok();
        } else
            return notFound("User not found");
    }
}
