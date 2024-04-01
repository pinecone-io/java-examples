package org.examples;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HuggingFaceHandlerTest {


    private List<JSONObject> articles;
    private JSONObject data;
    private JSONObject rowContents1;
    private JSONObject rowContents2;

    @BeforeEach
    public void setup() {
        JSONObject article1 = new JSONObject().put("article", "this is an article");
        JSONObject article2 = new JSONObject().put("article", "this is another article");

        articles = Arrays.asList(article1, article2);

        rowContents1 = new JSONObject()
                .put("claim", "this is a claim")
                .put("claim_label", 0)
                .put("claim_id", "0")
                .put("evidences", articles);
        JSONObject row1 = new JSONObject().put("row", rowContents1);
        rowContents2 = new JSONObject()
                .put("claim", "this is a different claim")
                .put("claim_label", 0)
                .put("claim_id", "1")
                .put("evidences", articles);
        JSONObject row2 = new JSONObject().put("row", rowContents2);

        data = new JSONObject().put("rows", new JSONArray().put(row1).put(row2));
    }

    @Test
    public void testExtract() {
        HuggingFaceHandler huggingFaceHandler = new HuggingFaceHandler();
        List<String> extractedData = huggingFaceHandler.extract();
        assertNotNull(extractedData, "Extracted data should not be null");
        assertFalse(extractedData.isEmpty(), "Extracted data should not be empty");
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

    @Test
    public void extractDataForMetadataPayloadTest() {
        int claimsSize = 2;
        int batchOfEmbeddingsSize = 1;

        String claimOne = rowContents1.get("claim").toString();
        String claimTwo = rowContents2.get("claim").toString();
        List<String> claims = Arrays.asList(claimOne, claimTwo);

        HuggingFaceHandler huggingFaceHandler = new HuggingFaceHandler();

        for (int i = 0; i < claimsSize; i++) {
            for (int j = 0; j < batchOfEmbeddingsSize; j++) {
                Struct metadata = huggingFaceHandler.extractDataForMetadataPayload(i, j, claims);
                assertNotNull(metadata, "Metadata should not be null");

                Map<String, Value> fieldsMap = metadata.getFieldsMap();
                assertTrue(fieldsMap.containsKey("claimText"), "Metadata Struct should contain key `claimText`");
                assertTrue(fieldsMap.containsKey("claimLabel"), "Metadata Struct should contain key `claimLabel`");
                assertTrue(fieldsMap.containsKey("claimID"), "Metadata Struct should contain key `claimID`");
                assertTrue(fieldsMap.containsKey("articleTitles"), "Metadata Struct should contain key " +
                        "`articleTitles`");

                fieldsMap.forEach((key, value) -> {
                    assertNotNull(value, "Metadata Struct field: " + key + " should not be null");
                    assertFalse(value.toString().isEmpty(), "Metadata Struct field: " + key + " should not be empty"
                    );
                });
            }
        }
    }

}
