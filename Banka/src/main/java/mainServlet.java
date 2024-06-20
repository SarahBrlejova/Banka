

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet implementation class mainServlet
 */
public class mainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String url = "jdbc:mysql://localhost/";
	private String login = "root";
	private String password = "";
	private String databaseName = "banka";
  

    public mainServlet() {
        super();
        
    }
    
    protected Connection spojenie(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			Connection con = (Connection) session.getAttribute("spojenie");
			if (con == null) {
				con = DriverManager.getConnection(url + databaseName, login, password);
				session.setAttribute("spojenie", con);
			}
			return con;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
    
	private void odhlas(PrintWriter out, HttpServletRequest request) {
		HttpSession ses = request.getSession();
		ses.invalidate();
	}
	
	private void foot(PrintWriter out, HttpServletRequest request) {
		out.println("<form action='index.html' method='post'>");
		out.println("<input type='hidden' name='operacia' value='logout'>");
		out.println("<input type='submit' value='Odhlásiť'>");
		out.println("</form><hr>");
		out.println("</html>");
	}

	public void init(ServletConfig config) throws ServletException {
		super.init();
		try {
			Class.forName("com.mysql.cj.jdbc.Driver"); // zaregistrovanie ovladaca

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void destroy() {
		super.destroy();
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			String operacia = request.getParameter("operacia");
			if (operacia == null) {
				operacia = "";
			}
			if (operacia.equals("login")) {
				if (!kontrola(out, request)) {
					vypisNeopravnenyPristup(out);
					return;
				}
			}
			int id = getLogedUser(request, out);
			if (id == 0)
				return;
			if (operacia.equals("logout")) {
				odhlas(out, request);
				response.sendRedirect("/");
				return;
			}
			createHeader(out,request);
			zobraz(out, request);
			
			out.println("<br><br><br><br><br>");
			zobrazSplatky(out, request);
			foot(out, request);
			
		} catch (Exception e) {
			out.println(e);
		}
	}
	protected int getLogedUser(HttpServletRequest request, PrintWriter out) {
		HttpSession ses = request.getSession();
		int id = (Integer) ses.getAttribute("id");
		if (id == 0) {
			out.println("Neprihlásený user");
			vypisNeopravnenyPristup(out);
		}
		return id;
	}

	protected void vypisNeopravnenyPristup(PrintWriter out) {
		out.println("Nemáš právo tu byť...");
		out.println("<a href=\"index.html\"> Prihlasenie</a>");
	}
	
	boolean kontrola(PrintWriter out, HttpServletRequest request) {
		try {
			String login = request.getParameter("login");
			String heslo = request.getParameter("heslo");
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			String SQL = "SELECT * FROM users WHERE login='" + login + "' and heslo='" + heslo + "'";
			ResultSet rs = stmt.executeQuery(SQL);
			HttpSession session = request.getSession();
			if (rs.next()) {;				
				session.setAttribute("id", rs.getInt("id"));
				//hodnota nemusi byt object lebo funguje tu nieco automaticke a prevedie primitivnu hodnotu na object
				session.setAttribute("login", rs.getString("login"));
				session.setAttribute("heslo", rs.getString("heslo"));
				session.setAttribute("meno", rs.getString("meno"));
				session.setAttribute("priezvisko", rs.getString("priezvisko"));
				session.setAttribute("bankar", rs.getString("je_bankar"));
				stmt.close();
				return true;
			} else {
				out.println("zle prihlasenie");
				session.invalidate();
				return false;
			}

		} catch (Exception e) {
			e.getMessage();
			return false;
		}
	}
	
	private void createHeader(PrintWriter out, HttpServletRequest request) {
		HttpSession ses = request.getSession();
		out.println("<html>");
		out.println("<head>");
		out.println("<meta charset=\"UTF-8\">");
		out.println("<title>Zobrazenie</title>");
		out.println("</head>");
		String vypis = (String) ses.getAttribute("meno") + " " + (String) ses.getAttribute("priezvisko");
		out.println(vypis);
		out.println("<form action='splatkaServlet' method='post'>");
		out.println("<input type='submit' value='Zaplat splatku'>");
		out.println("</form><hr>");
	}

	void zobraz(PrintWriter out, HttpServletRequest request) {
		try {
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			HttpSession session = request.getSession();
			int id=(int) session.getAttribute("id");
			String sql = "SELECT * FROM uver WHERE idUser = " + id;
			ResultSet rs = stmt.executeQuery(sql);
			out.println("<table>");
			out.println("<tr>");
			out.println("<th>Vyska hypoteky</th>");
			out.println("<th>Kolko treba doplatit</th>");
			out.println("<th>stav</th>");
			out.println("</tr>");
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>" +rs.getString("hypoteka")  + "</td>");
				out.println("<td>" +rs.getString("aktualnaCena")  + "</td>");
				out.println("<td>" +rs.getString("stav")  + "</td>");
				out.println("</tr>");
			}
			out.println("</table>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void zobrazSplatky(PrintWriter out, HttpServletRequest request) {
		 try {
		        Connection con = spojenie(request);
		        Statement stmt = con.createStatement();
		        HttpSession session = request.getSession();
		        int idUser = (int) session.getAttribute("id");
		        String sql = "SELECT * FROM splatky WHERE idUser = " + idUser;
		        ResultSet rs = stmt.executeQuery(sql);
		        out.println("<h2>Všetky splátky</h2>");
		        out.println("<table>");
		        out.println("<tr>");
		        out.println("<th>Výška splátky</th>");
		        out.println("<th>Dátum</th>");
		        out.println("</tr>");

		        while (rs.next()) {
		            out.println("<tr>");
		            out.println("<td>" + rs.getInt("splatka") + "</td>");
		            out.println("<td>" + rs.getString("datum") + "</td>");
		            out.println("</tr>");
		        }

		        out.println("</table>");
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	}


}
