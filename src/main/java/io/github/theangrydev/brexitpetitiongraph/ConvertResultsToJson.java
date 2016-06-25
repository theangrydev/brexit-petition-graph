package io.github.theangrydev.brexitpetitiongraph;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class ConvertResultsToJson {

    private final Path signatureCount = Paths.get("signature_count.txt");
    private final Path signatureCountJson = Paths.get("signature_count.json");

    public static void main(String[] args) {
        new ConvertResultsToJson().run();
    }

    private void run() {
        try {
            JSONArray json = new JSONArray();
            for (String line : Files.readAllLines(signatureCount)) {
                String[] data = line.split(",");
                JSONObject point = new JSONObject();
                point.put("date", data[0]);
                point.put("value", data[1]);
                json.put(point);
            }
            Files.write(signatureCountJson, json.toString(4).getBytes(), TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Problem loading: ");
            e.printStackTrace();
        }
    }
}
