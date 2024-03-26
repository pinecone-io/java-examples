package org.examples;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;


public class SemanticSearchExample {
    private static final Logger logger = LoggerFactory.getLogger(SemanticSearchExample.class);

    public static void main(String[] args) throws InterruptedException {
        EnvironmentManager envVarChecker = new EnvironmentManager();
        String pineconeApiKey = envVarChecker.getPineconeApiKey();
        String openAiApiKey = envVarChecker.getOpenAiApiKey();

        // Declare an index name
        String indexName = "java-test-index";

        // Make necessary connections:
        // Pinecone:
        PineconeManager pineconeManager = new PineconeManager(pineconeApiKey, indexName);
        Pinecone pineconeSvc = pineconeManager.connectToPineconeSvc();
        // OpenAI embedding service:
        EmbeddingsManager embeddingsManager = new EmbeddingsManager(openAiApiKey);

        // Check if index already exists, if not build it:
        if (pineconeManager.confirmIndexExists(pineconeSvc)) {
            logger.info("Creating index " + indexName);
            pineconeManager.buildIndex();
            // Wait for index to be ready for future operations
            Thread.sleep(10000);
        } else {
            logger.info("Index" + indexName + " already exists, moving on...");
        }

        // Grab data
        String apiUrl = "https://datasets-server.huggingface.co/rows?dataset=climate_fever&config=default&split=test&offset=0&length=100";
        DataManager dataManager = new DataManager(apiUrl);
        JSONArray parsedHfJsonData = dataManager.returnParsedHfData();

        // Extract text data from table that you want to vectorize
        List<String> claimsToEmbed = dataManager.extract();
//
//        // Connect to Pinecone index
        Index pineconeIndex = pineconeManager.connectToPineconeIndex();
//
//        // Embed data and upsert it into Pinecone
        if (pineconeIndex.describeIndexStats(null).getTotalVectorCount() > 0) {
            logger.info("Index is not empty. Skipping upsert process.");
        } else {
            logger.info("Index is empty. Embedding and upserting data...");
            for (int i = 0; i < claimsToEmbed.size(); i++) {
                // Grab each claim and embed it via OpenAI
                List<Float> embedding = embeddingsManager.returnEmbedding(claimsToEmbed.get(i));
                // Create ID for each vector
                String randomUUIDString = UUID.randomUUID().toString();
                // Create metadata payload to be attached to each vector
                String claimText = claimsToEmbed.get(i);
                String claimID = parsedHfJsonData.getJSONObject(i).get("claimID").toString();
                JSONArray articles = parsedHfJsonData.getJSONObject(i).getJSONArray("articleTitles");
                Integer claimSupported = (Integer) parsedHfJsonData.getJSONObject(i).get("claimLabel");
                Struct metadata = Struct.newBuilder()
                        .putFields("claimText", Value.newBuilder().setStringValue(claimText).build())
                        .putFields("claimID", Value.newBuilder().setStringValue(claimID).build())
                        .putFields("claimLabel", Value.newBuilder().setNumberValue(claimSupported).build())
                        .putFields("articleTitles", Value.newBuilder().setStringValue(articles.toString()).build())
                        .build();
                int maxRetries = 3;
                boolean success = false;
                while (!success && maxRetries > 0) {
                    try {
                        pineconeIndex.upsert(randomUUIDString, embedding, null, null, metadata, "test-namespace");
                        success = true;
                    } catch (io.grpc.StatusRuntimeException e) {
                        logger.info("Index isn't ready yet, retrying...");
                        maxRetries--;
                        Thread.sleep(1000);
                    }
                }
                if (!success) {
                    logger.error("Failed to upsert after " + maxRetries + " retries.");
                    return;
                }
            }
        }
//
//        // User query example
        String userQuery = "Climate change makes snow melt faster.";
//
//        // Embed user query
        List<Float> embeddedUserQuery = embeddingsManager.returnEmbedding(userQuery);
//
//        // Poll index until it's ready for querying
        while (pineconeIndex.describeIndexStats(null).getTotalVectorCount() == 0) {
            logger.info("Index isn't ready yet. Waiting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted while waiting for index to be ready. Continuing...");
            }
        }
//
//        // Make metadata filter to only get claims that are supported by the Wikipedia articles
        Struct queryFilter = Struct.newBuilder()
                .putFields("claimLabel", Value.newBuilder().setNumberValue(0).build())
                .build();
//
//        // Query Pinecone
        QueryResponseWithUnsignedIndices response = pineconeIndex.query(5, embeddedUserQuery, null, null, null, "test-namespace",
                queryFilter, false, true);
        logger.info("Query responses: " + response.getMatchesList());
//
//        // Close connection to Pinecone index
        pineconeManager.closePineconeIndex(pineconeIndex);
}
}
