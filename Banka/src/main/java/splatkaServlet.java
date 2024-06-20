
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class splatkaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String url = "jdbc:mysql://localhost/";
	private String login = "root";
	private String password = "";
	private String databaseName = "banka";

	public splatkaServlet() {
		super();
		// TODO Auto-generated constructor stub
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {

			String operacia = request.getParameter("operacia");
			if (operacia == null) {
				operacia = "";
			}
			HttpSession session = request.getSession();
			int id = (Integer) session.getAttribute("id");
			id = getLogedUser(request, out);
			if (id == 0)
				return;
			formular(out);
			if (operacia.equals("zaplat")) {
				uloz(out, request, response);
			}

		} catch (Exception e) {
			e.getMessage();

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

	void formular(PrintWriter out) {
		out.println("<form action='splatkaServlet' method='post'>");
		out.println("<input type=\"hidden\" name=\"operacia\" value=\"zaplat\">");
		out.println("Vyska splatky<input type='number' name='splatka'><br>");
		out.println("<input type='submit' value='Zaplat'>");
		out.println("</form>");

	}

	void uloz(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String splatka = request.getParameter("splatka");
		try {
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			HttpSession session = request.getSession();
			int idUser = (Integer) session.getAttribute("id");
			String sql = "SELECT * FROM uver WHERE idUser = " + idUser;
			ResultSet rs = stmt.executeQuery(sql);
			int idUver = 0;
			int aktualnaCena = 0;
			if (rs.next()) {
				idUver = rs.getInt("id");
				aktualnaCena = rs.getInt("aktualnaCena");
			}
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			String datum = dateFormat.format(date);
			String sql2 = "INSERT INTO splatky (idUser, idUver, splatka, datum) VALUES (" + idUser + ", " + idUver
					+ ", " + Integer.parseInt(splatka) + ", '" + datum + "')";
			stmt.executeUpdate(sql2);
			int novaCena=aktualnaCena - Integer.parseInt(splatka);
			String sql3 = "UPDATE uver SET aktualnaCena = " + novaCena + " WHERE id = " + idUver;
			stmt.executeUpdate(sql3);
			stmt.close();
			response.sendRedirect("mainServlet");

		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

}
