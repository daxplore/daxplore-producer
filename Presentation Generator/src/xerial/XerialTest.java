package xerial;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import StatSafty.NaiveBayesClassifier;

import datatransfer.SPSStoSQL;

public class XerialTest {
	public static void main(String[] args) throws ClassNotFoundException {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		
		String spssFileName;
		//args

		
		
		Connection connection = null;
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
			//connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists person");
			/*statement.executeUpdate("create table person (id integer, name string)");
			statement.executeUpdate("insert into person values(1, 'leo')");
			statement.executeUpdate("insert into person values(2, 'yui')");
			
			PreparedStatement prepared = connection.prepareStatement("insert into person values(?, ?)");
			
			prepared.setInt(1, 3);
			prepared.setString(2, "dan");
			prepared.execute();
			//Integer a = null;
			//prepared.setInt(1, a);
			prepared.setString(2, "the man");
			prepared.execute();
			
			prepared.setInt(1, 5);
			prepared.setString(2, "åäöĉäá");
			prepared.execute();
			
			ResultSet rs = statement.executeQuery("select * from person");
			while (rs.next()) {
				// read the result set
				System.out.println("name = " + rs.getString("name"));
				System.out.println("id = " + rs.getInt("id"));
			}*/
			if(args.length >0){
				spssFileName = args[0];
				File spssFile = new File(spssFileName);
				System.out.println("Spss file opened");
				statement.executeUpdate("drop table if exists rawdata");
				System.out.println("Table dropped");
				SPSStoSQL.loadRawData(spssFile, connection);
				System.out.println("Data imported");
			}
			
			//ResultSet rs = statement.executeQuery("select * from rawdata");
			/*while (rs.next()) {
				// read the result set
				System.out.println("A1 = " + rs.getInt("A1"));
				System.out.println("id = " + rs.getInt("A7AGG"));
			}*/
			//NaiveBayesClassifier.checkRow(rs);
			
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