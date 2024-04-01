package org.examples;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.json.JSONArray;


public class MetadataBuilder {
    public static Struct build(String claimText, String claimID, Integer claimSupported, JSONArray articles) {
        if (claimText == null || claimText.isEmpty()) {
            throw new IllegalArgumentException("claimText cannot be null or empty");
        }
        if (claimID == null || claimID.isEmpty()) {
            throw new IllegalArgumentException("claimID cannot be null or empty");
        }
        if (claimSupported == null) {
            throw new IllegalArgumentException("claimSupported cannot be null");
        }
        if (articles.isEmpty()) {
            throw new IllegalArgumentException("articles cannot be null or empty");
        }
        for (int i = 0; i < articles.length(); i++) {
            Object article = articles.get(i);
            if (article instanceof String && ((String) article).isEmpty()) {
                throw new IllegalArgumentException("Article at index " + i + " cannot be an empty string");
            }
        }

        return Struct.newBuilder()
                .putFields("claimText", Value.newBuilder().setStringValue(claimText).build())
                .putFields("claimID", Value.newBuilder().setStringValue(claimID).build())
                .putFields("claimLabel", Value.newBuilder().setNumberValue(claimSupported).build())
                .putFields("articleTitles", Value.newBuilder().setStringValue(articles.toString()).build())
                .build();
    }


}
