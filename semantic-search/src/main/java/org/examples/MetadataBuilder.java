package org.examples;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.json.JSONArray;

import java.util.UUID;

public class MetadataBuilder {
    public static Struct buildMetadataStruct(String claimText, String claimID, Integer claimSupported, JSONArray articles){
        return Struct.newBuilder()
                .putFields("claimText", Value.newBuilder().setStringValue(claimText).build())
                .putFields("claimID", Value.newBuilder().setStringValue(claimID).build())
                .putFields("claimLabel", Value.newBuilder().setNumberValue(claimSupported).build())
                .putFields("articleTitles", Value.newBuilder().setStringValue(articles.toString()).build())
                .build();
    }





}
