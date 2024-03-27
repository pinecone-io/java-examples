package org.examples;

import com.theokanning.openai.service.OpenAiService;

public class OpenAIConnector {
    private final String openAIApiKey;

    public OpenAIConnector(String openAIApiKey) {
        this.openAIApiKey = openAIApiKey;
    }

    public OpenAiService connect() {
        return new OpenAiService(this.openAIApiKey);
    }
}
