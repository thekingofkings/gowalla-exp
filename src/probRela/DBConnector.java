package probRela;

import java.sql.*;
import java.util.Properties;

public class DBConnector {
	Connection conn;
	String username;
	String password;
	String dbName;
	
	DBConnector(String usrname, String pwd, String dbName) {
		this.conn = null;
		this.username = usrname;
		this.password = pwd;
		this.dbName = dbName;
	}
	
	public Connection getConnection() {
		conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.username);
		connectionProps.put("password", this.password);
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/", connectionProps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Connected to database");		
		return conn;
	}
	
	public static void viewTable( Connection conn, String dbName ) throws SQLException {
		Statement stmt = null;
		String query = "SELECT userID, count(userID) AS cnt from " + 
				dbName + ".gowalla GROUP BY userID ORDER BY cnt DESC";
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int userID = rs.getInt("userID");
				int count = rs.getInt("cnt");
				if (count >= 2000) {
					System.out.println(String.format("User %d has %d records", userID, count));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}
	
	public static void main(String argv[]) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DBConnector dbc = new DBConnector("kdd14", "kdd14", "trajectory");
		try {
			viewTable(dbc.getConnection(), dbc.dbName);
		} catch (SQLException e) {
			System.out.println("Stmt does not close properly");
			e.printStackTrace();
		}
	}

}
