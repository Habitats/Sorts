package sorts;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

	private Properties prop;
	private Connection conn;

	public DatabaseConnection() {
		prop = loadConfig();
	}

	private Properties loadConfig() {
		Properties prop = null;
		try {
			prop.load(new FileInputStream(new File("org.cfg")));
		} catch (Exception e) {
			System.out.println("Couldn't find file.");
		}
		return prop;
	}

	public void initialize() {
		String jdbcDriver = prop.getProperty("jdbcDriver");
		String url = prop.getProperty("url");
		String user = prop.getProperty("username");
		String password = prop.getProperty("password");

		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Couldn't connect to db.");
		}
	}

	public boolean execute(String sql) throws SQLException {
		Statement st = conn.createStatement();
		return st.execute(sql);
	}

	public ResultSet makeSingleQuery(String sql) throws SQLException {
		Statement st = conn.createStatement();
		return st.executeQuery(sql);
	}

	public int makeUpdate(String sql) throws SQLException {
		Statement st = conn.createStatement();
		return st.executeUpdate(sql);
	}

	public PreparedStatement makeBatchUpdate(String sql) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		return ps;
	}
	
	public void closeConnection() throws SQLException{
		conn.close();
	}
}
