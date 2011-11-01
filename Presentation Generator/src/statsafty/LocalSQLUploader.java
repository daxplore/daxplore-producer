package statsafty;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalSQLUploader {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
    	File file = new File("src/statsafty/statstoredata.js");
    	RandomAccessFile raf = new RandomAccessFile(file, "r");

    	// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		Connection connection;
		connection = DriverManager.getConnection("jdbc:sqlite:statstore.db");
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.
		connection.setAutoCommit(false);
		
		statement.execute("drop table if exists statstore");
		statement.execute("create table statstore(key text, value text)");
		PreparedStatement preparedStatement = connection.prepareStatement("insert into statstore values(?, ?)");
		
    	int lines = 0;
    	String line;
    	while( (line = raf.readLine()) != null ){
    		lines++;
    	}
    	lines--;
    	System.out.println("Uploading " + lines  + " entries");
    	
    	raf.seek(0);
        
        try {
        	String[] tokens;
        	int i = 0;
        	line = raf.readLine();
        	if(line.equals("key,json")){
				while ((line = raf.readLine()) != null) {
					tokens = line.split(",", 2);
					if(tokens.length >1){
						preparedStatement.setString(1, tokens[0]);
						preparedStatement.setString(2, tokens[1].substring(1, tokens[1].length()-1).replace("\"\"", "\""));
						preparedStatement.execute();
						if((++i % 100) == 0){
							System.out.println("" + i + "/" + lines + " uploaded");
						}
					}
				}
				connection.commit();
				System.out.println("Uploaded "+ i + " entries");
        	} else {
        		System.out.println("error in file");
        	}
        	
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