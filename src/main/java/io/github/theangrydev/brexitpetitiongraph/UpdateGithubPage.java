package io.github.theangrydev.brexitpetitiongraph;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class UpdateGithubPage {

    private final Path signatureCountJson = Paths.get("signature_count.json");
    private final Properties properties = new Properties();

    public static void main(String[] args) {
        new UpdateGithubPage().run();
    }

    private void run() {
        loadProperties();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::updatePage, 0, 10, TimeUnit.MINUTES);
        updatePage();
    }

    private void loadProperties() {
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("secret.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Could not find secret.properties", e);
        }
    }

    private void updatePage() {
        ConvertResultsToJson.main(new String[0]);
        String projectPath = properties.getProperty("github.pages.project.path");

        try {
            Files.copy(signatureCountJson, Paths.get(projectPath).resolve("signature_count.json"), REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Could not write signature_count.json");
            e.printStackTrace();
        }

        try {
            Runtime.getRuntime().exec(properties.getProperty("git.command"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
