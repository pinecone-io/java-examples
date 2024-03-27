package org.examples;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAIHandler {

    public String embeddingModel;
    final OpenAiService connection;

    public OpenAIHandler(String openAIApiKey) {
        this.embeddingModel = "text-embedding-3-small";
        OpenAIConnector connector = new OpenAIConnector(openAIApiKey);
        this.connection = connector.connect();
    }

    public List<Float> returnEmbedding(String text) {
        EmbeddingRequest userQueryEmbeddingRequest = new EmbeddingRequest(this.embeddingModel, Collections.singletonList(text),
                null);
        EmbeddingResult userQueryEmbeddingResult = connection.createEmbeddings(userQueryEmbeddingRequest);
        List<Embedding> userQueryEmbeddings = userQueryEmbeddingResult.getData();
        return userQueryEmbeddings.get(0).getEmbedding().stream().map(Double::floatValue).collect(Collectors.toList());
    }

    public List<Embedding> batchEmbeddings(Integer batchSize, List<String> embeddingsList) {
        List<Embedding> allEmbeddings = new ArrayList<>();
        for (int i = 0; i < embeddingsList.size(); i += batchSize) {
            List<String> batch = embeddingsList.subList(i, Math.min(i + batchSize, embeddingsList.size()));
            EmbeddingRequest batchEmbeddingRequest = new EmbeddingRequest(this.embeddingModel, batch, null);
            EmbeddingResult batchEmbeddingResult = this.connection.createEmbeddings(batchEmbeddingRequest);
            allEmbeddings.addAll(batchEmbeddingResult.getData());
        }
        return allEmbeddings;
    }
}
