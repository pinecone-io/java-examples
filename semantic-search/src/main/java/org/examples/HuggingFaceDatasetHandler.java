package org.examples;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class HuggingFaceDatasetHandler {
    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceDatasetHandler.class);
    private final String apiUrl;

    public HuggingFaceDatasetHandler(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public JSONArray returnParsedHfData() {
        JSONObject hFAcquisition = this.returnHfData();
        return this.returnParsedHFData(hFAcquisition);
    }

    public List<String> extract() {
        List<String> extractedData = new ArrayList<>();
        this.returnParsedHfData().forEach(r -> {
            JSONObject row = (JSONObject) r;
            String claim = ((JSONObject) r).get("claim").toString();
            extractedData.add(claim);
        });
        return extractedData;
    }

    public JSONObject returnHfData() {
        try {
            return this.query();
        } catch (IOException e) {
            logger.error("Error occurred while querying Hugging Face API: {}", e.getMessage());
            return null;
        }
    }

    public JSONObject query() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseString = EntityUtils.toString(response.getEntity());
                return new JSONObject(responseString);
            }
        }
    }

    public JSONArray returnParsedHFData(JSONObject hfData) {
        JSONArray hFDataRows = hfData.getJSONArray("rows");

        JSONArray parsedData = new JSONArray();
        hFDataRows.forEach(r -> {
            JSONObject row = (JSONObject) r;
            JSONObject rowData = row.getJSONObject("row");
            String claimID = rowData.getString("claim_id");
            Integer claimLabel = rowData.getInt("claim_label");
            String claim = rowData.getString("claim");
            JSONArray evidences = rowData.getJSONArray("evidences");

            Set<String> articles = new HashSet<>();  // how can these be different types?
            for (int i = 0; i < evidences.length(); i++) {
                JSONObject evidence = evidences.getJSONObject(i);
                String article = evidence.getString("article");
                articles.add(article);
            }

            HashMap<String, Object> claimData = new HashMap<>();
            claimData.put("claimID", claimID);
            claimData.put("claimLabel", claimLabel);
            claimData.put("claim", claim);
            claimData.put("articleTitles", articles);

            parsedData.put(claimData);
        });
        return parsedData;
    }

}