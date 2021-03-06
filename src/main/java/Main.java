import model.CourseIdea;
import model.CourseIdeaDAO;
import model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        staticFileLocation("/public");
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();
        before( (request,response) -> {
            if(request.cookie("username") != null) {
               request.attribute("username",request.cookie("username"));
            }
        });

        before("/ideas", (request,response) -> {
            // TODO: csd - send message about redirect somehow
            if(request.attribute("username") == null) {
                response.redirect("/");
                halt();
            }
        });
//
        get("/", (request, response) -> {
            Map<String,String> model = new HashMap<>();
            model.put("username",request.attribute("username"));
            return new ModelAndView(model,"index.hbs");
        }, new HandlebarsTemplateEngine());

        post("/sign-in",(request, response) -> {
            String username = request.queryParams("username");
            response.cookie("username",username);
            Map<String,String> model = new HashMap<>();
            model.put("username",username);
            return new ModelAndView(model,"sign-in.hbs");
        }, new HandlebarsTemplateEngine());

        get("/ideas", (request, response) -> {
            Map<String,Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());
            return new ModelAndView(model, "ideas.hbs");
        },new HandlebarsTemplateEngine());

        post("/ideas", (request,response)->{
            String title = request.queryParams("title");
            CourseIdea courseIdea = new CourseIdea(title,
                    request.attribute("username"));
            dao.add(courseIdea);
            response.redirect("/ideas");
            return null;
        });

        post("/ideas/:slug/vote", (request,response) -> {
            CourseIdea idea = dao.findBySlug(request.params("slug"));
            idea.addVoter(request.attribute("username"));
            response.redirect("/ideas");
            return null;
        });

    }
}
