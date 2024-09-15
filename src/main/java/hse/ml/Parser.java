package hse.ml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Parser {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public List<PhishingEntity> parse() {
        Document doc;
        try {
            doc = Jsoup.connect("https://openphish.com/").get();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load page from openphish.com", e);
        }

        Element table = doc.select("table.pure-table").get(0);
        Elements rows = table.select("tr");

        Date currentDate = new Date(); // openphish.com provides data without date (only time HH:mm:ss), so we will set actual date
        System.out.println(currentDate);
        List<PhishingEntity> result = new ArrayList<>(rows.size() - 1);
        // skip header, so start with index=1
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cols = row.select("td");

            String url = cols.get(0).text();
            String targetedBrand = cols.get(1).text();
            Date date;
            try {
                date = DATE_FORMAT.parse(cols.get(2).text());
            } catch (ParseException e) {
                System.out.println("Failed to parse time=" + cols.get(2).text());
                date = new Date();
            }
            date.setYear(currentDate.getYear());
            date.setMonth(currentDate.getMonth());
            date.setDate(currentDate.getDate());
            result.add(new PhishingEntity(url, targetedBrand, date));
        }

        return result;
    }
}
