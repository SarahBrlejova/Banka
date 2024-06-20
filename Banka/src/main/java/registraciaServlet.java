
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

public class registraciaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String url = "jdbc:mysql://localhost/";
	private String login = "root";
	private String password = "";
	private String databaseName = "banka";

	public registraciaServlet() {
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
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		registracnyFormular(out);
		try {
			String registracia = request.getParameter("registracia");
			if (registracia != null && registracia.equals("nove")) {
				String meno = request.getParameter("meno");
				String priezvisko = request.getParameter("priezvisko");
				String prijem = request.getParameter("prijem");
				String vek = request.getParameter("vek");
				String uver = request.getParameter("uver");
				String deti = request.getParameter("deti");
				String cena = request.getParameter("cena");
				String login = request.getParameter("login");
				String heslo = request.getParameter("heslo");
				if (login.isEmpty() || priezvisko.isEmpty() || uver.isEmpty() || prijem.isEmpty() || meno.isEmpty()
						|| vek.isEmpty() || cena.isEmpty() || deti.isEmpty() || heslo.isEmpty()) {
					out.println("Všetky polia musia byť vyplnené.");
					return;
				}
				int vekk = Integer.parseInt(vek);
				if (vekk < 18) {
					out.println("moc mlady");
					return;
				}
				int hypo = Integer.parseInt(uver);
				int body = 0;
				int prijemm = Integer.parseInt(prijem);
				int detii = Integer.parseInt(deti);
				int cenaNehnutelnosti = Integer.parseInt(cena);
				if (vekk >= 18 && vekk <= 35) {
					body += 10;
				} else if (vekk > 35) {
					body += 5;
				}
				if (prijemm <= 1000) {
					body += 10;
				} else if (prijemm > 1000 && prijemm <= 2500) {
					body += 20;
				} else if (prijemm > 2500) {
					body += 30;
				}
				if (detii == 0) {
					body += 5;
				} else if (detii < 3) {
					body += 4;
				} else if (detii > 2) {
					body += 2;
				}
				int bodynehnutelnost = cenaNehnutelnosti / 10000 * 5;
				body += bodynehnutelnost;
				System.out.println(bodynehnutelnost);
				System.out.println(body);
				if (body(hypo, body)) {
					System.out.println("daju");
					uloz(out, request, response, body);
				}

			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void registracnyFormular(PrintWriter out) {
		out.println("<form action='registraciaServlet' method='post'>");
		out.println("<input type=\"hidden\" name=\"registracia\" value=\"nove\">");
		out.println("Meno: <input type='text' name='meno'><br>");
		out.println("Priezvisko: <input type='text' name='priezvisko'><br>");
		out.println("Prijem: <input type='number' name='prijem'><br>");
		out.println("Vek: <input type='number' name='vek'><br>");
		out.println("Deti: <input type='number' name='deti'><br>");
		out.println("Cena zalozenej nehnutelnosti: <input type='number' name='cena'><br>");
		out.println("Vyska uveru <input type='number' name='uver'><br>");
		out.println("Rodne cislo <input type='number' name='login'><br>");
		out.println("Heslo: <input type='password' name='heslo'><br>");
		out.println("<input type='submit' value='Registrovat'>");
		out.println("</form>");
	}

	boolean body(int uver, int body) {
		if (uver <= 50000 && body >= 50) {
			return true;
		}
		if (uver >= 50000 && uver <= 100000 && body >= 60) {
			return true;
		}
		if (uver > 100000 && body >= 100) {
			return true;
		} else
			return false;
	}

	private void uloz(PrintWriter out, HttpServletRequest request, HttpServletResponse response, int body) {
		String meno = request.getParameter("meno");
		String priezvisko = request.getParameter("priezvisko");
		String login = request.getParameter("login");
		String heslo = request.getParameter("heslo");
		String uver = request.getParameter("uver");
		String prijem = request.getParameter("prijem");
		String vek = request.getParameter("vek");
		String deti = request.getParameter("deti");
		String cena = request.getParameter("cena");
		try {
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			String sql = "INSERT INTO users (login, heslo, meno, priezvisko, je_bankar, body) VALUES " + "('"
					+ Integer.parseInt(login) + "', '" + heslo + "', '" + meno + "', '" + priezvisko + "', 0, " + body
					+ ")";
			int pocet = stmt.executeUpdate(sql);

			if (pocet == 1) {
				String sql2 = "SELECT id FROM users WHERE login = '" + Integer.parseInt(login) + "'";
				ResultSet resultSet = stmt.executeQuery(sql2);
				int id;
				if (resultSet.next()) {
					id = resultSet.getInt("id");

					String uverSql = "INSERT INTO uver (idUser, hypoteka, aktualnaCena, stav) VALUES " + "(" + id + ", "
							+ Integer.parseInt(uver) + ", " + Integer.parseInt(uver) + ", 'nevyplatena')";

					stmt.executeUpdate(uverSql);
					DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
					Date date = new Date();
					String formattedDate = dateFormat.format(date);
					String zakaznikSql = "INSERT INTO zakaznik (vek, prijem, deti, zalozenydom, datum, idUser) VALUES "
							+ "(" + Integer.parseInt(vek) + ", " + Integer.parseInt(prijem) + ", "
							+ Integer.parseInt(deti) + ", " + cena + ", '" + formattedDate + "', " + id + ")";
					stmt.executeUpdate(zakaznikSql);

				}
			}
			stmt.close();
			response.sendRedirect("index.html");
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

}
