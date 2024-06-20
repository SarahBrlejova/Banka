
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

/**
 * Servlet implementation class adminServlet
 */
public class adminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String url = "jdbc:mysql://localhost/";
	private String login = "root";
	private String password = "";
	private String databaseName = "banka";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public adminServlet() {
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
		super.init(config);
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			String operaciaAdmin = request.getParameter("operaciaAdmin");
			if (operaciaAdmin == null) {
				operaciaAdmin = "";
			}
			if (operaciaAdmin.equals("login")) {
				if (!kontrola(out, request)) {
					vypisNeopravnenyPristup(out);
					return;
				}
			}
			int id = getLogedUser(request, out);
			if (id == 0)
				return;


			out.print("<br> <br><br>");
			zobrazZakaznikov(out, request);
			out.print("<br> <br><br>");
			zobrazUver(out, request);
			out.print("<br> <br><br>");
			zobrazUser(out, request);
			out.print("<br> <br><br>");
			zobrazpodrobnosti(out, request);
			if (operaciaAdmin.equals("updateStav")) {
				updateStav(out, request, response);
			}
			if (operaciaAdmin.equals("updateCena")) {
				updateCena(out, request, response);
			}
			if (operaciaAdmin.equals("updateHypo")) {
				updateHypo(out, request, response);
			}
			if (operaciaAdmin.equals("updatePravo")) {
				updatePravo(out, request, response);
			}
			if (operaciaAdmin.equals("vymazUver")) {
				vymazUver(out, request, response);
			}
			if (operaciaAdmin.equals("vymazUser")) {
				vymazUser(out, request, response);
			}
			if (operaciaAdmin.equals("pridaj")) {
				pridaj(out, request, response);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void odhlas(PrintWriter out, HttpServletRequest request) {
		HttpSession ses = request.getSession();
		ses.invalidate();
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
			String SQL = "SELECT * FROM users WHERE login='" + login + "' and heslo='" + heslo + "' and je_bankar = 1";
			ResultSet rs = stmt.executeQuery(SQL);
			HttpSession session = request.getSession();
			if (rs.next()) {
				session.setAttribute("id", rs.getInt("id"));
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
	
	
	void zobrazZakaznikov(PrintWriter out, HttpServletRequest request) {
		try {
	        Connection con = spojenie(request);
	        Statement stmt = con.createStatement();
	        String sql = "SELECT * FROM users INNER JOIN uver ON users.id = uver.idUser";
	        ResultSet rs = stmt.executeQuery(sql);
	        out.println("<h2>Zoznam zákazníkov a uverov</h2>");
	        out.println("<table>");
	        out.println("<tr>");
	        out.println("<th>ID</th>");
	        out.println("<th>Login</th>");
	        out.println("<th>Meno</th>");
	        out.println("<th>Priezvisko</th>");
	        out.println("<th>Je Bankar</th>");
	        out.println("<th>Body</th>");
	        out.println("<th>Hypo</th>");
	        out.println("<th>Zmen hypo</th>");
	        out.println("<th></th>");
	        out.println("<th>Doplatit</th>");
	        out.println("<th>Zadaj vysku na splatenie</th>");
	        out.println("<th></th>");
	        out.println("<th>stav</th>");
	        out.println("<th>Zmen stav</th>");
	        out.println("</tr>");
	        while (rs.next()) {
	            out.println("<tr>");
	            out.println("<td>" + rs.getInt("users.id") + "</td>");
	            out.println("<td>" + rs.getInt("login") + "</td>");
	            out.println("<td>" + rs.getString("meno") + "</td>");
	            out.println("<td>" + rs.getString("priezvisko") + "</td>");
	            out.println("<td>" + rs.getInt("je_bankar") + "</td>");
	            out.println("<td>" + rs.getInt("body") + "</td>");
	            out.println("<td>" + rs.getInt("hypoteka") + "</td>");
	            out.println("<form action = 'adminServlet' method = 'post'>");
				out.println("<td>");
				out.println("<input type='hidden' name='idUver' value='" + rs.getString("uver.id") + "'>");
				out.println("<input type='number' name='hypo' ");
				out.println("</td>");
				out.println("<td>");
				out.println("<input type='hidden' name='operaciaAdmin' value='updateHypo'>");
				out.println("<input type='submit' value='Zmen hypo'>");
				out.println("</td>");
				out.println("</form>");
	            out.println("<td>" + rs.getInt("aktualnaCena") + "</td>");
	            out.println("<form action = 'adminServlet' method = 'post'>");
				out.println("<td>");
				out.println("<input type='hidden' name='idUver' value='" + rs.getString("uver.id") + "'>");
				out.println("<input type='number' name='cena' ");
				out.println("</td>");
				out.println("<td>");
				out.println("<input type='hidden' name='operaciaAdmin' value='updateCena'>");
				out.println("<input type='submit' value='Zmen cenu'>");
				out.println("</td>");
				out.println("</form>");
	            out.println("<td>" + rs.getString("stav") + "</td>");
				out.println("<form action = 'adminServlet' method = 'post'>");
				out.println("<td>");
				out.println("<input type='hidden' name='idUver' value='" + rs.getString("uver.id") + "'>");
				out.println("<select id='stav' name='stav'>");
				out.println("<option value='nevyplatena'>nevyplatena</option>");
				out.println("<option value='vyplatena'>vyplatena</option>");
				out.println("</select>");
				out.println("</td>");
				out.println("<td>");
				out.println("<input type='hidden' name='operaciaAdmin' value='updateStav'>");
				out.println("<input type='submit' value='Zmen stav'>");
				out.println("</td>");
				out.println("</form>");
	            out.println("</tr>");
	        }
	        out.println("</table>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	void zobrazpodrobnosti(PrintWriter out, HttpServletRequest request) {
		try {
	        Connection con = spojenie(request);
	        Statement stmt = con.createStatement();
	        String sql = "SELECT * FROM users INNER JOIN zakaznik ON users.id = zakaznik.idUser";
	        ResultSet rs = stmt.executeQuery(sql);
	        out.println("<h2>Podrobnosti o zakaznikoch</h2>");
	        out.println("<table>");
	        out.println("<tr>");
	        out.println("<th>Meno</th>");
	        out.println("<th>Priezvisko</th>");
	        out.println("<th>Vek</th>");
	        out.println("<th>deti</th>");
	        out.println("<th>prijem</th>");
	        out.println("<th>zalozeny dom</th>");
	        out.println("<th>datum</th>");
	        out.println("<th>vyska hypo</th>");
	        out.println("<th>rodne cislo</th>");
	        out.println("<th>heslo</th>");
	        out.println("<th>body</th>");
	        out.println("</tr>");
	        while (rs.next()) {
	            out.println("<tr>");
	            out.println("<td>" + rs.getString("meno") + "</td>");
	            out.println("<td>" + rs.getString("priezvisko") + "</td>");
	            out.println("<td>" + rs.getInt("vek") + "</td>");
	            out.println("<td>" + rs.getInt("deti") + "</td>");
	            out.println("<td>" + rs.getInt("prijem") + "</td>");
	            out.println("<td>" + rs.getInt("zalozenydom") + "</td>");
	            out.println("<td>" + rs.getDate("datum") + "</td>");
	            out.println("<td> </td>");
	            out.println("<td>" + rs.getInt("login") + "</td>");
	            out.println("<td> </td>");
	            out.println("<td>" + rs.getInt("body") + "</td>");
	            out.println("</tr>");
	        }
	        out.println("<tr>"); 
	        out.println("<form action='adminServlet' method='post'>");
	        out.println("<input type=\"hidden\" name=\"operaciaAdmin\" value=\"pridaj\">");
	        out.println("<td><input type='text' name='meno'></td>");
	        out.println("<td><input type='text' name='priezvisko'></td>");
	        out.println("<td><input type='number' name='vek'></td>");
	        out.println("<td><input type='number' name='deti'></td>");
	        out.println("<td><input type='number' name='prijem'></td>");
	        out.println("<td><input type='number' name='cena'></td>");
	        out.println("<td><input type='date' name='datum'></td>");
	        out.println("<td><input type='number' name='uver'></td>");
	        out.println("<td><input type='number' name='login'></td>");
	        out.println("<td><input type='password' name='heslo'></td>");
	        out.println("<td><input type='number' name='body'></td>");
	        out.println("<td><input type='submit' value='Pridaj'></td>");
	        out.println("</form>");
	        out.println("</tr>");
	        out.println("</table>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	void zobrazUver(PrintWriter out, HttpServletRequest request) {
		try {
	        Connection con = spojenie(request);
	        Statement stmt = con.createStatement();
	        String sql = "SELECT * FROM uver ";
	        ResultSet rs = stmt.executeQuery(sql);
	        out.println("<h2>Zoznam uverov</h2>");
	        out.println("<table>");
	        out.println("<tr>");
	        out.println("<th>ID</th>");
	        out.println("<th>idUSER</th>");
	        out.println("<th>Hypoteka</th>");
	        out.println("<th>Doplatit</th>");
	        out.println("<th>Stav</th>");
	        out.println("</tr>");
	        while (rs.next()) {
	            out.println("<tr>");
	            out.println("<td>" + rs.getInt("id") + "</td>");
	            out.println("<td>" + rs.getInt("idUser") + "</td>");
	            out.println("<td>" + rs.getInt("hypoteka") + "</td>");
	            out.println("<td>" + rs.getInt("aktualnaCena") + "</td>");
	            out.println("<td>" + rs.getString("stav") + "</td>");
	            out.println("<form action = 'adminServlet' method = 'post'>");
				out.println("<td>");
				out.println("<input type='hidden' name='idUver' value='" + rs.getString("id") + "'>");
				out.println("</td>");
				out.println("<td>");
				out.println("<input type='hidden' name='operaciaAdmin' value='vymazUver'>");
				out.println("<input type='submit' value='Vymaz Uver'>");
				out.println("</td>");
				out.println("</form>");
	            out.println("</tr>");
	        }
	        out.println("</table>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	void zobrazUser(PrintWriter out, HttpServletRequest request) {
		try {
	        Connection con = spojenie(request);
	        Statement stmt = con.createStatement();
	        String sql = "SELECT * FROM users ";
	        ResultSet rs = stmt.executeQuery(sql);
	        out.println("<h2>Zoznam userov</h2>");
	        out.println("<table>");
	        out.println("<tr>");
	        out.println("<th>ID</th>");
	        out.println("<th>login</th>");
	        out.println("<th>meno</th>");
	        out.println("<th>priezvisko</th>");
	        out.println("<th>pravomoc</th>");
	        out.println("</tr>");
	        while (rs.next()) {
	            out.println("<tr>");
	            out.println("<td>" + rs.getInt("id") + "</td>");
	            out.println("<td>" + rs.getInt("login") + "</td>");
	            out.println("<td>" + rs.getString("meno") + "</td>");
	            out.println("<td>" + rs.getString("priezvisko") + "</td>");
	            out.println("<td>" + rs.getInt("je_bankar") + "</td>");
	            out.println("<form action = 'adminServlet' method = 'post'>");
				out.println("<td>");
				out.println("<input type='hidden' name='idUser' value='" +  rs.getInt("id") + "'>");
				out.println("<select id='stav' name='stav'>");
				out.println("<option value='0'>0</option>");
				out.println("<option value='1'>1</option>");
				out.println("</select>");
				out.println("</td>");
				out.println("<td>");
				out.println("<input type='hidden' name='operaciaAdmin' value='updatePravo'>");
				out.println("<input type='submit' value='Zmen pravomoc'>");
				out.println("</td>");
				out.println("</form>");
	            out.println("<form action = 'adminServlet' method = 'post'>");
				out.println("<td>");
				out.println("<input type='hidden' name='idUser' value='" + rs.getString("id") + "'>");
				out.println("</td>");
				out.println("<td>");
				out.println("<input type='hidden' name='operaciaAdmin' value='vymazUser'>");
				out.println("<input type='submit' value='Vymaz User'>");
				out.println("</td>");
				out.println("</form>");
	            out.println("</tr>");
	        }
	        out.println("</table>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	protected void updateStav(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String stav = request.getParameter("stav");
		String idUver = request.getParameter("idUver");
		try {
			HttpSession ses = request.getSession();
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			String sql = "UPDATE uver SET stav = '" + stav + "' WHERE id = " + idUver;
			System.out.println(sql);
			stmt.executeUpdate(sql);
			response.sendRedirect("adminServlet");
		} catch (Exception e) {
			out.println(e.getMessage());
		}
	}
	protected void updateCena(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String cena = request.getParameter("cena");
		String idUver = request.getParameter("idUver");
		try {
			HttpSession ses = request.getSession();
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			String sql = "UPDATE uver SET aktualnaCena = '" + Integer.parseInt(cena) + "' WHERE id = " + idUver;
			System.out.println(sql);
			stmt.executeUpdate(sql);
			response.sendRedirect("adminServlet");
		} catch (Exception e) {
			out.println(e.getMessage());
		}
	}
	
	
	
	protected void updateHypo(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String hypo = request.getParameter("hypo");
	    String idUver = request.getParameter("idUver");
	    try {
	        HttpSession ses = request.getSession();
	        Connection con = spojenie(request);
	        Statement stmt = con.createStatement();
	        String sql = "UPDATE uver SET hypoteka = '" + Integer.parseInt(hypo) + "' WHERE id = " + idUver;
	        System.out.println(sql);
	        stmt.executeUpdate(sql);
	        response.sendRedirect("adminServlet");
	    } catch (Exception e) {
	        out.println(e.getMessage());
	    }}
	
	protected void updatePravo(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
	    String idUser = request.getParameter("idUser");
	    String stav = request.getParameter("stav");
	    try {
	        HttpSession ses = request.getSession();
	        Connection con = spojenie(request);
	        Statement stmt = con.createStatement();
	        String sql = "UPDATE users SET je_bankar = '" + Integer.parseInt(stav) + "' WHERE id = " + idUser;
	        stmt.executeUpdate(sql);
	        response.sendRedirect("adminServlet");
	    } catch (Exception e) {
	        out.println(e.getMessage());
	    }}
	
	protected void vymazUver(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String idUver = request.getParameter("idUver");
		try {
			HttpSession ses = request.getSession();
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			String sql = "delete from uver  WHERE id = " + idUver;
			stmt.executeUpdate(sql);
			String sql2 = "DELETE FROM splatky WHERE idUver = " + idUver;
	        stmt.executeUpdate(sql2);
			response.sendRedirect("adminServlet");
		} catch (Exception e) {
			out.println(e.getMessage());
		}
	}
	
	private void pridaj(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String meno = request.getParameter("meno");
		String priezvisko = request.getParameter("priezvisko");
		String login = request.getParameter("login");
		String heslo = request.getParameter("heslo");
		String uver = request.getParameter("uver");
		String prijem = request.getParameter("prijem");
		String vek = request.getParameter("vek");
		String deti = request.getParameter("deti");
		String cena = request.getParameter("cena");
		String body = request.getParameter("body");
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
			response.sendRedirect("adminServlet");
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	
	}
	
	protected void vymazUser(PrintWriter out, HttpServletRequest request, HttpServletResponse response) {
		String idUser = request.getParameter("idUser");
		try {
			HttpSession ses = request.getSession();
			Connection con = spojenie(request);
			Statement stmt = con.createStatement();
			
			String checkUverQuery = "SELECT COUNT(*) AS uver_count FROM uver WHERE idUser = " + idUser;
	        ResultSet rs2 = stmt.executeQuery(checkUverQuery);
	        rs2.next();
	        int uverCount = rs2.getInt("uver_count");
	        if (uverCount > 0) {
	            String deleteUveryQuery = "DELETE FROM uver WHERE idUser = " + idUser;
	            stmt.executeUpdate(deleteUveryQuery);
	            String deleteSplatkyQuery = "DELETE FROM splatky WHERE idUser = " + idUser;
	            stmt.executeUpdate(deleteSplatkyQuery);
	        }
			
			
			String sql = "delete from users  WHERE id = " + idUser;
			stmt.executeUpdate(sql);
			String sql2 = "DELETE FROM zakaznik WHERE idUser = " + idUser;
	        stmt.executeUpdate(sql2);
			response.sendRedirect("adminServlet");
		} catch (Exception e) {
			out.println(e.getMessage());
		}
	}
}
