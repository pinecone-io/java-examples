package org.examples;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HuggingFaceHandlerTest {

    // Test data
    private final JSONObject article1 = new JSONObject().put("article", "this is an article");
    private final JSONObject article2 = new JSONObject().put("article", "this is another article");

    private final List<JSONObject> articles = Arrays.asList(article1, article2);

    private final JSONObject rowContents1 = new JSONObject()
            .put("claim", "this is a claim")
            .put("claim_label", 0)
            .put("claim_id", "0")
            .put("evidences", articles);

    private final JSONObject rowContents2 = new JSONObject()
            .put("claim", "this is a different claim")
            .put("claim_label", 0)
            .put("claim_id", "1")
            .put("evidences", articles);

    private final JSONObject row1 = new JSONObject().put("row", rowContents1);
    private final JSONObject row2 = new JSONObject().put("row", rowContents2);

    private final JSONObject data = new JSONObject().put("rows", new JSONArray().put(row1).put(row2));

    @Test
    public void testExtract() {
        HuggingFaceHandler huggingFaceHandler = new HuggingFaceHandler();
        List<String> extractedData = huggingFaceHandler.extract();
        assertNotNull("Extracted data should not be null", extractedData);
        assertFalse("Extracted data should not be empty", extractedData.isEmpty());
    }

    @Test
    public void testReturnParsedHFData() {
        JSONArray actual = new HuggingFaceHandler().returnParsedHFData(this.data);

        JSONObject expectedContent1 = new JSONObject()
                .put("articleTitles", articles.stream().map(article -> article.getString("article")).toArray())
                .put("claim", rowContents1.get("claim"))
                .put("claimLabel", rowContents1.get("claim_label"))
                .put("claimID", rowContents1.get("claim_id"));
        JSONObject expectedContent2 = new JSONObject()
                .put("articleTitles", articles.stream().map(article -> article.getString("article")).toArray())
                .put("claim", rowContents2.get("claim"))
                .put("claimLabel", rowContents2.get("claim_label"))
                .put("claimID", rowContents2.get("claim_id"));
        JSONArray expected = new JSONArray().put(expectedContent1).put(expectedContent2);

        assert (actual.toString().equals(expected.toString()));
    }

} // closing bracket to main class
