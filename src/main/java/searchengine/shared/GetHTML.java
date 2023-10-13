package searchengine.shared;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class GetHTML {
    public String getHTML(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        return document.outerHtml();
    }
}
