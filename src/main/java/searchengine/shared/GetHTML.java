package searchengine.shared;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.*;

import java.io.IOException;

public class GetHTML {
    public String getHTML(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        String webpage = document.html();

        assertNotNull(webpage);
        assertTrue(webpage.contains("<html>"));

        return webpage;
    }
}
