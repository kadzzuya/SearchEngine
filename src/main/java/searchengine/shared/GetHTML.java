package searchengine.shared;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import static org.junit.Assert.*;

import java.io.IOException;

public class GetHTML {
    public String getHTML(String url) throws IOException {
        Document document = Jsoup.connect(url).get();

        // Удаление комментариев
        document.select("comment").remove();

        // Минимизация HTML-кода
        document.outputSettings().escapeMode(Entities.EscapeMode.extended);
        document.outputSettings().prettyPrint(false);

        // Удаление ненужных элементов
        document.select("script,noscript,style,iframe,object,embed,applet").remove();

        // Удаление ненужных атрибутов
        document.select("[style]").removeAttr("style");
        document.select("[class]").removeAttr("class");
        document.select("[id]").removeAttr("id");
        document.select("[name]").removeAttr("name");

        String minimizedHTML = document.html();

        return minimizedHTML;
    }
}