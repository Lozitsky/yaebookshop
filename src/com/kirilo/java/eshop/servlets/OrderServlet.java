package com.kirilo.java.eshop.servlets;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderServlet extends BaseServlet{

    private String[] ids;
    private String custName;
    private String custEmail;
    private String custPhone;

    @Override
    protected void createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException {
        // Display the name, email and phone (arranged in a table)
        out.println("<table>");
        out.println("<tr><td>Customer Name:</td><td>" + custName + "</td></tr>");
        out.println("<tr><td>Customer Email:</td><td>" + custEmail + "</td></tr>");
        out.println("<tr><td>Customer Phone Number:</td><td>" + custPhone + "</td></tr></table>");

        // Print the book(s) ordered in a table
        out.println("<br />");
        out.println("<table border='1' cellpadding='6'>");
        out.println("<tr><th>AUTHOR</th><th>TITLE</th><th>PRICE</th><th>QTY</th></tr>");

        float totalPrice = 0f;
        for (String id : ids) {
            String sqlStr = "SELECT * FROM books WHERE id = " + id;
            //System.out.println(sqlStr);  // for debugging
            ResultSet rset = statement.executeQuery(sqlStr);

            // Expect only one row in ResultSet
            rset.next();
            int qtyAvailable = rset.getInt("qty");
            String title = rset.getString("title");
            String author = rset.getString("author");
            float price = rset.getFloat("price");

            int qtyOrdered = Integer.parseInt(req.getParameter("qty" + id));
            sqlStr = "UPDATE books SET qty = qty - " + qtyOrdered + " WHERE id = " + id;
            //System.out.println(sqlStr);  // for debugging
            statement.executeUpdate(sqlStr);

            sqlStr = "INSERT INTO order_records values ("
                    + id + ", " + qtyOrdered + ", '" + custName + "', '"
                    + custEmail + "', '" + custPhone + "')";
            //System.out.println(sqlStr);  // for debugging
            statement.executeUpdate(sqlStr);

            // Display this book ordered
            out.println("<tr>");
            out.println("<td>" + author + "</td>");
            out.println("<td>" + title + "</td>");
            out.println("<td>" + price + "</td>");
            out.println("<td>" + qtyOrdered + "</td></tr>");
            totalPrice += price * qtyOrdered;
        }

        out.println("<tr><td colspan='4' align='right'>Total Price: $");
        out.printf("%.2f</td></tr>", totalPrice);
        out.println("</table>");

        out.println("<h3>Thank you.</h3>");
        out.println("<p><a href='start'>Back to Select Menu</a></p>");
    }

    @Override
    protected String createHeader() {
        return "YAEBS - Order Confirmation";
    }

    @Override
    protected String createTitle() {
        return "Order Confirmation";
    }

    @Override
    protected boolean validation(HttpServletRequest req, PrintWriter out) {
        // Possibly more than one values
        ids = req.getParameterValues("id");
        custName = req.getParameter("cust_name");
        boolean hasCustName = custName != null && ((custName = custName.trim()).length() > 0);
        custEmail = req.getParameter("cust_email");
        boolean hasCustEmail = custEmail != null && ((custEmail = custEmail.trim()).length() > 0);
        custPhone = req.getParameter("cust_phone");
        boolean hasCustPhone = custPhone != null && ((custPhone = custPhone.trim()).length() > 0);

        if (ids == null || ids.length == 0) {
            out.println("<h3>Please Select a Book!</h3>");
        } else if (!hasCustName) {
            out.println("<h3>Please Enter Your Name!</h3>");
        } else if (!hasCustEmail || (custEmail.indexOf('@') == -1)) {
            out.println("<h3>Please Enter Your e-mail (user@host)!</h3>");
        } else if (!hasCustPhone || (custPhone.length() != 8)) {
            out.println("<h3>Please Enter an 8-digit Phone Number!</h3>");
        } else {
            return true;
        }
        return false;
    }
}
