package sorts;
import java.sql.SQLException;

public class DatabaseQueries {

	private final DatabaseConnection dbConn;

	public DatabaseQueries(DatabaseConnection dbConn) {
		this.dbConn = dbConn;
	}

	public void addSingle(String rls) {
		try {
			dbConn.execute(String.format("INSERT INTO rls %s", rls));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean exists(String rls){
		return false;
		
	}
}
