package hse.ml;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Parser PARSER = new Parser();
    private static final SimpleDateFormat TARGET_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Path PARSED_FILE = Paths.get("src", "main", "resources", "parsed.csv");
    private static final Path REPORT_FILE = Paths.get("src", "main", "resources", "report.txt");

    public static void main(String[] args) throws InterruptedException {
        Date startTime = new Date();

        Set<PhishingEntity> parsedResults = new HashSet<>();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> parseAndSaveLocal(parsedResults), 0, 5, TimeUnit.MINUTES);

        Thread.sleep(TimeUnit.MINUTES.toMillis(60));
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1L, TimeUnit.MINUTES);
        saveParsedToFile(parsedResults);

        Date finishTime = new Date();
        generateReportFile(startTime, finishTime, parsedResults);
    }

    private static void parseAndSaveLocal(Set<PhishingEntity> parsedResults) {
        List<PhishingEntity> parsedList = PARSER.parse();
        parsedResults.addAll(parsedList);
        System.out.println("Saved...");
    }

    private static String convertToCSV(PhishingEntity entity) {
        StringJoiner stringJoiner = new StringJoiner(",");
        stringJoiner.add(entity.getUrl());
        stringJoiner.add("\"" + entity.getTargetedBrand() + "\""); // у некоторых компаний запятая в названии, поэтому название пишем в кавычках (это допустимо в csv)
        stringJoiner.add(TARGET_DATE_FORMAT.format(entity.getTime()));
        return stringJoiner.toString();
    }

    private static void saveParsedToFile(Set<PhishingEntity> parsedResults) {
        List<PhishingEntity> sorted = new ArrayList<>(parsedResults);
        sorted.sort(Comparator.comparing(PhishingEntity::getTime));
        try {
            Files.createFile(PARSED_FILE);
            try (PrintWriter out = new PrintWriter(PARSED_FILE.toFile())) {
                out.println("url,targetedBrand,time");

                for (PhishingEntity phishingEntity : sorted) {
                    out.println(convertToCSV(phishingEntity));
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to create file with path=" + PARSED_FILE);
            throw new RuntimeException(e);
        }
    }

    private static void generateReportFile(Date start, Date finish, Set<PhishingEntity> parsedResults) {
        try {
            Files.createFile(REPORT_FILE);
            try (PrintWriter out = new PrintWriter(REPORT_FILE.toFile())) {
                out.println("Время начала парсинга " + TARGET_DATE_FORMAT.format(start));
                out.println("Время окончания парсинга " + TARGET_DATE_FORMAT.format(finish));

                long distinctUrlCount = parsedResults.stream().map(PhishingEntity::getUrl).distinct().count();
                out.println("Количество уникальных URL сайтов за данный период " + distinctUrlCount);

                Map<String, Integer> brandToCount = new HashMap<>();
                parsedResults.forEach(e -> brandToCount.merge(e.getTargetedBrand(), 1, Integer::sum));
                List<String> topThree = brandToCount.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .limit(3)
                        .map(entry -> entry.getKey() + " - " + entry.getValue())
                        .toList();
                out.println("Топ 3 Наиболее часто атакуемых брендов " + topThree);

                out.println("Ссылка на google колаб со скриптом или на github: https://github.com/shakirov-aa/hse-ml/tree/HW-1");
            }
        } catch (IOException e) {
            System.out.println("Failed to create file with path=" + PARSED_FILE);
            throw new RuntimeException(e);
        }
    }
}