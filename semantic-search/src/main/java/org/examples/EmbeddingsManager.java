package org.examples;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EmbeddingsManager {
    private final String openAIApiKey;
    public String embeddingModel;

    public EmbeddingsManager(String openAIApiKey) {
        this.openAIApiKey = openAIApiKey;
        this.embeddingModel = "text-embedding-3-small";
    }

    public OpenAiService connect() {
        return new OpenAiService(this.openAIApiKey);
    }

    public List<Float> returnEmbedding(String text) {
        EmbeddingRequest userQueryEmbeddingRequest = new EmbeddingRequest(this.embeddingModel, Collections.singletonList(text),
                null);
        EmbeddingResult userQueryEmbeddingResult = this.connect().createEmbeddings(userQueryEmbeddingRequest);
        List<Embedding> userQueryEmbeddings = userQueryEmbeddingResult.getData();
        return userQueryEmbeddings.get(0).getEmbedding().stream().map(Double::floatValue).collect(Collectors.toList());
    }

    public List<Embedding> batchEmbeddings(Integer batchSize, List<String> embeddingsList) {
        List<Embedding> allEmbeddings = new ArrayList<>();
        for (int i = 0; i < embeddingsList.size(); i += batchSize) {
            List<String> batch = embeddingsList.subList(i, Math.min(i + batchSize, embeddingsList.size()));
            EmbeddingRequest batchEmbeddingRequest = new EmbeddingRequest(this.embeddingModel, batch, null);
            EmbeddingResult batchEmbeddingResult = this.connect().createEmbeddings(batchEmbeddingRequest);
            allEmbeddings.addAll(batchEmbeddingResult.getData());
        }
        return allEmbeddings;
    }
}
