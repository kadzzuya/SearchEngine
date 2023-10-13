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
import java.time.LocalDateTime;
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
        // Переменная для хранения сообщения об ошибке
        String errorMessage = "";
        boolean result = true;
        ResultSet resultat = null;

        UrlValidator urlValidator = new UrlValidator();

        if(urlValidator.isValid(url)) {
            // Успех
            System.out.println("Page is valid!");

            try(java.sql.Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                LocalDate date = LocalDate.now();

                final String queryCheck = "SELECT * FROM site WHERE name = ?";
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
                            System.out.printf("Added %d rows: ", rows);
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
                        System.out.printf("\nAdded %d rows: ", rows);
                    } else {
                        int rows = statement.executeUpdate("INSERT INTO site(status, status_time, last_error, url, name) VALUES('INDEXING', '" + date.toString() + "', 'NULL', '" + baseURL + "', '" + getDomainName(baseURL) + "')");
                        System.out.printf("\nAdded %d rows: ", rows);

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

                            String insertQuery = "INSERT INTO page(site_id, path, code, content) VALUES (?, ?, ?, ?)";
                            PreparedStatement preparedStatement = con.prepareStatement(insertQuery);
                            preparedStatement.setInt(1, site_id);
                            preparedStatement.setString(2, url);
                            preparedStatement.setInt(3, code);
                            preparedStatement.setString(4, html);

                            rows = preparedStatement.executeUpdate();
                            System.out.printf("\nAdded %d pages: ", rows);
                        }
                    }
                }
            } catch(Exception ex) {
                // Ошибка
                result = false;
                errorMessage = ex.getMessage(); // Получить сообщение об ошибке
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
        String errorMessage = null;

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

            // Сначала удаляем связанные записи из таблицы page
            String deletePageQuery = "DELETE p FROM page p JOIN site s ON p.site_id = s.id";
            statement.executeUpdate(deletePageQuery);
            System.out.println("\n1.1. Удалены связанные записи из таблицы page.");

            // Затем удаляем записи из таблицы site
            String deleteSiteQuery = "DELETE FROM site";
            statement.executeUpdate(deleteSiteQuery);
            System.out.println("1.2. Удалены записи из таблицы site.");

            for(int i = 0; i < sites.size(); i++) {
                ps2.setString(1, "INDEXING");
                ps2.setString(2, date.toString());
                ps2.setString(3, "NULL");
                ps2.setString(4, sites.get(i));
                ps2.setString(5, getDomainName(sites.get(i)));
                ps2.executeUpdate();

                // Добавьте код для обновления status_time на текущее время
                String updateStatusTimeQuery = "UPDATE site SET status_time = ? WHERE url = ?";
                PreparedStatement updatePs = con.prepareStatement(updateStatusTimeQuery);
                updatePs.setString(1, LocalDateTime.now().toString());
                updatePs.setString(2, sites.get(i));
                updatePs.executeUpdate();
            }

            System.out.println("2. Создал в таблице site новую запись со статусом INDEXING.");

            for(int j = 0; j < sites.size(); j++) {
                String url = sites.get(j);

                String pageURLQuery = "SELECT * FROM site WHERE url = ?";
                final PreparedStatement preparedStatement = con.prepareStatement(pageURLQuery);
                preparedStatement.setString(1, url); // Используем параметризированный запрос
                final ResultSet resultSet2 = preparedStatement.executeQuery();

                if(resultSet2.next()) {
                    URL check_URL = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection)check_URL.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int code = connection.getResponseCode();

                    GetHTML getHTML = new GetHTML();
                    String html = getHTML.getHTML(url);

                    ps3.setInt(1, resultSet2.getInt("id")); // SiteID
                    ps3.setString(2, url);
                    ps3.setInt(3, code);
                    ps3.setString(4, html);
                    ps3.executeUpdate();
                }
            }

            // После блока try/catch
            try(java.sql.Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                if(!result) {
                    // Если произошла ошибка, обновите статус и добавьте сообщение об ошибке
                    String updateFailedStatusQuery = "UPDATE site SET status = 'FAILED', last_error = ? WHERE status = 'INDEXING'";
                    PreparedStatement updateFailedStatusPs = conn.prepareStatement(updateFailedStatusQuery);
                    updateFailedStatusPs.setString(1, errorMessage);
                    updateFailedStatusPs.executeUpdate();

                    System.out.println("3. Обход завершен с ошибкой, статус изменен на FAILED, и добавлено сообщение об ошибке.");
                } else {
                    // Если ошибок не произошло, установите статус "INDEXED" для всех записей
                    String updateStatusQuery = "UPDATE site SET status = 'INDEXED' WHERE status = 'INDEXING'";
                    PreparedStatement updateStatusPs = conn.prepareStatement(updateStatusQuery);
                    updateStatusPs.executeUpdate();

                    System.out.println("3.1. Обошёл все страницы, начиная с главной, добавил их адреса, статусы и содержимое в базу данных в таблицу page.");
                    System.out.println("3.2. Статус изменен на INDEXED.");
                }
            } catch(Exception ex) {
                System.out.println("Ошибка при обновлении статуса или сообщения об ошибке: " + ex.getMessage());
            }
        } catch(Exception ex) {
            // Ошибка
            result = false;
            System.out.println("Exception: " + ex);
        }
    }
}