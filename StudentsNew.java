package st2169;

import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/studentsnew")
public class StudentsNew extends HttpServlet {

    private String driver = "org.sqlite.JDBC";
    private String dbURL = "jdbc:sqlite:C:/TED/workspace/eclipse/2169/st2169/src/main/webapp/WEB-INF/teddb.db";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String editId = req.getParameter("editId");
        res.setContentType("text/html");
        res.setCharacterEncoding("utf-8");
       
        if (editId != null && !editId.isEmpty()) {
            try {
                showEditForm(res.getWriter(), editId);
            } catch (SQLException e) {
                res.getWriter().println(e.toString());
            }
            return;
        }

        String qry = "select id, first_name, last_name, semester, email from students";
        String[] columns = new String[]{"id", "first_name", "last_name", "semester", "email"};
        String[] columnsVisible = new String[]{"ΑΜ", "ΟΝΟΜΑ", "ΕΠΩΝΥΜΟ", "ΕΞΑΜΗΝΟ", "EMAIL"};

        Connection dbCon;

        res.setContentType("text/html");
        res.setCharacterEncoding("utf-8");
        PrintWriter out = res.getWriter();

        try {
            Class.forName(driver);
            dbCon = DriverManager.getConnection(dbURL);
            ResultSet rs;
            Statement stmt;
            stmt = dbCon.createStatement();
            rs = stmt.executeQuery(qry);
            out.println("<!DOCTYPE html><html><body>");

            printForm(out);
            printAnyError(out, req);

            
            out.println("<hr/>");
            out.println("<table border=1><tr>");
            for (int i = 0; i < columns.length; i++) {
                out.print("<td><b>");
                out.print(columnsVisible[i]);
                out.print("</b></td>");
            }
            out.print("<td><b></b></td><td><b></b></td></tr>");

            while (rs.next()) {
                out.println("<tr>");
                for (int i = 0; i < columns.length; i++) {
                    out.println("<td>");
                    out.println(rs.getString(columns[i]));
                    out.println("</td>");
                }
                
                out.println("<td><form action=\"studentsnew\" method=\"GET\">"
                        + "<input type=\"hidden\" name=\"editId\" value=\"" + rs.getString("id") + "\">"
                        + "<input type=\"submit\" value=\"Edit\"></form></td>");
                out.println("<td><form action=\"studentsnew\" method=\"POST\">"
                        + "<input type=\"hidden\" name=\"action\" value=\"Delete\">"
                        + "<input type=\"hidden\" name=\"am\" value=\"" + rs.getString("id") + "\">"
                        + "<input type=\"submit\" value=\"Delete\"></form></td>");

                out.println("</tr>\n");
            }

            out.println("</table></body></html>");

            rs.close();
            stmt.close();
            dbCon.close();

        } catch (Exception e) {
            out.println(e.toString());
        } finally {
            out.close();
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String qry = "insert into students (id, first_name, last_name, semester, email) values (? ,? ,? ,?, ?)";

        Connection dbCon;

        req.setCharacterEncoding("utf-8");

        String action = req.getParameter("action");
        String am = req.getParameter("am");
        String onoma = req.getParameter("onoma");
        String eponimo = req.getParameter("eponimo");
        String examino = req.getParameter("examino");
        String email = req.getParameter("email");

        try {
            Class.forName(driver);
            dbCon = DriverManager.getConnection(dbURL);

            if ("Edit".equals(action)) {
               
                res.sendRedirect("editPage?am=" + am);
            } else if ("Delete".equals(action)) {
               
                String deleteQuery = "DELETE FROM students WHERE id = ?";
                PreparedStatement deleteStmt = dbCon.prepareStatement(deleteQuery);
                deleteStmt.setString(1, am);
                int rowsDeleted = deleteStmt.executeUpdate();
                System.out.println("Deleted " + rowsDeleted + " row(s)");
                res.sendRedirect("studentsnew");
            } else if ("Update".equals(action)) {
                
                PreparedStatement updateStmt = dbCon.prepareStatement("UPDATE students SET first_name=?, last_name=?, semester=?, email=? WHERE id=?");
                updateStmt.setString(1, onoma);
                updateStmt.setString(2, eponimo);
                updateStmt.setString(3, examino);
                updateStmt.setString(4, email);
                updateStmt.setString(5, am);

                int rowsUpdated = updateStmt.executeUpdate();
                System.out.println("Updated " + rowsUpdated + " row(s)");

                res.sendRedirect("studentsnew");
            } else {
               
                PreparedStatement stmt;
                stmt = dbCon.prepareStatement(qry);
                stmt.setString(1, am);
                stmt.setString(2, onoma);
                stmt.setString(3, eponimo);
                stmt.setString(4, examino);
                stmt.setString(5, email);

                int i = stmt.executeUpdate();
                System.out.println("Inserted " + i + " row(s)");

                res.sendRedirect("studentsnew");
            }

        } catch (Exception e) {
            res.sendRedirect("studentsnew?errormsg=" +
                    java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));

        }
    }

    void printForm(PrintWriter out) {
        out.println("<form action=\"studentsnew\" method=\"POST\">");
        out.println("<b> Παρακαλώ δώστε τα ακόλουθα στοιχεία: </b> I<br>");
        out.println("<b> Όνομα :  </b> <input type=\"text\" name=\"onoma\" ><br>");
        out.println("<b> Επώνυμο :  </b> <input type=\"text\" name=\"eponimo\" ><br>");
        out.println("<b> Αριθμός Μητρώου: </b> <input type=\"text\" name=\"am\" ><br>");
        out.println("<b> Εξάμηνο: </b>  <input type=\"text\" name=\"examino\" ><br>");
        out.println("<b> Email: </b> <input type=\"text\" name=\"email\" ><br>");
        out.println("<input type=\"submit\"  value=\"Insert\"> ");
        out.println("</form>");
    }

    void printAnyError(PrintWriter out, HttpServletRequest req) {
        String errorMessage = req.getParameter("errormsg");
        if (errorMessage != null) {
            out.println("<br><strong style=\"color:red\"> Error:Παρακαλώ εισάγεται ένα μη υπάρχον ΑΜ " +"</strong>");
        }
    }

    private void showEditForm(PrintWriter out, String editId) throws SQLException {
        String query = "SELECT * FROM students WHERE id = ?";
        
        try (Connection dbCon = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = dbCon.prepareStatement(query)) {
            stmt.setString(1, editId);
            ResultSet rs = stmt.executeQuery();
          
            if (rs.next()) {
            	
                out.println("<!DOCTYPE html><html><body>");
                out.println("<form action=\"studentsnew\" method=\"POST\">");
                out.println("<b> Παρακαλώ επεξεργαστείτε τα ακόλουθα στοιχεία: </b> <br>");
                out.println("<b> Όνομα :  </b> <input type=\"text\" name=\"onoma\" value=\"" + rs.getString("first_name") + "\"><br>");
                out.println("<b> Επώνυμο :  </b> <input type=\"text\" name=\"eponimo\" value=\"" + rs.getString("last_name") + "\"><br>");
                out.println("<b> Αριθμός Μητρώου: </b> <input type=\"text\" name=\"am\" value=\"" + rs.getString("id") + "\" readonly><br>");
                out.println("<b> Εξάμηνο: </b>  <input type=\"text\" name=\"examino\" value=\"" + rs.getString("semester") + "\"><br>");
                out.println("<b> Email: </b> <input type=\"text\" name=\"email\" value=\"" + rs.getString("email") + "\"><br>");
                out.println("<input type=\"hidden\" name=\"action\" value=\"Update\">");
                out.println("<input type=\"submit\"  value=\"Update\"> ");
                out.println("</form>");
                out.println("</body></html>");
            }
        }
    }
}
