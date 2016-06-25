package io.github.theangrydev.brexitpetitiongraph;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

    private static final boolean LEGEND = true;
    private static final boolean TOOLTIPS = true;
    private static final boolean URLS = false;
    private static final String VALUE_AXIS_LABEL = "Signatures";
    private static final String TIME_AXIS_LABEL = "Time";
    private static final String TITLE = "EU Referendum Rules triggering a 2nd EU Referendum";
    private static final String PETITION_URL = "https://petition.parliament.uk/petitions/131215.json";
    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";

    private final TimeSeries petition = new TimeSeries("petition");
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Path signatureCount = Paths.get("signature_count.txt");

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                TITLE,
                TIME_AXIS_LABEL,
                VALUE_AXIS_LABEL,
                new TimeSeriesCollection(petition),
                LEGEND,
                TOOLTIPS,
                URLS);

        XYPlot plot = chart.getXYPlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setAutoRange(true);

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setAutoRange(true);

        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.getContentPane().add(new ChartPanel(chart));

        frame.pack();
        frame.setVisible(true);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.execute(this::loadExisting);
        executorService.scheduleAtFixedRate(this::pollVotes, 0, 1, TimeUnit.MINUTES);
    }

    private void loadExisting() {
        try {
            for (String line : Files.readAllLines(signatureCount)) {
                String[] data = line.split(",");
                Date date = new SimpleDateFormat(DATE_FORMAT).parse(data[0]);
                long signatures = Long.parseLong(data[1]);
                petition.add(new Minute(date), signatures);
            }
        } catch (IOException e) {
            System.err.println("Problem loading: ");
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("Problem parsing: ");
            e.printStackTrace();
        }
    }

    private void pollVotes() {
        try {
            Response response = httpClient.newCall(new Request.Builder().url(PETITION_URL).build()).execute();
            String body = response.body().string();
            long signatures = new JSONObject(body).getJSONObject("data").getJSONObject("attributes").getLong("signature_count");
            Date now = new Date();
            Files.write(signatureCount, String.format("%s,%s%n", new SimpleDateFormat(DATE_FORMAT).format(now), signatures).getBytes(), CREATE, APPEND);
            petition.add(new Minute(now), signatures);
        } catch (IOException e) {
            System.err.println("Problem polling: ");
            e.printStackTrace();
        }
    }
}
