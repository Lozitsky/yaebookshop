package com.kirilo.java.eshop.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseServlet extends HttpServlet {
    protected String password;
    protected String username;
    protected String databaseURL;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final ServletContext servletContext = config.getServletContext();
        databaseURL = servletContext.getInitParameter("databaseURL");
        username = servletContext.getInitParameter("username");
        password = servletContext.getInitParameter("password");
    }

    //    https://refactoring.guru/design-patterns/template-method/java/example
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;");
        resp.setCharacterEncoding("UTF-8");

        //            https://stackoverflow.com/a/8106090/9586230
        //            https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-connect-drivermanager.html
        try (PrintWriter out = resp.getWriter()) {
            //            createPageTop(out);
            out.println("<html><head><title>" + createTitle() + "</title></head><body>");
            out.println("<h2>" + createHeader() + "</h2>");
            if (validation(req, out)) {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                try (Connection connection = DriverManager.getConnection(databaseURL, username, password);
                     final Statement statement = connection.createStatement()) {

                    createDynamicPageBody(out, statement);

                    out.println("</body></html>");

                } catch (SQLException throwables) {
                    out.println("<h3>Service not available. Please try again later!</h3></body></html>");
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Connection error!", throwables);
                }
            }
        } catch (IllegalAccessException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "IllegalAccess!", e);
        } catch (InstantiationException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "InstantiationException!", e);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ClassNotFound!", e);
        }
    }

    protected abstract void createDynamicPageBody(PrintWriter out, Statement statement) throws SQLException;

    protected abstract String createHeader();

    protected abstract String createTitle();

    protected abstract boolean validation(HttpServletRequest req, PrintWriter out);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
