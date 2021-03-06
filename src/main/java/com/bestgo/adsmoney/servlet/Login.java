package com.bestgo.adsmoney.servlet;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Created by jikai on 5/31/17.
 */
@WebServlet(name = "login", urlPatterns = "/login")
public class Login extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");

        if ("money999".equals(user) && "money999".equals(pass)) {
            HttpSession session = request.getSession();
            session.setAttribute("isAdmin", true);
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setPath("/");
            cookie.setMaxAge(-1);
            response.addCookie(cookie);
            JsonObject json = new JsonObject();
            json.addProperty("ret", 1);
            json.addProperty("jsp", "index.jsp");
            response.getWriter().write(json.toString());
        } else if ("game".equals(user) && "supergame".equals(pass)) {
            HttpSession session = request.getSession();
            session.setAttribute("isvisitor", true);
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setPath("/");
            cookie.setMaxAge(-1);
            response.addCookie(cookie);
            JsonObject json = new JsonObject();
            json.addProperty("ret", 1);
            json.addProperty("jsp", "app_trend.jsp");
            response.getWriter().write(json.toString());
        } else {
            JsonObject json = new JsonObject();
            json.addProperty("ret", 0);
            response.getWriter().write(json.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
