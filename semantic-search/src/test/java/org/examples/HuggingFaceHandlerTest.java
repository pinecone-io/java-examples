package org.examples;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HuggingFaceHandlerTest {

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

//    @Test
//    public void testExtractDataForMetadataPayload() {
//        HuggingFaceHandler huggingFaceHandler = new HuggingFaceHandler();
//        String claimOne = "this is a claim";
//        String claimTwo = "this is another claim";
//        List<String> claimsToEmbed = Arrays.asList(claimOne, claimTwo);
//
//        JSONObject jsonObjectOne = new JSONObject();
//        jsonObjectOne.put("claimID", "145");
//        jsonObjectOne.put("ArticleTitles", new JSONArray().put("article1").put("article2"));
//        jsonObjectOne.put("claimLabel", 0);
//
//        JSONObject jsonObjectTwo = new JSONObject();
//        jsonObjectTwo.put("claimID", "657");
//        jsonObjectTwo.put("ArticleTitles", new JSONArray().put("article3").put("article4"));
//        jsonObjectTwo.put("claimLabel", 1);
//
//
//        int i = 0;
//        int j = 1;
//        Struct actualMetadataStruct = huggingFaceHandler.extractDataForMetadataPayload(i, j, claimsToEmbed);
//        Struct expectedMetadataStruct = Struct.newBuilder()
//                .putFields("claimText", Value.newBuilder().setStringValue("this is another claim").build())
//                .putFields("claimID", Struct.newBuilder().putFields("stringValue", "1").build())
//                .putFields("claimSupported", Struct.newBuilder().putFields("numberValue", 1).build())
//                .putFields("articles", Struct.newBuilder().putFields("listValue", Struct.newBuilder().putFields("values", Struct.newBuilder().putFields("stringValue", "article1").build()).build()).build())
//                .build();
//    }


} // closing bracket to main class
