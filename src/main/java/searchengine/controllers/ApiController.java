package searchengine.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import searchengine.dto.statistics.StatisticsResponse;
import org.springframework.http.ResponseEntity;
import searchengine.services.StatisticsService;
import org.apache.commons.validator.routines.UrlValidator;
import searchengine.shared.GetHTML;

import static searchengine.model.GlobalConstants.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @PostMapping("/indexPage")
    public void indexPage(String url) {
        boolean result = true;
        ResultSet resultat = null;

        UrlValidator urlValidator = new UrlValidator();

        if(urlValidator.isValid(url)) {
            // Успех
            System.out.println("Page is valid!");

            try(java.sql.Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                LocalDate date = LocalDate.now();

                final String queryCheck = "SELECT * from site WHERE name = ?";
                final PreparedStatement ps = con.prepareStatement(queryCheck);
                ps.setString(1, getDomainName(url));
                final ResultSet resultSet = ps.executeQuery();

                Statement statement = con.createStatement();

                if(resultSet.next()) {
                    // Если код здесь запустился, то такое имя уже существует
                    final String query = "SELECT * FROM site WHERE url = ?";
                    final PreparedStatement preparedStatement = con.prepareStatement(query);
                    preparedStatement.setString(1, url);
                    final ResultSet rs = preparedStatement.executeQuery();

                    if(rs.next()) {
                        // Ссылки идентичны
                    } else {
                        // Ссылки не идентичны
                        URL url_to_path = new URL(url);
                        String path = url_to_path.getFile();
                        HttpURLConnection connection = (HttpURLConnection)url_to_path.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        int code = connection.getResponseCode();

                        final String querik = "SELECT * FROM site WHERE name = ?";
                        final PreparedStatement pSt = con.prepareStatement(querik);
                        pSt.setString(1, getDomainName(url));
                        final ResultSet rSe = pSt.executeQuery();

                        if(rSe.next()) {
                            int site_id = rSe.getInt("id");
                            GetHTML getHTML = new GetHTML();
                            String html = getHTML.getHTML(url);

                            final String insertQuery = "INSERT INTO page (site_id, path, code, content) VALUES (?, ?, ?, ?)";
                            PreparedStatement prStatement = con.prepareStatement(insertQuery);
                            prStatement.setInt(1, site_id);
                            prStatement.setString(2, path);
                            prStatement.setInt(3, code);
                            prStatement.setString(4, html);

                            int rows = prStatement.executeUpdate();
                            System.out.printf("Added %d rows", rows);
                        }
                    }

                    final int count = resultSet.getInt(1);
                } else {
                    // Если код здесь запустился, то такого имени не существует
                    URL check_URL = new URL(url);
                    String baseURL = check_URL.getProtocol() + "://" + check_URL.getHost();

                    if(baseURL.equals(url)) {
                        //int rows = statement.executeUpdate("INSERT INTO site(status, status_time, last_error, url, name) VALUES('INDEXING', '" + date.toString() + "', 'NULL', '" + url + "', '" + getDomainName(url) + "')");
                        String insertQuery = "INSERT INTO site(status, status_time, last_error, url, name) VALUES(?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = con.prepareStatement(insertQuery);
                        preparedStatement.setString(1, "INDEXING");
                        preparedStatement.setString(2, date.toString());
                        preparedStatement.setString(3, "NULL");
                        preparedStatement.setString(4, url);
                        preparedStatement.setString(5, getDomainName(url));

                        int rows = preparedStatement.executeUpdate();
                        System.out.printf("\nAdded %d rows", rows);
                    } else {
                        int rows = statement.executeUpdate("INSERT INTO site(status, status_time, last_error, url, name) VALUES('INDEXING', '" + date.toString() + "', 'NULL', '" + baseURL + "', '" + getDomainName(baseURL) + "')");
                        System.out.printf("\nAdded %d rows", rows);

                        final String querik = "SELECT * FROM site WHERE url = ?";
                        final PreparedStatement pSt = con.prepareStatement(querik);
                        pSt.setString(1, baseURL);
                        final ResultSet rSe = pSt.executeQuery();

                        if (rSe.next()) {
                            int site_id = rSe.getInt("id");
                            System.out.println(url);

                            GetHTML getHTML = new GetHTML();
                            String html = getHTML.getHTML(url);

                            HttpURLConnection connection = (HttpURLConnection)check_URL.openConnection();
                            connection.setRequestMethod("GET");
                            connection.connect();
                            int code = connection.getResponseCode();

                            rows = statement.executeUpdate("INSERT INTO page(site_id, path, code, content) VALUES('" + site_id + "', '" + url + "', '" + code + "', '" + html + "')");
                            System.out.printf("\nAdded %d pages", rows);
                        }
                    }
                }
            } catch(Exception ex) {
                // Ошибка
                result = false;
                System.out.println("Exception: " + ex);
            }
        } else {
            // Неудача
            result = false;
            System.out.println("Page is invalid!");
        }
    }

    public static String getDomainName(String url) throws MalformedURLException {
        if(!url.startsWith("http") && !url.startsWith("https")) {
            url = "http://" + url;
        }

        URL netUrl = new URL(url);
        String host = netUrl.getHost();

        if(host.startsWith("www")) {
            host = host.substring("www".length()+1);
        }

        return host;
    }

    @GetMapping("/startIndexing")
    public void startIndexing() {
        boolean result = true;

        LocalDate date = LocalDate.now();
        ArrayList<String> sites = new ArrayList<String>();

        try(java.sql.Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            // Успех
            final String queryCheck = "SELECT url from site";
            final String siteQuery = "INSERT INTO site (status, status_time, last_error, url, name) VALUES (?, ?, ?, ?, ?)";
            final String pageQuery = "INSERT INTO page (site_id, path, code, content) VALUES (?, ?, ?, ?)";

            final PreparedStatement ps = con.prepareStatement(queryCheck);
            PreparedStatement ps2 = con.prepareStatement(siteQuery);
            PreparedStatement ps3 = con.prepareStatement(pageQuery);

            final ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String url = resultSet.getString("url");
                sites.add(url);
            }

            Statement statement = con.createStatement();

            String deleteLemmaQuery = "DELETE FROM site";
            statement.executeUpdate(deleteLemmaQuery);
            String deletePageQuery = "DELETE FROM page";
            statement.executeUpdate(deletePageQuery);
            System.out.println("1. Удалил все имеющиеся данные по этому сайту (Записи из таблиц site и page).");

            for(int i = 0; i < sites.size(); i++) {
                ps2.setString(1, "INDEXING");
                ps2.setString(2, date.toString());
                ps2.setString(3, "NULL");
                ps2.setString(4, sites.get(i));
                ps2.setString(5, getDomainName(sites.get(i)));
                ps2.executeUpdate();
            }

            System.out.println("2. Создал в таблице site новую запись со статусом INDEXING.");

            for(int j = 0; j < sites.size(); j++) {
                String url = sites.get(j);

                final String pageURLQuery = "SELECT * FROM site WHERE url = '" + url + "'";
                final PreparedStatement preparedStatement = con.prepareStatement(pageURLQuery);
                final ResultSet resultSet2 = preparedStatement.executeQuery();

                URL check_URL = new URL(url);
                HttpURLConnection connection = (HttpURLConnection)check_URL.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int code = connection.getResponseCode();

                GetHTML getHTML = new GetHTML();
                String html = getHTML.getHTML(url); // EXCEPTION HERE!!!

                ps3.setInt(1, resultSet2.getInt("id")); // SiteID
                ps3.setString(2, url);
                ps3.setInt(3, code);
                ps3.setString(4, html);
                ps3.executeUpdate();
            }

            System.out.println("3. Обошёл все страницы, начиная с главной, добавил их адреса, статусы и содержимое в базу данных в таблицу page.");
        } catch(Exception ex) {
            // Ошибка
            result = false;
            System.out.println("Exception: " + ex);
        }
    }
}