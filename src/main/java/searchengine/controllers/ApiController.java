package searchengine.controllers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import searchengine.dto.statistics.StatisticsResponse;
import org.springframework.http.ResponseEntity;
import searchengine.services.StatisticsService;
import org.apache.commons.validator.routines.UrlValidator;
import searchengine.shared.GetHTML;

import static org.springframework.util.ResourceUtils.getFile;
import static searchengine.model.GlobalConstants.*;
import static searchengine.shared.GetHTML.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.*;
import java.util.ArrayList;
import java.util.List;

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
        String pageUrl = url;
        boolean result = true;
        ResultSet resultat = null;

        UrlValidator urlValidator = new UrlValidator();

        if(urlValidator.isValid(pageUrl)) {
            // Успех

            System.out.println("Page is valid!");

            try(Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
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

                        if(rSe.next()){
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
                        } else {
                            System.out.println("SOSI MOI HUI");
                        }
                    }

                    final int count = resultSet.getInt(1);
                } else {
                    // Если код здесь запустился, то такого имени не существует

                    int rows = statement.executeUpdate("INSERT site(status, status_time, last_error, url, name) VALUES('INDEXING', '" + date.toString() + "', 'NULL', '" + url + "', '" + getDomainName(url) + "')");
                    System.out.printf("Added %d rows", rows);
                }
            } catch(Exception ex) {
                // Ошибка

                result = false;
                System.out.println("Данная страница находится за пределами сайтов, указанных в конфигурационном файле.");
                System.out.println("Exception: " + ex);

            }
            // Добавляем в индекс отдельную страницу, адрес которой url
        } else {
            // Неудача

            result = false;
            System.out.println("Page is invalid!");
        }
    }

    public static String getDomainName(String url) throws MalformedURLException {
        if(!url.startsWith("http") && !url.startsWith("https")){
            url = "http://" + url;
        }
        URL netUrl = new URL(url);
        String host = netUrl.getHost();
        if(host.startsWith("www")){
            host = host.substring("www".length()+1);
        }
        return host;
    }

    @GetMapping("/startIndexing")
    public void startIndexing() {
        int size = 0;
        ArrayList<String> sites = new ArrayList<String>();

        try(Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            final String queryCheck = "SELECT url from site";
            final PreparedStatement ps = con.prepareStatement(queryCheck);
            final ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String url = resultSet.getString("url");
                sites.add(url);
            }

            Statement statement = con.createStatement();

            String deleteSiteQuery = "DELETE FROM lemma";
            statement.executeUpdate(deleteSiteQuery);
            String deletePageQuery = "DELETE FROM page";
            statement.executeUpdate(deletePageQuery);
            String deleteLemmaQuery = "DELETE FROM site";
            statement.executeUpdate(deleteLemmaQuery);

            System.out.println("Данные удалены из таблиц");
            System.out.println(sites);
        } catch(Exception ex) {
            System.out.println("Exception: " + ex);
        }
    }
}