package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.sql.*;
import static searchengine.model.GlobalConstants.*;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        //Checking for database connection
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            System.out.println("Database connected!");
        } catch(Exception ex) {
            System.out.println("Cannot connect the database! Exception: " + ex);
        }
    }
}