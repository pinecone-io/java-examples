package org.examples;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAIHandler {

    private final OpenAiService connection;
    protected String embeddingModel;

    public OpenAIHandler(OpenAiService connection) {
        this.embeddingModel = "text-embedding-3-small";
        this.connection = connection;

        if (!this.listModels().contains(this.embeddingModel)) {
            throw new IllegalArgumentException("OpenAI model provided is invalid.");
        }
    }

    List<String> listModels() {
        List<String> models = new ArrayList<>();
        for (Model model : this.connection.listModels()) {
            models.add(model.id);
        }
        return models;
    }

    public List<Float> embedOne(String text) {
        if (text.isEmpty()) {
            throw new IllegalArgumentException("User query cannot be empty.");
        }

        EmbeddingRequest userQueryEmbeddingRequest = new EmbeddingRequest(this.embeddingModel, Collections.singletonList(text),
                null);
        EmbeddingResult userQueryEmbeddingResult = connection.createEmbeddings(userQueryEmbeddingRequest);
        List<Embedding> userQueryEmbeddings = userQueryEmbeddingResult.getData();
        return userQueryEmbeddings.get(0).getEmbedding().stream().map(Double::floatValue).collect(Collectors.toList());
    }

    public List<List<Float>> embedMany(List<String> strings) {
        // Raise an exception if the input list is empty
        if (strings.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be empty.");
        }

        // Raise exception if any item within the input list is empty
        if (strings.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("Input list cannot contain empty strings.");
        }

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
