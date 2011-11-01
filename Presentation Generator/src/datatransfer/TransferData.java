package datatransfer;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import statsafty.NaiveBayesClassifier;

public class TransferData {
	public static void main(String[] args) throws ClassNotFoundException {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		
		String spssFileName;
		
		Connection connection = null;
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:rawdata.db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists person");

			if(args.length >0){
				spssFileName = args[0];
				File spssFile = new File(spssFileName);
				System.out.println("Spss file opened");
				statement.executeUpdate("drop table if exists rawdata");
				System.out.println("Table dropped");
				SPSStoSQL.loadRawData(spssFile, connection);
				System.out.println("Data imported");
			}else{
				System.err.println("Not enough arguments given");
				return;
			}
			
			ResultSet rs = statement.executeQuery("select * from rawdata");
			/*while (rs.next()) {
				// read the result set
				//System.out.println("A1 = " + rs.getInt("A1"));
				//System.out.println("id = " + rs.getInt("A7AGG"));
			}*/
			NaiveBayesClassifier.checkRow(rs);
			
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}
}