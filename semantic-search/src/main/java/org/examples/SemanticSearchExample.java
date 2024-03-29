package org.examples;

import com.google.protobuf.Struct;
import com.theokanning.openai.service.OpenAiService;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class SemanticSearchExample {
    private static final Logger logger = LoggerFactory.getLogger(SemanticSearchExample.class);

    public static void main(String[] args) throws InterruptedException {
        LocalEnvironInitializer envVarChecker = new LocalEnvironInitializer();
        String pineconeApiKey = envVarChecker.getPineconeApiKey();
        String openAiApiKey = envVarChecker.getOpenAiApiKey();

        // Declare an index name
        String indexName = "java-test-index";

        // Set up Pinecone, OpenAI, and HuggingFace access:
        PineconeWrapper pinecone = new PineconeWrapper(pineconeApiKey, indexName);
        OpenAiService openAIConnection = new OpenAiService(openAiApiKey);
        OpenAIHandler openAI = new OpenAIHandler(openAIConnection);
        HuggingFaceHandler huggingFace = new HuggingFaceHandler();

        // Check if index already exists, if not build it:
        if (pinecone.confirmIndexExists()) {
            logger.info("Creating index " + indexName);
            pinecone.buildServerlessIndex();
            // Wait for index to be ready for future operations
            Thread.sleep(10000);
        } else {
            logger.info("Index " + indexName + " already exists, moving on...");
        }

        // Extract text data that you want to embed
        List<String> claimsToEmbed = huggingFace.extract();

        // Embed data and upsert it into PineconeWrapper
        if (pinecone.indexFull()) {
            logger.info("Index is not empty. Skipping upsert process.");
        } else {
            logger.info("Index is empty. Embedding and upserting data...");

            // Set batch size for embedding and upserting
            int batchSize = 10;

            // Iterate through HuggingFace data
            for (int i = 0; i < claimsToEmbed.size(); i += batchSize) {
                // Create a sublist of claims where sublist.size() <= batchSize
                List<String> batch = claimsToEmbed.subList(i, Math.min(i + batchSize, claimsToEmbed.size()));
                List<List<Float>> batchOfEmbeddings = openAI.embedMany(batch);

                // Create list to hold object you will index into PineconeWrapper
                List<VectorWithUnsignedIndices> objectsToIndex = new ArrayList<>();

                // For each embedding in the batch, create a unique ID and metadata payload and attach to objectToIndex
                for (int j = 0; j < batchOfEmbeddings.size(); j++) {
                    String id = UUID.randomUUID().toString();
                    List<Float> embedding = batchOfEmbeddings.get(j);
                    Struct metadata = huggingFace.extractDataForMetadataPayload(i, j, claimsToEmbed);
                    VectorWithUnsignedIndices toIndex = new VectorWithUnsignedIndices(id, embedding, metadata, null);
                    objectsToIndex.add(toIndex);
                }
                // Poll index for upsert operation
                int maxRetries = 3;
                boolean success = false;
                while (!success && maxRetries > 0) {
                    try {
                        pinecone.upsert(objectsToIndex, "test-namespace");
                        success = true;
                    } catch (io.grpc.StatusRuntimeException e) {
                        logger.info("Index isn't ready yet, retrying...");
                        maxRetries--;
                        Thread.sleep(1000);
                    }
                }
            }
        }
        // Declare sample user query claim
        String userQuery = "Forest fires make the world hotter";

        // Embed user claim
        List<Float> embeddedUserQuery = openAI.embedOne(userQuery);

        // Poll index until it's ready for querying
        while (!pinecone.indexFull()) {
            logger.info("Index isn't ready yet. Waiting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted while waiting for index to be ready. Continuing...");
            }
        }
        // Query PineconeWrapper
        QueryResponseWithUnsignedIndices response =
                pinecone.query(5,
                        embeddedUserQuery,
                        null,
                        null,
                        null,
                        "test-namespace",
                        pinecone.buildClaimFilter(),  // Filter for claims that are supported by Wikipedia articles
                        false,
                        true);
        logger.info("Query responses: " + response.getMatchesList());

        // Close connection to PineconeWrapper index
        pinecone.closePineconeIndexConnection();
    }
}
