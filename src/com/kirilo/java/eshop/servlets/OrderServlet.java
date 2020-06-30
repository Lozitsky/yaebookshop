package com.kirilo.java.eshop.servlets;

import com.kirilo.java.eshop.utils.InputFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderServlet extends BaseServlet {

    private String[] ids;
    private String custName;
    private String custEmail;
    private String custPhone;

    @Override
    protected boolean createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException {
        // We shall build our output in a buffer, so that it will not be interrupted
        //  by error messages.
        final StringBuilder outBuf = new StringBuilder();
        // Display the name, email and phone (arranged in a table)
        outBuf.append("<table>");
        outBuf.append("<tr><td>Customer Name:</td><td>").append(custName).append("</td></tr>");
        outBuf.append("<tr><td>Customer Email:</td><td>").append(custEmail).append("</td></tr>");
        outBuf.append("<tr><td>Customer Phone Number:</td><td>").append(custPhone).append("</td></tr></table>");

        // Print the book(s) ordered in a table
        outBuf.append("<br />");
        outBuf.append("<table border='1' cellpadding='6'>");
        outBuf.append("<tr><th>AUTHOR</th><th>TITLE</th><th>PRICE</th><th>QTY</th></tr>");

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

            // Validate quantity ordered
            final int qtyOrdered = InputFilter.parsePositiveInt(req.getParameter("qty" + id));
            if (qtyOrdered == 0) {
                out.println("<h3>Please Enter a valid quantity for \"" + title + "\"!</h3>");
                return false;
            } else if (qtyOrdered > qtyAvailable) {
                out.println("<h3>There are insufficient copies of \"" + title + "\" available!</h3>");
                return false;
            } else {
                sqlStr = "UPDATE books SET qty = qty - " + qtyOrdered + " WHERE id = " + id;
                //System.out.println(sqlStr);  // for debugging
                statement.executeUpdate(sqlStr);

                sqlStr = "INSERT INTO order_records values ("
                        + id + ", " + qtyOrdered + ", '" + custName + "', '"
                        + custEmail + "', '" + custPhone + "')";
                //System.out.println(sqlStr);  // for debugging
                statement.executeUpdate(sqlStr);

                // Display this book ordered
                outBuf.append("<tr>");
                outBuf.append("<td>").append(author).append("</td>");
                outBuf.append("<td>").append(title).append("</td>");
                outBuf.append("<td>").append(price).append("</td>");
                outBuf.append("<td>").append(qtyOrdered).append("</td></tr>");
                totalPrice += price * qtyOrdered;
            }
        }

        outBuf.append("<tr><td colspan='4' align='right'>Total Price: $");
        outBuf.append(String.format("%.2f</td></tr>", totalPrice));
        outBuf.append("</table>");

        outBuf.append("<h3>Thank you.</h3>");
        outBuf.append("<p><a href='start'>Back to Select Menu</a></p>");
        out.println(outBuf.toString());
        return true;
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
        boolean hasCustName = custName != null &&
                ((custName = InputFilter.htmlFilter(custName.trim())).length() > 0);
        custEmail = req.getParameter("cust_email");
        boolean hasCustEmail = custEmail != null &&
                ((custEmail = InputFilter.htmlFilter(custEmail.trim())).length() > 0);
        custPhone = req.getParameter("cust_phone");
        boolean hasCustPhone = custPhone != null &&
                ((custPhone = InputFilter.htmlFilter(custPhone.trim())).length() > 0);

        if (ids == null || ids.length == 0) {
            out.println("<h3>Please Select a Book!</h3>");
        } else if (!hasCustName) {
            out.println("<h3>Please Enter Your Name!</h3>");
        } else if (!hasCustEmail || custEmail.indexOf('@') == -1) {
            out.println("<h3>Please Enter Your e-mail (user@host)!</h3>");
        } else if (!hasCustPhone || !InputFilter.isValidPhone(custPhone)) {
            out.println("<h3>Please Enter an 8-digit Phone Number!</h3>");
        } else {
            return true;
        }
        return false;
    }
}
