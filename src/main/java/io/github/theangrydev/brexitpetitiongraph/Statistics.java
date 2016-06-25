package io.github.theangrydev.brexitpetitiongraph;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class Statistics {
    private static final String PETITION_URL = "https://petition.parliament.uk/petitions/131215.json";

    public static void main(String[] args) {
        new Statistics().run();
    }

    private void run() {
        try {
            Response response = new OkHttpClient().newCall(new Request.Builder().url(PETITION_URL).build()).execute();
            String body = response.body().string();
            JSONObject attributes = new JSONObject(body).getJSONObject("data").getJSONObject("attributes");
            long signatures = attributes.getLong("signature_count");
            long sumAcrossCountries = sumAcrossCountries(attributes);
            long sumAcrossConstituencies = sumAcrossConstituencies(attributes);
            long unitedKingomCount = unitedKingdomCount(attributes);
            System.out.println("sumAcrossCountries = " + sumAcrossCountries);
            System.out.println("sumAcrossConstituencies = " + sumAcrossConstituencies);
            System.out.println("unitedKingomCount = " + unitedKingomCount);
            System.out.println("sumAcrossCountries - unitedKingomCount = " + (sumAcrossCountries - unitedKingomCount));
            System.out.println("sumAcrossConstituencies + sumAcrossCountries - unitedKingdomCount = " + (sumAcrossConstituencies + sumAcrossCountries - unitedKingomCount));
            System.out.println("signatures = " + signatures);
        } catch (Exception e) {
            System.err.println("Problem polling: ");
            e.printStackTrace();
        }
    }

    private long unitedKingdomCount(JSONObject attributes) {
        JSONArray countries = attributes.getJSONArray("signatures_by_country");
        for (int i = 0; i < countries.length(); i++) {
            JSONObject country = countries.getJSONObject(i);
            if ("United Kingdom".equals(country.getString("name"))) {
                return country.getLong("signature_count");
            }
        }
        throw new IllegalStateException("Could not find United Kingdom");
    }

    private long sumAcrossConstituencies(JSONObject attributes) {
        JSONArray countries = attributes.getJSONArray("signatures_by_constituency");
        long sumAcrossCountries = 0;
        for (int i = 0; i < countries.length(); i++) {
            JSONObject country = countries.getJSONObject(i);
            sumAcrossCountries += country.getLong("signature_count");
        }
        return sumAcrossCountries;
    }

    private long sumAcrossCountries(JSONObject attributes) {
        JSONArray countries = attributes.getJSONArray("signatures_by_country");
        long sumAcrossCountries = 0;
        for (int i = 0; i < countries.length(); i++) {
            JSONObject country = countries.getJSONObject(i);
            sumAcrossCountries += country.getLong("signature_count");
        }
        return sumAcrossCountries;
    }
}
