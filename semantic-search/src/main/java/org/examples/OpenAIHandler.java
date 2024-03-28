package org.examples;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAIHandler {

    public String embeddingModel;
    private final OpenAiService connection;

    public OpenAIHandler(String openAIApiKey) {
        this.embeddingModel = "text-embedding-3-small";
        this.connection = new OpenAiService(openAIApiKey);
    }

    public List<List<Float>> embedMany(List<String> strings) {
        // Create an embedding request for all items in the batch
        EmbeddingRequest batchEmbeddingRequest = new EmbeddingRequest(this.embeddingModel, strings,
                null);
        // Generate embeddings for all items in the batch
        EmbeddingResult batchEmbeddingResult = this.connection.createEmbeddings(batchEmbeddingRequest);
        // Grab embedding values for all items in the batch
        List<Embedding> embeddings = batchEmbeddingResult.getData();
        // Turn each Embedding obj into a List of Floats (necessary for Pinecone upsert)
        return embeddings.stream()
                .map(embedding -> embedding.getEmbedding().stream()
                        .map(Double::floatValue)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

}
