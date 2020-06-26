package com.kirilo.java.eshop.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryServlet extends HttpServlet {

    private String password;
    private String username;
    private String databaseURL;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final ServletContext servletContext = config.getServletContext();
        databaseURL = servletContext.getInitParameter("databaseURL");
        username = servletContext.getInitParameter("username");
        password = servletContext.getInitParameter("password");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;");
        resp.setCharacterEncoding("UTF-8");
        //            https://stackoverflow.com/a/8106090/9586230
//            https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-connect-drivermanager.html
        try (PrintWriter out = resp.getWriter()) {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            try (Connection connection = DriverManager.getConnection(databaseURL, username, password);
                 final Statement statement = connection.createStatement()) {
                String sqlString = "select distinct author from books where qty > 0";
                final ResultSet resultSet = statement.executeQuery(sqlString);
                out.println("<html><head><title>Welcome to YaEshop</title></head><body>");
                out.println("<h2>Welcome to Yet Another E-BookShop</h2>");
                // Begin an HTML form
                out.println("<form method='get' action='search'>");
                // A pull-down menu of all the authors with a no-selection option
                out.println("Choose an Author: <select name='author' size='1'>");
                out.println("<option value=''>Select...</option>");  // no-selection
                while (resultSet.next()) {  // list all the authors
                    String author = resultSet.getString("author");
                    out.println("<option value='" + author + "'>" + author + "</option>");
                }
                out.println("</select><br />");
                out.println("<p>OR</p>");
                // A text field for entering search word for pattern matching
                out.println("Search \"Title\" or \"Author\": <input type='text' name='search' />");

                // Submit and reset buttons
                out.println("<br /><br />");
                out.println("<input type='submit' value='SEARCH' />");
                out.println("<input type='reset' value='CLEAR' />");
                out.println("</form>");
                out.println("</body></html>");
            } catch (SQLException throwables) {
                out.println("<h3>Service not available. Please try again later!</h3></body></html>");
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Connection error!", throwables);
            }
        } catch (IllegalAccessException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "IllegalAccess!", e);
        } catch (InstantiationException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "InstantiationException!", e);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ClassNotFound!", e);

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
