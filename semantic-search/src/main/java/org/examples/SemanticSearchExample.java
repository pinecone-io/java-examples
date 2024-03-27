package org.examples;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.SparseValuesWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SemanticSearchExample {
    private static final Logger logger = LoggerFactory.getLogger(SemanticSearchExample.class);

    public static void main(String[] args) throws InterruptedException {
        LocalEnvironInitializer envVarChecker = new LocalEnvironInitializer();
        String pineconeApiKey = envVarChecker.getPineconeApiKey();
        String openAiApiKey = envVarChecker.getOpenAiApiKey();

        // Declare an index name
        String indexName = "java-test-index";

        // Set up Pinecone access:
        PineconeWrapper pineconeHandler = new PineconeWrapper(pineconeApiKey, indexName);

        // Set up OpenAI access
        OpenAIHandler openAIHandler = new OpenAIHandler(openAiApiKey);

        // Check if index already exists, if not build it:
        if (pineconeHandler.confirmIndexExists()) {
            logger.info("Creating index " + indexName);
            pineconeHandler.buildServerlessIndex();
            // Wait for index to be ready for future operations
            Thread.sleep(10000);
        } else {
            logger.info("Index" + indexName + " already exists, moving on...");
        }

        // Grab data from HuggingFace
        String apiUrl = "https://datasets-server.huggingface.co/rows?dataset=climate_fever&config=default&split=test&offset=0&length=100";
        HuggingFaceDatasetHandler huggingFaceDataset = new HuggingFaceDatasetHandler(apiUrl);
        JSONArray parsedHfJsonData = huggingFaceDataset.returnParsedHfData();

        // Extract text data from table that you want to vectorize
        List<String> claimsToEmbed = huggingFaceDataset.extract();

        // Set batch size for embedding and upserting
        int batchSize = 10;

        // Embed data and upsert it into PineconeWrapper
        if (pineconeHandler.indexEmpty()) {
            logger.info("Index is not empty. Skipping upsert process.");
        } else {
            logger.info("Index is empty. Embedding and upserting data...");

            // Iterate through claims from dataset
            for (int i = 0; i < claimsToEmbed.size(); i += batchSize) {
                // Create a sublist of claims where sublist.size() <= batchSize
                List<String> batch = claimsToEmbed.subList(i, Math.min(i + batchSize, claimsToEmbed.size()));
                // Create an embedding request for all items in the batch
                EmbeddingRequest batchEmbeddingRequest = new EmbeddingRequest(openAIHandler.embeddingModel, batch, null);
                // Generate embeddings for all items in the batch
                EmbeddingResult batchEmbeddingResult = openAIHandler.connection.createEmbeddings(batchEmbeddingRequest);
                // Grab embedding values for all items in the batch
                List<Embedding> batchOfEmbeddings = batchEmbeddingResult.getData();  // this is 5 embeddings

                // Create list to hold object you will index into PineconeWrapper
                List<VectorWithUnsignedIndices> objectsToIndex = new ArrayList<>();

                // For each embedding in the batch, create a unique ID and metadata payload to pair with it
                for (int j = 0; j < batchOfEmbeddings.size(); j++) {
                    // ID:
                    String randomUUIDString = UUID.randomUUID().toString();
                    // Metadata payload:
                    String claimText = claimsToEmbed.get(i + j);
                    String claimID = parsedHfJsonData.getJSONObject(i + j).get("claimID").toString();
                    JSONArray articles = parsedHfJsonData.getJSONObject(i + j).getJSONArray("articleTitles");
                    Integer claimSupported = (Integer) parsedHfJsonData.getJSONObject(i + j).get("claimLabel");
                    Struct metadata = Struct.newBuilder()
                            .putFields("claimText", Value.newBuilder().setStringValue(claimText).build())
                            .putFields("claimID", Value.newBuilder().setStringValue(claimID).build())
                            .putFields("claimLabel", Value.newBuilder().setNumberValue(claimSupported).build())
                            .putFields("articleTitles", Value.newBuilder().setStringValue(articles.toString()).build())
                            .build();
                    // Transform embedding values from Double to Float
                    List<Float> embedding =
                            batchOfEmbeddings.get(j).getEmbedding().stream().map(Double::floatValue).collect(Collectors.toList());

                    // TODO: Once VectorWithUnsignedIndices doesn't throw a NullPointerException, remove this block
                    // Create dummy sparse vector (needed for instantiating VectorWithUnsignedIndices obj)
                    int size = 99; // Must be < 1k
                    // Lists of Longs and Floats make up your dummy sparse vector
                    List<Long> longList = IntStream.range(0, size)
                            .mapToObj(y -> (long) y)
                            .collect(Collectors.toList());
                    List<Float> floatList = IntStream.range(0, size)
                            .mapToObj(x -> (float) x)
                            .collect(Collectors.toList());

                    // Attach the combo of ID, embedding, metadata, and dummy sparse vector to objectsToIndex list
                    VectorWithUnsignedIndices toIndex = new VectorWithUnsignedIndices(randomUUIDString, embedding,
                            metadata, new SparseValuesWithUnsignedIndices(longList, floatList));
                    objectsToIndex.add(toIndex);
                }
                // Poll index for upsert operation
                int maxRetries = 3;
                boolean success = false;
                while (!success && maxRetries > 0) {
                    try {
                        pineconeHandler.upsert(objectsToIndex, "test-namespace");
                        success = true;
                    } catch (io.grpc.StatusRuntimeException e) {
                        logger.info("Index isn't ready yet, retrying...");
                        maxRetries--;
                        Thread.sleep(1000);
                    }
                }
            }

            // Declare sample user query claim
            String userQuery = "Climate change makes snow melt faster.";

            // Embed user claim
            List<Float> embeddedUserQuery = openAIHandler.returnEmbedding(userQuery);

            // Poll index until it's ready for querying
            while (pineconeHandler.indexEmpty()) {
                logger.info("Index isn't ready yet. Waiting...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted while waiting for index to be ready. Continuing...");
                }
            }

            // Query PineconeWrapper
            QueryResponseWithUnsignedIndices response =
                    pineconeHandler.query(5,
                            embeddedUserQuery,
                            null,
                            null,
                            null,
                            "test-namespace",
                            pineconeHandler.buildClaimFilter(),  // Filter for claims that are supported by Wikipedia articles
                            false,
                            true);
            logger.info("Query responses: " + response.getMatchesList());

            // Close connection to PineconeWrapper index
            pineconeHandler.closePineconeIndexConnection();
        }
    }
}
