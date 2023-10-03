package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        String url = "jdbc:mysql://localhost:3306/search_engine";
        String user = "root";
        String pass = "1234";

        //Checking for database connection
        try(Connection connection = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Database connected!");
        } catch(Exception ex) {
            System.out.println("Cannot connect the database! Exception: " + ex);
        }
    }
}