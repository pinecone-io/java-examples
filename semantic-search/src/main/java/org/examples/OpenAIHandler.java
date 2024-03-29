package org.examples;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.service.OpenAiService;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAIHandler {

    protected String embeddingModel;
    private final OpenAiService connection;

    public OpenAIHandler(String openAIApiKey) {
        this.embeddingModel = "text-embedding-3-small";
        this.connection = new OpenAiService(openAIApiKey);

        if (!this.listModels().contains(this.embeddingModel)){
            throw new IllegalArgumentException("OpenAI model provided is invalid.");
        }
    }

    private List<String> listModels(){
        List<String> models = new ArrayList<>();
        for (Model model : this.connection.listModels()){
            models.add(model.id);
        }
        return models;
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
